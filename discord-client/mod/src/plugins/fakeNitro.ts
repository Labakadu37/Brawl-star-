import { getChannel, getCurrentUser } from "../lib/discord";
import { filters } from "../lib/metro";
import { definePlugin } from "../lib/plugins";

const emojiUrl = (emoji: any) =>
    `https://cdn.discordapp.com/emojis/${emoji.id}.${emoji.animated ? "gif" : "webp"}?size=48&quality=lossless&name=${encodeURIComponent(emoji.name ?? "emoji")}`;

/** Replaces emojis a non-Nitro user can't send with their image link. */
export function replaceLockedEmojis(channelId: string, message: any) {
    if (getCurrentUser()?.premiumType) return; // real Nitro: nothing to do
    if (!Array.isArray(message?.validNonShortcutEmojis) || typeof message.content !== "string") return;
    const guildId = getChannel(channelId)?.guild_id;
    message.validNonShortcutEmojis = message.validNonShortcutEmojis.filter((emoji: any) => {
        if (!emoji?.id) return true; // unicode emoji
        const usable = !emoji.animated && emoji.guildId && emoji.guildId === guildId;
        if (usable) return true;
        const name = emoji.originalName ?? emoji.name;
        message.content = message.content.replace(new RegExp(`<a?:${name}:${emoji.id}>`, "g"), emojiUrl(emoji));
        return false;
    });
}

export default definePlugin({
    name: "FakeNitro",
    description: "Utilise tous les emojis (animés, autres serveurs) sans Nitro : ils sont envoyés en image",
    authors: ["JZSCord"],
    start({ waitFor, instead, before }) {
        // Unlock the emoji picker.
        waitFor(filters.byProps("canUseEmojisEverywhere", "canUseAnimatedEmojis"), premium => {
            instead(premium, "canUseEmojisEverywhere", () => true);
            instead(premium, "canUseAnimatedEmojis", () => true);
        });
        waitFor(filters.byProps("sendMessage", "editMessage"), messageActions => {
            before(messageActions, "sendMessage", ([channelId, message]) => replaceLockedEmojis(channelId, message));
        });
    },
});
