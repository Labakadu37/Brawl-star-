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
        Image: function Image() {
            return null;
        },
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
    module.exports = {
        BUILT_IN_SECTIONS: { "-1": { id: "-1", type: 1, name: "Intégré" } },
        getBuiltInCommands: () => [{ name: "shrug", applicationId: "-1" }],
    };
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
    const subscribers = {};
    module.exports = {
        _interceptors: [],
        dispatch(action) {
            if (this._interceptors.some(intercept => intercept(action))) return;
            dispatched.push(JSON.parse(JSON.stringify(action)));
            (subscribers[action.type] ?? []).forEach(handler => handler(action));
        },
        subscribe: (type, handler) => (subscribers[type] ??= []).push(handler),
        unsubscribe: (type, handler) => {
            const list = subscribers[type] ?? [];
            if (list.includes(handler)) list.splice(list.indexOf(handler), 1);
        },
    };
}, 14);
__d((g, r, d, a, module) => {
    const messages = {
        m1: { id: "m1", content: "salut", author: { id: "other" } },
        m2: { id: "m2", content: "le mien", author: { id: "me" } },
        m3: { id: "m3", content: "bulk", author: { id: "other" } },
    };
    const channelMessages = [
        { id: "p1", content: "hello", author: { id: "pcAndPhone" } },
        { id: "o1", content: "other", author: { id: "other" } },
        { id: "p2", content: "again", author: { id: "pcAndPhone" } },
    ];
    module.exports = {
        getName: () => "MessageStore",
        getMessage: (channelId, id) => messages[id],
        getMessages: channelId => ({ toArray: () => (channelId === "visible" ? channelMessages : []) }),
    };
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
        // Users Discord mobile has no per-device status for.
        getStatus: id => ({ estMobile: "dnd", estPc: "online", estGone: "offline" })[id],
        isMobileOnline: id => id === "estMobile",
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
let selectedChannel = "visible";
__d((g, r, d, a, module) => {
    module.exports = { getName: () => "SelectedChannelStore", getChannelId: () => selectedChannel };
}, 28);
function RenderedBadge() {}
__d((g, r, d, a, module) => {
    module.exports = { jsx: (type, props) => ({ type, props }), jsxs: (type, props) => ({ type, props }) };
}, 29);
const storeWatches = [];
__d((g, r, d, a, module) => {
    module.exports = { useStateFromStores: (stores, getter) => (storeWatches.push(stores.length), getter()) };
}, 30);
__d((g, r, d, a, module) => {
    function useBadges() {
        return [{ id: "real", description: "Real badge", icon: "abc" }];
    }
    module.exports = { __esModule: true, default: useBadges };
}, 31);

for (const id of [99, 98, 0, 10, 1, 2, 3, 4, 5, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31]) __r(id);
await new Promise(resolve => setTimeout(resolve, 10));

const { jzcord: vercors } = globalThis;
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
const vercorsCmd = commands.find(c => c.name === "jzcord");
assert.ok(vercorsCmd, "/jzcord registered");
// Our commands go in a "JZCord" section headed by our logo, not Built-In.
const section = __r(3).BUILT_IN_SECTIONS.jzcord;
assert.equal(section.name, "JZCord");
assert.match(section.icon, /jzcord\.png$/);
assert.equal(section.type, 1, "section shape cloned from a real one");
assert.equal(vercorsCmd.applicationId, "jzcord");
assert.match(vercorsCmd.id, /^-\d+$/);
assert.equal(vercorsCmd.untranslatedName, "jzcord");
assert.ok(!__r(3).getBuiltInCommands(2).some(c => c.name === "jzcord"), "filtered by command type");
assert.ok(__r(3).getBuiltInCommands([1, 2]).some(c => c.name === "jzcord"));
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
assert.match(config.VERCORS.icon.uri, /\/discord-client\/assets\/logo\/jzcord\.png$/, "our logo on the settings row");
const { list } = __r(12).createList({ sections: [{ label: "Compte", settings: ["ACCOUNT"] }, { label: "App", settings: ["X"] }] });
assert.deepEqual(list.map(s => s.label), ["Compte", "JZCord", "App"]);
config.VERCORS.onPress();
assert.equal(navigations.pop()[0], "VERCORS_PAGE");

// The page renders (with React Native fallbacks) and toggles plugins live.
const Page = config.VERCORS_PAGE.screen.getComponent();
let renderer;
TestRenderer.act(() => void (renderer = TestRenderer.create(React.createElement(Page))));
const header = renderer.root.findAll(n => n.type?.name === "Image");
assert.equal(header.length, 1, "logo at the top of the page");
assert.match(header[0].props.source.uri, /jzcord\.png$/);
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
const fileOf = url => files.get(url.replace("file:///docs/", "/docs/"));
assert.equal(rowFor("pcAndPhone").clanBadgeUrl, undefined, "off by default");
await run(vercorsCmd, { action: "enable", plugin: "PlatformIndicators" });
calls.pop();

// Chat: one badge per device, each in its own color slot.
const pcAndPhone = rowFor("pcAndPhone");
assert.equal(pcAndPhone.username, "Pseudo", "no text added to the name");
assert.ok(pcAndPhone.clanBadgeUrl.startsWith("file:///docs/vercors/platform-one-desktop-online"), pcAndPhone.clanBadgeUrl);
assert.ok(pcAndPhone.lobbyTagIconUrl.startsWith("file:///docs/vercors/platform-one-mobile-idle"), pcAndPhone.lobbyTagIconUrl);
assert.equal(pcAndPhone.tagText, undefined, "the white APP slot stays free");
await new Promise(r => setTimeout(r, 5));
const png = fileOf(pcAndPhone.clanBadgeUrl);
assert.ok(png, "icon file written");
assert.deepEqual([...png.subarray(0, 8)], [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a], "a PNG");
// The pixel data must inflate (valid zlib) to 64 rows of 1 filter byte + 64 pixels.
const { inflateSync } = await import("node:zlib");
const idat = png.indexOf(Buffer.from("IDAT"));
const idatLength = png.readUInt32BE(idat - 4);
assert.equal(inflateSync(png.subarray(idat + 4, idat + 4 + idatLength)).length, 64 * 65);
assert.equal(rowFor("pcAndPhone").clanBadgeUrl, pcAndPhone.clanBadgeUrl, "icons are cached");
assert.ok(rowFor("web").clanBadgeUrl.includes("one-web-dnd"));
assert.equal(rowFor("gone").clanBadgeUrl, undefined, "offline platforms are hidden");
assert.equal(rowFor("unknown").clanBadgeUrl, undefined);
// Our own platforms come from our sessions (desktop + mobile, best status each).
const me = rowFor("me");
assert.ok(me.clanBadgeUrl.includes("one-desktop-online") && me.lobbyTagIconUrl.includes("one-mobile-online"));
// Without per-device statuses, others are estimated from status + isMobileOnline.
assert.ok(rowFor("estMobile").clanBadgeUrl.includes("platform-one-mobile-dnd"));
assert.ok(rowFor("estPc").clanBadgeUrl.includes("platform-one-desktop-online"));
assert.equal(rowFor("estGone").clanBadgeUrl, undefined);
assert.ok(logs.some(l => l.includes("no per-device status for others")));
const bot = new (__r(17).default)().generate({ message: { id: "b", author: { id: "pcAndPhone", username: "Bot", bot: true } } });
assert.equal(bot.message.clanBadgeUrl, undefined, "no badges on bots");
// Someone showing a server tag keeps it. One color slot left: devices never
// share a badge, the most active one gets it (here desktop, online).
const withClan = rowFor("pcAndPhone", { clanTag: "JZBS", clanBadgeUrl: "https://cdn/clan.png" });
assert.equal(withClan.clanTag, "JZBS");
assert.equal(withClan.clanBadgeUrl, "https://cdn/clan.png");
assert.equal(withClan.lobbyTagIconUrl, pcAndPhone.clanBadgeUrl);
assert.equal(withClan.tagText, undefined);
// No color slot left: the APP tag slot, unless a real app tag is there.
assert.ok(rowFor("pcAndPhone", { clanTag: "JZBS", lobbyTagIconUrl: "x" }).tagIconUrl.includes("platform-one-desktop-online"));
assert.equal(rowFor("pcAndPhone", { clanTag: "JZBS", lobbyTagIconUrl: "x", tagText: "APP" }).tagText, "APP", "real app tags are kept");
// Once a real role icon is seen, ours copies its shape.
rowFor("pcAndPhone", { clanTag: "JZBS", roleIcon: { source: { uri: "https://cdn/role.png" }, name: "Mask", size: 20 } });
// The role icon slot comes first: it shows the image big, so ours is framed.
const withRole = rowFor("pcAndPhone", { clanTag: "JZBS" });
assert.equal(withRole.roleIcon.name, "Mask");
assert.equal(withRole.roleIcon.size, 20);
assert.ok(withRole.roleIcon.source.uri.startsWith("file:///docs/vercors/platform-framed-desktop-online"), withRole.roleIcon.source.uri);
assert.equal(withRole.lobbyTagIconUrl, pcAndPhone.lobbyTagIconUrl);
await new Promise(r => setTimeout(r, 5));
const framedPng = fileOf(withRole.roleIcon.source.uri);
assert.equal(framedPng.readUInt8(25), 6, "framed icon is a truecolor+alpha PNG");
assert.equal(inflateSync(framedPng.subarray(framedPng.indexOf(Buffer.from("IDAT")) + 4, framedPng.length - 12)).length, 64 * (64 * 4 + 1));
// "One device" option: only the most active one, in the big slot.
const piPlugin = vercors.plugins.findPlugin("PlatformIndicators");
vercors.plugins.setPluginOption(piPlugin, "devices", "one");
const onlyOne = rowFor("pcAndPhone");
assert.ok(onlyOne.roleIcon.source.uri.includes("framed-desktop-online"));
assert.equal(onlyOne.clanTag, undefined);
assert.equal(onlyOne.lobbyTagIconUrl, undefined);
vercors.plugins.setPluginOption(piPlugin, "devices", "all");

// A slot chosen in the settings is used first; applies right away.
assert.equal(vercors.plugins.getPluginOption(piPlugin, "slot"), "auto");
vercors.plugins.setPluginOption(piPlugin, "slot", "tag");
const inTag = rowFor("pcAndPhone");
assert.ok(inTag.tagIconUrl.includes("platform-one-desktop-online"));
assert.ok(inTag.roleIcon.source.uri.includes("platform-framed-mobile-idle"), "the next device in the next slot");
vercors.plugins.setPluginOption(piPlugin, "slot", "bogus");
assert.equal(vercors.plugins.getPluginOption(piPlugin, "slot"), "auto", "unknown values fall back to the default");
vercors.plugins.setPluginOption(piPlugin, "slot", "auto");
// In the settings page, the option shows under the plugin and cycles on tap.
let page;
TestRenderer.act(() => void (page = TestRenderer.create(React.createElement(Page))));
const optionRow = () =>
    page.root.findAll(n => n.type === "Pressable").find(p => /Emplacement/.test(p.findByType("Text").props.children));
assert.equal(optionRow().findByType("Text").props.children, "   ↳ Emplacement dans le chat : Auto (un badge par appareil)");
TestRenderer.act(() => optionRow().props.onPress());
assert.equal(optionRow().findByType("Text").props.children, "   ↳ Emplacement dans le chat : Tag de serveur");
assert.equal(vercors.plugins.getPluginOption(piPlugin, "slot"), "clan");
TestRenderer.act(() => page.unmount());
vercors.plugins.setPluginOption(piPlugin, "slot", "auto");

// Live: a presence change rebuilds that user's messages in the open channel.
dispatched.length = 0;
__r(14).dispatch({ type: "PRESENCE_UPDATES", updates: [{ user: { id: "pcAndPhone" }, status: "dnd" }] });
await new Promise(r => setTimeout(r, 600));
const refreshes = dispatched.filter(a => a.type === "MESSAGE_UPDATE");
assert.deepEqual(refreshes.map(a => a.message.id), ["p1", "p2"]);
assert.ok(refreshes.every(a => a.__vercors && a.message.guild_id === "g1"));
assert.equal(refreshes[0].message.content, "hello", "content unchanged");

// Profile: our badges come first in Discord's badge list...
const useBadges = __r(31).default;
storeWatches.length = 0;
const badges = useBadges({ userId: "pcAndPhone" });
assert.deepEqual(badges.map(b => b.id), ["jzs-platform-pcAndPhone-desktop", "jzs-platform-pcAndPhone-mobile", "real"]);
assert.deepEqual(badges.slice(0, 2).map(b => b.description), ["PC", "Téléphone"]);
assert.deepEqual(storeWatches, [2], "the profile re-renders on presence changes");
assert.deepEqual(useBadges({ userId: "unknown" }).map(b => b.id), ["real"]);
// ...and are drawn with our icons.
const shown = __r(29).jsx(RenderedBadge, { id: "jzs-platform-pcAndPhone-mobile", label: "?" });
// Profiles load the icons hosted in the repo over https.
assert.match(shown.props.source.uri, /^https:\/\/raw\.githubusercontent\.com\/.+\/discord-client\/assets\/badges\/mobile-idle\.png$/);
// Every hosted icon the plugin can ask for exists in the repo.
const { existsSync } = await import("node:fs");
for (const platform of ["desktop", "mobile", "web", "embedded"])
    for (const status of ["online", "idle", "dnd"])
        assert.ok(existsSync(new URL(`../../assets/badges/${platform}-${status}.png`, import.meta.url)), `${platform}-${status}.png`);
assert.equal(shown.props.label, "Téléphone");
assert.equal(__r(29).jsxs(RenderedBadge, { id: "real" }).props.source, undefined, "Discord's badges untouched");
// Whatever component draws it, the image Discord builds from our badge's
// `icon` gets our URL (jsx runtime and classic createElement).
assert.equal(badges[1].icon, "jzs_platform_mobile_idle");
function BadgeImage() {}
const cdnImage = __r(29).jsx(BadgeImage, { source: { uri: `https://cdn.discordapp.com/badge-icons/${badges[1].icon}.png?size=96` } });
assert.match(cdnImage.props.source.uri, /\/assets\/badges\/mobile-idle\.png$/);
const classic = React.createElement("Image", { source: [{ uri: `https://cdn.discordapp.com/badge-icons/${badges[0].icon}.png` }] });
assert.match(classic.props.source.uri, /\/assets\/badges\/desktop-online\.png$/);
const otherImage = __r(29).jsx(BadgeImage, { source: { uri: "https://cdn.discordapp.com/badge-icons/abc.png" } });
assert.equal(otherImage.props.source.uri, "https://cdn.discordapp.com/badge-icons/abc.png", "other images untouched");

await run(vercorsCmd, { action: "disable", plugin: "PlatformIndicators" });
calls.pop();
assert.equal(rowFor("pcAndPhone").clanBadgeUrl, undefined);
assert.deepEqual(useBadges({ userId: "pcAndPhone" }).map(b => b.id), ["real"]);
dispatched.length = 0;
__r(14).dispatch({ type: "PRESENCE_UPDATES", updates: [{ user: { id: "pcAndPhone" } }] });
await new Promise(r => setTimeout(r, 600));
assert.equal(dispatched.filter(a => a.type === "MESSAGE_UPDATE").length, 0, "no refresh once disabled");

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
assert.ok(!logs.some(l => l.startsWith("[JZCord] ") && /failed|threw/i.test(l) && !/eval/.test(l)), logs.filter(l => /failed|threw/i.test(l)).join("\n"));

if (process.env.SHOW_LOGS) console.log(logs.join("\n"));
console.log("smoke test passed");
