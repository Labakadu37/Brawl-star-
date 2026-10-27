import { getChannel, toast } from "../lib/discord";
import { logger } from "../lib/logger";
import { filters, find } from "../lib/metro";
import { definePlugin } from "../lib/plugins";

// Discord's ChannelTypes: DMs and categories are never "hidden".
const SKIPPED_TYPES = [1 /* DM */, 3 /* GROUP_DM */, 4 /* GUILD_CATEGORY */];

let permissions: any;
let viewChannel: unknown;
// Set while we ask Discord for the real answer.
let realCheck = false;

function isHidden(channelOrId: any): boolean {
    const channel = typeof channelOrId === "string" ? getChannel(channelOrId) : channelOrId;
    if (!channel || !permissions || viewChannel === undefined || SKIPPED_TYPES.includes(channel.type)) return false;
    realCheck = true;
    try {
        return !permissions.can(viewChannel, channel);
    } finally {
        realCheck = false;
    }
}

function explain(channelOrId: any) {
    const channel = typeof channelOrId === "string" ? getChannel(channelOrId) : channelOrId;
    const topic = channel?.topic ? ` — ${String(channel.topic).slice(0, 120)}` : "";
    toast(`🔒 #${channel?.name ?? "salon"} est caché : tu n'y as pas accès${topic}`);
}

export default definePlugin({
    name: "ShowHiddenChannels",
    description: "Affiche les salons auxquels tu n'as pas accès (on ne peut pas lire leurs messages)",
    authors: ["JZCord"],
    start({ waitFor, after, instead, cleanup }) {
        waitFor(m => m.Permissions?.VIEW_CHANNEL !== undefined, constants => {
            viewChannel = constants.Permissions.VIEW_CHANNEL;
        });

        let loggedHidden = false;
        waitFor(filters.byProps("getChannelPermissions", "can"), store => {
            permissions = store;
            // Pretend every channel is visible, so the channel list shows them all.
            after(store, "can", ([permission, channel], result) => {
                if (!realCheck && viewChannel !== undefined && permission === viewChannel && channel && !SKIPPED_TYPES.includes(channel.type)) {
                    // Log one hidden channel's fields, to learn where its real name lives.
                    if (!loggedHidden && result === false) {
                        loggedHidden = true;
                        logger.log("ShowHiddenChannels: hidden channel:", {
                            name: channel.name,
                            type: channel.type,
                            keys: Object.keys(channel).join(","),
                        });
                    }
                    return true;
                }
                return result;
            });
        });

        // But don't actually open or load a hidden channel: Discord would fail.
        waitFor(filters.byProps("transitionToGuild"), router => {
            instead(router, "transitionToGuild", (args, original) => {
                if (isHidden(args[1])) return explain(args[1]);
                return original(...args);
            });
        });
        waitFor(m => typeof m.fetchMessages === "function" && typeof m.jumpToMessage === "function", fetcher => {
            instead(fetcher, "fetchMessages", (args, original) => {
                const channelId = typeof args[0] === "object" ? args[0]?.channelId : args[0];
                if (isHidden(channelId)) return Promise.resolve();
                return original(...args);
            });
        });

        cleanup(() => {
            permissions = undefined;
            viewChannel = undefined;
        });
        const timer = setTimeout(() => {
            if (viewChannel === undefined) logger.warn("ShowHiddenChannels: VIEW_CHANNEL permission not found yet");
        }, 20000);
        cleanup(() => clearTimeout(timer));
    },
});
