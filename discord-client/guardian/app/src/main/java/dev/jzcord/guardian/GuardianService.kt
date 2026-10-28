package dev.jzcord.guardian

import android.app.*
import android.content.*
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONObject

/**
 * Foreground service: a persistent process the OS keeps alive even when
 * Discord is closed or you killed it from recents. It holds the gateway
 * WebSocket open and reacts to new sessions in seconds.
 */
class GuardianService : Service() {

    private var gateway: DiscordGateway? = null
    private val knownHashes = mutableSetOf<String>()
    private var currentStatus = "Démarrage…"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotification(getString(R.string.notification_text)))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopGateway()
            stopSelf()
            return START_NOT_STICKY
        }

        val prefs = Prefs.get(this)
        if (prefs.token.isEmpty() || prefs.password.isEmpty()) {
            updateNotification("Token ou mot de passe manquant")
            stopSelf()
            return START_NOT_STICKY
        }

        knownHashes.clear()
        knownHashes.addAll(prefs.knownSessionHashes)

        gateway?.stop()
        gateway = DiscordGateway(
            token = prefs.token,
            knownHashes = knownHashes,
            onNewSession = { hash, info -> onNewSession(hash, info) },
            onStatus = { status -> updateNotification(status) },
        ).also { it.start() }

        return START_STICKY
    }

    private fun onNewSession(hash: String, info: JSONObject) {
        val os = info.optJSONObject("client_info")?.optString("os") ?: "?"
        val location = info.optJSONObject("client_info")?.optString("location") ?: "lieu ?"
        Log.w(TAG, "new session $hash from $os / $location")

        alertNewSession("Nouvelle session : $os ($location)")

        val password = Prefs.get(this).password
        val ok = gateway?.kickSessions(listOf(hash), password) == true
        if (ok) {
            Log.i(TAG, "session $hash kicked")
            // Remove from our known set so a reconnect still counts as new.
            knownHashes.remove(hash)
            Prefs.get(this).knownSessionHashes = knownHashes.toSet()
            alertNewSession("Session étrangère virée (${os})")
        } else {
            alertNewSession("⚠ Impossible de virer la session (mot de passe faux ?)")
        }
    }

    private fun stopGateway() {
        gateway?.stop()
        gateway = null
        Prefs.get(this).knownSessionHashes = knownHashes.toSet()
    }

    override fun onDestroy() {
        stopGateway()
        super.onDestroy()
    }

    private fun updateNotification(status: String) {
        currentStatus = status
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification(status))
    }

    private fun alertNewSession(text: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
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
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "JZCordGuardian"
        private const val CHANNEL_ID = "jzcord-guardian"
        private const val NOTIF_ID = 1
        private const val ALERT_ID_BASE = 100
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
