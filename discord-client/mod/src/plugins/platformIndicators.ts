import { isRowManager } from "../lib/discord";
import { logger } from "../lib/logger";
import { filters, find } from "../lib/metro";
import { definePlugin } from "../lib/plugins";

// Keys of Discord's clientStatuses, in display order.
const PLATFORMS: Record<string, string> = {
    desktop: "🖥️",
    mobile: "📱",
    web: "🌐",
    embedded: "🎮",
};

function badgesFor(userId: string): string {
    const presences = find(filters.byStoreName("PresenceStore"));
    let statuses = presences?.getState?.()?.clientStatuses?.[userId];
    // Discord doesn't list our own sessions there; we're on a phone right now.
    if (!statuses && userId === find(filters.byStoreName("UserStore"))?.getCurrentUser?.()?.id) {
        statuses = { mobile: "online" };
    }
    if (!statuses) return "";
    return Object.keys(PLATFORMS)
        .filter(platform => statuses[platform] && statuses[platform] !== "offline")
        .map(platform => PLATFORMS[platform])
        .join("");
}

export default definePlugin({
    name: "PlatformIndicators",
    description: "Affiche à côté du pseudo si la personne est sur PC 🖥️, téléphone 📱 ou navigateur 🌐",
    authors: ["JZSCord"],
    start({ waitFor, after }) {
        let loggedShape = false;
        waitFor(isRowManager, RowManager => {
            after(RowManager.prototype, "generate", ([row], result) => {
                const author = row?.message?.author;
                const message = result?.message;
                if (!author?.id || !message || typeof message !== "object") return;
                if (!loggedShape) {
                    loggedShape = true;
                    logger.log("PlatformIndicators: row fields:", Object.keys(message).join(", "));
                }
                // Only rows that show the author's name (first message of a group).
                if (typeof message.username !== "string" || !message.username) return;
                const badges = badgesFor(author.id);
                if (badges && !message.username.endsWith(badges)) message.username = `${message.username} ${badges}`;
            });
        });
    },
});
