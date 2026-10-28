package dev.jzcord.guardian

import android.content.Context
import android.content.SharedPreferences

/**
 * Local-only settings. Nothing ever leaves the device: token and password
 * are stored in the app's private SharedPreferences, which other apps
 * cannot read on a non-rooted phone.
 */
class Prefs private constructor(context: Context) {
    private val sp: SharedPreferences = context.getSharedPreferences("guardian", Context.MODE_PRIVATE)

    var token: String
        get() = sp.getString(KEY_TOKEN, "") ?: ""
        set(value) = sp.edit().putString(KEY_TOKEN, value).apply()

    var password: String
        get() = sp.getString(KEY_PASSWORD, "") ?: ""
        set(value) = sp.edit().putString(KEY_PASSWORD, value).apply()

    var enabled: Boolean
        get() = sp.getBoolean(KEY_ENABLED, false)
        set(value) = sp.edit().putBoolean(KEY_ENABLED, value).apply()

    /** Hashes of sessions seen on first connect so new strangers stand out. */
    var knownSessionHashes: Set<String>
        get() = sp.getStringSet(KEY_KNOWN, emptySet()) ?: emptySet()
        set(value) = sp.edit().putStringSet(KEY_KNOWN, value).apply()

    fun clearKnown() = sp.edit().remove(KEY_KNOWN).apply()

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_KNOWN = "known_session_hashes"

        @Volatile private var instance: Prefs? = null
        fun get(context: Context): Prefs =
            instance ?: synchronized(this) {
                instance ?: Prefs(context.applicationContext).also { instance = it }
            }
    }
}
