package dev.vercors.xposed

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Behind the "JZCord" icon on the home screen: opens Discord, where the mod
 * runs. Discord's own icon and name can't be changed without repackaging its
 * APK, so this icon stands in for it.
 */
class LaunchDiscordActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launch = packageManager.getLaunchIntentForPackage(DISCORD_PACKAGE)
        if (launch != null) startActivity(launch)
        else Toast.makeText(this, "Discord (patché avec JZCord) n'est pas installé", Toast.LENGTH_LONG).show()
        finish()
    }

    private companion object {
        const val DISCORD_PACKAGE = "com.discord"
    }
}
