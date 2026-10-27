import { isRowManager } from "../lib/discord";
import { writeBase64File } from "../lib/fs";
import { logger } from "../lib/logger";
import { filters, find } from "../lib/metro";
import { renderPlatformIcons, renderPlatformIconsBase64 } from "../lib/platformIcons";
import { definePlugin } from "../lib/plugins";
import { getReactNative } from "../lib/react";

// Keys of Discord's clientStatuses, in display order.
const PLATFORMS = ["desktop", "mobile", "web", "embedded"];
const LABELS: Record<string, string> = { desktop: "PC", mobile: "Téléphone", web: "Navigateur", embedded: "Console" };

const iconCache = new Map<string, string>();

function platformsOf(userId: string): [string, string][] {
    let statuses = find(filters.byStoreName("PresenceStore"))?.getState?.()?.clientStatuses?.[userId];
    // Discord doesn't list our own sessions there; we're on a phone right now.
    if (!statuses && userId === find(filters.byStoreName("UserStore"))?.getCurrentUser?.()?.id) {
        statuses = { mobile: "online" };
    }
    if (!statuses) return [];
    return PLATFORMS.filter(p => statuses[p] && statuses[p] !== "offline").map(p => [p, statuses[p]]);
}

function iconFor(platforms: [string, string][]): string {
    const key = platforms.map(p => p.join(":")).join(",");
    let uri = iconCache.get(key);
    if (!uri) {
        // Prefer a file:// URL: the tag/badge image loader doesn't take data: URIs.
        const fileName = `platform-${key.replace(/[^a-z0-9]/gi, "_")}.png`;
        uri = writeBase64File(fileName, renderPlatformIconsBase64(platforms)) ?? renderPlatformIcons(platforms);
        iconCache.set(key, uri);
    }
    return uri;
}

export default definePlugin({
    name: "PlatformIndicators",
    description: "Badge à côté du pseudo : PC, téléphone, navigateur ou console, en couleur du statut",
    authors: ["JZSCord"],
    start({ waitFor, after }) {
        const logged = new Set<string>();
        // Logs one real example of a row field, to learn how Discord fills it.
        const logOnce = (what: string, value: unknown) => {
            if (logged.has(what)) return;
            logged.add(what);
            logger.log(`PlatformIndicators: real ${what}:`, value);
        };

        waitFor(isRowManager, RowManager => {
            after(RowManager.prototype, "generate", ([row], result) => {
                const author = row?.message?.author;
                const message = result?.message;
                // Only rows showing the author's name (first message of a group).
                if (!author?.id || !message || typeof message.username !== "string" || !message.username) return;

                if (message.clanTag) logOnce("clan tag", { clanTag: message.clanTag, clanBadgeUrl: message.clanBadgeUrl, clanTagGuildId: message.clanTagGuildId });
                if (message.roleIcon) logOnce("role icon", message.roleIcon);
                if (message.tagText) logOnce("tag", { tagText: message.tagText, tagIconUrl: message.tagIconUrl, tagVerified: message.tagVerified });

                const platforms = platformsOf(author.id);
                if (!platforms.length) return;
                const icon = iconFor(platforms);
                const label = platforms.map(([p]) => LABELS[p]).join(", ");

                // The server-tag slot shows its badge image in color (the
                // "APP" tag slot tints its icon white), so use it when free.
                if (!message.clanTag) {
                    message.clanTag = "\u200b";
                    message.clanBadgeUrl = icon;
                    message.clanTagGuildId = undefined;
                    return;
                }

                // The user already shows a server tag: fall back to the tag
                // slot (icon tinted by Discord), unless an app tag is there.
                if (message.tagText) return;
                const processColor = getReactNative()?.processColor;
                message.tagText = "\u200b";
                message.tagIconUrl = icon;
                message.tagVerified = false;
                message.tagBackgroundColor = processColor?.("rgba(128,132,142,0.25)") ?? 0x4080848e;
                message.tagTextColor = processColor?.("rgba(0,0,0,0)") ?? 0;
                message.tagAccessibilityLabel = label;
            });
        });
    },
});
