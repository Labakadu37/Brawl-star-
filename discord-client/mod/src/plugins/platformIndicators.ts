import { getChannel, isRowManager } from "../lib/discord";
import { writeBase64File } from "../lib/fs";
import { logger } from "../lib/logger";
import { filters, find, waitForAll } from "../lib/metro";
import { renderPlatformIconBase64, renderPlatformIcons } from "../lib/platformIcons";
import { definePlugin, getPluginOption, PluginDef } from "../lib/plugins";
import { getReactNative } from "../lib/react";

// Keys of Discord's clientStatuses, in display order.
const PLATFORMS = ["desktop", "mobile", "web", "embedded"];
const LABELS: Record<string, string> = { desktop: "PC", mobile: "Téléphone", web: "Navigateur", embedded: "Console" };

const store = (name: string) => find(filters.byStoreName(name));
const currentUserId = (): string | undefined => store("UserStore")?.getCurrentUser?.()?.id;

// Our own platforms aren't in PresenceStore; like Vencord, read them from our
// open sessions (one per device), keeping the most active status per client.
function ownStatuses(): Record<string, string> | undefined {
    const sessions = store("SessionsStore")?.getSessions?.();
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

/** [platform, status] pairs for a user, e.g. [["desktop", "online"], ["mobile", "dnd"]]. */
function platformsOf(userId: string): [string, string][] {
    let statuses: Record<string, string> | undefined;
    if (userId === currentUserId()) {
        // Fallback when sessions are unavailable: we're on a phone right now.
        statuses = ownStatuses() ?? { mobile: "online" };
    } else {
        const presences = store("PresenceStore");
        statuses = presences?.getClientStatus?.(userId) ?? presences?.getState?.()?.clientStatuses?.[userId];
    }
    if (!statuses) return [];
    return PLATFORMS.filter(p => statuses[p] && statuses[p] !== "offline").map(p => [p, statuses[p]]);
}

// Icons are written once to files: the badge image loaders take file:// URLs
// but not data: URIs.
const iconCache = new Map<string, string>();

function cachedIcon(name: string, base64: () => string, dataUri: () => string): string {
    let uri = iconCache.get(name);
    if (!uri) {
        uri = writeBase64File(`platform-${name.replace(/[^a-z0-9-]/gi, "_")}.png`, base64()) ?? dataUri();
        iconCache.set(name, uri);
    }
    return uri;
}

/** One platform's icon, in its status color, filling the image. */
const singleIcon = (platform: string, status: string) =>
    cachedIcon(`one-${platform}-${status}`, () => renderPlatformIconBase64(platform, status), () => renderPlatformIcons([[platform, status]]));

// Profile badge images don't load local files, so profiles use the same icons
// hosted in this repository (pinned to the commit that added them;
// regenerate with mod/scripts/export-badges.mjs).
const BADGE_BASE_URL =
    "https://raw.githubusercontent.com/Labakadu37/Brawl-star-/6d8a50783ac292dd410fed488ca7de9c23c80cf6/discord-client/assets/badges";
const hostedIcon = (platform: string, status: string) =>
    `${BADGE_BASE_URL}/${platform}-${["online", "idle", "dnd"].includes(status) ? status : "online"}.png`;

/** Most active first (online, idle, dnd), then desktop, mobile, web, console. */
function byActivity(platforms: [string, string][]): [string, string][] {
    const rank = (status: string) => {
        const index = ["online", "idle", "dnd"].indexOf(status);
        return index === -1 ? 3 : index;
    };
    return [...platforms].sort((a, b) => rank(a[1]) - rank(b[1]) || PLATFORMS.indexOf(a[0]) - PLATFORMS.indexOf(b[0]));
}

// --- Chat ---

// Places next to the name in a chat row where an image can go. The first
// three are drawn in color; the APP tag slot tints its icon white.
type Slot = "clan" | "role" | "lobby" | "tag";
const COLOR_SLOTS: Slot[] = ["clan", "role", "lobby"];

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

function isFree(slot: Slot, message: any): boolean {
    switch (slot) {
        case "clan":
            return !message.clanTag;
        case "role":
            return !message.roleIcon && !!roleIconTemplate;
        case "lobby":
            return !message.lobbyTagIconUrl;
        case "tag":
            return !message.tagText;
    }
}

function place(slot: Slot, message: any, icon: string, label: string) {
    switch (slot) {
        case "clan":
            message.clanTag = "​";
            message.clanBadgeUrl = icon;
            // A server tag without a server may not be drawn at all.
            message.clanTagGuildId = message.guildId ?? message.clanTagGuildId;
            break;
        case "role":
            message.roleIcon = withUrl(roleIconTemplate, icon);
            break;
        case "lobby":
            message.lobbyTagIconUrl = icon;
            message.lobbyAdditionalName = message.lobbyAdditionalName ?? "​";
            break;
        case "tag": {
            const processColor = getReactNative()?.processColor;
            message.tagText = "​";
            message.tagIconUrl = icon;
            message.tagVerified = false;
            message.tagBackgroundColor = processColor?.("rgba(128,132,142,0.25)") ?? 0x4080848e;
            message.tagTextColor = processColor?.("rgba(0,0,0,0)") ?? 0;
            message.tagAccessibilityLabel = label;
            break;
        }
    }
}

/**
 * One badge per device, each in its own slot: the slot chosen in the settings
 * first, then the free color slots. Devices never share a badge; with fewer
 * slots than devices, the most active ones are shown (the profile shows all).
 */
function decorateRow(message: any, platforms: [string, string][], chosen: Slot | "auto"): Slot[] {
    const preferred = chosen === "auto" ? COLOR_SLOTS : [chosen, ...COLOR_SLOTS.filter(slot => slot !== chosen)];
    let slots = preferred.filter(slot => isFree(slot, message));
    // No color slot left: the white APP tag beats no badge at all.
    if (!slots.length && isFree("tag", message)) slots = ["tag"];

    const devices = byActivity(platforms);
    const used = slots.slice(0, devices.length);
    used.forEach((slot, i) => {
        const [platform, status] = devices[i];
        place(slot, message, singleIcon(platform, status), LABELS[platform]);
    });
    return used;
}

/**
 * Chat rows are only rebuilt when their message changes, so after presence
 * updates, ask Discord to rebuild the visible messages of those users.
 */
function refreshMessagesOf(userIds: Set<string>) {
    const channelId = store("SelectedChannelStore")?.getChannelId?.();
    if (!channelId) return;
    const messages = store("MessageStore")?.getMessages?.(channelId);
    const list: any[] = messages?.toArray?.() ?? messages?._array ?? [];
    const dispatcher = find(filters.byProps("_interceptors", "dispatch"));
    const guildId = getChannel(channelId)?.guild_id;
    for (const message of list.slice(-50)) {
        if (!userIds.has(message?.author?.id)) continue;
        dispatcher?.dispatch({
            type: "MESSAGE_UPDATE",
            message: { id: message.id, channel_id: channelId, guild_id: guildId, content: message.content },
            // Marks our own actions, so MessageLogger leaves them alone.
            __vercors: true,
        });
    }
}

// --- Profile ---

// Props for our profile badges, keyed by badge id, applied when Discord
// renders them (RenderedBadge only knows how to show Discord's own icons).
const profileBadgeProps = new Map<string, Record<string, unknown>>();

const plugin: PluginDef = definePlugin({
    name: "PlatformIndicators",
    description: "Badges PC, téléphone, navigateur ou console, en couleur du statut, dans le chat et les profils",
    authors: ["JZSCord"],
    options: [
        {
            key: "slot",
            label: "Emplacement dans le chat",
            default: "auto",
            choices: [
                { value: "auto", label: "Auto (un badge par appareil)" },
                { value: "clan", label: "Tag de serveur" },
                { value: "role", label: "Icône de rôle" },
                { value: "lobby", label: "Tag de lobby" },
                { value: "tag", label: "Étiquette APP (blanc)" },
            ],
        },
    ],
    start({ waitFor, after, cleanup }) {
        const logged = new Set<string>();
        // Logs one example of something, to learn how this Discord build works.
        const logOnce = (what: string, value: unknown) => {
            if (logged.has(what)) return;
            logged.add(what);
            logger.log(`PlatformIndicators: ${what}:`, value);
        };

        // Chat rows.
        waitFor(isRowManager, RowManager => {
            after(RowManager.prototype, "generate", ([row], result) => {
                const author = row?.message?.author;
                const message = result?.message;
                // Only rows showing the author's name (first message of a group).
                if (!author?.id || author.bot || !message || typeof message.username !== "string" || !message.username) return;

                if (message.roleIcon) {
                    roleIconTemplate ??= message.roleIcon;
                    logOnce("real role icon", message.roleIcon);
                }
                if (message.clanTag) logOnce("real clan tag", { clanTag: message.clanTag, clanBadgeUrl: message.clanBadgeUrl });

                const platforms = platformsOf(author.id);
                if (!platforms.length) return;
                const used = decorateRow(message, platforms, getPluginOption(plugin, "slot") as Slot | "auto");
                if (used.length) logOnce(`slots ${used.join("+")}`, author.id);
            });
        });

        // Live updates of chat rows when someone's platforms or status change.
        waitFor(filters.byProps("_interceptors", "dispatch", "subscribe"), dispatcher => {
            const pending = new Set<string>();
            let timer: number | undefined;
            const queue = (ids: (string | undefined)[]) => {
                ids.forEach(id => id && pending.add(id));
                if (timer !== undefined || !pending.size) return;
                timer = setTimeout(() => {
                    timer = undefined;
                    const ids = new Set(pending);
                    pending.clear();
                    try {
                        refreshMessagesOf(ids);
                    } catch (e) {
                        logger.error("PlatformIndicators: refresh failed:", e);
                    }
                }, 500);
            };
            const onPresences = (action: any) => queue((action?.updates ?? [action]).map((u: any) => u?.user?.id ?? u?.userId));
            const onSessions = () => queue([currentUserId()]);
            const events: [string, (action: any) => void][] = [
                ["PRESENCE_UPDATES", onPresences],
                ["PRESENCE_UPDATE", onPresences],
                ["SESSIONS_REPLACE", onSessions],
            ];
            events.forEach(([type, handler]) => dispatcher.subscribe(type, handler));
            cleanup(() => {
                events.forEach(([type, handler]) => dispatcher.unsubscribe?.(type, handler));
                if (timer !== undefined) clearTimeout(timer);
            });
        });

        // Profile badges (Revenge's method): add entries to the list returned
        // by Discord's useBadges hook...
        waitFor(m => typeof m?.default === "function" && m.default.name === "useBadges", badgesModule => {
            let watch: ((userId: string) => unknown) | undefined;
            after(badgesModule, "default", ([user], badges) => {
                // Re-render the profile when presences change. Resolved once,
                // so this hook is called the same way on every render.
                if (!watch) {
                    const useStateFromStores = find(filters.byProps("useStateFromStores"))?.useStateFromStores;
                    const stores = [store("PresenceStore"), store("SessionsStore")].filter(Boolean);
                    watch =
                        typeof useStateFromStores === "function"
                            ? userId => useStateFromStores(stores, () => platformsOf(userId).map(p => p.join(":")).join(","))
                            : () => undefined;
                }
                const userId: string | undefined = user?.userId ?? user?.id;
                watch(userId ?? "");
                if (!userId || !Array.isArray(badges) || store("UserStore")?.getUser?.(userId)?.bot) return;

                const ours = byActivity(platformsOf(userId)).map(([platform, status]) => {
                    const id = `jzs-platform-${userId}-${platform}`;
                    profileBadgeProps.set(id, { source: { uri: hostedIcon(platform, status) }, label: LABELS[platform] });
                    return { id, description: LABELS[platform], icon: "_" };
                });
                logOnce("profile badges", ours.length);
                return [...ours, ...badges];
            });
        });

        // ...then give them our image when Discord renders them. Every module
        // exposing the JSX runtime is patched: builds can have more than one.
        const onCreate = ([Component]: any[], element: any) => {
            if (typeof Component !== "function" || Component.name !== "RenderedBadge") return;
            const ours = profileBadgeProps.get(element?.props?.id);
            logOnce(ours ? "RenderedBadge (ours)" : "RenderedBadge", Object.keys(element?.props ?? {}).join(","));
            if (ours) element.props = { ...element.props, ...ours };
        };
        cleanup(
            waitForAll(m => typeof m?.jsx === "function" && typeof m?.jsxs === "function", runtime => {
                after(runtime, "jsx", onCreate);
                after(runtime, "jsxs", onCreate);
            }),
        );
    },
});

export default plugin;
