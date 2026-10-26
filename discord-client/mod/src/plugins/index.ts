import { registerPlugin } from "../lib/plugins";
import clearUrls from "./clearUrls";
import devTools from "./devTools";
import experiments from "./experiments";
import messageLogger from "./messageLogger";
import moreCommands from "./moreCommands";
import noTrack from "./noTrack";
import silentTyping from "./silentTyping";

const builtinPlugins = [messageLogger, noTrack, clearUrls, silentTyping, moreCommands, experiments, devTools];

export function registerBuiltinPlugins() {
    builtinPlugins.forEach(registerPlugin);
}
