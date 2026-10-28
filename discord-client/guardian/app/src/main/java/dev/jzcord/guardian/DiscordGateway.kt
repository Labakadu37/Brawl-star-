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

    data class KickResult(val success: Boolean, val reason: String)

    /** POSTs /auth/sessions/logout, handling Discord's MFA ticket flow transparently. */
    fun kickSessions(sessionHashes: List<String>, password: String, totpCode: String? = null): KickResult {
        if (sessionHashes.isEmpty()) return KickResult(false, "aucune session à virer")
        val payload = JSONObject()
            .put("session_id_hashes", JSONArray(sessionHashes))
            .put("password", password)

        // First attempt: no MFA header. Discord either accepts or hands us a ticket.
        val firstTry = postLogout(payload, mfaToken = null)
        if (firstTry.success) return KickResult(true, "ok")

        // 60008 ⇒ MFA required; the response body carries an mfa.ticket.
        val mfa = firstTry.mfaTicket
        if (mfa != null && !totpCode.isNullOrEmpty()) {
            val mfaToken = finishMfa(mfa, totpCode)
            if (mfaToken == null) return KickResult(false, "code 2FA invalide ou expiré")
            val secondTry = postLogout(payload, mfaToken = mfaToken)
            if (secondTry.success) return KickResult(true, "ok (MFA)")
            return KickResult(false, parseError(secondTry.status, secondTry.body))
        }
        return KickResult(false, parseError(firstTry.status, firstTry.body))
    }

    private data class LogoutResponse(
        val success: Boolean,
        val status: Int,
        val body: String,
        val mfaTicket: String?,
    )

    private fun postLogout(payload: JSONObject, mfaToken: String?): LogoutResponse {
        val builder = Request.Builder()
            .url("https://discord.com/api/v9/auth/sessions/logout")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .header("Authorization", token)
        if (!mfaToken.isNullOrEmpty()) builder.header("X-Discord-MFA-Authorization", mfaToken)

        return try {
            http.newCall(builder.build()).execute().use { response ->
                val body = response.body?.string() ?: ""
                Log.i(TAG, "logout status=${response.code} body=$body")
                val ticket = parseMfaTicket(body)
                LogoutResponse(response.isSuccessful, response.code, body, ticket)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "logout POST failed", e)
            LogoutResponse(false, -1, "", null)
        }
    }

    private fun parseMfaTicket(body: String): String? = try {
        JSONObject(body).optJSONObject("mfa")?.optString("ticket")?.takeIf { it.isNotEmpty() }
    } catch (_: Throwable) { null }

    /** POST /mfa/finish → returns a short-lived MFA token, or null on failure. */
    private fun finishMfa(ticket: String, totpCode: String): String? {
        val payload = JSONObject()
            .put("ticket", ticket)
            .put("mfa_type", "totp")
            .put("data", totpCode)
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://discord.com/api/v9/mfa/finish")
            .post(payload)
            .header("Authorization", token)
            .build()
        return try {
            http.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                Log.i(TAG, "mfa/finish status=${response.code} body=$body")
                if (response.isSuccessful) JSONObject(body).optString("token").takeIf { it.isNotEmpty() } else null
            }
        } catch (e: Throwable) {
            Log.e(TAG, "mfa/finish failed", e)
            null
        }
    }

    private fun parseError(status: Int, body: String): String {
        try {
            val json = JSONObject(body)
            val code = json.optInt("code", -1)
            val message = json.optString("message", "")
            val mfa = json.optJSONObject("mfa")
            if (code == 60008 || mfa != null) return "2FA requise — ton mot de passe seul suffit pas"
            if (code == 50018) return "mdp invalide"
            if (code != -1) return "code $code: $message"
            if (status == 401) return "token expiré (401)"
            if (status == 403) return "refusé (403): $message"
            if (status == 400) return "rejeté (400): $message"
        } catch (_: Throwable) {}
        return "HTTP $status"
    }

    /** GET /users/@me. Returns null if the token is invalid, or the body if OK. */
    fun checkToken(): String? {
        val request = Request.Builder()
            .url("https://discord.com/api/v9/users/@me")
            .header("Authorization", token)
            .build()
        return try {
            http.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (e: Throwable) {
            null
        }
    }

    /** GET /auth/sessions. Returns the list, or null on failure. */
    fun fetchSessions(): List<JSONObject>? {
        val request = Request.Builder()
            .url("https://discord.com/api/v9/auth/sessions")
            .header("Authorization", token)
            .build()
        return try {
            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string() ?: return@use null
                val root = JSONObject(body)
                val list = root.optJSONArray("user_sessions") ?: return@use emptyList<JSONObject>()
                (0 until list.length()).mapNotNull { list.optJSONObject(it) }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "sessions fetch failed", e)
            null
        }
    }

    companion object {
        private const val TAG = "JZCordGuardian"
    }
}
