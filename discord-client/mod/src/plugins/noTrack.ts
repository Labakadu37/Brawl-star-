import { filters } from "../lib/metro";
import { definePlugin } from "../lib/plugins";

// Discord's telemetry endpoints.
const TRACKING_ROUTES = /\/(science|track|metrics)(\/|\?|$)/;

function requestUrl(request: unknown): string {
    if (typeof request === "string") return request;
    const url = (request as any)?.url;
    return typeof url === "string" ? url : "";
}

export default definePlugin({
    name: "NoTrack",
    description: "Bloque la télémétrie et les analytics envoyées à Discord",
    authors: ["Vercors"],
    enabledByDefault: true,
    start({ waitFor, instead }) {
        waitFor(filters.byProps("get", "post", "put", "del", "patch"), http => {
            instead(http, "post", (args, original) => {
                if (TRACKING_ROUTES.test(requestUrl(args[0]))) {
                    return Promise.resolve({ ok: true, status: 204, headers: {}, body: null, text: "" });
                }
                return original(...args);
            });
        });
    },
});
