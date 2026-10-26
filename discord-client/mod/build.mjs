import { build } from "esbuild";
import { readFileSync } from "node:fs";

const { version } = JSON.parse(readFileSync(new URL("./package.json", import.meta.url)));

const common = {
    bundle: true,
    target: "es2016",
    legalComments: "none",
    define: { __VERCORS_VERSION__: JSON.stringify(version) },
    jsx: "transform",
    jsxFactory: "h",
    jsxFragment: "Fragment",
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
    banner: { js: `// JZSCord v${version}` },
});
