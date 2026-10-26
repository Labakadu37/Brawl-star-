import { isRowManager } from "../lib/discord";
import { logger } from "../lib/logger";
import { filters, find } from "../lib/metro";
import { renderPlatformIcons } from "../lib/platformIcons";
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
    if (!uri) iconCache.set(key, (uri = renderPlatformIcons(platforms)));
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
                // An icon-only tag: zero-width text on a transparent background.
                message.tagText = "​";
                message.tagIconUrl = iconFor(platforms);
                message.tagBackgroundColor = processColor?.("rgba(0,0,0,0.01)") ?? 0x03000000;
                message.tagTextColor = processColor?.("rgba(0,0,0,0)") ?? 0;
                message.tagVerified = false;
                message.tagAccessibilityLabel = platforms.map(([p]) => LABELS[p]).join(", ");
            });
        });
    },
});
