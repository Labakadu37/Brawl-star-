// Settings live in files/vercors/settings.json in Discord's data dir. The
// Xposed loader reads that file and hands it to us in __VERCORS_LOADER__, and
// its native menu writes it too; /vercors writes it through Discord's file
// native module when that is available.

import { logger } from "./logger";

const SETTINGS_PATH = "vercors/settings.json";

export interface Settings {
    plugins: Record<string, { enabled?: boolean; options?: Record<string, string> }>;
}

export const settings: Settings = { plugins: {} };

const loaderSettings = (globalThis as any).__VERCORS_LOADER__?.settings;
if (loaderSettings && typeof loaderSettings.plugins === "object") settings.plugins = loaderSettings.plugins;

let saveTimer: number | undefined;

function getFileModule() {
    const g = globalThis as any;
    for (const name of ["NativeFileModule", "RTNFileManager", "DCDFileManager"]) {
        try {
            const mod = g.__turboModuleProxy?.(name) ?? g.nativeModuleProxy?.[name];
            if (mod) return mod;
        } catch {}
    }
    return undefined;
}

export function saveSettings() {
    if (saveTimer !== undefined) clearTimeout(saveTimer);
    saveTimer = setTimeout(() => {
        saveTimer = undefined;
        const fileModule = getFileModule();
        if (!fileModule) return logger.warn("No file native module, settings change won't survive a restart");
        Promise.resolve(fileModule.writeFile("documents", SETTINGS_PATH, JSON.stringify(settings), "utf8")).catch(
            (e: unknown) => logger.error("Failed to save settings:", e),
        );
    }, 500);
}
