import { OptionType } from "../lib/commands";
import { definePlugin } from "../lib/plugins";

function stringify(value: unknown): string {
    if (typeof value === "function") return value.toString();
    try {
        return JSON.stringify(value, null, 2) ?? String(value);
    } catch {
        return String(value);
    }
}

export default definePlugin({
    name: "DevTools",
    description: "Ajoute /eval pour explorer les modules Discord (globalThis.jzscord) — pour développeurs",
    authors: ["JZSCord"],
    start({ registerCommand }) {
        registerCommand({
            name: "eval",
            description: "Exécute du JavaScript dans Discord",
            options: [{ name: "code", description: "Code à exécuter", type: OptionType.STRING, required: true }],
            async execute({ code }, { reply }) {
                // Indirect eval runs in the global scope, which is all Hermes supports.
                const result = await (0, eval)(String(code));
                const text = stringify(result);
                reply("```js\n" + (text.length > 1900 ? text.slice(0, 1900) + "\n…" : text) + "\n```");
            },
        });
    },
});
