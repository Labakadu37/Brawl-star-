import { filters } from "../lib/metro";
import { definePlugin } from "../lib/plugins";

export default definePlugin({
    name: "SilentTyping",
    description: "Les autres ne voient plus « est en train d'écrire… »",
    authors: ["Vercors"],
    start({ waitFor, instead }) {
        waitFor(filters.byProps("startTyping", "stopTyping"), typing => {
            instead(typing, "startTyping", () => undefined);
        });
    },
});
