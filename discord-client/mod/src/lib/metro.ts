// Discord's JS is a Metro bundle: every module is registered through
// `__d(factory, id, deps)` and initialised lazily by `__r(id)`. We run before
// the bundle, so we can trap the assignment of `__d` and wrap every factory to
// capture its exports the moment the module initialises.

import { logger } from "./logger";

export type Filter = (exports: any) => boolean;
type ModuleListener = (id: number, exports: any) => void;

const definedIds = new Set<number>();
const exportsCache = new Map<number, any>();
const listeners = new Set<ModuleListener>();

function onModuleInitialized(id: number, exports: any) {
    exportsCache.set(id, exports);
    listeners.forEach(listener => {
        try {
            listener(id, exports);
        } catch (e) {
            logger.error(`Module listener failed for module ${id}:`, e);
        }
    });
}

function wrapDefine(define: Function) {
    return function (this: unknown, factory: Function, id: number) {
        definedIds.add(id);
        // Metro calls factory(global, require, importDefault, importAll, module, exports, deps)
        const wrapped = function (this: unknown) {
            const moduleObject = arguments[4];
            const result = factory.apply(this, arguments);
            if (moduleObject) onModuleInitialized(id, moduleObject.exports);
            return result;
        };
        const args = Array.prototype.slice.call(arguments);
        args[0] = wrapped;
        return define.apply(this, args);
    };
}

export function installMetroHook() {
    const g = globalThis as any;
    if (typeof g.__d === "function") {
        logger.warn("Injected after Discord's bundle started: modules defined so far are invisible");
        g.__d = wrapDefine(g.__d);
        return;
    }

    let wrappedDefine: Function | undefined;
    Object.defineProperty(g, "__d", {
        configurable: true,
        enumerable: true,
        get: () => wrappedDefine,
        set: (define: Function) => {
            logger.log("Metro define() captured");
            wrappedDefine = wrapDefine(define);
        },
    });
}

function isObjectLike(value: unknown): value is Record<string, any> {
    return value != null && (typeof value === "object" || typeof value === "function");
}

// Returns the matching export (the module itself or its `default`), or undefined.
function testExports(exports: any, filter: Filter): any {
    if (!isObjectLike(exports)) return undefined;
    try {
        if (filter(exports)) return exports;
    } catch {}
    try {
        if (isObjectLike(exports.default) && filter(exports.default)) return exports.default;
    } catch {}
    return undefined;
}

export const filters = {
    byProps: (...props: string[]): Filter => m => isObjectLike(m) && props.every(p => m[p] !== undefined),
    byName: (name: string): Filter => m => typeof m === "function" && m.name === name,
    byDisplayName: (name: string): Filter => m => isObjectLike(m) && m.displayName === name,
    byStoreName: (name: string): Filter => m => isObjectLike(m) && typeof m.getName === "function" && m.getName() === name,
};

/**
 * Searches modules that are already initialised. With `force`, also
 * initialises not-yet-loaded modules one by one, which can have side effects:
 * prefer `waitFor` in plugins.
 */
export function find(filter: Filter, force = false): any {
    for (const exports of exportsCache.values()) {
        const found = testExports(exports, filter);
        if (found !== undefined) return found;
    }
    if (!force) return undefined;

    const metroRequire = (globalThis as any).__r;
    if (typeof metroRequire !== "function") return undefined;
    for (const id of definedIds) {
        if (exportsCache.has(id)) continue;
        try {
            const found = testExports(metroRequire(id), filter);
            if (found !== undefined) return found;
        } catch {}
    }
    return undefined;
}

export function findAll(filter: Filter): any[] {
    const results: any[] = [];
    for (const exports of exportsCache.values()) {
        const found = testExports(exports, filter);
        if (found !== undefined) results.push(found);
    }
    return results;
}

export const findByProps = (...props: string[]) => find(filters.byProps(...props));
export const findByName = (name: string) => find(filters.byName(name));
export const findByStoreName = (name: string) => find(filters.byStoreName(name));

/**
 * Calls `callback` once with the first module matching `filter`, immediately
 * if it is already loaded, otherwise right after it initialises (before any
 * other module gets to use it). Returns a function cancelling the wait.
 */
export function waitFor(filter: Filter, callback: (exports: any) => void): () => void {
    const existing = find(filter);
    if (existing !== undefined) {
        callback(existing);
        return () => {};
    }

    const listener: ModuleListener = (_id, exports) => {
        const found = testExports(exports, filter);
        if (found === undefined) return;
        listeners.delete(listener);
        callback(found);
    };
    listeners.add(listener);
    return () => listeners.delete(listener);
}

export function getModuleStats() {
    return { defined: definedIds.size, initialized: exportsCache.size };
}
