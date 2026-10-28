// Reads the current user's auth token, same way Discord's own code does it.
// On Android, the token lives in a module exposed as getToken()/setToken(), and
// in the authentication constants (Z_AUTH_TOKENS → the current user's token).

import { logger } from "./logger";
import { filters, find, findByProps } from "./metro";

function fromGetter(): string | undefined {
    const candidates = [
        () => findByProps("getToken", "setToken"),
        () => findByProps("getToken"),
        () => findByProps("getAuthToken"),
    ];
    for (const get of candidates) {
        try {
            const module = get();
            const token = module?.getToken?.() ?? module?.getAuthToken?.();
            if (typeof token === "string" && token.length > 20) return token;
        } catch {}
    }
    return undefined;
}

function fromAuthStore(): string | undefined {
    try {
        const store = find(filters.byStoreName("AuthenticationStore"));
        const token = store?.getToken?.();
        if (typeof token === "string" && token.length > 20) return token;
    } catch {}
    return undefined;
}

/** Returns the current account's token, or undefined if it cannot be read. */
export function getUserToken(): string | undefined {
    return fromGetter() ?? fromAuthStore();
}

/** "mfa.aBcDeF...xyz" → "mfa.aBcDe…xyz" for a safe preview on screen. */
export function maskToken(token: string): string {
    if (token.length <= 12) return "•".repeat(token.length);
    return `${token.slice(0, 6)}…${token.slice(-4)}`;
}

export function logTokenAvailability() {
    const token = getUserToken();
    logger.log(`Token: ${token ? `available (${token.length} chars)` : "not found"}`);
}
