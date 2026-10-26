import { definePlugin } from "../lib/plugins";

export default definePlugin({
    name: "Experiments",
    description: "Débloque le menu développeur et les expériences de Discord dans les paramètres",
    authors: ["Vercors"],
    start({ waitFor, cleanup }) {
        waitFor(
            m => m?.constructor?.displayName === "DeveloperExperimentStore",
            store => {
                // An own property shadows the store's getter.
                Object.defineProperty(store, "isDeveloper", { configurable: true, get: () => true });
                cleanup(() => delete store.isDeveloper);
            },
        );
    },
});
