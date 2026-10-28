// Blocks any outgoing network request that tries to send your Discord token
// to a non-Discord server — the exact shape of every token grabber.
//
// How a grabber works: a malicious plugin / webhook script reads the token,
// then POSTs it (in body, URL or Authorization header) to a webhook or a
// server it controls. We intercept global fetch() and XMLHttpRequest, read
// the live token at send time, and refuse any request whose target is not
// a Discord domain and that carries the token in any field.

import { toast } from "../lib/discord";
import { logger } from "../lib/logger";
import { definePlugin } from "../lib/plugins";
import { getUserToken } from "../lib/token";

// Discord's own domains — the only places your token is ever meant to go.
const DISCORD_HOSTS = /(^|\.)(discord\.com|discordapp\.com|discord\.gg|discord\.media|discordapp\.net|discordstatus\.com)$/i;

interface Blocked {
    at: number;
    url: string;
    where: "url" | "body" | "header";
}

const blocked: Blocked[] = [];
export const getBlocked = () => blocked.slice();

function hostOf(url: string): string {
    try {
        return new URL(url, "https://discord.com").hostname;
    } catch {
        return "";
    }
}

function isDiscordHost(url: string): boolean {
    const host = hostOf(url);
    return host === "" || DISCORD_HOSTS.test(host);
}

function headerValues(headers: any): string[] {
    if (!headers) return [];
    const values: string[] = [];
    try {
        if (typeof headers.forEach === "function") {
            headers.forEach((value: string) => void values.push(String(value)));
        } else if (Array.isArray(headers)) {
            for (const [, value] of headers) values.push(String(value));
        } else if (typeof headers === "object") {
            for (const value of Object.values(headers)) values.push(String(value));
        }
    } catch {}
    return values;
}

function record(url: string, where: Blocked["where"]) {
    blocked.push({ at: Date.now(), url, where });
    if (blocked.length > 50) blocked.splice(0, blocked.length - 50);
    const short = url.slice(0, 60);
    logger.warn(`AntiTokenGrab blocked ${where} to ${short}`);
    toast(`🛡 JZCord a bloqué un vol de token (${short})`);
}

function detectGrab(url: string, body: unknown, headers: any, token: string): Blocked["where"] | null {
    if (!token || isDiscordHost(url)) return null;
    if (url.includes(token)) return "url";
    const bodyText = typeof body === "string" ? body : "";
    if (bodyText.includes(token)) return "body";
    if (headerValues(headers).some(value => value.includes(token))) return "header";
    return null;
}

export default definePlugin({
    name: "AntiTokenGrab",
    description: "Bloque tout envoi de ton token Discord vers un site qui n'est pas Discord",
    authors: ["JZCord"],
    enabledByDefault: true,
    start({ cleanup, registerCommand }) {
        registerCommand({
            name: "grab-log",
            description: "Montre les tentatives de vol de token bloquées",
            execute: (_, { reply }) => {
                if (!blocked.length) return reply("🛡 Aucun vol de token bloqué pour l'instant. Tu es safe.");
                const lines = blocked
                    .slice(-10)
                    .map(({ at, url, where }) => {
                        const time = new Date(at).toLocaleTimeString();
                        return `• ${time} — ${where} → \`${url.slice(0, 70)}\``;
                    })
                    .join("\n");
                reply(`🛡 **${blocked.length} tentative(s) bloquée(s)**, 10 plus récentes :\n${lines}`);
            },
        });


        const g = globalThis as any;

        // --- fetch() ---
        const origFetch = g.fetch;
        if (typeof origFetch === "function") {
            const wrapped = function (this: any, input: any, init?: any) {
                const url = typeof input === "string" ? input : input?.url ?? "";
                const headers = init?.headers ?? (typeof input === "object" ? input?.headers : undefined);
                const token = getUserToken();
                const where = token ? detectGrab(url, init?.body, headers, token) : null;
                if (where) {
                    record(url, where);
                    return Promise.reject(new Error("Blocked by JZCord AntiTokenGrab"));
                }
                return origFetch.call(this, input, init);
            };
            g.fetch = wrapped;
            cleanup(() => void (g.fetch = origFetch));
        }

        // --- XMLHttpRequest ---
        const XHR = g.XMLHttpRequest;
        if (typeof XHR === "function") {
            const openPrototype = XHR.prototype.open;
            const sendPrototype = XHR.prototype.send;
            const setHeaderPrototype = XHR.prototype.setRequestHeader;

            XHR.prototype.open = function (this: any, method: string, url: string, ...rest: any[]) {
                this.__jzcord_url = url;
                this.__jzcord_headers = {};
                return openPrototype.call(this, method, url, ...rest);
            };
            XHR.prototype.setRequestHeader = function (this: any, name: string, value: string) {
                (this.__jzcord_headers ??= {})[name] = value;
                return setHeaderPrototype.call(this, name, value);
            };
            XHR.prototype.send = function (this: any, body?: any) {
                const token = getUserToken();
                const url = this.__jzcord_url ?? "";
                const where = token ? detectGrab(url, body, this.__jzcord_headers, token) : null;
                if (where) {
                    record(url, where);
                    // Fake a network error on the XHR so the caller moves on.
                    try {
                        this.onerror?.(new Error("Blocked by JZCord AntiTokenGrab"));
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
                XHR.prototype.setRequestHeader = setHeaderPrototype;
            });
        }

        logger.log(`AntiTokenGrab: shielding ${getUserToken() ? "with" : "without"} a known token`);
    },
});
