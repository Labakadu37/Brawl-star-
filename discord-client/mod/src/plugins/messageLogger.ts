import { isRowManager } from "../lib/discord";
import { logger } from "../lib/logger";
import { filters, find } from "../lib/metro";
import { definePlugin } from "../lib/plugins";
import { getReactNative } from "../lib/react";

const RED = "#f23f43";
const deletedIds = new Set<string>();
// Edited messages: the server's current text and the previous versions.
const serverContent = new Map<string, string>();
const previousVersions = new Map<string, string[]>();
let highlightActive = false;

const dispatcherFilter = filters.byProps("_interceptors", "dispatch");

/**
 * The MESSAGE_UPDATE that keeps a deleted message in the chat, or undefined
 * when the message isn't loaded (nothing to keep).
 */
function updateFor(channelId: string, id: string, guildId?: string) {
    const message = find(filters.byStoreName("MessageStore"))?.getMessage?.(channelId, id);
    if (!message) return undefined;

    // A delete often arrives twice (local + gateway): mark the text only once.
    const alreadyKept = deletedIds.has(id);
    if (!alreadyKept) logger.log(`MessageLogger: kept deleted message ${id}`);
    deletedIds.add(id);
    const content = typeof message.content === "string" ? message.content : "";
    return {
        type: "MESSAGE_UPDATE",
        message: {
            id,
            channel_id: channelId,
            guild_id: guildId,
            // Without the red highlight, mark the text instead.
            content: highlightActive || alreadyKept ? content : `🗑️ ${content}`,
        },
        __vercors: true,
    };
}

/** Keeps the previous text of an edited message under the new one. */
function keepEditHistory(action: any) {
    const update = action.message;
    if (!update?.id || typeof update.content !== "string") return; // embed-only updates
    const channelId = update.channel_id ?? action.channelId;
    const stored = find(filters.byStoreName("MessageStore"))?.getMessage?.(channelId, update.id);
    const current = serverContent.get(update.id) ?? stored?.content;
    const versions = previousVersions.get(update.id) ?? [];
    if (typeof current === "string" && current && current !== update.content) {
        versions.push(current.replace(/\s*\n\s*/g, " "));
        previousVersions.set(update.id, versions);
    }
    serverContent.set(update.id, update.content);
    // Re-append on every update, or a repeated one would hide the history.
    // "-#" is Discord's small grey subtext.
    if (versions.length) update.content += versions.map(v => `\n-# ✏️ ${v}`).join("");
}

// The same action can reach both the interceptor and dispatch().
const seen = new WeakSet<object>();

/** Rewrites delete and edit actions in place so nothing is lost. */
function rewrite(action: any) {
    if (!action || typeof action !== "object" || action.__vercors || seen.has(action)) return;
    seen.add(action);

    if (action.type === "MESSAGE_UPDATE") return keepEditHistory(action);

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
    description: "Garde les messages supprimés (en rouge) et les anciennes versions des messages modifiés (✏️), jusqu'au redémarrage",
    authors: ["JZSCord"],
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
        waitFor(isRowManager, RowManager => {
            highlightActive = true;
            after(RowManager.prototype, "generate", ([row], result) => {
                const id = row?.message?.id ?? result?.message?.id;
                if (!result || !id || !deletedIds.has(id)) return;
                const color = (c: string) => getReactNative()?.processColor?.(c) ?? c;
                result.backgroundHighlight = { backgroundColor: color(`${RED}33`), gutterColor: color(RED) };
            });
        });
        cleanup(() => {
            highlightActive = false;
            serverContent.clear();
            previousVersions.clear();
        });

        const timer = setTimeout(() => {
            if (!highlightActive) logger.warn("MessageLogger: RowManager not found, deleted messages get a 🗑️ marker");
        }, 20000);
        cleanup(() => clearTimeout(timer));
    },
});
