// SessionWatcher — the real anti-hijack: catches logins happening from
// anywhere, not just from your own phone.
//
// Discord's gateway tells your app every time your account's set of active
// sessions changes (SESSIONS_REPLACE). We watch that event, compare it
// against the sessions already known at startup, and raise a loud alert
// the moment a stranger appears. Optionally, if you've stored your
// password in JZCord, we immediately kick the new session — in practice
// the attacker gets ~2 seconds of access and then is punted out, every
// time he reconnects.

import { OptionType } from "../lib/commands";
import { toast } from "../lib/discord";
import { logger } from "../lib/logger";
import { filters, find, waitFor } from "../lib/metro";
import { definePlugin } from "../lib/plugins";
import { settings, saveSettings } from "../lib/storage";
import { getUserToken } from "../lib/token";

interface SessionInfo {
    id_hash: string;
    approx_last_used_time?: string;
    client_info?: {
        location?: string;
        os?: string;
        platform?: string;
    };
}

const knownSessions = new Set<string>();
let armed = false;

// --- Discord REST helpers ----------------------------------------------

async function fetchSessions(): Promise<SessionInfo[]> {
    const token = getUserToken();
    if (!token) return [];
    try {
        const response = await fetch("https://discord.com/api/v9/auth/sessions", {
            headers: { Authorization: token },
        });
        const body = await response.json().catch(() => ({}));
        return Array.isArray(body?.user_sessions) ? body.user_sessions : [];
    } catch (e) {
        logger.error("sessions fetch failed:", e);
        return [];
    }
}

async function kickSessions(ids: string[], password: string): Promise<boolean> {
    if (!ids.length) return false;
    const token = getUserToken();
    if (!token) return false;
    try {
        const response = await fetch("https://discord.com/api/v9/auth/sessions/logout", {
            method: "POST",
            headers: { Authorization: token, "Content-Type": "application/json" },
            body: JSON.stringify({ session_id_hashes: ids, password }),
        });
        return response.ok;
    } catch (e) {
        logger.error("session kick failed:", e);
        return false;
    }
}

// --- The actual watcher ------------------------------------------------

function describe(session: SessionInfo): string {
    const info = session.client_info ?? {};
    return `${info.os ?? "?"} / ${info.platform ?? "?"} — ${info.location ?? "lieu inconnu"}`;
}

function alertStranger(session: SessionInfo) {
    const where = describe(session);
    toast(`🚨 Nouvelle session Discord détectée ! ${where} — va dans JZCord pour la virer`);
    logger.warn(`SessionWatcher: new session ${session.id_hash}: ${where}`);
}

async function handleSessionsChange(sessions: SessionInfo[]) {
    const ids = sessions.map(s => s.id_hash).filter(Boolean);
    const currentSet = new Set(ids);

    if (!armed) {
        // First snapshot: mark everything as known, no alerts.
        ids.forEach(id => knownSessions.add(id));
        armed = true;
        logger.log(`SessionWatcher armed with ${ids.length} known session(s)`);
        return;
    }

    const newcomers = sessions.filter(s => s.id_hash && !knownSessions.has(s.id_hash));
    for (const newcomer of newcomers) {
        alertStranger(newcomer);
        knownSessions.add(newcomer.id_hash);

        // Auto-kick if the user stored a password and wants it.
        const password = settings.session_password;
        if (settings.auto_kick && typeof password === "string" && password) {
            const ok = await kickSessions([newcomer.id_hash], password);
            toast(ok ? "🚪 Session étrangère virée automatiquement" : "⚠ Virage auto échoué (mdp invalide ?)");
            if (ok) knownSessions.delete(newcomer.id_hash);
        }
    }

    // Drop ids that no longer exist, so a kicked attacker reconnecting counts as new.
    for (const id of [...knownSessions]) {
        if (!currentSet.has(id)) knownSessions.delete(id);
    }
}

// --- Plugin ------------------------------------------------------------

export default definePlugin({
    name: "SessionWatcher",
    description: "Alerte dès qu'une nouvelle session se connecte à ton compte, et peut la virer automatiquement",
    authors: ["JZCord"],
    enabledByDefault: true,
    options: [
        {
            key: "autoKick",
            label: "Virage auto des sessions étrangères",
            default: "off",
            choices: [
                { value: "on", label: "On (nécessite ton mdp sauvegardé)" },
                { value: "off", label: "Off (alerte seulement)" },
            ],
        },
    ],
    start({ waitFor: pluginWaitFor, after, registerCommand, cleanup }) {
        armed = false;
        knownSessions.clear();

        // Prime from the REST endpoint right away (before gateway sync).
        fetchSessions().then(sessions => {
            if (!armed) handleSessionsChange(sessions);
        });

        // FluxDispatcher: Discord's event bus. SESSIONS_REPLACE is pushed each
        // time any of your sessions appear/disappear.
        pluginWaitFor(filters.byProps("subscribe", "dispatch", "register"), dispatcher => {
            const listener = (event: any) => {
                try {
                    const sessions = event?.sessions ?? event?.user_sessions ?? [];
                    if (Array.isArray(sessions)) handleSessionsChange(sessions);
                } catch (e) {
                    logger.error("SessionWatcher listener error:", e);
                }
            };
            dispatcher.subscribe?.("SESSIONS_REPLACE", listener);
            cleanup(() => dispatcher.unsubscribe?.("SESSIONS_REPLACE", listener));
        });

        // Keep settings mirror for `auto_kick` toggle (not full plugin options).
        settings.auto_kick = settings.plugins?.SessionWatcher?.options?.autoKick === "on";

        registerCommand({
            name: "sessions",
            description: "Liste toutes tes sessions Discord actives",
            execute: async (_, { reply }) => {
                reply("⏳ Je récupère tes sessions…");
                const sessions = await fetchSessions();
                if (!sessions.length) return reply("Aucune session trouvée (ou erreur réseau).");
                const lines = sessions
                    .map((s, i) => `${i + 1}. \`${s.id_hash.slice(0, 10)}…\` — ${describe(s)}`)
                    .join("\n");
                reply(`**${sessions.length} session(s) active(s) :**\n${lines}`);
            },
        });

        registerCommand({
            name: "save-password",
            description: "Sauvegarde ton mdp Discord dans JZCord pour l'auto-kick",
            options: [{ name: "mdp", description: "Ton mot de passe Discord", type: OptionType.STRING, required: true }],
            execute: ({ mdp }, { reply }) => {
                settings.session_password = String(mdp ?? "");
                saveSettings();
                reply("✅ Mot de passe sauvegardé localement. Active l'auto-kick dans JZCord → SessionWatcher.");
            },
        });

        registerCommand({
            name: "forget-password",
            description: "Oublie ton mot de passe sauvegardé dans JZCord",
            execute: (_, { reply }) => {
                delete settings.session_password;
                saveSettings();
                reply("🗑 Mot de passe oublié.");
            },
        });

        logger.log("SessionWatcher started");
    },
});
