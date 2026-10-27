import { logger } from "../lib/logger";
import { definePlugin } from "../lib/plugins";

export default definePlugin({
    name: "Experiments",
    description: "Débloque le menu développeur et les expériences de Discord dans les paramètres",
    authors: ["JZCord"],
    start({ waitFor, cleanup }) {
        // Matched on the module (not the store) so its `default` can be swapped.
        waitFor(
            m => m?.default?.constructor?.displayName === "DeveloperExperimentStore",
            module => {
                const store = module.default;
                // The store's own isDeveloper can't be redefined, so expose a
                // stand-in. Its target is a blank object, which keeps the Proxy
                // free of invariants tied to the store's locked properties.
                const proxy = new Proxy(Object.create(store), {
                    get: (_, key) => (key === "isDeveloper" ? true : Reflect.get(store, key, store)),
                    set: (_, key, value) => Reflect.set(store, key, value, store),
                    has: (_, key) => key in store,
                });
                try {
                    module.default = proxy;
                    if (module.default !== proxy) throw new Error("default is read-only");
                } catch (e) {
                    return logger.error("Experiments: can't replace the store:", e);
                }
                cleanup(() => (module.default = store));
            },
        );
    },
});
