import { blockPosts } from "../lib/http";
import { definePlugin } from "../lib/plugins";

// Discord's telemetry endpoints.
const TRACKING_ROUTES = /\/(science|track|metrics)(\/|\?|$)/;

export default definePlugin({
    name: "NoTrack",
    description: "Bloque la télémétrie et les analytics envoyées à Discord",
    authors: ["JZCord"],
    enabledByDefault: true,
    start(ctx) {
        blockPosts(ctx, TRACKING_ROUTES);
    },
});
