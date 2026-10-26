// Everything printed here ends up in logcat under the "ReactNativeJS" tag.
const PREFIX = "[Vercors]";

export const logger = {
    log: (...args: unknown[]) => console.log(PREFIX, ...args),
    warn: (...args: unknown[]) => console.warn(PREFIX, ...args),
    error: (...args: unknown[]) => console.error(PREFIX, ...args),
};
