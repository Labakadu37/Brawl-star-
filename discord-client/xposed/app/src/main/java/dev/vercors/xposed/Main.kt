package dev.vercors.xposed

import android.app.Activity
import android.app.AndroidAppHelper
import android.os.Bundle as AndroidBundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.json.JSONObject
import java.io.File

const val TAG = "Vercors"
const val LOADER_VERSION = "0.1.3"

fun log(message: String) {
    Log.i(TAG, message)
    runCatching { XposedBridge.log("[$TAG] $message") }
}

/** Short on-screen message, so problems are visible without adb. */
fun toast(message: String, retries: Int = 20) {
    val main = Handler(Looper.getMainLooper())
    main.post {
        val app = AndroidAppHelper.currentApplication()
        if (app != null) Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
        else if (retries > 0) main.postDelayed({ toast(message, retries - 1) }, 500)
    }
}

/**
 * Entry point (see assets/xposed_init). Runs inside Discord's process, writes
 * the JS mod to Discord's data dir, makes React Native evaluate it right
 * before Discord's own bundle, and adds the Vercors menu button.
 */
class Main : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private var modulePath: String? = null

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!lpparam.isFirstApplication) return
        log("Loader v$LOADER_VERSION in ${lpparam.packageName}")

        val dir = File(lpparam.appInfo.dataDir, "files/vercors").apply { mkdirs() }
        val bundles = BundleProvider(dir, modulePath, Main::class.java.classLoader)
        val settings = Settings(File(dir, "settings.json"), bundles.readEmbeddedText("plugins.json"))
        installMenu(settings)

        val bundle = bundles.prepare()
        if (bundle == null) {
            log("No JS bundle available, Discord will start unmodded")
            toast("Vercors : bundle JS introuvable")
            return
        }

        val preload = File(dir, "preload.js")
        val loaderInfo = JSONObject()
            .put("name", "VercorsXposed")
            .put("version", LOADER_VERSION)
            .put("bundleSource", bundle.source)
            .put("dataDir", dir.absolutePath)
            .put("settings", settings.read())
        preload.writeText("globalThis.__VERCORS_LOADER__ = $loaderInfo;\n")

        val injector = ScriptInjector(listOf(preload, bundle.file)) { toast("Vercors injecté ✓") }
        if (injector.install(lpparam.classLoader) == 0) toast("Vercors : chargeur React Native introuvable")
        bundles.refreshRemoteInBackground()
    }

    private fun installMenu(settings: Settings) {
        XposedHelpers.findAndHookMethod(Activity::class.java, "onCreate", AndroidBundle::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                runCatching { VercorsMenu.attach(param.thisObject as Activity, settings) }
                    .onFailure { log("Failed to add menu button: $it") }
            }
        })
    }
}
