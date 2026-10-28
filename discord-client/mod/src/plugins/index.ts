import { registerPlugin } from "../lib/plugins";
import alwaysAnimate from "./alwaysAnimate";
import clearUrls from "./clearUrls";
import devTools from "./devTools";
import experiments from "./experiments";
import fakeNitro from "./fakeNitro";
import messageLogger from "./messageLogger";
import moreCommands from "./moreCommands";
import noReplyMention from "./noReplyMention";
import noTrack from "./noTrack";
import platformIndicators from "./platformIndicators";
import showHiddenChannels from "./showHiddenChannels";
import showMeYourName from "./showMeYourName";
import silentTyping from "./silentTyping";
import themes from "./themes";
import translate from "./translate";

const builtinPlugins = [
    messageLogger,
    showHiddenChannels,
    platformIndicators,
    themes,
    translate,
    fakeNitro,
    showMeYourName,
    noReplyMention,
    alwaysAnimate,
    silentTyping,
    noTrack,
    clearUrls,
    moreCommands,
    experiments,
    devTools,
];

export function registerBuiltinPlugins() {
    builtinPlugins.forEach(registerPlugin);
}
