import { isRowManager } from "../lib/discord";
import { definePlugin } from "../lib/plugins";

export default definePlugin({
    name: "ShowMeYourName",
    description: "Affiche le vrai nom d'utilisateur après le surnom : « Surnom (pseudo) »",
    authors: ["JZSCord"],
    start({ waitFor, after }) {
        waitFor(isRowManager, RowManager => {
            after(RowManager.prototype, "generate", ([row], result) => {
                const username = row?.message?.author?.username;
                const message = result?.message;
                if (!username || typeof message?.username !== "string" || !message.username) return;
                if (message.username.toLowerCase() === username.toLowerCase()) return;
                if (message.username.endsWith(`(${username})`)) return;
                message.username = `${message.username} (${username})`;
            });
        });
    },
});
