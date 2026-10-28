import { distance, remapFrom, toRgb, withRgb } from "../lib/color";
import { logger } from "../lib/logger";
import { filters } from "../lib/metro";
import { definePlugin, getPluginOption, PluginDef } from "../lib/plugins";

// Discord's accent (blurple). Colors near it are remapped to the chosen accent.
const BLURPLE = { r: 88, g: 101, b: 242 };
const TOLERANCE = 70;

const ACCENTS: Record<string, { label: string; rgb: { r: number; g: number; b: number } }> = {
    default: { label: "Bleu (défaut)", rgb: BLURPLE },
    jzcord: { label: "Rose JZCord", rgb: { r: 255, g: 10, b: 140 } },
    red: { label: "Rouge", rgb: { r: 237, g: 66, b: 69 } },
    green: { label: "Vert", rgb: { r: 35, g: 165, b: 90 } },
    purple: { label: "Violet", rgb: { r: 155, g: 89, b: 232 } },
    orange: { label: "Orange", rgb: { r: 240, g: 142, b: 50 } },
    cyan: { label: "Cyan", rgb: { r: 0, g: 180, b: 200 } },
};

const plugin: PluginDef = definePlugin({
    name: "Themes",
    description: "Change la couleur d'accent de tout Discord (boutons, liens, sélection…)",
    authors: ["JZCord"],
    options: [
        {
            key: "accent",
            label: "Couleur d'accent",
            default: "jzcord",
            choices: Object.entries(ACCENTS).map(([value, { label }]) => ({ value, label })),
        },
    ],
    start({ waitFor, instead }) {
        // resolveSemanticColor(theme, token) returns the final color for a UI
        // role. Remap results near Discord's blurple to the chosen accent.
        waitFor(filters.byProps("resolveSemanticColor"), module => {
            let logged = false;
            instead(module, "resolveSemanticColor", (args, original) => {
                const result = original(...args);
                const accentKey = getPluginOption(plugin, "accent");
                if (accentKey === "default") return result;
                const accent = ACCENTS[accentKey]?.rgb ?? BLURPLE;

                const rgb = toRgb(result);
                if (!rgb || distance(rgb, BLURPLE) > TOLERANCE) return result;
                if (!logged) {
                    logged = true;
                    logger.log("Themes: recoloring accent, sample:", result);
                }
                // Shade variants (hover/pressed) keep their offset from blurple.
                return withRgb(result, remapFrom(rgb, BLURPLE, accent));
            });
        });
    },
});

export default plugin;
