#!/usr/bin/env python3
"""JZS Brawl - smali injector.

Inserts:
    invoke-static {p0}, Lcom/jzs/brawl/JZSInit;->install(Landroid/app/Activity;)V

immediately before the final `return-void` of
    com.supercell.titan.GameApp#onCreate(Landroid/os/Bundle;)V

Idempotent: re-runs on an already-patched file are a no-op.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

METHOD_HEADER = re.compile(
    r"^\.method\s+public\s+onCreate\(Landroid/os/Bundle;\)V\s*$"
)
METHOD_END = re.compile(r"^\.end\s+method\s*$")
INJECTION = (
    "    invoke-static {p0}, "
    "Lcom/jzs/brawl/JZSInit;->install(Landroid/app/Activity;)V\n"
)


def patch(path: Path) -> bool:
    src = path.read_text(encoding="utf-8").splitlines(keepends=True)
    out: list[str] = []
    in_method = False
    method_lines: list[str] = []
    patched = False

    for line in src:
        if not in_method and METHOD_HEADER.match(line):
            in_method = True
            method_lines = [line]
            continue
        if in_method:
            method_lines.append(line)
            if METHOD_END.match(line):
                body = "".join(method_lines)
                if "Lcom/jzs/brawl/JZSInit;->install" in body:
                    out.extend(method_lines)
                else:
                    last_ret_idx = None
                    for i in range(len(method_lines) - 1, -1, -1):
                        stripped = method_lines[i].strip()
                        if stripped == "return-void":
                            last_ret_idx = i
                            break
                    if last_ret_idx is None:
                        raise SystemExit(
                            "Could not find return-void inside onCreate"
                        )
                    method_lines.insert(last_ret_idx, INJECTION)
                    patched = True
                    out.extend(method_lines)
                in_method = False
                method_lines = []
            continue
        out.append(line)

    path.write_text("".join(out), encoding="utf-8")
    return patched


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: inject_overlay.py path/to/GameApp.smali", file=sys.stderr)
        return 2
    target = Path(sys.argv[1])
    if not target.is_file():
        print(f"not a file: {target}", file=sys.stderr)
        return 2
    changed = patch(target)
    print("patched" if changed else "already patched (no-op)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
