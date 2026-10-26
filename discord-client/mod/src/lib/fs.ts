// Writing files into Discord's data dir, to serve generated images as file://
// URLs (the mobile tag/badge image loader doesn't take data: URIs).

import { logger } from "./logger";

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

/** files/vercors, where the loader wrote the bundle and settings. */
export function dataDir(): string | undefined {
    return (globalThis as any).__VERCORS_LOADER__?.dataDir;
}

const writtenFiles = new Set<string>();

/**
 * Writes `base64` to `vercors/<relativePath>` once, and returns a file:// URL
 * for it. The write is async; the URL is usable on the next render.
 */
export function writeBase64File(relativePath: string, base64: string): string | undefined {
    const dir = dataDir();
    const fileModule = getFileModule();
    if (!dir || !fileModule) return undefined;

    const url = `file://${dir}/${relativePath}`;
    if (!writtenFiles.has(relativePath)) {
        writtenFiles.add(relativePath);
        Promise.resolve(fileModule.writeFile("documents", `vercors/${relativePath}`, base64, "base64")).catch((e: unknown) => {
            writtenFiles.delete(relativePath);
            logger.error(`Failed to write ${relativePath}:`, e);
        });
    }
    return url;
}
