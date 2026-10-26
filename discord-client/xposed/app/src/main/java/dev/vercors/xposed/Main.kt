package dev.vercors.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.json.JSONObject
import java.io.File

const val TAG = "Vercors"
const val LOADER_VERSION = "0.1.0"

fun log(message: String) = XposedBridge.log("[$TAG] $message")

/**
 * Entry point (see assets/xposed_init). Runs inside Discord's process, writes
 * the JS mod to Discord's data dir and makes React Native evaluate it right
 * before Discord's own bundle.
 */
class Main : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private var modulePath: String? = null

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!lpparam.isFirstApplication) return

        val dir = File(lpparam.appInfo.dataDir, "files/vercors").apply { mkdirs() }
        val bundles = BundleProvider(dir, modulePath, Main::class.java.classLoader)
        val bundle = bundles.prepare()
        if (bundle == null) {
            log("No JS bundle available, Discord will start unmodded")
            return
        }

        val preload = File(dir, "preload.js")
        val loaderInfo = JSONObject()
            .put("name", "VercorsXposed")
            .put("version", LOADER_VERSION)
            .put("bundleSource", bundle.source)
            .put("dataDir", dir.absolutePath)
        preload.writeText("globalThis.__VERCORS_LOADER__ = $loaderInfo;\n")

        ScriptInjector(listOf(preload, bundle.file)).install(lpparam.classLoader)
        bundles.refreshRemoteInBackground()
    }
}
