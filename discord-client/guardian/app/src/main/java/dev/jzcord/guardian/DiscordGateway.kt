package dev.jzcord.guardian

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * Keeps a Discord gateway WebSocket open and tells `onNewSession(idHash)`
 * every time a brand-new session appears on the account. Reconnects on
 * drop with a growing back-off.
 *
 * Discord's user-token gateway protocol, cut to what we actually need:
 *   10 HELLO       → the server gives heartbeat_interval
 *    2 IDENTIFY    → we send the token
 *    1 HEARTBEAT   → we send one every heartbeat_interval
 *    0 READY       → we get the initial list in d.sessions
 *      SESSIONS_REPLACE → the live list each time it changes
 */
class DiscordGateway(
    private val token: String,
    private val knownHashes: MutableSet<String>,
    private val onNewSession: (String, JSONObject) -> Unit,
    private val onStatus: (String) -> Unit,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val http = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    private var socket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var sequence: Int? = null
    private var backoffMs = 2_000L
    private var closed = false
    private var armed = false

    fun start() {
        closed = false
        connect()
    }

    fun stop() {
        closed = true
        heartbeatJob?.cancel()
        socket?.close(1000, "stopping")
        socket = null
    }

    private fun connect() {
        if (closed) return
        onStatus("Connexion au gateway…")
        val request = Request.Builder()
            .url("wss://gateway.discord.gg/?v=9&encoding=json")
            .build()

        socket = http.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onStatus("Connecté au gateway")
                backoffMs = 2_000L
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    handleFrame(JSONObject(text), webSocket)
                } catch (e: Throwable) {
                    Log.e(TAG, "frame handling failed", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "ws failure: ${t.message}")
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "ws closed: $code $reason")
                if (!closed) scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        heartbeatJob?.cancel()
        if (closed) return
        onStatus("Perdu la connexion, reconnexion dans ${backoffMs / 1000}s…")
        launch {
            delay(backoffMs)
            backoffMs = (backoffMs * 2).coerceAtMost(60_000)
            connect()
        }
    }

    private fun handleFrame(frame: JSONObject, socket: WebSocket) {
        val op = frame.optInt("op", -1)
        if (frame.has("s") && !frame.isNull("s")) sequence = frame.optInt("s")

        when (op) {
            10 -> {
                val interval = frame.getJSONObject("d").optLong("heartbeat_interval", 41_250)
                startHeartbeat(socket, interval)
                sendIdentify(socket)
            }
            11 -> { /* HEARTBEAT_ACK, no-op */ }
            0 -> {
                val type = frame.optString("t")
                val data = frame.optJSONObject("d") ?: JSONObject()
                when (type) {
                    "READY" -> {
                        val sessions = data.optJSONArray("sessions")
                        if (sessions != null) processSessions(sessions)
                        onStatus("Prêt — surveillance active")
                    }
                    "SESSIONS_REPLACE" -> {
                        val list = frame.optJSONArray("d") ?: data.optJSONArray("sessions")
                        if (list != null) processSessions(list)
                    }
                }
            }
            9 -> {
                // Invalid session: reconnect clean.
                scheduleReconnect()
            }
            7 -> {
                // Reconnect requested.
                scheduleReconnect()
            }
        }
    }

    private fun sendIdentify(socket: WebSocket) {
        val properties = JSONObject()
            .put("os", "Android")
            .put("browser", "Discord Android")
            .put("device", "jzcord-guardian")
            .put("system_locale", "fr-FR")
            .put("client_version", "250.0")
            .put("release_channel", "stable")
            .put("device_vendor_id", "jzcord-guardian")

        val payload = JSONObject()
            .put("op", 2)
            .put("d", JSONObject()
                .put("token", token)
                .put("capabilities", 16381)
                .put("properties", properties)
                .put("presence", JSONObject()
                    .put("status", "online")
                    .put("since", 0)
                    .put("activities", JSONArray())
                    .put("afk", false))
                .put("compress", false))
        socket.send(payload.toString())
    }

    private fun startHeartbeat(socket: WebSocket, intervalMs: Long) {
        heartbeatJob?.cancel()
        heartbeatJob = launch {
            // Discord expects the first heartbeat after a jittered delay.
            delay((intervalMs * Math.random()).toLong())
            while (isActive) {
                socket.send(JSONObject().put("op", 1).put("d", sequence).toString())
                delay(intervalMs)
            }
        }
    }

    private fun processSessions(list: JSONArray) {
        val current = mutableSetOf<String>()
        val newcomers = mutableListOf<JSONObject>()
        for (i in 0 until list.length()) {
            val session = list.optJSONObject(i) ?: continue
            val hash = session.optString("id_hash", session.optString("session_id"))
            if (hash.isEmpty()) continue
            current.add(hash)
            if (armed && !knownHashes.contains(hash)) newcomers.add(session)
        }

        if (!armed) {
            knownHashes.clear()
            knownHashes.addAll(current)
            armed = true
            onStatus("Référence initiale : ${current.size} session(s) connue(s)")
            return
        }

        // Drop vanished sessions so a kicked attacker reconnecting is NEW again.
        knownHashes.retainAll(current)

        for (session in newcomers) {
            val hash = session.optString("id_hash", session.optString("session_id"))
            knownHashes.add(hash)
            onNewSession(hash, session)
        }
    }

    /** POSTs /auth/sessions/logout to kick the given session ids. */
    fun kickSessions(sessionHashes: List<String>, password: String): Boolean {
        if (sessionHashes.isEmpty()) return false
        val body = JSONObject()
            .put("session_id_hashes", JSONArray(sessionHashes))
            .put("password", password)
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://discord.com/api/v9/auth/sessions/logout")
            .post(body)
            .header("Authorization", token)
            .build()
        return try {
            http.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Throwable) {
            Log.e(TAG, "kick failed", e)
            false
        }
    }

    companion object {
        private const val TAG = "JZCordGuardian"
    }
}
