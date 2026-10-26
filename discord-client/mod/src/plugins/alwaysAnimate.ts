import { isRowManager } from "../lib/discord";
import { definePlugin } from "../lib/plugins";

export default definePlugin({
    name: "AlwaysAnimate",
    description: "Anime toujours les emojis, GIF et avatars animés dans le chat",
    authors: ["JZSCord"],
    start({ waitFor, after }) {
        waitFor(isRowManager, RowManager => {
            after(RowManager.prototype, "generate", (_, result) => {
                const message = result?.message;
                if (!message || typeof message !== "object") return;
                message.animateEmoji = true;
                message.gifAutoPlay = true;
                // Animated avatars have an "a_" hash; ask the CDN for the GIF.
                if (typeof message.avatarURL === "string" && /\/a_[0-9a-f]+\.(png|webp)/.test(message.avatarURL)) {
                    message.avatarURL = message.avatarURL.replace(/\.(png|webp)/, ".gif");
                }
            });
        });
    },
});
