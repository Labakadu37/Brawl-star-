// Account utility commands:
//   /view-my-token               ephemeral, shows the current account's token
//   /checkname <name>            asks Discord if a username is available
//   /searchname <length>         generates a random username of that length
//                                and reports whether it is available

import { OptionType } from "../lib/commands";
import { logger } from "../lib/logger";
import { definePlugin } from "../lib/plugins";
import { getUserToken } from "../lib/token";

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

let loggedShape = false;

async function checkAvailability(username: string): Promise<"available" | "taken" | "rate-limited" | "error"> {
    const token = getUserToken();
    if (!token) {
        logger.error("checkname: token not found");
        return "error";
    }
    try {
        const response = await fetch("https://discord.com/api/v9/users/@me/pomelo-attempt", {
            method: "POST",
            headers: {
                "Authorization": token,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ username }),
        });
        if (response.status === 429) return "rate-limited";
        const body = await response.json().catch(() => ({}));
        if (!loggedShape) {
            loggedShape = true;
            logger.log(`checkname: status=${response.status}, body=${JSON.stringify(body)}`);
        }
        if (typeof body.taken === "boolean") return body.taken ? "taken" : "available";
        if (response.status === 200 || response.status === 204) return "available";
        return "error";
    } catch (e: any) {
        if (!loggedShape) {
            loggedShape = true;
            logger.log("checkname: fetch threw:", String(e?.message ?? e));
        }
        return "error";
    }
}

const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

const MAX_ATTEMPTS = 200;
const DELAY_MS = 150; // light pacing to avoid instant rate-limit

/** Keeps generating random names of `length` until one is available. */
async function hunt(length: number, onProgress: (text: string) => void): Promise<void> {
    const tried = new Set<string>();
    for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
        let name = randomUsername(length);
        for (let i = 0; i < 10 && tried.has(name); i++) name = randomUsername(length);
        tried.add(name);

        const status = await checkAvailability(name);
        if (status === "available") return onProgress(`✅ \`${name}\` est **libre** (après ${attempt} essai${attempt > 1 ? "s" : ""})`);
        if (status === "rate-limited") return onProgress(`⚠ Rate-limit de Discord atteint après ${attempt} essai${attempt > 1 ? "s" : ""} — ré-essaie dans ~30s`);
        if (status === "error") return onProgress(`⚠ Erreur réseau après ${attempt} essai${attempt > 1 ? "s" : ""} — \`${name}\` n'a pas pu être testé`);

        if (attempt % 25 === 0) onProgress(`🔎 ${attempt} essais, aucun libre pour l'instant…`);
        await sleep(DELAY_MS);
    }
    onProgress(`⚠ ${MAX_ATTEMPTS} essais sans trouver — relance la commande`);
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
            description: "Cherche en boucle un pseudo libre de la longueur donnée",
            options: [{ name: "longueur", description: "Nombre de caractères (2–32)", type: OptionType.INTEGER, required: true }],
            execute: async ({ longueur }, { reply }) => {
                const length = Math.max(2, Math.min(32, Math.floor(Number(longueur)) || 4));
                reply(`🔎 Je cherche un pseudo libre de ${length} caractères (max 200 essais)…`);
                await hunt(length, reply);
            },
        });
    },
});
