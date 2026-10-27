import { isRowManager } from "../lib/discord";
import { writeBase64File } from "../lib/fs";
import { logger } from "../lib/logger";
import { filters, find } from "../lib/metro";
import { renderPlatformIcons, renderPlatformIconsBase64 } from "../lib/platformIcons";
import { definePlugin, getPluginOption, PluginDef } from "../lib/plugins";
import { getReactNative } from "../lib/react";

// Keys of Discord's clientStatuses, in display order.
const PLATFORMS = ["desktop", "mobile", "web", "embedded"];
const LABELS: Record<string, string> = { desktop: "PC", mobile: "Téléphone", web: "Navigateur", embedded: "Console" };

const iconCache = new Map<string, string>();

// Our own platforms aren't in PresenceStore; like Vencord, read them from our
// open sessions (one per device), keeping the most active status per client.
function ownStatuses(): Record<string, string> | undefined {
    const sessions = find(filters.byStoreName("SessionsStore"))?.getSessions?.();
    if (!sessions || typeof sessions !== "object") return undefined;
    const rank = (status: string) => ["online", "idle", "dnd"].indexOf(status);
    const statuses: Record<string, string> = {};
    for (const session of Object.values<any>(sessions)) {
        const client = session?.clientInfo?.client;
        if (!client || client === "unknown" || !session.status) continue;
        const current = statuses[client];
        if (!current || (rank(session.status) !== -1 && (rank(current) === -1 || rank(session.status) < rank(current)))) {
            statuses[client] = session.status;
        }
    }
    return Object.keys(statuses).length ? statuses : undefined;
}

function platformsOf(userId: string): [string, string][] {
    let statuses: Record<string, string> | undefined;
    if (userId === find(filters.byStoreName("UserStore"))?.getCurrentUser?.()?.id) {
        // Fallback when sessions are unavailable: we're on a phone right now.
        statuses = ownStatuses() ?? { mobile: "online" };
    } else {
        const presences = find(filters.byStoreName("PresenceStore"));
        statuses = presences?.getClientStatus?.(userId) ?? presences?.getState?.()?.clientStatuses?.[userId];
    }
    if (!statuses) return [];
    return PLATFORMS.filter(p => statuses[p] && statuses[p] !== "offline").map(p => [p, statuses[p]]);
}

function iconFor(platforms: [string, string][]): string {
    const key = platforms.map(p => p.join(":")).join(",");
    let uri = iconCache.get(key);
    if (!uri) {
        // Prefer a file:// URL: the tag/badge image loader doesn't take data: URIs.
        const fileName = `platform-v2-${key.replace(/[^a-z0-9]/gi, "_")}.png`;
        uri = writeBase64File(fileName, renderPlatformIconsBase64(platforms)) ?? renderPlatformIcons(platforms);
        iconCache.set(key, uri);
    }
    return uri;
}

// Places next to the name where a row can show an image. Which ones Discord
// draws in color, and for whom, depends on the build: the user picks.
type Slot = "clan" | "role" | "lobby" | "tag";
const AUTO_ORDER: Slot[] = ["clan", "role", "lobby", "tag"];

// A real role icon, copied to build ours with the exact shape Discord expects.
let roleIconTemplate: any;

function withUrl(template: any, url: string): any {
    if (typeof template === "string") return /^(https?|file|data):/.test(template) ? url : template;
    if (Array.isArray(template)) return template.map(v => withUrl(v, url));
    if (template && typeof template === "object") {
        const copy: any = {};
        for (const key of Object.keys(template)) copy[key] = withUrl(template[key], url);
        return copy;
    }
    return template;
}

/** Puts the badge in `slot`; false if that slot is taken or unusable here. */
function place(slot: Slot, message: any, icon: string, label: string): boolean {
    switch (slot) {
        case "clan":
            if (message.clanTag) return false;
            message.clanTag = "​";
            message.clanBadgeUrl = icon;
            // A server tag without a server may not be drawn at all.
            message.clanTagGuildId = message.guildId ?? message.clanTagGuildId;
            return true;
        case "role":
            if (message.roleIcon || !roleIconTemplate) return false;
            message.roleIcon = withUrl(roleIconTemplate, icon);
            return true;
        case "lobby":
            if (message.lobbyTagIconUrl) return false;
            message.lobbyTagIconUrl = icon;
            message.lobbyAdditionalName = message.lobbyAdditionalName ?? "​";
            return true;
        case "tag": {
            if (message.tagText) return false;
            const processColor = getReactNative()?.processColor;
            message.tagText = "​";
            message.tagIconUrl = icon;
            message.tagVerified = false;
            message.tagBackgroundColor = processColor?.("rgba(128,132,142,0.25)") ?? 0x4080848e;
            message.tagTextColor = processColor?.("rgba(0,0,0,0)") ?? 0;
            message.tagAccessibilityLabel = label;
            return true;
        }
    }
}

const plugin: PluginDef = definePlugin({
    name: "PlatformIndicators",
    description: "Badge à côté du pseudo : PC, téléphone, navigateur ou console, en couleur du statut",
    authors: ["JZSCord"],
    options: [
        {
            key: "slot",
            label: "Emplacement",
            default: "auto",
            choices: [
                { value: "auto", label: "Auto" },
                { value: "clan", label: "Tag de serveur" },
                { value: "role", label: "Icône de rôle" },
                { value: "lobby", label: "Tag de lobby" },
                { value: "tag", label: "Étiquette APP (blanc)" },
            ],
        },
    ],
    start({ waitFor, after }) {
        const logged = new Set<string>();
        // Logs one real example of a row field, to learn how Discord fills it.
        const logOnce = (what: string, value: unknown) => {
            if (logged.has(what)) return;
            logged.add(what);
            logger.log(`PlatformIndicators: ${what}:`, value);
        };

        waitFor(isRowManager, RowManager => {
            after(RowManager.prototype, "generate", ([row], result) => {
                const author = row?.message?.author;
                const message = result?.message;
                // Only rows showing the author's name (first message of a group).
                if (!author?.id || !message || typeof message.username !== "string" || !message.username) return;

                if (message.clanTag) logOnce("real clan tag", { clanTag: message.clanTag, clanBadgeUrl: message.clanBadgeUrl, clanTagGuildId: message.clanTagGuildId });
                if (message.roleIcon) {
                    roleIconTemplate ??= message.roleIcon;
                    logOnce("real role icon", message.roleIcon);
                }
                if (message.tagText) logOnce("real tag", { tagText: message.tagText, tagIconUrl: message.tagIconUrl, tagVerified: message.tagVerified });
                if (message.lobbyTagIconUrl) logOnce("real lobby tag", { lobbyTagIconUrl: message.lobbyTagIconUrl, lobbyAdditionalName: message.lobbyAdditionalName });

                const platforms = platformsOf(author.id);
                if (!platforms.length) return;
                const icon = iconFor(platforms);
                const label = platforms.map(([p]) => LABELS[p]).join(", ");

                const chosen = getPluginOption(plugin, "slot") as Slot | "auto";
                // A chosen slot that's taken (e.g. the person shows a server
                // tag) falls back to the other slots, in the Auto order.
                const order = chosen === "auto" ? AUTO_ORDER : [chosen, ...AUTO_ORDER.filter(s => s !== chosen)];
                const used = order.find(slot => place(slot, message, icon, label));
                if (used) logOnce(`slot ${used}`, `${author.id} → ${label}`);
            });
        });
    },
});

export default plugin;
