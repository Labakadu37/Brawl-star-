// Slash commands that only exist client-side, injected in Discord's list of
// built-in commands (like /shrug). This is also Vercors' settings UI.

import { sendLocalMessage, sendMessage } from "./discord";
import { logger } from "./logger";
import { filters, waitFor } from "./metro";
import { after } from "./patcher";

// Values from Discord's ApplicationCommand enums.
const CommandType = { CHAT: 1 } as const;
const CommandInputType = { BUILT_IN_TEXT: 1 } as const;
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

function toDiscordCommand(def: CommandDef) {
    return {
        id: `vercors-${def.name}`,
        applicationId: "-1",
        type: CommandType.CHAT,
        inputType: CommandInputType.BUILT_IN_TEXT,
        name: def.name,
        displayName: def.name,
        description: def.description,
        displayDescription: def.description,
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
    waitFor(filters.byProps("getBuiltInCommands"), module => {
        after(module, "getBuiltInCommands", (_args, result) => {
            if (Array.isArray(result)) return result.concat(registered);
        });
    });
}
