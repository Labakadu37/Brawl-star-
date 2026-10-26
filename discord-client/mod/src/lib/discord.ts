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

/**
 * The class whose prototype.generate() builds each chat row (message, author
 * name, colors) before the native chat list draws it.
 */
export const isRowManager = (m: any) =>
    typeof m === "function" &&
    (m.name === "RowManager" || (typeof m.prototype?.generate === "function" && /row/i.test(m.name ?? "")));
