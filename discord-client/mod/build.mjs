import { build } from "esbuild";
import { readFileSync } from "node:fs";

const { version } = JSON.parse(readFileSync(new URL("./package.json", import.meta.url)));

// Discord runs on Hermes, which lacks some modern syntax (async functions,
// newer class features). es2016 makes esbuild lower async/await to generators.
await build({
    entryPoints: ["src/index.ts"],
    outfile: "dist/vercors.js",
    bundle: true,
    format: "iife",
    target: "es2016",
    minify: process.argv.includes("--minify"),
    legalComments: "none",
    define: { __VERCORS_VERSION__: JSON.stringify(version) },
    banner: { js: `// Vercors Mobile v${version}` },
    logLevel: "info",
});
