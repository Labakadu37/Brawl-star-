import { build } from "esbuild";
import { readFileSync, writeFileSync } from "node:fs";
import vm from "node:vm";

const { version } = JSON.parse(readFileSync(new URL("./package.json", import.meta.url)));

const common = {
    bundle: true,
    target: "es2016",
    legalComments: "none",
    define: { __VERCORS_VERSION__: JSON.stringify(version) },
    logLevel: "info",
};

// Discord runs on Hermes, which lacks some modern syntax (async functions,
// newer class features). es2016 makes esbuild lower async/await to generators.
await build({
    ...common,
    entryPoints: ["src/index.ts"],
    outfile: "dist/vercors.js",
    format: "iife",
    minify: process.argv.includes("--minify"),
    banner: { js: `// Vercors Mobile v${version}` },
});

// plugins.json lists the plugins for the loader's native menu.
const { outputFiles } = await build({
    ...common,
    entryPoints: ["src/plugins/manifest.ts"],
    format: "iife",
    globalName: "manifest",
    write: false,
    logLevel: "silent",
});
const sandbox = {};
vm.runInNewContext(outputFiles[0].text + ";this.result = manifest.default;", sandbox);
writeFileSync("dist/plugins.json", JSON.stringify({ version, plugins: sandbox.result }, null, 2) + "\n");
