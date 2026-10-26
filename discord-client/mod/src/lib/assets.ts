// Discord's image assets (icons) are registered by id; keep a name → id map.

import { logger } from "./logger";
import { filters, waitFor } from "./metro";
import { after } from "./patcher";

const ids = new Map<string, number>();
let registry: any;
let scanned = false;

export function initAssets() {
    waitFor(filters.byProps("registerAsset", "getAssetByID"), module => {
        registry = module;
        after(module, "registerAsset", ([asset], id) => {
            if (asset?.name && typeof id === "number" && !ids.has(asset.name)) ids.set(asset.name, id);
        });
    });
}

export function findAssetId(name: string): number | undefined {
    if (ids.has(name)) return ids.get(name);
    // Assets registered before our patch: ids are small consecutive integers.
    if (!registry || scanned) return undefined;
    scanned = true;
    for (let id = 1; id < 20000; id++) {
        let asset: any;
        try {
            asset = registry.getAssetByID(id);
        } catch {
            break;
        }
        if (!asset) continue;
        if (!ids.has(asset.name)) ids.set(asset.name, id);
        if (asset.name === name) return id;
    }
    logger.warn(`Asset ${name} not found`);
    return undefined;
}
