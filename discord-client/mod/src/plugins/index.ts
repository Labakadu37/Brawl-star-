import { registerPlugin } from "../lib/plugins";
import clearUrls from "./clearUrls";
import devTools from "./devTools";
import noTrack from "./noTrack";
import silentTyping from "./silentTyping";

export const builtinPlugins = [noTrack, clearUrls, silentTyping, devTools];

export function registerBuiltinPlugins() {
    builtinPlugins.forEach(registerPlugin);
}
