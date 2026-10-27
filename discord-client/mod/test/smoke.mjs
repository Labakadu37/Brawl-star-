// Runs the built bundle against a fake Metro runtime and fake Discord modules,
// to check the injection order and the plugins without a phone.
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import vm from "node:vm";
import React from "react";
import TestRenderer from "react-test-renderer";

const files = new Map();
const calls = [];

const logs = [];
globalThis.nativeLoggingHook = message => logs.push(message);
globalThis.nativeModuleProxy = {
    NativeFileModule: {
        writeFile: async (dir, path, data, encoding) => void files.set(`/docs/${path}`, encoding === "base64" ? Buffer.from(data, "base64") : data),
    },
};
// What the Xposed loader's preload.js provides.
globalThis.__VERCORS_LOADER__ = { version: "test", dataDir: "/docs/vercors", settings: { plugins: { ClearURLs: { enabled: true } } } };

// Inject Vercors first, exactly like the Xposed loader does.
vm.runInThisContext(readFileSync(new URL("../dist/vercors.js", import.meta.url), "utf8"));

// --- Minimal Metro runtime (what Discord's bundle starts with) ---
const registry = new Map();
globalThis.__d = (factory, id, deps) => registry.set(id, { factory, deps, module: null });
globalThis.__r = id => {
    const entry = registry.get(id);
    if (!entry.module) {
        entry.module = { exports: {} };
        entry.factory(globalThis, globalThis.__r, null, null, entry.module, entry.module.exports, entry.deps);
    }
    return entry.module.exports;
};

// --- Fake Discord modules ---
// Like Discord's module 31: a Proxy answering every property, defined first.
__d((g, r, d, a, module) => {
    module.exports = new Proxy({}, { get: () => ({}), has: () => true });
}, 99);
// A module that looks like react-native's index but has no components.
__d((g, r, d, a, module) => {
    module.exports = { AppRegistry: {}, ToastAndroid: { SHORT: 0, show: m => calls.push(["toast", m]) }, NativeModules: {} };
}, 98);
__d((g, r, d, a, module) => {
    module.exports = {
        AppRegistry: {},
        ScrollView: "ScrollView",
        View: "View",
        Text: "Text",
        Switch: "Switch",
        Pressable: "Pressable",
        processColor: color => `processed(${color})`,
        ToastAndroid: { SHORT: 0, show: message => calls.push(["toast", message]) },
    };
}, 0);
__d((g, r, d, a, module) => {
    module.exports = { startTyping: id => calls.push(["startTyping", id]), stopTyping: () => {} };
}, 1);
__d((g, r, d, a, module) => {
    module.exports = {
        sendMessage: (channelId, message) => calls.push(["sendMessage", channelId, message.content]),
        editMessage: (channelId, messageId, message) => calls.push(["editMessage", messageId, message.content]),
    };
}, 2);
__d((g, r, d, a, module) => {
    module.exports = { getBuiltInCommands: () => [{ name: "shrug" }] };
}, 3);
__d((g, r, d, a, module) => {
    module.exports = { sendBotMessage: (channelId, content) => calls.push(["bot", channelId, content]) };
}, 4);
__d((g, r, d, a, module) => {
    const request = url => Promise.resolve({ ok: true, real: url });
    module.exports = { get: request, post: request, put: request, del: request, patch: request };
}, 5);

__d((g, r, d, a, module) => void (module.exports = React), 10);
__d((g, r, d, a, module) => {
    module.exports = { SETTING_RENDERER_CONFIG: { ACCOUNT: { type: "route" } } };
}, 11);
__d((g, r, d, a, module) => {
    module.exports = { createList: config => ({ list: config.sections }) };
}, 12);
const navigations = [];
__d((g, r, d, a, module) => {
    module.exports = { getRootNavigationRef: () => ({ navigate: (...args) => navigations.push(args) }) };
}, 13);
const dispatched = [];
__d((g, r, d, a, module) => {
    module.exports = {
        _interceptors: [],
        dispatch(action) {
            if (this._interceptors.some(intercept => intercept(action))) return;
            dispatched.push(JSON.parse(JSON.stringify(action)));
        },
    };
}, 14);
__d((g, r, d, a, module) => {
    const messages = {
        m1: { id: "m1", content: "salut", author: { id: "other" } },
        m2: { id: "m2", content: "le mien", author: { id: "me" } },
        m3: { id: "m3", content: "bulk", author: { id: "other" } },
    };
    module.exports = { getName: () => "MessageStore", getMessage: (channelId, id) => messages[id] };
}, 15);
__d((g, r, d, a, module) => {
    module.exports = { getName: () => "UserStore", getCurrentUser: () => ({ id: "me" }) };
}, 16);
__d((g, r, d, a, module) => {
    const clientStatuses = { pcAndPhone: { desktop: "online", mobile: "idle" }, web: { web: "dnd" }, gone: { desktop: "offline" } };
    module.exports = {
        getName: () => "PresenceStore",
        getState: () => ({ clientStatuses }),
        getClientStatus: id => clientStatuses[id],
    };
}, 19);
__d((g, r, d, a, module) => {
    module.exports = {
        getName: () => "SessionsStore",
        getSessions: () => ({
            a: { status: "idle", clientInfo: { client: "mobile" } },
            b: { status: "online", clientInfo: { client: "desktop" } },
            c: { status: "online", clientInfo: { client: "mobile" } },
            d: { status: "online", clientInfo: { client: "unknown" } },
        }),
    };
}, 27);
__d((g, r, d, a, module) => {
    function RowManager() {}
    RowManager.prototype.generate = row => ({
        message: {
            ...row.message,
            username: row.message.author ? row.message.author.globalName ?? row.message.author.username : undefined,
            author: undefined,
        },
    });
    module.exports = { __esModule: true, default: RowManager };
}, 17);
__d((g, r, d, a, module) => {
    function DeveloperExperimentStore() {}
    DeveloperExperimentStore.displayName = "DeveloperExperimentStore";
    const store = new DeveloperExperimentStore();
    store.getName = () => "DeveloperExperimentStore";
    Object.defineProperty(store, "isDeveloper", { get: () => false, configurable: false });
    module.exports = { __esModule: true, default: store };
}, 18);

// Boot order similar to Discord: react-native first, then the app modules.
const channels = {
    visible: { id: "visible", type: 0, guild_id: "g1", name: "general" },
    secret: { id: "secret", type: 0, guild_id: "g1", name: "admins", topic: "staff only" },
    dm: { id: "dm", type: 1 },
};
__d((g, r, d, a, module) => {
    module.exports = { getName: () => "ChannelStore", getChannel: id => channels[id] };
}, 20);
__d((g, r, d, a, module) => {
    module.exports = { Permissions: { VIEW_CHANNEL: 1024n, SEND_MESSAGES: 2048n } };
}, 21);
__d((g, r, d, a, module) => {
    module.exports = { getChannelPermissions: () => 0n, can: (permission, channel) => channel?.id !== "secret" };
}, 22);
const navigated = [];
__d((g, r, d, a, module) => {
    module.exports = { transitionToGuild: (guildId, channelId) => navigated.push(channelId) };
}, 23);
const fetched = [];
__d((g, r, d, a, module) => {
    module.exports = { fetchMessages: ({ channelId }) => (fetched.push(channelId), Promise.resolve("real")), jumpToMessage: () => {} };
}, 24);
__d((g, r, d, a, module) => {
    module.exports = { createPendingReply: reply => calls.push(["reply", reply.shouldMention]) };
}, 25);
__d((g, r, d, a, module) => {
    module.exports = { canUseEmojisEverywhere: () => false, canUseAnimatedEmojis: () => false };
}, 26);

for (const id of [99, 98, 0, 10, 1, 2, 3, 4, 5, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27]) __r(id);
await new Promise(resolve => setTimeout(resolve, 10));

const { jzscord: vercors } = globalThis;
assert.equal(typeof vercors.version, "string");

assert.ok(calls.some(c => c[0] === "toast" && /chargé/.test(c[1])), "load toast shown");
calls.length = 0;

// SilentTyping is off by default.
__r(1).startTyping("c1");
assert.deepEqual(calls.pop(), ["startTyping", "c1"]);

// ClearURLs (on by default)
__r(2).sendMessage("c1", { content: "regarde https://youtu.be/abc?si=XYZ&t=42 et https://x.com/a?utm_source=tw#top" });
assert.deepEqual(calls.pop(), ["sendMessage", "c1", "regarde https://youtu.be/abc?t=42 et https://x.com/a#top"]);

// NoTrack (on by default)
assert.equal((await __r(5).post({ url: "/science" })).status, 204);
assert.equal((await __r(5).post({ url: "/channels/1/messages" })).real.url, "/channels/1/messages");

// Commands are appended to Discord's built-ins.
const commands = __r(3).getBuiltInCommands(1);
assert.equal(commands[0].name, "shrug");
const vercorsCmd = commands.find(c => c.name === "jzscord");
assert.ok(vercorsCmd, "/vercors registered");
assert.match(vercorsCmd.id, /^-\d+$/);
assert.equal(vercorsCmd.untranslatedName, "jzscord");
assert.ok(!__r(3).getBuiltInCommands(2).some(c => c.name === "jzscord"), "filtered by command type");
assert.ok(__r(3).getBuiltInCommands([1, 2]).some(c => c.name === "jzscord"));
assert.ok(!commands.some(c => c.name === "eval"), "/eval only with DevTools");

const run = (cmd, args) =>
    cmd.execute(Object.entries(args).map(([name, value]) => ({ name, value })), { channel: { id: "c1" } });

await run(vercorsCmd, { action: "enable", plugin: "silenttyping" });
assert.match(calls.pop()[2], /SilentTyping\*\* activé/);
__r(1).startTyping("c1");
assert.notDeepEqual(calls.at(-1), ["startTyping", "c1"]);

await run(vercorsCmd, { action: "list" });
assert.match(calls.pop()[2], /🟢 \*\*SilentTyping/);

await run(vercorsCmd, { action: "disable", plugin: "SilentTyping" });
calls.pop();
__r(1).startTyping("c2");
assert.deepEqual(calls.pop(), ["startTyping", "c2"]);

// SilentTyping also blocks the typing request at the HTTP layer.
await run(vercorsCmd, { action: "enable", plugin: "SilentTyping" });
calls.pop();
assert.equal((await __r(5).post({ url: "/channels/123/typing" })).status, 204);
assert.equal((await __r(5).post({ url: "/science" })).status, 204);
assert.equal((await __r(5).post({ url: "/channels/123/messages" })).real.url, "/channels/123/messages");
await run(vercorsCmd, { action: "disable", plugin: "SilentTyping" });
calls.pop();
assert.equal((await __r(5).post({ url: "/channels/123/typing" })).real.url, "/channels/123/typing");

await run(vercorsCmd, { action: "search", plugin: "typing" });
assert.match(calls.pop()[2], /1: startTyping, stopTyping/);

// DevTools adds /eval when enabled.
await run(vercorsCmd, { action: "enable", plugin: "DevTools" });
calls.pop();
const evalCmd = __r(3).getBuiltInCommands(1).find(c => c.name === "eval");
await run(evalCmd, { code: "1 + 1" });
assert.match(calls.pop()[2], /```js\n2\n```/);

// Settings are persisted (debounced).
await new Promise(resolve => setTimeout(resolve, 600));
const saved = JSON.parse(files.get("/docs/vercors/settings.json"));
assert.deepEqual(saved.plugins, {
    ClearURLs: { enabled: true },
    SilentTyping: { enabled: false },
    DevTools: { enabled: true },
});
assert.ok(logs.some(l => l.includes("SilentTyping: module found")));
assert.ok(logs.some(l => l.includes("Metro define() captured")));
assert.ok(logs.some(l => l.includes("Commands module found")));
assert.ok(!logs.some(l => l.includes("not a function")), "proxy module must not match filters");


// --- Settings: a Vercors section in Discord's own settings ---
const config = __r(11).SETTING_RENDERER_CONFIG;
assert.ok(config.ACCOUNT, "Discord's rows are kept");
assert.equal(config.VERCORS.type, "pressable");
const { list } = __r(12).createList({ sections: [{ label: "Compte", settings: ["ACCOUNT"] }, { label: "App", settings: ["X"] }] });
assert.deepEqual(list.map(s => s.label), ["Compte", "JZSCord", "App"]);
config.VERCORS.onPress();
assert.equal(navigations.pop()[0], "VERCORS_PAGE");

// The page renders (with React Native fallbacks) and toggles plugins live.
const Page = config.VERCORS_PAGE.screen.getComponent();
let renderer;
TestRenderer.act(() => void (renderer = TestRenderer.create(React.createElement(Page))));
const switchFor = label =>
    renderer.root.findAll(n => n.type === "View" && n.props.style?.flexDirection === "row")
        .find(row => row.findAllByType("Text").some(t => t.props.children === label))
        .findByType("Switch");
assert.equal(switchFor("MessageLogger").props.value, false);
TestRenderer.act(() => switchFor("MessageLogger").props.onValueChange(true));
assert.equal(switchFor("MessageLogger").props.value, true);

// --- MessageLogger ---
const dispatcher = __r(14);
dispatcher.dispatch({ type: "MESSAGE_DELETE", id: "m1", channelId: "c1" });
const kept = dispatched.pop();
assert.equal(kept.type, "MESSAGE_UPDATE", "deleted message is kept");
assert.deepEqual(kept.message, { id: "m1", channel_id: "c1", content: "salut" });
const row = new (__r(17).default)().generate({ message: { id: "m1" } });
assert.deepEqual(row.backgroundHighlight, { backgroundColor: "processed(#f23f4333)", gutterColor: "processed(#f23f43)" });
assert.equal(new (__r(17).default)().generate({ message: { id: "other" } }).backgroundHighlight, undefined);

dispatcher.dispatch({ type: "MESSAGE_DELETE", id: "m2", channelId: "c1" });
assert.equal(dispatched.pop().type, "MESSAGE_UPDATE", "own messages are kept too");
dispatcher.dispatch({ type: "MESSAGE_DELETE", id: "m1", channelId: "c1" });
assert.equal(dispatched.pop().message.content, "salut", "second delete event changes nothing");

dispatcher.dispatch({ type: "MESSAGE_DELETE_BULK", ids: ["m3", "unknown"], channelId: "c1" });
assert.deepEqual(dispatched.pop().ids, ["unknown"]);
await new Promise(resolve => setTimeout(resolve, 5));
assert.equal(dispatched.pop().message.id, "m3");

TestRenderer.act(() => switchFor("MessageLogger").props.onValueChange(false));
dispatcher.dispatch({ type: "MESSAGE_DELETE", id: "m1", channelId: "c1" });
assert.equal(dispatched.pop().type, "MESSAGE_DELETE", "off means deletes go through");
assert.equal(dispatcher._interceptors.length, 0, "interceptor removed");

// --- PlatformIndicators ---
const rowFor = (userId, extra = {}) =>
    new (__r(17).default)().generate({ message: { id: "x", author: { id: userId, username: "Pseudo" }, ...extra } }).message;
assert.equal(rowFor("pcAndPhone").clanBadgeUrl, undefined, "off by default");
await run(vercorsCmd, { action: "enable", plugin: "PlatformIndicators" });
calls.pop();
// No server tag: the badge goes in the server-tag slot, shown in color.
const pcAndPhone = rowFor("pcAndPhone");
assert.equal(pcAndPhone.username, "Pseudo", "no text added to the name");
assert.equal(pcAndPhone.clanTag, "​");
assert.equal(pcAndPhone.tagText, undefined, "the tinted tag slot stays free");
assert.ok(pcAndPhone.clanBadgeUrl.startsWith("file:///docs/vercors/platform-"), pcAndPhone.clanBadgeUrl);
await new Promise(r => setTimeout(r, 5));
const png = files.get(pcAndPhone.clanBadgeUrl.replace("file:///docs/", "/docs/"));
assert.ok(png, "icon file written");
assert.deepEqual([...png.subarray(0, 8)], [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a], "a PNG");
// The pixel data must inflate (valid zlib) to 48 rows of 1 filter byte + 48 pixels.
const { inflateSync } = await import("node:zlib");
const idat = png.indexOf(Buffer.from("IDAT"));
const idatLength = png.readUInt32BE(idat - 4);
assert.equal(inflateSync(png.subarray(idat + 4, idat + 4 + idatLength)).length, 48 * 49);
assert.equal(rowFor("pcAndPhone").clanBadgeUrl, pcAndPhone.clanBadgeUrl, "icons are cached");
assert.notEqual(rowFor("web").clanBadgeUrl, pcAndPhone.clanBadgeUrl);
assert.equal(rowFor("gone").clanBadgeUrl, undefined, "offline platforms are hidden");
assert.equal(rowFor("unknown").clanBadgeUrl, undefined);
// Our own platforms come from our sessions (desktop + mobile, best status each).
assert.ok(rowFor("me").clanBadgeUrl.includes("desktop_online_mobile_online"), rowFor("me").clanBadgeUrl);
// Someone showing a server tag keeps it; the badge falls back to the tag slot.
const withClan = rowFor("pcAndPhone", { clanTag: "JZBS", clanBadgeUrl: "https://cdn/clan.png" });
assert.equal(withClan.clanTag, "JZBS");
assert.equal(withClan.clanBadgeUrl, "https://cdn/clan.png");
assert.equal(withClan.tagIconUrl, pcAndPhone.clanBadgeUrl);
assert.equal(withClan.tagVerified, false);
assert.equal(withClan.tagAccessibilityLabel, "PC, Téléphone");
const withBoth = rowFor("pcAndPhone", { clanTag: "JZBS", tagText: "APP" });
assert.equal(withBoth.tagText, "APP", "real app tags are kept");
await run(vercorsCmd, { action: "disable", plugin: "PlatformIndicators" });
calls.pop();
assert.equal(rowFor("pcAndPhone").clanBadgeUrl, undefined);

const enable = async (name, on = true) => {
    await run(vercorsCmd, { action: on ? "enable" : "disable", plugin: name });
    calls.pop();
};

// --- ShowHiddenChannels ---
const perms = __r(22);
assert.equal(perms.can(1024n, channels.secret), false);
await enable("ShowHiddenChannels");
assert.equal(perms.can(1024n, channels.secret), true, "hidden channel listed");
assert.equal(perms.can(2048n, channels.secret), false, "other permissions untouched");
__r(23).transitionToGuild("g1", "secret");
assert.deepEqual(navigated, [], "can't open a hidden channel");
__r(23).transitionToGuild("g1", "visible");
assert.deepEqual(navigated, ["visible"]);
assert.equal(await __r(24).fetchMessages({ channelId: "secret" }), undefined);
assert.equal(await __r(24).fetchMessages({ channelId: "visible" }), "real");
await enable("ShowHiddenChannels", false);
assert.equal(perms.can(1024n, channels.secret), false);

// --- FakeNitro ---
assert.equal(__r(26).canUseAnimatedEmojis(), false);
await enable("FakeNitro");
assert.equal(__r(26).canUseAnimatedEmojis(), true);
__r(2).sendMessage("visible", {
    content: "yo <:ok:111> <a:dance:222> <:other:333>",
    validNonShortcutEmojis: [
        { id: "111", name: "ok", guildId: "g1", animated: false },
        { id: "222", name: "dance", guildId: "g1", animated: true },
        { id: "333", name: "other", guildId: "g2", animated: false },
    ],
});
assert.equal(
    calls.pop()[2],
    "yo <:ok:111> https://cdn.discordapp.com/emojis/222.gif?size=48&quality=lossless&name=dance https://cdn.discordapp.com/emojis/333.webp?size=48&quality=lossless&name=other",
);
await enable("FakeNitro", false);

// --- NoReplyMention ---
await enable("NoReplyMention");
__r(25).createPendingReply({ shouldMention: true });
assert.deepEqual(calls.pop(), ["reply", false]);
await enable("NoReplyMention", false);

// --- ShowMeYourName & AlwaysAnimate ---
const generate = author => new (__r(17).default)().generate({ message: { id: "y", author, avatarURL: "https://cdn/avatars/1/a_abc.png?size=64" } }).message;
await enable("ShowMeYourName");
assert.equal(generate({ id: "1", username: "vrai", globalName: "Affiché" }).username, "Affiché (vrai)");
assert.equal(generate({ id: "1", username: "pareil" }).username, "pareil");
await enable("ShowMeYourName", false);
await enable("AlwaysAnimate");
const animated = generate({ id: "1", username: "a" });
assert.equal(animated.avatarURL, "https://cdn/avatars/1/a_abc.gif?size=64");
assert.equal(animated.animateEmoji, true);
await enable("AlwaysAnimate", false);

// --- MessageLogger: edit history ---
await enable("MessageLogger");
dispatcher.dispatch({ type: "MESSAGE_UPDATE", message: { id: "m1", channel_id: "c1", content: "salut v2" } });
assert.equal(dispatched.pop().message.content, "salut v2\n-# ✏️ salut");
dispatcher.dispatch({ type: "MESSAGE_UPDATE", message: { id: "m1", channel_id: "c1", content: "salut v3" } });
assert.equal(dispatched.pop().message.content, "salut v3\n-# ✏️ salut\n-# ✏️ salut v2");
dispatcher.dispatch({ type: "MESSAGE_UPDATE", message: { id: "m1", channel_id: "c1", content: "salut v3" } });
assert.equal(dispatched.pop().message.content, "salut v3\n-# ✏️ salut\n-# ✏️ salut v2", "repeated update keeps history");
dispatcher.dispatch({ type: "MESSAGE_UPDATE", message: { id: "m1", channel_id: "c1", embeds: [] } });
assert.equal(dispatched.pop().message.content, undefined, "embed-only update untouched");
await enable("MessageLogger", false);

// --- ClearURLs: TikTok ---
__r(2).sendMessage("c1", { content: "https://www.tiktok.com/@a/video/1?is_from_webapp=1&sender_device=pc" });
assert.deepEqual(calls.pop(), ["sendMessage", "c1", "https://www.tiktok.com/@a/video/1"]);

// --- Experiments & MoreCommands ---
await run(vercorsCmd, { action: "enable", plugin: "Experiments" });
calls.pop();
assert.equal(__r(18).default.isDeveloper, true);
assert.equal(__r(18).default.getName(), "DeveloperExperimentStore", "other store members still work");
await run(vercorsCmd, { action: "disable", plugin: "Experiments" });
calls.pop();
assert.equal(__r(18).default.isDeveloper, false);

await run(vercorsCmd, { action: "enable", plugin: "MoreCommands" });
calls.pop();
const builtIns = __r(3).getBuiltInCommands(1);
await run(builtIns.find(c => c.name === "reverse"), { texte: "salut" });
assert.deepEqual(calls.pop(), ["sendMessage", "c1", "tulas"]);
await run(builtIns.find(c => c.name === "mock"), { texte: "bonjour" });
assert.deepEqual(calls.pop(), ["sendMessage", "c1", "bOnJoUr"]);
await run(builtIns.find(c => c.name === "timestamp"), { minutes: 0 });
assert.match(calls.pop()[2], /^<t:\d+:F> \(<t:\d+:R>\)$/);

TestRenderer.act(() => renderer.unmount());
assert.ok(!logs.some(l => l.startsWith("[JZSCord] ") && /failed|threw/i.test(l) && !/eval/.test(l)), logs.filter(l => /failed|threw/i.test(l)).join("\n"));

if (process.env.SHOW_LOGS) console.log(logs.join("\n"));
console.log("smoke test passed");
