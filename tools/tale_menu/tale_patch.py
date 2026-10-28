import struct, sys, re

SRC, DST = sys.argv[1], sys.argv[2]
d = bytearray(open(SRC, 'rb').read())

def block(marker):
    i = d.find(marker)
    assert i > 0 and d.count(marker) == 1, marker
    n = struct.unpack('<I', d[i - 4:i])[0]
    return i, n

TALE_CSS = r"""
:host .scrim{background:rgb(0 0 0 / .25)!important}
:host .window,:host([data-theme]) .window{background:linear-gradient(180deg,rgb(78 84 108 / .9),rgb(62 67 90 / .9))!important;border:2px solid rgb(20 22 32 / .85)!important;border-radius:22px!important;box-shadow:0 8px 24px rgb(0 0 0 / .45),inset 0 1px rgb(255 255 255 / .18)!important;overflow:visible!important}
:host .window-body,:host([right-handed]) .window-body{display:flex!important;flex-direction:column!important;grid-template-columns:none!important;min-height:0}
:host .sidebar,:host([right-handed]) .sidebar{order:0!important;flex-direction:row!important;align-items:center!important;width:auto!important;min-width:0!important;max-width:none!important;background:none!important;border:0!important;padding:14px 70px 8px 18px!important;gap:10px!important;overflow-x:auto!important;overflow-y:visible!important}
:host .sidebar>*{display:flex!important;flex-direction:row!important;flex-wrap:nowrap!important;gap:10px!important;background:none!important;border:0!important;padding:0 0 5px!important;margin:0!important;overflow:visible!important}
:host .content,:host([right-handed]) .content{order:1!important;background:none!important;border:0!important}
:host .sidebar .nav-item{--t:#86dd2c;--b:#55a512;--e:#3b7a08;flex:0 0 auto!important;display:flex!important;flex-direction:row!important;align-items:center!important;gap:7px!important;height:40px!important;min-height:40px!important;min-width:132px!important;width:auto!important;padding:0 18px!important;margin:0!important;justify-content:center!important;border:2px solid #10121a!important;border-radius:7px!important;transform:none!important;color:#fff!important;font:400 15px/1 var(--font-brand)!important;-webkit-text-stroke:1.1px #000!important;text-shadow:0 2px 0 #000!important;background:linear-gradient(180deg,var(--t) 0 50%,var(--b) 50% 100%)!important;box-shadow:0 4px 0 #10121a,inset 0 2px rgb(255 255 255 / .45),inset 0 -3px var(--e)!important;opacity:1!important;outline:0!important}
:host .sidebar .nav-item:nth-child(6n+2){--t:#c3f1fb;--b:#93dcef;--e:#62b5cc}
:host .sidebar .nav-item:nth-child(6n+3){--t:#f2404f;--b:#cf1d2e;--e:#9a0f1c}
:host .sidebar .nav-item:nth-child(6n+4){--t:#35b2f2;--b:#168bd6;--e:#0d64a3}
:host .sidebar .nav-item:nth-child(6n+5){--t:#be6cf2;--b:#9a42dc;--e:#6f22aa}
:host .sidebar .nav-item:nth-child(6n){--t:#ffaa45;--b:#f08a1e;--e:#b85f07}
:host .sidebar .nav-item:not(.is-active){filter:saturate(.9)}
:host .sidebar .nav-item.is-active{box-shadow:0 4px 0 #10121a,0 0 0 2px #fff,inset 0 2px rgb(255 255 255 / .5),inset 0 -3px var(--e)!important}
:host .sidebar .nav-name{display:inline!important;color:#fff!important;font:inherit!important;white-space:nowrap}
:host .sidebar .nav-page-icon{width:20px!important;height:20px!important;color:#fff!important;filter:drop-shadow(0 1px 0 #000)}
:host .sidebar .nav-count{display:none!important}
:host .sidebar .nav-search{flex:0 0 auto!important;width:46px!important;height:46px!important;min-height:46px!important;margin:0 0 0 6px!important;border-radius:50%!important;border:2px solid #10121a!important;background:radial-gradient(circle at 50% 35%,#2e3a5a,#151b2c)!important;box-shadow:0 3px 0 #10121a,inset 0 0 0 2px #3c4a72!important;color:#5ec8ff!important}
:host .nav-group-head,:host .nav-group-label{display:none!important}
:host .content-head:has(.content-tab),:host .content-tab{display:none!important}
:host .panels,:host .panel-col,:host .panels-stacked{gap:10px!important}
:host .panel:has(.panel-tiles){background:none!important;border:0!important;box-shadow:none!important}
:host .panel:has(.panel-tiles)>.panel-head{display:none!important}
:host .panel-tiles{grid-template-columns:repeat(5,minmax(0,1fr))!important;gap:12px!important;padding:2px 6px!important}
:host .tile,:host .tile.is-on{min-height:76px!important;border:2px solid #121420!important;border-radius:6px!important;background:linear-gradient(180deg,#454b64 0,#3a3f56 45%,#33384d 100%)!important;box-shadow:0 4px 0 #121420,inset 0 2px rgb(255 255 255 / .14),inset 0 -2px rgb(0 0 0 / .25)!important;transform:none!important}
:host .tile:before{display:none!important}
:host .tile.is-on{border-color:#121420!important;box-shadow:0 4px 0 #121420,inset 0 0 0 2px #6fe24a,inset 0 2px rgb(255 255 255 / .14)!important}
:host .tile-toggle{gap:4px!important;padding:8px 10px!important;justify-content:flex-start!important}
:host .tile-name{font:400 19px/1.05 var(--font-brand)!important;color:#ffd43b!important;-webkit-text-stroke:1.1px #000!important;text-shadow:0 2px 0 #000!important;white-space:nowrap;overflow:hidden;text-overflow:clip;max-width:100%}
:host .tile-desc{color:#f2f4fa;font:600 8.5px/1.25 var(--font);overflow:hidden;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;max-width:100%}
:host .tile-pill,:host .tile-glyph{display:none!important}
:host .tile-side{flex:0 0 26px!important;border-left:1px solid rgb(0 0 0 / .35)!important}
:host .window>.topbar{order:9!important;position:static!important;justify-content:space-between!important;margin:8px 60px 12px!important;padding:5px!important;border:2px solid #0e1018!important;border-radius:30px!important;background:#1c2030!important;box-shadow:0 3px 0 #0e1018!important;gap:4px!important}
:host .topbar .brand,:host .topbar .action-icon,:host .topbar .config-chevron-box,:host .topbar .config-icon{display:none!important}
:host .topbar .config,:host .topbar .action{flex:1 1 0!important;width:auto!important;min-width:0!important;height:40px!important;padding:0 14px!important;display:flex!important;align-items:center!important;justify-content:center!important;transform:none!important;background:none!important;border:0!important;border-radius:24px!important;box-shadow:none!important;color:#f3f5fb!important;font:400 15px/1 var(--font-brand)!important;text-transform:uppercase!important;letter-spacing:.03em}
:host .topbar .config:first-of-type{color:#ffcb2e!important;background:#2c3247!important}
:host .topbar-actions{display:contents!important}
:host .topbar .action-label{display:inline!important;color:inherit!important;font:inherit!important}
:host .topbar .action-close{position:absolute!important;flex:none!important;top:10px!important;right:12px!important;width:44px!important;height:44px!important;padding:0!important;border-radius:50%!important;background:#1d2232!important;border:2px solid #0e1018!important}
:host .topbar .action-close .action-icon{display:block!important;width:22px!important;height:22px!important;color:#fff!important;filter:grayscale(1) brightness(3)}
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
