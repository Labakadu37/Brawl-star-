package dev.vercors.xposed

import android.content.res.AssetManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.File
import java.lang.reflect.Method
import java.util.Collections
import java.util.WeakHashMap

/**
 * Hooks the JSBundleLoaderDelegate React Native uses to load the app's JS
 * bundle and, the first time each delegate loads one, evaluates [scripts]
 * before it.
 */
class ScriptInjector(private val scripts: List<File>, private val onInjected: (String) -> Unit) {
    private val delegateClasses = listOf(
        // New architecture (bridgeless): anonymous delegate created in ReactInstance.loadJSBundle
        "com.facebook.react.runtime.ReactInstance\$loadJSBundle\$1",
        // Legacy bridge
        "com.facebook.react.bridge.CatalystInstanceImpl",
    )

    private val injectedDelegates: MutableSet<Any> = Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))
    private val hookedLoaderClasses: MutableSet<Class<*>> = Collections.synchronizedSet(HashSet())

    /** Returns how many methods were hooked. */
    fun install(classLoader: ClassLoader): Int {
        var hooked = 0
        for (name in delegateClasses) {
            val clazz = XposedHelpers.findClassIfExists(name, classLoader) ?: continue
            hooked += hookDelegate(clazz)
        }
        // R8 may rename or merge the anonymous delegate class, so also catch it at runtime:
        // ReactInstance.loadJSBundle(loader) -> loader.loadScript(delegate).
        hooked += hookReactInstanceFallback(classLoader)
        log(if (hooked > 0) "Hooked $hooked bundle loader method(s)" else "No React Native bundle loader found")
        return hooked
    }

    private fun hookDelegate(clazz: Class<*>): Int {
        val fromFile = clazz.findMethod("loadScriptFromFile", String::class.java, String::class.java, Boolean::class.javaPrimitiveType!!)
            ?: return 0
        val fromAssets = clazz.findMethod("loadScriptFromAssets", AssetManager::class.java, String::class.java, Boolean::class.javaPrimitiveType!!)
        val hook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                injectInto(param.thisObject, fromFile, param.args[2] as? Boolean ?: false, clazz.name)
            }
        }
        listOfNotNull(fromFile, fromAssets).forEach { XposedBridge.hookMethod(it, hook) }
        return if (fromAssets != null) 2 else 1
    }

    private fun hookReactInstanceFallback(classLoader: ClassLoader): Int {
        val reactInstance = XposedHelpers.findClassIfExists("com.facebook.react.runtime.ReactInstance", classLoader) ?: return 0
        val loaderClass = XposedHelpers.findClassIfExists("com.facebook.react.bridge.JSBundleLoader", classLoader) ?: return 0
        val delegateInterface = XposedHelpers.findClassIfExists("com.facebook.react.bridge.JSBundleLoaderDelegate", classLoader) ?: return 0

        val loadJSBundle = reactInstance.declaredMethods.filter { m ->
            m.parameterTypes.size == 1 && loaderClass.isAssignableFrom(m.parameterTypes[0])
        }
        loadJSBundle.forEach { method ->
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val loader = param.args[0] ?: return
                    if (hookedLoaderClasses.add(loader.javaClass)) hookLoadScript(loader.javaClass, delegateInterface)
                }
            })
        }
        return loadJSBundle.size
    }

    /** Hooks `JSBundleLoader.loadScript(delegate)` on the concrete loader class. */
    private fun hookLoadScript(loaderClass: Class<*>, delegateInterface: Class<*>) {
        var clazz: Class<*>? = loaderClass
        while (clazz != null) {
            clazz.declaredMethods
                .filter { it.parameterTypes.size == 1 && it.parameterTypes[0] == delegateInterface && !java.lang.reflect.Modifier.isAbstract(it.modifiers) }
                .forEach { method ->
                    XposedBridge.hookMethod(method, object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val delegate = param.args[0] ?: return
                            val fromFile = delegate.javaClass.findMethodInHierarchy(
                                "loadScriptFromFile", String::class.java, String::class.java, Boolean::class.javaPrimitiveType!!,
                            ) ?: return log("Delegate ${delegate.javaClass.name} has no loadScriptFromFile")
                            injectInto(delegate, fromFile, false, "${delegate.javaClass.name} (fallback)")
                        }
                    })
                }
            clazz = clazz.superclass
        }
    }

    private fun injectInto(delegate: Any, loadFromFile: Method, sync: Boolean, via: String) {
        if (!injectedDelegates.add(delegate)) return
        log("Injecting through $via")
        for (script in scripts) {
            try {
                XposedBridge.invokeOriginalMethod(loadFromFile, delegate, arrayOf(script.absolutePath, script.absolutePath, sync))
            } catch (t: Throwable) {
                log("Failed to load ${script.name}: $t")
            }
        }
        onInjected(via)
    }

    private fun Class<*>.findMethod(name: String, vararg params: Class<*>): Method? =
        runCatching { getDeclaredMethod(name, *params).apply { isAccessible = true } }.getOrNull()

    private fun Class<*>.findMethodInHierarchy(name: String, vararg params: Class<*>): Method? {
        var clazz: Class<*>? = this
        while (clazz != null) {
            clazz.findMethod(name, *params)?.let { return it }
            clazz = clazz.superclass
        }
        return null
    }
}
