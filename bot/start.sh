#!/bin/bash
cd "$(dirname "$0")"
[ -d node_modules ] || npm install --omit=dev
node index.js
