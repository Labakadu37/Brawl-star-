package dev.jzcord.guardian

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Re-starts the guard automatically after a phone reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = Prefs.get(context)
        if (prefs.enabled && prefs.token.isNotEmpty() && prefs.password.isNotEmpty()) {
            GuardianService.start(context)
        }
    }
}
