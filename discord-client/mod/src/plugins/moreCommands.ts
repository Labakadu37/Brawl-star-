import { OptionType } from "../lib/commands";
import { definePlugin } from "../lib/plugins";

const text = { name: "texte", description: "Le texte", type: OptionType.STRING, required: true };

const mock = (s: string) => Array.from(s, (c, i) => (i % 2 ? c.toUpperCase() : c.toLowerCase())).join("");
const owo = (s: string) =>
    s
        .replace(/[rl]/g, "w")
        .replace(/[RL]/g, "W")
        .replace(/n([aeiou])/gi, "ny$1")
        .concat(" owo");

export default definePlugin({
    name: "MoreCommands",
    description: "Ajoute /lenny, /reverse, /mock, /owo, /upper, /lower, /flip, /roll",
    authors: ["JZSCord"],
    start({ registerCommand }) {
        registerCommand({ name: "lenny", description: "( ͡° ͜ʖ ͡°)", execute: () => "( ͡° ͜ʖ ͡°)" });
        registerCommand({
            name: "reverse",
            description: "Écrit le texte à l'envers",
            options: [text],
            execute: ({ texte }) => Array.from(String(texte)).reverse().join(""),
        });
        registerCommand({ name: "mock", description: "tExTe MoQuEuR", options: [text], execute: ({ texte }) => mock(String(texte)) });
        registerCommand({ name: "owo", description: "Twansfowme we texte owo", options: [text], execute: ({ texte }) => owo(String(texte)) });
        registerCommand({ name: "upper", description: "TEXTE EN MAJUSCULES", options: [text], execute: ({ texte }) => String(texte).toUpperCase() });
        registerCommand({ name: "lower", description: "texte en minuscules", options: [text], execute: ({ texte }) => String(texte).toLowerCase() });
        registerCommand({
            name: "flip",
            description: "Pile ou face (visible que par toi)",
            execute: (_, { reply }) => reply(Math.random() < 0.5 ? "🪙 Pile" : "🪙 Face"),
        });
        registerCommand({
            name: "roll",
            description: "Lance un dé (visible que par toi)",
            options: [{ name: "faces", description: "Nombre de faces (6 par défaut)", type: OptionType.INTEGER }],
            execute: ({ faces }, { reply }) => {
                const max = Math.max(2, Number(faces) || 6);
                reply(`🎲 ${1 + Math.floor(Math.random() * max)} (sur ${max})`);
            },
        });
    },
});
