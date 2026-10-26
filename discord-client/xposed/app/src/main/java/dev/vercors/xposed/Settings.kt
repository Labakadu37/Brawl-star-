package dev.vercors.xposed

import org.json.JSONObject
import java.io.File

class PluginInfo(val name: String, val description: String, val enabledByDefault: Boolean)

/** files/vercors/settings.json, shared with the JS side: { "plugins": { "Name": { "enabled": true } } } */
class Settings(private val file: File, manifestJson: String?) {
    val plugins: List<PluginInfo> = runCatching {
        val array = JSONObject(manifestJson!!).getJSONArray("plugins")
        (0 until array.length()).map { i ->
            val p = array.getJSONObject(i)
            PluginInfo(p.getString("name"), p.optString("description"), p.optBoolean("enabledByDefault"))
        }
    }.getOrDefault(emptyList())

    fun read(): JSONObject = runCatching { JSONObject(file.readText()) }.getOrNull() ?: JSONObject()

    fun isEnabled(plugin: PluginInfo): Boolean =
        read().optJSONObject("plugins")?.optJSONObject(plugin.name)?.optBoolean("enabled", plugin.enabledByDefault)
            ?: plugin.enabledByDefault

    fun setEnabled(states: Map<String, Boolean>) {
        val json = read()
        val plugins = json.optJSONObject("plugins") ?: JSONObject().also { json.put("plugins", it) }
        for ((name, enabled) in states) plugins.put(name, JSONObject().put("enabled", enabled))
        file.parentFile?.mkdirs()
        file.writeText(json.toString())
    }
}
