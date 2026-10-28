import struct, sys, re

SRC, DST = sys.argv[1], sys.argv[2]
d = bytearray(open(SRC, 'rb').read())

def block(marker):
    i = d.find(marker)
    assert i > 0 and d.count(marker) == 1, marker
    n = struct.unpack('<I', d[i - 4:i])[0]
    return i, n

TALE_CSS = r"""
:host .scrim{background:rgb(0 0 0 / .35)!important}
:host .window,:host([data-theme]) .window{background:linear-gradient(180deg,rgb(52 58 82 / .94),rgb(38 42 62 / .94))!important;border:3px solid #0b0d16!important;border-radius:26px!important;box-shadow:0 10px 30px rgb(0 0 0 / .6),inset 0 2px rgb(255 255 255 / .12)!important}
:host .window-body,:host([right-handed]) .window-body{display:flex!important;flex-direction:column!important;grid-template-columns:none!important}
:host .sidebar,:host([right-handed]) .sidebar{order:0!important;flex-direction:row!important;align-items:center!important;width:auto!important;min-width:0!important;max-width:none!important;background:none!important;border:0!important;padding:12px 64px 6px 16px!important;gap:8px!important;overflow-x:auto!important;overflow-y:hidden!important}
:host .sidebar>*{display:flex!important;flex-direction:row!important;flex-wrap:nowrap!important;gap:8px!important;background:none!important;border:0!important;padding:0!important;margin:0!important;overflow:visible!important}
:host .content,:host([right-handed]) .content{order:1!important;background:none!important;border:0!important}
:host .sidebar .nav-item{flex:0 0 auto!important;display:flex!important;flex-direction:row!important;align-items:center!important;gap:6px!important;min-height:38px!important;height:38px!important;min-width:0!important;width:auto!important;padding:0 16px!important;margin:0!important;border:2px solid #0b0d16!important;border-radius:9px!important;transform:skew(-6deg)!important;color:#fff!important;font:400 15px/1 var(--font-brand)!important;-webkit-text-stroke:1px #000!important;text-shadow:0 2px 0 #000!important;box-shadow:0 3px 0 #0b0d16,inset 0 2px rgb(255 255 255 / .35)!important;background:linear-gradient(180deg,#67e34a,#3cb72a)!important;opacity:.85}
:host .sidebar .nav-item>*{transform:skew(6deg)}
:host .sidebar .nav-item:nth-child(6n+2){background:linear-gradient(180deg,#9fe6ff,#5cc5f0)!important}
:host .sidebar .nav-item:nth-child(6n+3){background:linear-gradient(180deg,#ff5a5a,#d42a2a)!important}
:host .sidebar .nav-item:nth-child(6n+4){background:linear-gradient(180deg,#3fb4ff,#1a7fe0)!important}
:host .sidebar .nav-item:nth-child(6n+5){background:linear-gradient(180deg,#c86bff,#8f35e0)!important}
:host .sidebar .nav-item:nth-child(6n){background:linear-gradient(180deg,#ffad42,#f07f12)!important}
:host .sidebar .nav-item.is-active{opacity:1;outline:3px solid #fff!important;outline-offset:-1px}
:host .sidebar .nav-name{display:inline!important;color:#fff!important;font:inherit!important;white-space:nowrap}
:host .sidebar .nav-page-icon{width:18px!important;height:18px!important}
:host .sidebar .nav-count{position:static!important;margin-left:4px!important;background:#000a!important;color:#fff!important;border-radius:8px!important;padding:0 5px!important;font:800 11px/16px var(--font)!important;-webkit-text-stroke:0!important}
:host .sidebar .nav-search{flex:0 0 auto!important;width:40px!important;height:40px!important;min-height:40px!important;margin:0 0 0 4px!important;border-radius:50%!important;border:2px solid #0b0d16!important;background:radial-gradient(circle at 40% 35%,#6fd0ff,#1f7fe0)!important;color:#fff!important}
:host .nav-group-head,:host .nav-group-label{display:none!important}
:host .panels,:host .panel-col{gap:10px!important}
:host .panel:has(.panel-tiles){background:none!important;border:0!important;box-shadow:none!important}
:host .panel:has(.panel-tiles)>.panel-head{border:0!important;padding:6px 6px 0!important;color:#c9d2ea!important;font:400 14px var(--font-brand)!important;letter-spacing:.04em!important;-webkit-text-stroke:.6px #000}
:host .panel-tiles{grid-template-columns:repeat(auto-fill,minmax(150px,1fr))!important;gap:10px!important;padding:6px 4px!important}
:host .tile,:host .tile.is-on{min-height:74px!important;border:2px solid #0b0d16!important;border-radius:8px!important;background:linear-gradient(180deg,#3e4560,#2c3148)!important;box-shadow:0 4px 0 #0b0d16,inset 0 2px rgb(255 255 255 / .1)!important;transform:skew(-3deg)}
:host .tile:before{display:none!important}
:host .tile.is-on{border-color:#4be35a!important;box-shadow:0 4px 0 #0b0d16,0 0 10px #4be35a88,inset 0 0 0 1px #4be35a!important}
:host .tile-toggle{gap:3px!important;padding:8px 8px 8px 10px!important;justify-content:flex-start!important;transform:skew(3deg)}
:host .tile-name{font:400 16px/1.1 var(--font-brand)!important;color:#ffe066!important;-webkit-text-stroke:1px #000!important;text-shadow:0 2px 0 #000!important}
:host .tile.is-on .tile-name{color:#7dff6b!important}
:host .tile-desc{display:block;max-width:100%;color:#e8ecf7;font:600 9px/1.3 var(--font);opacity:.85;overflow:hidden;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical}
:host .tile-pill,:host .tile-glyph{display:none!important}
:host .tile-side{flex:0 0 30px!important;border-left:1px solid #0006!important;transform:skew(3deg)}
:host .window>.topbar{order:9!important;margin:6px 14px 12px!important;padding:6px 10px!important;border:2px solid #0b0d16!important;border-radius:30px!important;background:linear-gradient(180deg,#1c2031,#141826)!important;box-shadow:inset 0 2px rgb(255 255 255 / .06)!important;gap:8px!important}
:host .topbar .brand-logo{width:34px!important;height:34px!important}
:host .topbar .brand-text{display:none!important}
:host .topbar .config,:host .topbar .action{transform:none!important;background:none!important;border:0!important;box-shadow:none!important;color:#cfd6ea!important;font:400 14px var(--font-brand)!important;text-transform:uppercase;letter-spacing:.04em}
:host .topbar .config:first-of-type{color:#ffd23f!important;background:#ffffff14!important;border-radius:20px!important}
:host .topbar .action{width:auto!important;height:40px!important;padding:0 10px!important}
:host .topbar .action-label{display:inline!important;color:inherit!important}
:host .content-tab{display:none!important}
[class*=season]{display:none!important}
"""
TALE_CSS = re.sub(r'\n', '', TALE_CSS)

def drop_season(css):
    out, i, n = [], 0, len(css)
    while i < n:
        j = css.find('{', i)
        if j < 0:
            out.append(css[i:]); break
        sel = css[i:j]
        depth, k = 1, j + 1
        while depth:
            c = css[k]
            depth += (c == '{') - (c == '}')
            k += 1
        body = css[j + 1:k - 1]
        if sel.lstrip().startswith('@'):
            if '{' in body:
                inner = drop_season(body)
                if inner.strip():
                    out.append(sel + '{' + inner + '}')
            else:
                out.append(css[i:k])
        elif 'season' not in sel:
            out.append(css[i:k])
        i = k
    return ''.join(out)

# ---- shell (CSS)
si, sn = block(b'<!doctype html><html><head><title>JZS')
shell = bytes(d[si:si + sn]).decode('utf-8')
a = shell.index('<template id="bs-menu-styles">') + len('<template id="bs-menu-styles">')
b = shell.index('</template>')
css = drop_season(shell[a:b]) + TALE_CSS
pad = len(shell[a:b].encode()) - len(css.encode())
assert pad >= 0, pad
new_shell = (shell[:a] + css + ' ' * pad + shell[b:]).encode()
assert len(new_shell) == sn
d[si:si + sn] = new_shell
print('css free bytes left', pad)

# ---- bundle (JS): add description under tile name
bi, bn = block(b'/* Local menu client */')
js = bytes(d[bi:bi + bn]).decode('utf-8')
old = 'n("span",{class:"tile-name"},i),s)'
new = 'n("span",{class:"tile-name"},i),n("small",{class:"tile-desc"},this.explanation(t)||""),s)'
assert js.count(old) == 1
js = js.replace(old, new).replace('/* Local menu client */\n', '', 1)
trim = ", including any changes you've made"
assert js.count(trim) == 1
js = js.replace(trim, '', 1)
jb = js.encode()
jb += b' ' * (bn - len(jb))
assert len(jb) == bn
d[bi:bi + bn] = jb
open(DST, 'wb').write(d)
print('ok')
