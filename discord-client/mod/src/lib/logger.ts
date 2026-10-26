// Logs go to logcat (tag ReactNativeJS), where the Vercors menu's log viewer
// reads them. We run before React Native defines `console`, so use the native
// hook it is built on when available.

const PREFIX = "[Vercors]";
const enum Level {
    Info = 1,
    Warn = 2,
    Error = 3,
}

function format(value: unknown): string {
    if (value instanceof Error) return value.stack ?? `${value.name}: ${value.message}`;
    if (typeof value === "string") return value;
    try {
        return JSON.stringify(value) ?? String(value);
    } catch {
        return String(value);
    }
}

function write(level: Level, args: unknown[]) {
    const message = [PREFIX, ...args.map(format)].join(" ");
    const g = globalThis as any;
    try {
        if (typeof g.nativeLoggingHook === "function") return g.nativeLoggingHook(message, level);
        g.console?.log(message);
    } catch {}
}

export const logger = {
    log: (...args: unknown[]) => write(Level.Info, args),
    warn: (...args: unknown[]) => write(Level.Warn, args),
    error: (...args: unknown[]) => write(Level.Error, args),
};
