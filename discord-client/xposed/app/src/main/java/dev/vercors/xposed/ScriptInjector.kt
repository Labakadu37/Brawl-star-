package dev.vercors.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.File
import java.lang.reflect.Method
import java.util.Collections
import java.util.WeakHashMap

/**
 * Hooks the methods React Native uses to load the app's JS bundle and, the
 * first time each JS runtime loads one, evaluates [scripts] before it.
 */
class ScriptInjector(private val scripts: List<File>) {
    private class Target(val className: String, val fromAssets: String, val fromFile: String)

    private val targets = listOf(
        // Legacy bridge
        Target("com.facebook.react.bridge.CatalystInstanceImpl", "loadScriptFromAssets", "loadScriptFromFile"),
        // New architecture (bridgeless)
        Target("com.facebook.react.runtime.ReactInstance", "loadJSBundleFromAssets", "loadJSBundleFromFile"),
    )

    private val injectedInstances: MutableSet<Any> = Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))

    fun install(classLoader: ClassLoader) {
        var hooked = 0
        for (target in targets) {
            val clazz = XposedHelpers.findClassIfExists(target.className, classLoader) ?: continue
            val loadFromFile = clazz.declaredMethods.firstOrNull { it.name == target.fromFile && it.takesTwoStrings() }
                ?: continue
            loadFromFile.isAccessible = true

            val hook = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (injectedInstances.add(param.thisObject)) injectInto(param, loadFromFile)
                }
            }
            clazz.declaredMethods
                .filter { it.name == target.fromAssets || it.name == target.fromFile }
                .forEach {
                    XposedBridge.hookMethod(it, hook)
                    hooked++
                }
        }
        log(if (hooked > 0) "Hooked $hooked bundle loader method(s)" else "No React Native bundle loader found")
    }

    private fun injectInto(param: XC_MethodHook.MethodHookParam, loadFromFile: Method) {
        for (script in scripts) {
            val args = arrayOfNulls<Any>(loadFromFile.parameterTypes.size)
            args[0] = script.absolutePath
            args[1] = script.absolutePath
            // Remaining params are flags like loadSynchronously: reuse the caller's value.
            for (i in 2 until args.size) args[i] = param.args.getOrNull(i) as? Boolean ?: false
            try {
                XposedBridge.invokeOriginalMethod(loadFromFile, param.thisObject, args)
            } catch (t: Throwable) {
                log("Failed to load ${script.name}: $t")
            }
        }
    }

    private fun Method.takesTwoStrings() =
        parameterTypes.size >= 2 && parameterTypes[0] == String::class.java && parameterTypes[1] == String::class.java
}
