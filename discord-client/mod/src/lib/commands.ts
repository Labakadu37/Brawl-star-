// Slash commands that only exist client-side, injected in Discord's list of
// built-in commands (like /shrug). This is also Vercors' settings UI.

import { sendLocalMessage, sendMessage } from "./discord";
import { logger } from "./logger";
import { filters, waitFor } from "./metro";
import { after } from "./patcher";

// Values from Discord's ApplicationCommand enums.
const CommandType = { CHAT: 1 } as const;
const CommandInputType = { BUILT_IN: 0 } as const;
export const OptionType = { STRING: 3, INTEGER: 4, BOOLEAN: 5 } as const;

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
        applicationId: "-1",
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

export function initCommands() {
    let loggedShape = false;
    waitFor(filters.byProps("getBuiltInCommands"), module => {
        logger.log("Commands module found, adding", registered.length, "command(s)");
        logger.log("Commands module keys:", Object.keys(module).join(","));
        // getBuiltInCommands(type | type[], includeHidden, ...)
        after(module, "getBuiltInCommands", ([type], result) => {
            if (!Array.isArray(result)) return;
            // Log a real built-in command once, to learn how its icon/section
            // is represented (for a future JZCord section with our logo).
            if (!loggedShape && result.length) {
                loggedShape = true;
                const sample = result.find(c => c && c.name) ?? result[0];
                logger.log("Built-in command shape:", Object.keys(sample).join(","));
                logger.log("Built-in command section fields:", {
                    applicationId: sample.applicationId,
                    section: sample.__registeredSection ?? sample.section,
                    icon: sample.icon,
                    id: sample.id,
                });
            }
            const wanted = (command: any) =>
                type == null || (Array.isArray(type) ? type.includes(command.type) : type === command.type);
            return result.concat(registered.filter(wanted));
        });
    });

    // Log how command sections are built, to add a JZCord one later.
    waitFor(
        m => typeof m?.getBuiltInSectionsWithCommands === "function" || typeof m?.useApplicationCommandSections === "function" || typeof m?.getApplicationSections === "function",
        sections => logger.log("Command sections module keys:", Object.keys(sections).join(",")),
    );
}
