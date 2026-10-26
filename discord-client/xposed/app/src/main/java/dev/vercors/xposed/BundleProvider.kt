package dev.vercors.xposed

import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipFile

class Bundle(val file: File, val source: String)

/**
 * Picks the JS bundle to inject:
 *  1. `remote.js`, downloaded from the `bundleUrl` set in `loader.json`
 *     (lets you update the mod without reinstalling the APK),
 *  2. otherwise the copy embedded in this module's APK.
 */
class BundleProvider(
    private val dir: File,
    private val modulePath: String?,
    private val moduleClassLoader: ClassLoader?,
) {
    private val configFile = File(dir, "loader.json")
    private val remoteFile = File(dir, "remote.js")
    private val embeddedFile = File(dir, "vercors.js")

    private val remoteUrl: String? by lazy {
        runCatching { JSONObject(configFile.readText()).optString("bundleUrl").ifBlank { null } }.getOrNull()
    }

    fun prepare(): Bundle? {
        if (remoteUrl != null && remoteFile.length() > 0) return Bundle(remoteFile, "remote")

        val copied = runCatching {
            openEmbedded()?.use { input -> embeddedFile.outputStream().use { input.copyTo(it) } } != null
        }.onFailure { log("Failed to extract embedded bundle: $it") }.getOrDefault(false)

        return when {
            copied -> Bundle(embeddedFile, "embedded")
            embeddedFile.length() > 0 -> Bundle(embeddedFile, "embedded (cached)")
            else -> null
        }
    }

    private fun openEmbedded(name: String = "vercors.js"): InputStream? {
        // Normal LSPosed install: the module APK is on our class loader's path.
        moduleClassLoader?.getResourceAsStream("assets/$name")?.let { return it }
        // LSPatch & co. may load the module's dex from memory: read the APK itself.
        val zip = modulePath?.let { ZipFile(it) } ?: return null
        val entry = zip.getEntry("assets/$name") ?: return null.also { zip.close() }
        return object : java.io.FilterInputStream(zip.getInputStream(entry)) {
            override fun close() {
                super.close()
                zip.close()
            }
        }
    }

    /** Downloads the remote bundle for the *next* launch, never blocking this one. */
    fun refreshRemoteInBackground() {
        val url = remoteUrl ?: return
        Thread {
            runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 10_000
                connection.readTimeout = 30_000
                try {
                    check(connection.responseCode == 200) { "HTTP ${connection.responseCode}" }
                    val tmp = File(dir, "remote.js.tmp")
                    connection.inputStream.use { input -> tmp.outputStream().use { input.copyTo(it) } }
                    check(tmp.renameTo(remoteFile)) { "rename failed" }
                } finally {
                    connection.disconnect()
                }
                log("Remote bundle updated from $url")
            }.onFailure { log("Remote bundle update failed: $it") }
        }.apply { name = "VercorsBundleUpdate"; isDaemon = true }.start()
    }
}
