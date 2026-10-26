// Runs the built bundle against a fake Metro runtime and fake Discord modules,
// to check the injection order and the plugins without a phone.
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import vm from "node:vm";

const files = new Map();
const calls = [];

const logs = [];
globalThis.nativeLoggingHook = message => logs.push(message);
globalThis.nativeModuleProxy = {
    NativeFileModule: { writeFile: async (dir, path, data) => void files.set(`/docs/${path}`, data) },
};
// What the Xposed loader's preload.js provides.
globalThis.__VERCORS_LOADER__ = { version: "test", settings: { plugins: { ClearURLs: { enabled: true } } } };

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
__d((g, r, d, a, module) => {
    module.exports = {
        AppRegistry: {},
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

// Boot order similar to Discord: react-native first, then the app modules.
for (const id of [99, 0, 1, 2, 3, 4, 5]) __r(id);
await new Promise(resolve => setTimeout(resolve, 10));

const { vercors } = globalThis;
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
const vercorsCmd = commands.find(c => c.name === "vercors");
assert.ok(vercorsCmd, "/vercors registered");
assert.match(vercorsCmd.id, /^-\d+$/);
assert.equal(vercorsCmd.untranslatedName, "vercors");
assert.ok(!__r(3).getBuiltInCommands(2).some(c => c.name === "vercors"), "filtered by command type");
assert.ok(__r(3).getBuiltInCommands([1, 2]).some(c => c.name === "vercors"));
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

const manifest = JSON.parse(readFileSync(new URL("../dist/plugins.json", import.meta.url), "utf8"));
assert.deepEqual(manifest.plugins.map(p => p.name), ["NoTrack", "ClearURLs", "SilentTyping", "DevTools"]);

console.log("smoke test passed");
