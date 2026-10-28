package dev.jzcord.guardian

import android.app.*
import android.content.*
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.json.JSONObject

/**
 * Foreground service: watches the Discord account from a persistent process
 * the OS keeps alive even when Discord is closed or you killed it from
 * recents. It polls /auth/sessions every 3 seconds — simpler and more
 * reliable than relying on gateway events — and immediately kicks any new
 * session it finds.
 */
class GuardianService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollJob: Job? = null
    private var gateway: DiscordGateway? = null
    private val knownHashes = mutableSetOf<String>()
    private var wakeLock: PowerManager.WakeLock? = null
    private var primed = false
    private var pollCount = 0
    private var lastKickCount = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotification("Démarrage…"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            cleanup()
            stopSelf()
            return START_NOT_STICKY
        }

        val prefs = Prefs.get(this)
        if (prefs.token.isEmpty() || prefs.password.isEmpty()) {
            updateNotification("Token ou mot de passe manquant")
            stopSelf()
            return START_NOT_STICKY
        }

        acquireWakeLock()
        knownHashes.clear()
        primed = false
        pollCount = 0
        lastKickCount = 0

        gateway = DiscordGateway(
            token = prefs.token,
            knownHashes = mutableSetOf(), // we don't use the gateway for detection anymore
            onNewSession = { _, _ -> /* unused */ },
            onStatus = { /* unused */ },
        )

        pollJob?.cancel()
        pollJob = scope.launch { pollLoop(prefs) }
        return START_STICKY
    }

    private suspend fun pollLoop(prefs: Prefs) = coroutineScope {
        // Validate the token once at startup.
        val me = gateway?.checkToken()
        if (me == null) {
            updateNotification("❌ Token invalide — ouvre JZCord, fais /view-my-token et re-colle le token ici")
            return@coroutineScope
        }
        val username = try { JSONObject(me).optString("username") } catch (e: Throwable) { "?" }
        updateNotification("🟢 Connecté comme $username — surveillance active")

        while (isActive) {
            try {
                val sessions = gateway?.fetchSessions()
                if (sessions == null) {
                    updateNotification("⚠ Pas de réponse de Discord — nouveau essai…")
                } else {
                    handleSessions(sessions, prefs)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "poll iteration failed", e)
            }
            pollCount++
            delay(POLL_INTERVAL_MS)
        }
    }

    private fun handleSessions(sessions: List<JSONObject>, prefs: Prefs) {
        val currentHashes = sessions.mapNotNull { it.optString("id_hash").takeIf { s -> s.isNotEmpty() } }.toSet()

        if (!primed) {
            // First snapshot: everything there IS the trusted baseline.
            knownHashes.clear()
            knownHashes.addAll(currentHashes)
            primed = true
            prefs.knownSessionHashes = knownHashes.toSet()
            updateNotification("🟢 ${currentHashes.size} session(s) de confiance enregistrée(s) — surveillance active")
            Log.i(TAG, "primed with ${currentHashes.size} sessions")
            return
        }

        // Drop vanished sessions so a reconnect counts as new.
        knownHashes.retainAll(currentHashes)

        val newcomers = sessions.filter { s ->
            val hash = s.optString("id_hash")
            hash.isNotEmpty() && !knownHashes.contains(hash)
        }

        if (newcomers.isEmpty()) {
            val base = "🟢 Surveillance active — ${currentHashes.size} session(s)"
            val tail = if (lastKickCount > 0) " · $lastKickCount virage(s)" else ""
            updateNotification(base + tail)
            return
        }

        for (newcomer in newcomers) {
            val hash = newcomer.optString("id_hash")
            val info = newcomer.optJSONObject("client_info")
            val os = info?.optString("os") ?: "?"
            val location = info?.optString("location") ?: "?"
            Log.w(TAG, "NEW SESSION detected: $hash from $os / $location")
            alertNewSession("Nouvelle session détectée : $os ($location)")

            val result = gateway?.kickSessions(listOf(hash), prefs.password, prefs.totpSecret.takeIf { it.isNotEmpty() }?.let { Totp.generate(it) })
            if (result?.success == true) {
                lastKickCount++
                alertNewSession("✅ Session étrangère virée ($os / $location)")
                Log.i(TAG, "kicked $hash")
            } else {
                val reason = result?.reason ?: "erreur inconnue"
                alertNewSession("❌ VIRAGE RATÉ — $reason")
                Log.w(TAG, "FAILED to kick $hash: $reason")
                // Mark it as known anyway so we don't spam alerts.
                knownHashes.add(hash)
            }
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock != null) return
        val power = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "jzcord:guardian").apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    private fun releaseWakeLock() {
        try { wakeLock?.release() } catch (_: Throwable) {}
        wakeLock = null
    }

    private fun cleanup() {
        pollJob?.cancel()
        pollJob = null
        gateway = null
        releaseWakeLock()
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    private fun updateNotification(status: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification(status))
    }

    private fun alertNewSession(text: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
            .setContentTitle("🚨 JZCord Guardian")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()
        manager.notify(ALERT_ID_BASE + (System.currentTimeMillis() % 10_000).toInt(), notification)
    }

    private fun buildNotification(status: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(status)
            .setStyle(NotificationCompat.BigTextStyle().bigText(status))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(
            CHANNEL_ID, getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW,
        ))
        manager.createNotificationChannel(NotificationChannel(
            CHANNEL_ID_ALERT, "Alertes JZCord", NotificationManager.IMPORTANCE_HIGH,
        ))
    }

    companion object {
        private const val TAG = "JZCordGuardian"
        private const val CHANNEL_ID = "jzcord-guardian"
        private const val CHANNEL_ID_ALERT = "jzcord-guardian-alert"
        private const val NOTIF_ID = 1
        private const val ALERT_ID_BASE = 100
        private const val POLL_INTERVAL_MS = 3_000L
        const val ACTION_STOP = "stop"

        fun start(context: Context) {
            val intent = Intent(context, GuardianService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, GuardianService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}
