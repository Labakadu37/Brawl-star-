// Account utility commands:
//   /view-my-token               ephemeral, shows the current account's token
//   /checkname <name>            asks Discord if a username is available
//   /searchname <length>         generates a random username of that length
//                                and reports whether it is available

import { OptionType } from "../lib/commands";
import { find } from "../lib/metro";
import { definePlugin } from "../lib/plugins";
import { getUserToken } from "../lib/token";
import { httpFilter } from "../lib/http";

// Pomelo (new-style Discord usernames) allow lowercase letters, digits, "." and "_".
const USERNAME_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789_.";
const SAFE_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"; // avoid leading dot etc.

function randomUsername(length: number): string {
    const n = Math.max(2, Math.min(32, Math.floor(length) || 4));
    let name = "";
    for (let i = 0; i < n; i++) name += SAFE_CHARS[Math.floor(Math.random() * SAFE_CHARS.length)];
    return name;
}

function isValidUsername(name: string): string | null {
    if (name.length < 2 || name.length > 32) return "2–32 caractères requis";
    if (!/^[a-z0-9_.]+$/.test(name)) return "lettres minuscules, chiffres, _ et . uniquement";
    if (name.startsWith(".") || name.endsWith(".")) return "ne peut pas commencer ou finir par un point";
    if (name.includes("..")) return "pas de points consécutifs";
    return null;
}

async function checkAvailability(username: string): Promise<"available" | "taken" | "error"> {
    const http = find(httpFilter);
    if (!http) return "error";
    try {
        const response = await http.post({
            url: "/users/@me/pomelo-attempt",
            body: { username },
            oldFormErrors: true,
        });
        const body = response?.body ?? {};
        // Discord returns `taken: true/false` on success.
        if (typeof body.taken === "boolean") return body.taken ? "taken" : "available";
        return "error";
    } catch {
        return "error";
    }
}

export default definePlugin({
    name: "AccountTools",
    description: "Commandes /view-my-token, /checkname, /searchname",
    authors: ["JZCord"],
    start({ registerCommand }) {
        registerCommand({
            name: "view-my-token",
            description: "Affiche ton propre token (visible que par toi)",
            execute: (_, { reply }) => {
                const token = getUserToken();
                if (!token) return reply("❌ Token introuvable");
                reply(`🔑 **Ton token** (ne le partage JAMAIS, c'est un accès total à ton compte) :\n\`\`\`\n${token}\n\`\`\``);
            },
        });

        registerCommand({
            name: "checkname",
            description: "Vérifie si un pseudo Discord est libre",
            options: [{ name: "nom", description: "Pseudo à tester", type: OptionType.STRING, required: true }],
            execute: async ({ nom }, { reply }) => {
                const name = String(nom ?? "").trim().toLowerCase();
                const problem = isValidUsername(name);
                if (problem) return reply(`❌ \`${name}\` invalide : ${problem}`);
                reply(`⏳ Je vérifie \`${name}\`…`);
                const status = await checkAvailability(name);
                if (status === "available") reply(`✅ \`${name}\` est **libre**`);
                else if (status === "taken") reply(`❌ \`${name}\` est **pris**`);
                else reply(`⚠ Je n'ai pas pu vérifier \`${name}\` (erreur réseau ou rate-limit)`);
            },
        });

        registerCommand({
            name: "searchname",
            description: "Génère un pseudo aléatoire et dit s'il est libre",
            options: [{ name: "longueur", description: "Nombre de caractères (2–32)", type: OptionType.INTEGER, required: true }],
            execute: async ({ longueur }, { reply }) => {
                const name = randomUsername(Number(longueur));
                reply(`🎲 Je teste \`${name}\`…`);
                const status = await checkAvailability(name);
                if (status === "available") reply(`✅ \`${name}\` est **libre** — relance la commande pour en essayer un autre`);
                else if (status === "taken") reply(`❌ \`${name}\` est **pris** — relance la commande pour en essayer un autre`);
                else reply(`⚠ Je n'ai pas pu vérifier \`${name}\` (erreur réseau ou rate-limit)`);
            },
        });
    },
});
