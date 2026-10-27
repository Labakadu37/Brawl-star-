import { getChannel, isRowManager } from "../lib/discord";
import { writeBase64File } from "../lib/fs";
import { logger } from "../lib/logger";
import { filters, find, waitForAll } from "../lib/metro";
import { renderFramedPlatformIconBase64, renderPlatformIconBase64, renderPlatformIcons } from "../lib/platformIcons";
import { definePlugin, getPluginOption, PluginDef } from "../lib/plugins";
import { getReactNative, isComponent } from "../lib/react";

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

/**
 * Discord mobile doesn't always keep other users' per-device statuses. It
 * still knows their status and whether they're on mobile: use that, and count
 * any other device as a PC.
 */
let loggedEstimate = false;

function estimatedStatuses(presences: any, userId: string): Record<string, string> | undefined {
    const status = presences?.getStatus?.(userId);
    const mobile = presences?.isMobileOnline?.(userId);
    if (!loggedEstimate) {
        loggedEstimate = true;
        logger.log("PlatformIndicators: no per-device status for others, estimating:", {
            status,
            isMobileOnline: mobile,
            stateKeys: Object.keys(presences?.getState?.() ?? {}).join(","),
        });
    }
    if (!status || status === "offline" || status === "invisible") return undefined;
    return mobile ? { mobile: status } : { desktop: status };
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
        if (!statuses || !Object.keys(statuses).length) statuses = estimatedStatuses(presences, userId);
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

/** Same, inside a grey frame, for the role icon slot which draws none. */
const framedIcon = (platform: string, status: string) =>
    cachedIcon(`framed-${platform}-${status}`, () => renderFramedPlatformIconBase64(platform, status), () => renderPlatformIcons([[platform, status]]));

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
// three are drawn in color; the APP tag slot tints its icon white. The role
// icon shows the image bigger (we draw its frame); the others are pills with
// a small icon.
type Slot = "clan" | "role" | "lobby" | "tag";
const COLOR_SLOTS: Slot[] = ["role", "clan", "lobby"];

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
function decorateRow(message: any, platforms: [string, string][], chosen: Slot | "auto", onlyOne: boolean): Slot[] {
    const preferred = chosen === "auto" ? COLOR_SLOTS : [chosen, ...COLOR_SLOTS.filter(slot => slot !== chosen)];
    let slots = preferred.filter(slot => isFree(slot, message));
    // No color slot left: the white APP tag beats no badge at all.
    if (!slots.length && isFree("tag", message)) slots = ["tag"];

    const devices = byActivity(platforms).slice(0, onlyOne ? 1 : undefined);
    const used = slots.slice(0, devices.length);
    used.forEach((slot, i) => {
        const [platform, status] = devices[i];
        place(slot, message, slot === "role" ? framedIcon(platform, status) : singleIcon(platform, status), LABELS[platform]);
    });
    return used;
}

/**
 * Chat rows are only rebuilt when their message changes, so after presence
 * updates, ask Discord to rebuild the visible messages of those users.
 */
function refreshMessagesOf(userIds: Set<string> | null) {
    const channelId = store("SelectedChannelStore")?.getChannelId?.();
    if (!channelId) return;
    const messages = store("MessageStore")?.getMessages?.(channelId);
    const list: any[] = messages?.toArray?.() ?? messages?._array ?? [];
    const dispatcher = find(filters.byProps("_interceptors", "dispatch"));
    const guildId = getChannel(channelId)?.guild_id;
    for (const message of list.slice(-50)) {
        // null = every author (a batch of presences just loaded).
        if (userIds && !userIds.has(message?.author?.id)) continue;
        dispatcher?.dispatch({
            type: "MESSAGE_UPDATE",
            message: { id: message.id, channel_id: channelId, guild_id: guildId, content: message.content },
            // Marks our own actions, so MessageLogger leaves them alone.
            __vercors: true,
        });
    }
}

// --- Profile ---

// Prefix of the `icon` of our profile badges.
const ICON_MARKER = "jzs_platform_";

// Props for our profile badges, keyed by badge id, applied when Discord
// renders them (RenderedBadge only knows how to show Discord's own icons).
const profileBadgeProps = new Map<string, Record<string, unknown>>();

const plugin: PluginDef = definePlugin({
    name: "PlatformIndicators",
    description: "Badges PC, téléphone, navigateur ou console, en couleur du statut, dans le chat et les profils",
    authors: ["JZCord"],
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
        {
            key: "devices",
            label: "Appareils dans le chat",
            default: "all",
            choices: [
                { value: "all", label: "Tous (un badge chacun)" },
                { value: "one", label: "Un seul, le plus actif" },
            ],
        },
        {
            key: "avatarDot",
            label: "Rond de statut sur l'avatar",
            default: "icon",
            choices: [
                { value: "icon", label: "Icône plateforme (PC/mobile)" },
                { value: "normal", label: "Rond normal de Discord" },
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
                if (!platforms.length) {
                    // Learn what Discord mobile knows about others' presence.
                    if (author.id !== currentUserId()) {
                        const presences = store("PresenceStore");
                        logOnce("no platforms for others", {
                            status: presences?.getStatus?.(author.id),
                            clientStatus: presences?.getClientStatus?.(author.id),
                            isMobileOnline: presences?.isMobileOnline?.(author.id),
                            stateKeys: Object.keys(presences?.getState?.() ?? {}).join(","),
                        });
                    }
                    return;
                }
                const onlyOne = getPluginOption(plugin, "devices") === "one";
                const used = decorateRow(message, platforms, getPluginOption(plugin, "slot") as Slot | "auto", onlyOne);
                if (used.length) logOnce(`slots ${used.join("+")}`, author.id);
            });
        });

        // Live updates of chat rows when someone's platforms or status change.
        waitFor(filters.byProps("_interceptors", "dispatch", "subscribe"), dispatcher => {
            const pending = new Set<string>();
            let pendingAll = false;
            let timer: number | undefined;
            const flush = () => {
                timer = undefined;
                const ids = pendingAll ? null : new Set(pending);
                pending.clear();
                pendingAll = false;
                try {
                    refreshMessagesOf(ids);
                } catch (e) {
                    logger.error("PlatformIndicators: refresh failed:", e);
                }
            };
            const arm = () => {
                if (timer === undefined) timer = setTimeout(flush, 500);
            };
            const queue = (ids: (string | undefined)[]) => {
                ids.forEach(id => id && pending.add(id));
                if (pending.size) arm();
            };
            // Bulk events (presences loaded in batch, member list scrolled):
            // the user ids aren't always given, so refresh the open channel.
            const queueAll = () => {
                pendingAll = true;
                arm();
            };
            const onPresences = (action: any) => queue((action?.updates ?? [action]).map((u: any) => u?.user?.id ?? u?.userId));
            const onSessions = () => queue([currentUserId()]);
            const events: [string, (action: any) => void][] = [
                ["PRESENCE_UPDATES", onPresences],
                ["PRESENCE_UPDATE", onPresences],
                ["SESSIONS_REPLACE", onSessions],
                // Fire when Discord lazily loads other users' presence/devices.
                ["PRESENCES_REPLACE", queueAll],
                ["GUILD_MEMBER_LIST_UPDATE", queueAll],
                ["GUILD_PRESENCES", queueAll],
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
                    // Discord builds the badge image URL from `icon`; the
                    // marker lets us spot that image and swap in ours.
                    return { id, description: LABELS[platform], icon: `${ICON_MARKER}${platform}_${status}` };
                });
                logOnce("profile badges", ours.length);
                return [...ours, ...badges];
            });
        });

        // ...then make sure they show our image. However this build draws a
        // badge, it ends up creating an image whose URL contains the marker
        // from `icon`: rewrite that URL. Components that receive the badge id
        // (RenderedBadge, as in Revenge) also get our source and label.
        const onCreate = ([Component]: any[], element: any) => {
            const props = element?.props;
            if (!props || typeof props !== "object") return;
            const name = Component?.displayName ?? Component?.name;

            // Replace the avatar status dot with our platform icon.
            if (
                name === "Status" &&
                "isMobileOnline" in props &&
                getPluginOption(plugin, "avatarDot") === "icon"
            ) {
                const status: string | undefined = props.status;
                const Image = getReactNative()?.Image;
                if (status && status !== "offline" && status !== "invisible" && isComponent(Image)) {
                    const platform = props.isVROnline ? "embedded" : props.isMobileOnline ? "mobile" : "desktop";
                    const size = Math.round((props.size ?? 16) * 1.35);
                    // Render our icon in place of Discord's dot (type/props swap).
                    element.type = Image;
                    element.props = { source: { uri: hostedIcon(platform, status) }, style: [props.style, { width: size, height: size }] };
                }
                return;
            }

            if (typeof props.id === "string" && props.id.startsWith("jzs-platform-")) {
                logOnce(`badge drawn by ${Component?.displayName ?? Component?.name ?? typeof Component}`, Object.keys(props).join(","));
                const ours = profileBadgeProps.get(props.id);
                if (ours) element.props = { ...props, ...ours };
                return;
            }
            const source = props.source;
            const uri = Array.isArray(source) ? source[0]?.uri : source?.uri;
            if (typeof uri !== "string" || !uri.includes(ICON_MARKER)) return;
            const match = new RegExp(`${ICON_MARKER}([a-z]+)_([a-z]+)`).exec(uri);
            if (!match) return;
            logOnce("badge image rewritten", uri);
            element.props = { ...props, source: { uri: hostedIcon(match[1], match[2]) } };
        };
        // Every module exposing the JSX runtime (builds can have more than
        // one), and classic React.createElement.
        cleanup(
            waitForAll(m => typeof m?.jsx === "function" && typeof m?.jsxs === "function", runtime => {
                after(runtime, "jsx", onCreate);
                after(runtime, "jsxs", onCreate);
            }),
        );
        waitFor(filters.byProps("createElement", "useState", "useEffect"), react => {
            after(react, "createElement", onCreate);
        });
    },
});

export default plugin;
