import { blockPosts } from "../lib/http";
import { definePlugin } from "../lib/plugins";

export default definePlugin({
    name: "SilentTyping",
    description: "Les autres ne voient plus « est en train d'écrire… »",
    authors: ["Vercors"],
    start(ctx) {
        // Discord's typing actions, when they can be found…
        ctx.waitForAll(m => typeof m.startTyping === "function", typing => {
            ctx.instead(typing, "startTyping", () => undefined);
        });
        // …and in any case the request that tells the server we're typing.
        blockPosts(ctx, /\/channels\/\d+\/typing(\?|$)/);
    },
});
