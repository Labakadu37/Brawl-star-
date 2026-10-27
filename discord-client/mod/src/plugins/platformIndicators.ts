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
        let loggedRealTag = false;
        waitFor(isRowManager, RowManager => {
            after(RowManager.prototype, "generate", ([row], result) => {
                const author = row?.message?.author;
                const message = result?.message;
                // Only rows showing the author's name (first message of a group).
                if (!author?.id || !message || typeof message.username !== "string" || !message.username) return;

                if (message.tagText) {
                    // Keep real tags (APP, BOT…); log one to learn how they look.
                    if (!loggedRealTag) {
                        loggedRealTag = true;
                        const { tagText, tagType, tagIconUrl, tagBackgroundColor, tagTextColor, tagVerified } = message;
                        logger.log("PlatformIndicators: real tag:", { tagText, tagType, tagIconUrl, tagBackgroundColor, tagTextColor, tagVerified });
                    }
                    return;
                }

                const platforms = platformsOf(author.id);
                if (!platforms.length) return;
                const processColor = getReactNative()?.processColor;
                // Vencord look: status-colored icons on a grey pill. Not
                // "verified": that mode tints the icon to a single color.
                // Zero-width text keeps the tag icon-only.
                message.tagText = "​";
                message.tagIconUrl = iconFor(platforms);
                message.tagVerified = false;
                message.tagBackgroundColor = processColor?.("rgba(128,132,142,0.25)") ?? 0x4080848e;
                message.tagTextColor = processColor?.("rgba(0,0,0,0)") ?? 0;
                message.tagAccessibilityLabel = platforms.map(([p]) => LABELS[p]).join(", ");
            });
        });
    },
});
