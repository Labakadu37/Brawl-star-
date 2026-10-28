// Second line of defense after AntiTokenGrab: even if someone gets your
// session, they still can't change your password, email or 2FA without
// first unlocking the guard from inside *your* JZCord app.
//
// How a hijack usually goes:
//   1. attacker gets a session (grabbed token / stolen phone / phished)
//   2. they change the password → your token is invalidated, you're out
//   3. they change the email and disable 2FA → recovery is dead too
//
// AccountGuard blocks step 2 and 3. The attacker's "change password" POST
// is rejected until the real owner taps "Déverrouiller 60s" in JZCord
// settings on their own device. Discord gives you time to spot a new
// login (gateway events, email notification) and kick the attacker out
// with /kick-sessions before they can do anything permanent.

import { OptionType } from "../lib/commands";
import { toast } from "../lib/discord";
import { logger } from "../lib/logger";
import { definePlugin } from "../lib/plugins";
import { getUserToken } from "../lib/token";

// Endpoints that let a hijacker lock you out of your own account.
// Format: method + URL path. We match any URL that ends with that path.
const PROTECTED: { method: string; pattern: RegExp; label: string }[] = [
    { method: "PATCH", pattern: /\/users\/@me(\?|$)/, label: "modif du profil (mot de passe, email, pseudo)" },
    { method: "POST", pattern: /\/users\/@me\/mfa\/.*\/disable/, label: "désactivation du 2FA" },
    { method: "DELETE", pattern: /\/users\/@me\/mfa\//, label: "retrait d'une méthode 2FA" },
    { method: "POST", pattern: /\/users\/@me\/mfa\/sms\/disable/, label: "désactivation du SMS 2FA" },
    { method: "POST", pattern: /\/users\/@me\/delete/, label: "suppression du compte" },
    { method: "POST", pattern: /\/users\/@me\/disable/, label: "désactivation du compte" },
    { method: "POST", pattern: /\/auth\/logout/, label: "déconnexion forcée" },
];

interface Blocked {
    at: number;
    method: string;
    url: string;
    label: string;
}

const blocked: Blocked[] = [];
export const getGuardBlocked = () => blocked.slice();

// Timestamp until which sensitive changes are allowed through.
let unlockedUntil = 0;
export function unlockFor(seconds: number) {
    unlockedUntil = Date.now() + seconds * 1000;
    toast(`🔓 Protection levée ${seconds}s — fais tes changements maintenant`);
}
export const guardIsUnlocked = () => Date.now() < unlockedUntil;
export const guardSecondsLeft = () => Math.max(0, Math.ceil((unlockedUntil - Date.now()) / 1000));

function matchProtected(method: string, url: string) {
    const m = method.toUpperCase();
    for (const rule of PROTECTED) {
        if (rule.method !== m) continue;
        if (rule.pattern.test(url)) return rule;
    }
    return null;
}

function record(method: string, url: string, label: string) {
    blocked.push({ at: Date.now(), method, url, label });
    if (blocked.length > 50) blocked.splice(0, blocked.length - 50);
    logger.warn(`AccountGuard blocked ${method} ${url} (${label})`);
    toast(`🔒 JZCord a bloqué : ${label}. Déverrouille dans les réglages si c'est toi.`);
}

async function callLogoutEverywhere(password: string): Promise<boolean> {
    const token = getUserToken();
    if (!token) return false;
    try {
        // List sessions first so we can target them all except the current one.
        const sessionsResponse = await fetch("https://discord.com/api/v9/auth/sessions", {
            headers: { Authorization: token },
        });
        const { user_sessions } = await sessionsResponse.json().catch(() => ({ user_sessions: [] }));
        const ids: string[] = (user_sessions ?? [])
            .map((s: any) => s?.id_hash)
            .filter((h: unknown): h is string => typeof h === "string");
        if (!ids.length) return false;

        const response = await fetch("https://discord.com/api/v9/auth/sessions/logout", {
            method: "POST",
            headers: { Authorization: token, "Content-Type": "application/json" },
            body: JSON.stringify({ session_id_hashes: ids, password }),
        });
        return response.ok;
    } catch (e) {
        logger.error("logout-everywhere failed:", e);
        return false;
    }
}

export default definePlugin({
    name: "AccountGuard",
    description: "Bloque les changements de mot de passe/email/2FA tant que tu n'as pas levé la protection",
    authors: ["JZCord"],
    enabledByDefault: true,
    start({ cleanup, registerCommand }) {
        const g = globalThis as any;

        const origFetch = g.fetch;
        if (typeof origFetch === "function") {
            g.fetch = function (this: any, input: any, init?: any) {
                const url = typeof input === "string" ? input : input?.url ?? "";
                const method = String(init?.method ?? (typeof input === "object" ? input?.method : undefined) ?? "GET").toUpperCase();
                const rule = matchProtected(method, url);
                if (rule && !guardIsUnlocked()) {
                    record(method, url, rule.label);
                    return Promise.reject(new Error("Blocked by JZCord AccountGuard"));
                }
                return origFetch.call(this, input, init);
            };
            cleanup(() => void (g.fetch = origFetch));
        }

        const XHR = g.XMLHttpRequest;
        if (typeof XHR === "function") {
            const openPrototype = XHR.prototype.open;
            const sendPrototype = XHR.prototype.send;
            XHR.prototype.open = function (this: any, method: string, url: string, ...rest: any[]) {
                this.__jzcord_guard_method = method;
                this.__jzcord_guard_url = url;
                return openPrototype.call(this, method, url, ...rest);
            };
            XHR.prototype.send = function (this: any, body?: any) {
                const rule = matchProtected(this.__jzcord_guard_method ?? "GET", this.__jzcord_guard_url ?? "");
                if (rule && !guardIsUnlocked()) {
                    record(this.__jzcord_guard_method, this.__jzcord_guard_url, rule.label);
                    try {
                        this.onerror?.(new Error("Blocked by JZCord AccountGuard"));
                    } catch {}
                    try {
                        this.abort?.();
                    } catch {}
                    return;
                }
                return sendPrototype.call(this, body);
            };
            cleanup(() => {
                XHR.prototype.open = openPrototype;
                XHR.prototype.send = sendPrototype;
            });
        }

        registerCommand({
            name: "unlock-guard",
            description: "Lève la protection pendant 60s pour changer ton mot de passe/email/2FA",
            execute: (_, { reply }) => {
                unlockFor(60);
                reply("🔓 Protection levée pendant 60 secondes. Fais tes changements maintenant.");
            },
        });

        registerCommand({
            name: "guard-log",
            description: "Montre les derniers changements de compte bloqués",
            execute: (_, { reply }) => {
                if (!blocked.length) return reply("🔒 Aucun blocage pour l'instant. Personne n'a essayé.");
                const lines = blocked
                    .slice(-10)
                    .map(({ at, method, label }) => `• ${new Date(at).toLocaleTimeString()} — ${method} (${label})`)
                    .join("\n");
                reply(`🔒 **${blocked.length} blocage(s)**, 10 plus récents :\n${lines}`);
            },
        });

        registerCommand({
            name: "kick-everywhere",
            description: "Déconnecte toutes les autres sessions (nécessite ton mot de passe)",
            options: [{ name: "password", description: "Ton mot de passe Discord", type: OptionType.STRING, required: true }],
            execute: async ({ password }, { reply }) => {
                reply("🚪 Déconnexion de toutes les autres sessions…");
                const ok = await callLogoutEverywhere(String(password ?? ""));
                reply(ok ? "✅ Toutes les autres sessions sont déconnectées. Toi tu restes." : "❌ Échec — mauvais mot de passe, 2FA requis, ou erreur réseau.");
            },
        });

        logger.log(`AccountGuard: shielding ${PROTECTED.length} endpoint(s)`);
    },
});
