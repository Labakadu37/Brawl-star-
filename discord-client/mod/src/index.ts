// Entry point. The Xposed loader evaluates this file right before Discord's
// own bundle, so only synchronous setup can happen at the top level: the
// React Native runtime (timers, promises, native modules) isn't there yet.

import { initAssets } from "./lib/assets";
import { initCommands } from "./lib/commands";
import { logger } from "./lib/logger";
import * as metro from "./lib/metro";
import * as patcher from "./lib/patcher";
import { findPlugin, getPluginOption, getPlugins, setPluginEnabled, setPluginOption, startEnabledPlugins } from "./lib/plugins";
import { initSettingsUI } from "./lib/settingsUI";
import { registerBuiltinPlugins } from "./plugins";
import { registerSettingsCommand } from "./settingsCommand";

const g = globalThis as any;

function toast(reactNative: any, message: string) {
    try {
        reactNative.ToastAndroid?.show(message, reactNative.ToastAndroid.SHORT ?? 0);
    } catch (e) {
        logger.warn("Toast failed:", e);
    }
}

function start() {
    logger.log(`v${__VERCORS_VERSION__} starting, loader:`, g.__VERCORS_LOADER__?.version ?? "none");

    // Parts of Discord's code reference `window`, which is only defined later in its bundle.
    g.window ??= g;
    // Discord freezes some module exports, which would make them impossible to patch.
    Object.freeze = Object.seal = ((o: unknown) => o) as any;
    metro.installMetroHook();
    g.jzcord = { version: __VERCORS_VERSION__, metro, patcher, plugins: { getPlugins, findPlugin, setPluginEnabled, getPluginOption, setPluginOption } };

    registerBuiltinPlugins();
    registerSettingsCommand();
    initCommands();
    initAssets();
    initSettingsUI();
    // Plugins only subscribe to modules here; their patches apply as Discord loads them.
    startEnabledPlugins();

    // react-native's index module is loaded once InitializeCore has set up the
    // runtime; `in` avoids triggering its lazy getters.
    metro.waitFor(
        m => "AppRegistry" in m && "ToastAndroid" in m,
        reactNative => {
            const { defined, initialized } = metro.getModuleStats();
            logger.log(`React Native ready (${initialized}/${defined} modules)`);
            toast(reactNative, `JZCord v${__VERCORS_VERSION__} chargé ✓`);
        },
    );
}

if (g.jzcord) {
    logger.warn("JZCord was injected twice, ignoring the second copy");
} else {
    try {
        start();
    } catch (e) {
        logger.error("Startup failed:", e);
    }
}
