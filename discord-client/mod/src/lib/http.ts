// Discord's REST helper (get/post/put/del…). Several modules can match, so
// plugins patch all of them.

import { Filter } from "./metro";
import type { PluginContext } from "./plugins";

const isFunction = (value: unknown) => typeof value === "function";

export const httpFilter: Filter = m =>
    isFunction(m.get) && isFunction(m.post) && isFunction(m.put) && (isFunction(m.del) || isFunction(m.patch));

export function requestUrl(request: unknown): string {
    if (typeof request === "string") return request;
    const url = (request as any)?.url;
    return typeof url === "string" ? url : "";
}

/** Makes POST requests whose URL matches `route` succeed without reaching Discord. */
export function blockPosts(ctx: PluginContext, route: RegExp) {
    ctx.waitForAll(httpFilter, http => {
        ctx.instead(http, "post", (args, original) => {
            if (route.test(requestUrl(args[0]))) {
                return Promise.resolve({ ok: true, status: 204, headers: {}, body: null, text: "" });
            }
            return original(...args);
        });
    });
}
