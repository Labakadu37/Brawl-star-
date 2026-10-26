// Entry point. The Xposed loader evaluates this file right before Discord's
// own bundle, so only synchronous setup can happen at the top level: the
// React Native runtime (timers, promises, native modules) isn't there yet.

import { initCommands } from "./lib/commands";
import { logger } from "./lib/logger";
import * as metro from "./lib/metro";
import * as patcher from "./lib/patcher";
import { findPlugin, getPlugins, setPluginEnabled, startEnabledPlugins } from "./lib/plugins";
import { loadSettings } from "./lib/storage";
import { registerBuiltinPlugins } from "./plugins";
import { registerSettingsCommand } from "./settingsCommand";

const g = globalThis as any;

if (g.vercors) {
    logger.warn("Vercors was injected twice, ignoring the second copy");
} else {
    metro.installMetroHook();
    g.vercors = { version: __VERCORS_VERSION__, metro, patcher, plugins: { getPlugins, findPlugin, setPluginEnabled } };

    registerBuiltinPlugins();
    registerSettingsCommand();
    initCommands();

    // react-native's index module is loaded once InitializeCore has set up the
    // runtime; `in` avoids triggering its lazy getters.
    metro.waitFor(
        m => "AppRegistry" in m && "NativeModules" in m,
        reactNative => {
            loadSettings(reactNative.NativeModules)
                .catch(e => logger.error("Settings:", e))
                .then(() => {
                    startEnabledPlugins();
                    logger.log(`v${__VERCORS_VERSION__} loaded`);
                });
        },
    );
}
