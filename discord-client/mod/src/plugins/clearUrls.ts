import { filters } from "../lib/metro";
import { definePlugin } from "../lib/plugins";

// Query parameters that only serve tracking. Patterns match the full name.
const TRACKING_PARAMS = [
    /^utm_\w+$/,
    /^(fbclid|gclid|dclid|msclkid|mc_eid|mc_cid|igshid|igsh|yclid|twclid|_hsenc|_hsmi|mkt_tok)$/,
    /^(ref_src|ref_url|si|feature|pp|spm|scm)$/,
    // TikTok share links
    /^(is_from_webapp|sender_device|sender_web_id|is_copy_url|share_app_id|share_link_id|share_item_id|u_code|_r|_t|social_sharing|web_id)$/,
];
const URL_PATTERN = /https?:\/\/[^\s<>]+/g;

export function cleanUrl(url: string): string {
    const hashIndex = url.indexOf("#");
    const hash = hashIndex === -1 ? "" : url.slice(hashIndex);
    const withoutHash = hashIndex === -1 ? url : url.slice(0, hashIndex);
    const queryIndex = withoutHash.indexOf("?");
    if (queryIndex === -1) return url;

    const base = withoutHash.slice(0, queryIndex);
    const kept = withoutHash
        .slice(queryIndex + 1)
        .split("&")
        .filter(pair => {
            const name = decodeURIComponent(pair.split("=")[0]);
            return pair !== "" && !TRACKING_PARAMS.some(pattern => pattern.test(name));
        });
    return base + (kept.length ? `?${kept.join("&")}` : "") + hash;
}

export function cleanContent(content: string): string {
    return content.replace(URL_PATTERN, url => {
        try {
            return cleanUrl(url);
        } catch {
            return url;
        }
    });
}

function cleanMessage(message: any) {
    if (typeof message?.content === "string") message.content = cleanContent(message.content);
}

export default definePlugin({
    name: "ClearURLs",
    description: "Retire les paramètres de pistage (utm_*, fbclid, si…) des liens que tu envoies",
    authors: ["JZCord"],
    enabledByDefault: true,
    start({ waitFor, before }) {
        waitFor(filters.byProps("sendMessage", "editMessage"), messageActions => {
            // sendMessage(channelId, message, ...) / editMessage(channelId, messageId, message)
            before(messageActions, "sendMessage", args => cleanMessage(args[1]));
            before(messageActions, "editMessage", args => cleanMessage(args[2]));
        });
    },
});
