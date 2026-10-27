// /vercors: lists plugins and turns them on/off. Stands in for a settings
// screen, which would break on every Discord UI update.

import { OptionType, registerCommand } from "./lib/commands";
import { getModuleStats, searchKeys } from "./lib/metro";
import { findPlugin, getPlugins, isRunning, setPluginEnabled } from "./lib/plugins";

export function registerSettingsCommand() {
    registerCommand({
        name: "jzcord",
        description: "Gérer les plugins JZCord",
        options: [
            {
                name: "action",
                description: "Que faire",
                type: OptionType.STRING,
                required: true,
                choices: [
                    { name: "liste", value: "list" },
                    { name: "activer", value: "enable" },
                    { name: "désactiver", value: "disable" },
                    { name: "infos", value: "info" },
                    { name: "chercher", value: "search" },
                ],
            },
            { name: "plugin", description: "Nom du plugin (ou mot à chercher)", type: OptionType.STRING },
        ],
        execute({ action, plugin: pluginName }, { reply }) {
            if (action === "list") {
                const lines = getPlugins().map(p => `${isRunning(p.name) ? "🟢" : "⚪"} **${p.name}** — ${p.description}`);
                reply(`**Plugins JZCord**\n${lines.join("\n")}`);
                return;
            }
            if (action === "info") {
                const { defined, initialized } = getModuleStats();
                const loader = (globalThis as any).__VERCORS_LOADER__;
                reply(
                    `**JZCord** v${__VERCORS_VERSION__}\n` +
                        `Loader : ${loader ? `${loader.name} v${loader.version} (bundle ${loader.bundleSource})` : "inconnu"}\n` +
                        `Modules Discord : ${initialized} chargés / ${defined} définis`,
                );
                return;
            }

            if (action === "search") {
                if (!pluginName) return reply("Indique un mot à chercher dans « plugin », par ex. typing");
                const results = searchKeys(String(pluginName));
                reply(results.length ? "```\n" + results.join("\n") + "\n```" : "Aucun module chargé ne contient ce mot.");
                return;
            }

            const plugin = pluginName ? findPlugin(String(pluginName)) : undefined;
            if (!plugin) {
                reply(`Plugin introuvable. Plugins disponibles : ${getPlugins().map(p => p.name).join(", ")}`);
                return;
            }
            const enable = action === "enable";
            setPluginEnabled(plugin, enable);
            reply(`${enable ? "🟢" : "⚪"} **${plugin.name}** ${enable ? "activé" : "désactivé"}`);
        },
    });
}
