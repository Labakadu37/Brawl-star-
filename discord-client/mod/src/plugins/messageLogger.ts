import { logger } from "../lib/logger";
import { filters, find } from "../lib/metro";
import { definePlugin } from "../lib/plugins";
import { getReactNative } from "../lib/react";

const RED = "#f23f43";
const deletedIds = new Set<string>();
let highlightActive = false;

const dispatcherFilter = filters.byProps("_interceptors", "dispatch");

function currentUserId(): string | undefined {
    try {
        return find(filters.byStoreName("UserStore"))?.getCurrentUser?.()?.id;
    } catch {
        return undefined;
    }
}

/**
 * The MESSAGE_UPDATE that keeps a deleted message in the chat, or undefined
 * when the delete should go through (unknown message, or our own).
 */
function updateFor(channelId: string, id: string, guildId?: string) {
    const message = find(filters.byStoreName("MessageStore"))?.getMessage?.(channelId, id);
    if (!message) return undefined;
    if (message.author?.id && message.author.id === currentUserId()) return undefined;

    deletedIds.add(id);
    const content = typeof message.content === "string" ? message.content : "";
    return {
        type: "MESSAGE_UPDATE",
        message: {
            id,
            channel_id: channelId,
            guild_id: guildId,
            // Without the red highlight, mark the text instead.
            content: highlightActive ? content : `🗑️ ${content}`,
        },
        __vercors: true,
    };
}

/** Rewrites delete actions in place so the messages stay. */
function rewrite(action: any) {
    if (!action || action.__vercors) return;

    if (action.type === "MESSAGE_DELETE") {
        const update = updateFor(action.channelId, action.id, action.guildId);
        if (!update) return;
        for (const key of Object.keys(action)) delete action[key];
        Object.assign(action, update);
    } else if (action.type === "MESSAGE_DELETE_BULK" && Array.isArray(action.ids)) {
        const updates = action.ids
            .map((id: string) => updateFor(action.channelId, id, action.guildId))
            .filter(Boolean);
        if (!updates.length) return;
        const kept = new Set(updates.map((u: any) => u.message.id));
        action.ids = action.ids.filter((id: string) => !kept.has(id));
        // Can't dispatch from inside a dispatch: queue the updates.
        const dispatcher = find(dispatcherFilter);
        setTimeout(() => updates.forEach((u: any) => dispatcher?.dispatch(u)), 0);
    }
}

export default definePlugin({
    name: "MessageLogger",
    description: "Garde les messages supprimés par les autres, surlignés en rouge (jusqu'au redémarrage)",
    authors: ["Vercors"],
    start({ waitFor, before, after, cleanup }) {
        waitFor(dispatcherFilter, dispatcher => {
            // Gateway events go through the interceptors, local ones through dispatch().
            const interceptor = (action: any) => {
                rewrite(action);
                return false; // never block, only rewrite
            };
            if (Array.isArray(dispatcher._interceptors)) {
                dispatcher._interceptors.push(interceptor);
                cleanup(() => {
                    const index = dispatcher._interceptors.indexOf(interceptor);
                    if (index !== -1) dispatcher._interceptors.splice(index, 1);
                });
            }
            before(dispatcher, "dispatch", ([action]) => void rewrite(action));
        });

        // Chat rows are built by RowManager.generate: color the deleted ones.
        waitFor(filters.byName("RowManager"), RowManager => {
            highlightActive = true;
            after(RowManager.prototype, "generate", ([row], result) => {
                const id = row?.message?.id ?? result?.message?.id;
                if (!result || !id || !deletedIds.has(id)) return;
                const color = (c: string) => getReactNative()?.processColor?.(c) ?? c;
                result.backgroundHighlight = { backgroundColor: color(`${RED}33`), gutterColor: color(RED) };
            });
        });
        cleanup(() => (highlightActive = false));

        const timer = setTimeout(() => {
            if (!highlightActive) logger.warn("MessageLogger: RowManager not found, deleted messages get a 🗑️ marker");
        }, 20000);
        cleanup(() => clearTimeout(timer));
    },
});
