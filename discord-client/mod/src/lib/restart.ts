// Reloads Discord's JS (fast) or restarts the app, and a confirm dialog.

import { filters, find } from "./metro";
import { getReactNative } from "./react";

/** Reloads the React Native bundle, re-running JZCord cleanly. */
export function reloadDiscord() {
    const reloaders = [
        () => find(filters.byProps("reload", "getConstants"))?.reload?.(), // BundleUpdaterManager
        () => find(filters.byProps("reload"))?.reload?.(),
        () => (getReactNative()?.NativeModules?.BundleUpdaterManager ?? getReactNative()?.NativeModules?.DevSettings)?.reload?.(),
        () => (globalThis as any).nativeModuleProxy?.BundleUpdaterManager?.reload?.(),
    ];
    for (const reload of reloaders) {
        try {
            reload();
            return true;
        } catch {}
    }
    return false;
}

/** Discord's confirm dialog: "Redémarrer pour appliquer ?" with a button. */
export function promptRestart(message = "Redémarre Discord pour appliquer les changements.") {
    const alerts = find(filters.byProps("openLazy", "close")) ?? find(filters.byProps("show", "openLazy"));
    const title = "JZCord";
    try {
        if (alerts?.show) {
            alerts.show({
                title,
                body: message,
                confirmText: "Redémarrer",
                cancelText: "Plus tard",
                onConfirm: reloadDiscord,
            });
            return;
        }
    } catch {}
    // Fallback: React Native's Alert.
    try {
        getReactNative()?.Alert?.alert(title, message, [
            { text: "Plus tard", style: "cancel" },
            { text: "Redémarrer", onPress: reloadDiscord },
        ]);
    } catch {}
}
