// Writes the platform badge icons (one per platform and status) to
// discord-client/assets/badges, from where profiles load them over https:
// Discord's profile badge images don't load local files.
import { build } from "esbuild";
import { mkdirSync, writeFileSync } from "node:fs";

const { outputFiles } = await build({
    entryPoints: [new URL("../src/lib/platformIcons.ts", import.meta.url).pathname],
    bundle: true,
    write: false,
    format: "esm",
    platform: "node",
});
const icons = await import("data:text/javascript;base64," + Buffer.from(outputFiles[0].text).toString("base64"));

const out = new URL("../../assets/badges/", import.meta.url);
mkdirSync(out, { recursive: true });
for (const platform of ["desktop", "mobile", "web", "embedded"]) {
    for (const status of ["online", "idle", "dnd"]) {
        writeFileSync(new URL(`${platform}-${status}.png`, out), Buffer.from(icons.renderPlatformIconBase64(platform, status), "base64"));
    }
}
console.log("badges written to", out.pathname);
