// Discord modules used by the core, resolved lazily by their properties.

import { findByProps } from "./metro";

export const getMessageActions = () => findByProps("sendMessage", "editMessage");
export const getClydeUtils = () => findByProps("sendBotMessage");

/** Shows a local, ephemeral "Only you can see this" message from Clyde. */
export function sendLocalMessage(channelId: string, content: string) {
    getClydeUtils()?.sendBotMessage(channelId, content);
}

export function sendMessage(channelId: string, content: string) {
    getMessageActions()?.sendMessage(channelId, {
        content,
        tts: false,
        invalidEmojis: [],
        validNonShortcutEmojis: [],
    });
}
