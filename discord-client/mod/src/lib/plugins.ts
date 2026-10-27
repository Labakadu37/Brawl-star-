import { CommandDef, registerCommand } from "./commands";
import { logger } from "./logger";
import { Filter, waitFor, waitForAll } from "./metro";
import * as patcher from "./patcher";
import { saveSettings, settings } from "./storage";

/** Tools handed to a plugin; everything is undone automatically on stop. */
export interface PluginContext {
    before: (target: any, name: string, cb: patcher.BeforeCallback) => void;
    after: (target: any, name: string, cb: patcher.AfterCallback) => void;
    instead: (target: any, name: string, cb: patcher.InsteadCallback) => void;
    waitFor: (filter: Filter, callback: (exports: any) => void) => void;
    /** Calls `callback` for every module matching `filter`, including ones loaded later. */
    waitForAll: (filter: Filter, callback: (exports: any) => void) => void;
    registerCommand: (def: CommandDef) => void;
    /** Runs `fn` when the plugin stops. */
    cleanup: (fn: () => void) => void;
}

/** A setting shown under the plugin in JZSCord's settings page. */
export interface PluginOption {
    key: string;
    label: string;
    choices: { value: string; label: string }[];
    default: string;
}

export interface PluginDef {
    name: string;
    description: string;
    authors: string[];
    enabledByDefault?: boolean;
    options?: PluginOption[];
    start?: (ctx: PluginContext) => void;
    stop?: () => void;
}

const plugins = new Map<string, PluginDef>();
const cleanups = new Map<string, (() => void)[]>();

export const definePlugin = (def: PluginDef) => def;

export function registerPlugin(def: PluginDef) {
    plugins.set(def.name, def);
}

export const getPlugins = () => Array.from(plugins.values());
export const findPlugin = (name: string) => getPlugins().find(p => p.name.toLowerCase() === name.toLowerCase());
export const isRunning = (name: string) => cleanups.has(name);

export function isEnabled(plugin: PluginDef) {
    return settings.plugins[plugin.name]?.enabled ?? plugin.enabledByDefault ?? false;
}

export function startPlugin(plugin: PluginDef) {
    if (cleanups.has(plugin.name)) return;
    const disposers: (() => void)[] = [];
    cleanups.set(plugin.name, disposers);
    // Logs each module a plugin hooks into, so the journal shows what worked.
    const found = (cb: (exports: any) => void) => (exports: any) => {
        logger.log(`${plugin.name}: module found (${Object.keys(exports).slice(0, 4).join(", ")})`);
        cb(exports);
    };

    const ctx: PluginContext = {
        before: (t, n, cb) => void disposers.push(patcher.before(t, n, cb)),
        after: (t, n, cb) => void disposers.push(patcher.after(t, n, cb)),
        instead: (t, n, cb) => void disposers.push(patcher.instead(t, n, cb)),
        waitFor: (filter, cb) => void disposers.push(waitFor(filter, found(cb))),
        waitForAll: (filter, cb) => void disposers.push(waitForAll(filter, found(cb))),
        registerCommand: def => void disposers.push(registerCommand(def)),
        cleanup: fn => void disposers.push(fn),
    };
    try {
        plugin.start?.(ctx);
        logger.log(`Started ${plugin.name}`);
    } catch (e) {
        logger.error(`Failed to start ${plugin.name}:`, e);
        stopPlugin(plugin);
    }
}

export function stopPlugin(plugin: PluginDef) {
    const disposers = cleanups.get(plugin.name);
    if (!disposers) return;
    cleanups.delete(plugin.name);
    try {
        plugin.stop?.();
    } catch (e) {
        logger.error(`Error while stopping ${plugin.name}:`, e);
    }
    // Undo in reverse order so stacked patches unwind cleanly.
    for (const dispose of disposers.reverse()) {
        try {
            dispose();
        } catch (e) {
            logger.error(`Error while cleaning up ${plugin.name}:`, e);
        }
    }
}

export function getPluginOption(plugin: PluginDef, key: string): string {
    const option = plugin.options?.find(o => o.key === key);
    const value = settings.plugins[plugin.name]?.options?.[key];
    return option?.choices.some(c => c.value === value) ? value! : option?.default ?? "";
}

/** Plugins read their options when they run, so a change applies right away. */
export function setPluginOption(plugin: PluginDef, key: string, value: string) {
    const entry = (settings.plugins[plugin.name] ??= {});
    entry.options = { ...entry.options, [key]: value };
    saveSettings();
}

export function setPluginEnabled(plugin: PluginDef, enabled: boolean) {
    settings.plugins[plugin.name] = { ...settings.plugins[plugin.name], enabled };
    saveSettings();
    if (enabled) startPlugin(plugin);
    else stopPlugin(plugin);
}

export function startEnabledPlugins() {
    for (const plugin of plugins.values()) {
        if (isEnabled(plugin)) startPlugin(plugin);
    }
}
