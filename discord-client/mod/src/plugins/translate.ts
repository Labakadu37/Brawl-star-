import { OptionType } from "../lib/commands";
import { definePlugin, getPluginOption, PluginDef } from "../lib/plugins";
import { translate } from "../lib/translate";

// Common languages for the command's choices.
const LANGUAGES = [
    ["fr", "Français"],
    ["en", "Anglais"],
    ["es", "Espagnol"],
    ["ar", "Arabe"],
    ["de", "Allemand"],
    ["pt", "Portugais"],
    ["it", "Italien"],
    ["ja", "Japonais"],
    ["ru", "Russe"],
    ["tr", "Turc"],
] as const;

const plugin: PluginDef = definePlugin({
    name: "Translate",
    description: "Traduit un texte avec /translate (envoyé) ou /tr (visible par toi seul)",
    authors: ["JZCord"],
    options: [
        {
            key: "target",
            label: "Langue par défaut",
            default: "fr",
            choices: LANGUAGES.map(([value, label]) => ({ value, label })),
        },
    ],
    start({ registerCommand }) {
        const langOption = {
            name: "langue",
            description: "Langue de destination",
            type: OptionType.STRING,
            choices: LANGUAGES.map(([value, name]) => ({ name, value })),
        };
        const textOption = { name: "texte", description: "Le texte à traduire", type: OptionType.STRING, required: true };

        const run = async (args: Record<string, any>) => {
            const target = args.langue || getPluginOption(plugin, "target") || "fr";
            const { text, source } = await translate(String(args.texte), target);
            return { text, source, target };
        };

        // Sends the translation as a real message.
        registerCommand({
            name: "translate",
            description: "Traduit un texte et l'envoie",
            options: [textOption, langOption],
            execute: async args => (await run(args)).text,
        });

        // Shows the translation only to you.
        registerCommand({
            name: "tr",
            description: "Traduit un texte (visible par toi seul)",
            options: [textOption, langOption],
            execute: async (args, { reply }) => {
                const { text, source, target } = await run(args);
                reply(`🌐 **${source} → ${target}**\n${text}`);
            },
        });
    },
});

export default plugin;
