// Slash commands that only exist client-side, injected in Discord's list of
// built-in commands (like /shrug). This is also Vercors' settings UI.

import { LOGO_URL } from "./branding";
import { sendLocalMessage, sendMessage } from "./discord";
import { logger } from "./logger";
import { filters, waitFor } from "./metro";
import { after } from "./patcher";

// Values from Discord's ApplicationCommand enums.
const CommandType = { CHAT: 1 } as const;
const CommandInputType = { BUILT_IN: 0 } as const;
export const OptionType = { STRING: 3, INTEGER: 4, BOOLEAN: 5 } as const;

// Our commands go in their own "/" section, headed by the JZCord logo, instead
// of Discord's "Built-In". The id ties a command to its section.
const SECTION_ID = "jzcord";

export interface CommandOption {
    name: string;
    description: string;
    type: number;
    required?: boolean;
    choices?: { name: string; value: string | number }[];
}

export interface CommandContext {
    channelId: string;
    reply: (content: string) => void;
}

export interface CommandDef {
    name: string;
    description: string;
    options?: CommandOption[];
    /** Return a string to send it as a real message. */
    execute: (args: Record<string, any>, ctx: CommandContext) => string | void | Promise<string | void>;
}

const registered: any[] = [];
// Discord sorts commands by numeric id; built-ins use small negative ids.
let nextId = -100000;

function toDiscordCommand(def: CommandDef) {
    return {
        id: String(nextId--),
        applicationId: SECTION_ID,
        type: CommandType.CHAT,
        inputType: CommandInputType.BUILT_IN,
        name: def.name,
        displayName: def.name,
        untranslatedName: def.name,
        description: def.description,
        displayDescription: def.description,
        untranslatedDescription: def.description,
        options: (def.options ?? []).map(option => ({
            ...option,
            displayName: option.name,
            displayDescription: option.description,
            choices: option.choices?.map(choice => ({ ...choice, displayName: choice.name })),
        })),
        async execute(rawArgs: { name: string; value: unknown }[], rawCtx: any) {
            const channelId: string = rawCtx?.channel?.id;
            const ctx: CommandContext = { channelId, reply: content => sendLocalMessage(channelId, content) };
            const args: Record<string, any> = {};
            for (const arg of rawArgs ?? []) args[arg.name] = arg.value;
            try {
                const result = await def.execute(args, ctx);
                if (typeof result === "string" && result) sendMessage(channelId, result);
            } catch (e) {
                logger.error(`/${def.name} failed:`, e);
                ctx.reply(`❌ /${def.name} a échoué : ${String(e)}`);
            }
        },
    };
}

export function registerCommand(def: CommandDef): () => void {
    const command = toDiscordCommand(def);
    registered.push(command);
    return () => {
        const index = registered.indexOf(command);
        if (index !== -1) registered.splice(index, 1);
    };
}

/**
 * Adds a "JZCord" section to the "/" menu, headed by our logo, by cloning an
 * existing section so the shape matches this Discord build exactly.
 */
function addSection(module: any) {
    const sections = module.BUILT_IN_SECTIONS;
    if (!sections) return logger.warn("BUILT_IN_SECTIONS not found; commands stay under Built-In");

    const entries: any[] = Array.isArray(sections) ? sections : Object.values(sections);
    logger.log("A built-in section:", entries[0] ? JSON.stringify(entries[0]) : "none");
    if (entries.some(s => s?.id === SECTION_ID)) return;

    const template = entries.find(s => s && typeof s === "object") ?? {};
    const section = { ...template, id: SECTION_ID, name: "JZCord", icon: LOGO_URL };
    if (Array.isArray(sections)) sections.push(section);
    else sections[SECTION_ID] = section;
    logger.log("JZCord section added:", JSON.stringify(section));
}

export function initCommands() {
    waitFor(filters.byProps("getBuiltInCommands", "BUILT_IN_SECTIONS"), module => {
        logger.log("Commands module found, adding", registered.length, "command(s)");
        try {
            addSection(module);
        } catch (e) {
            logger.error("Failed to add the JZCord section:", e);
        }
        // getBuiltInCommands(type | type[], includeHidden, ...)
        after(module, "getBuiltInCommands", ([type], result) => {
            if (!Array.isArray(result)) return;
            const wanted = (command: any) =>
                type == null || (Array.isArray(type) ? type.includes(command.type) : type === command.type);
            return result.concat(registered.filter(wanted));
        });
    });
}
