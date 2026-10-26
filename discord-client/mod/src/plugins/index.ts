import { registerPlugin } from "../lib/plugins";
import clearUrls from "./clearUrls";
import devTools from "./devTools";
import noTrack from "./noTrack";
import silentTyping from "./silentTyping";

export function registerBuiltinPlugins() {
    [noTrack, clearUrls, silentTyping, devTools].forEach(registerPlugin);
}
