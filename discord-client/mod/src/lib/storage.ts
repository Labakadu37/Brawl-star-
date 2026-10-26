// Settings persisted as JSON in Discord's documents directory, through the
// file native module Discord already ships (its name changed across versions).

import { logger } from "./logger";

const SETTINGS_PATH = "vercors/settings.json";

export interface Settings {
    plugins: Record<string, { enabled: boolean }>;
}

export const settings: Settings = { plugins: {} };

let fileModule: any;
let saveTimer: number | undefined;

function resolveFileModule(nativeModules: any) {
    const turboModuleProxy = (globalThis as any).__turboModuleProxy;
    for (const name of ["NativeFileModule", "RTNFileManager", "DCDFileManager"]) {
        const mod = nativeModules?.[name] ?? turboModuleProxy?.(name);
        if (mod) return mod;
    }
    return undefined;
}

function documentsDir(): string | undefined {
    return fileModule?.getConstants?.().DocumentsDirPath ?? fileModule?.DocumentsDirPath;
}

export async function loadSettings(nativeModules: any) {
    fileModule = resolveFileModule(nativeModules);
    const dir = documentsDir();
    if (!fileModule || !dir) {
        logger.warn("No file native module found, settings will not persist");
        return;
    }
    try {
        const path = `${dir}/${SETTINGS_PATH}`;
        if (!(await fileModule.fileExists(path))) return;
        const saved = JSON.parse(await fileModule.readFile(path, "utf8"));
        if (saved && typeof saved.plugins === "object") settings.plugins = saved.plugins;
    } catch (e) {
        logger.error("Failed to load settings:", e);
    }
}

export function saveSettings() {
    if (!fileModule) return;
    if (saveTimer !== undefined) clearTimeout(saveTimer);
    saveTimer = setTimeout(() => {
        saveTimer = undefined;
        Promise.resolve(fileModule.writeFile("documents", SETTINGS_PATH, JSON.stringify(settings), "utf8")).catch(
            (e: unknown) => logger.error("Failed to save settings:", e),
        );
    }, 500);
}
