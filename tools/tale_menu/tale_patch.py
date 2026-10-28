import struct, sys, re

SRC, DST = sys.argv[1], sys.argv[2]
d = bytearray(open(SRC, 'rb').read())

def block(marker):
    i = d.find(marker)
    assert i > 0 and d.count(marker) == 1, marker
    n = struct.unpack('<I', d[i - 4:i])[0]
    return i, n

TALE_CSS = r"""
:host,:host([data-theme]){--scrim:rgb(0 0 0/.25)!important;--window-bg:#41465c!important;--window-border:#11131c!important;--window-shadow:0 8px 24px #0007!important;--edge:#11131c!important;--topbar-bg:#1c2030!important;--topbar-line:#11131c!important;--topbar-inset:#fff1!important;--sidebar-bg:transparent!important;--sidebar-line:#11131c!important;--content-bg:transparent!important;--panel-bg:#363b51!important;--panel-border:#11131c!important;--panel-shadow:0 3px 0 #11131c!important;--panel-divider:#0003!important;--text:#f2f4fa!important;--text-muted:#c9d0e3!important;--text-faint:#9aa3bb!important;--btn-top:#4a5069!important;--btn-bottom:#373c52!important;--btn-shadow:#11131c!important;--btn-border:#11131c!important;--btn-text:#fff!important;--btn-stroke:#000!important;--btn-active-top:#4a5069!important;--btn-active-bottom:#373c52!important;--btn-glow:#ffd43b80!important;--primary-top:#ffd43b!important;--primary-bottom:#f2a91c!important;--primary-shadow:#9a6407!important;--primary-text:#fff!important;--primary-border:#11131c!important;--row-hover:#fff1!important;--nav-border:#11131c!important;--dot-off:#2a2f42!important;--accent:#ffd43b!important;--accent-edge:#ffd43bcc!important;--track-off:#252a3b!important;--track-on:#6fe24a!important;--track-border:#11131c!important;--thumb-border:#11131c!important;--slider-thumb-top:#ffe27a!important;--slider-thumb-bottom:#ffd43b!important;--slider-thumb-edge:#11131c!important;--slider-glow:#ffd43b99!important;--value:#ffd43b!important;--search-bg:#0004!important;--search-border:#11131c!important;--float-top:#2c3044e6!important;--float-mid:#2c3044e6!important;--float-bottom:#2c3044e6!important;--float-border:#11131c!important;--float-panel-top:#3a3f55eb!important;--float-panel-bottom:#2c3044f0!important;--float-panel-border:#11131c!important;--float-chip:linear-gradient(180deg,#ffd43b,#f2a91c)!important}
:host .scrim{background:rgb(0 0 0 / .25)!important}
:host .window,:host([data-theme]) .window{background:rgb(24 28 40 / .16)!important;-webkit-backdrop-filter:blur(6px);backdrop-filter:blur(6px);border:2px solid rgb(0 0 0 / .22)!important;border-radius:24px!important;box-shadow:none!important;overflow:visible!important;width:86vw!important;height:94vh!important}
:host .window-body,:host([right-handed]) .window-body{display:flex!important;flex-direction:column!important;grid-template-columns:none!important;min-height:0}
:host .sidebar,:host([right-handed]) .sidebar{order:0!important;flex-direction:row!important;align-items:center!important;width:auto!important;min-width:0!important;max-width:none!important;background:none!important;border:0!important;padding:14px 70px 8px 18px!important;gap:10px!important;overflow-x:auto!important;overflow-y:visible!important}
:host .sidebar>*{display:flex!important;flex-direction:row!important;flex-wrap:nowrap!important;gap:10px!important;background:none!important;border:0!important;padding:0 0 5px!important;margin:0!important;overflow:visible!important}
:host .content,:host([right-handed]) .content{order:1!important;background:none!important;border:0!important}
:host .sidebar .nav-item{--t:#8cf545;--b:#5fe01c;--e:#2f8a08;flex:0 0 auto!important;display:flex!important;flex-direction:row!important;align-items:center!important;gap:7px!important;height:max(30px,6vh)!important;min-height:0!important;min-width:0!important;width:11.5vw!important;padding:0 1vw!important;margin:0 4px!important;justify-content:center!important;border:2px solid #15161f!important;border-radius:6px!important;transform:skew(-12deg)!important;color:#fff!important;font:400 max(13px,2.7vh)/1 var(--font-brand)!important;-webkit-text-stroke:1.2px #1a1a1a!important;text-shadow:0 2px 0 #1a1a1a!important;background:linear-gradient(180deg,rgb(255 255 255 / .55) 0,rgb(255 255 255 / .22) 42%,transparent 48%),linear-gradient(180deg,var(--t),var(--b) 55%)!important;box-shadow:0 3px 0 #15161f,inset 0 -3px var(--e),inset 0 2px rgb(255 255 255 / .7)!important;opacity:1!important;outline:0!important}
:host .sidebar .nav-item>*{transform:skew(12deg)}
:host .sidebar .nav-item:nth-child(6n+2){--t:#cdf4fc;--b:#a8e6f4;--e:#5aa9bf}
:host .sidebar .nav-item:nth-child(6n+3){--t:#f5566a;--b:#e3223b;--e:#8e0f1f}
:host .sidebar .nav-item:nth-child(6n+4){--t:#4fc6f7;--b:#1fa8e8;--e:#0b5f96}
:host .sidebar .nav-item:nth-child(6n+5){--t:#c46ae0;--b:#a43fc4;--e:#5f1d78}
:host .sidebar .nav-item:nth-child(6n){--t:#ffb070;--b:#f7904a;--e:#b45512}
:host .sidebar .nav-name{display:inline!important;color:#fff!important;font:inherit!important;white-space:nowrap}
:host .sidebar .nav-page-icon{width:20px!important;height:20px!important;color:#fff!important;filter:drop-shadow(0 1px 0 #000)}
:host .sidebar .nav-count{display:none!important}
:host .sidebar .nav-search{flex:0 0 auto!important;width:max(36px,8vh)!important;height:max(36px,8vh)!important;min-height:0!important;margin:0 0 0 8px!important;border-radius:50%!important;border:3px solid #36394c!important;background:radial-gradient(circle at 50% 40%,#3a4a70,#1d2438)!important;box-shadow:0 2px 0 #0c0d14!important;color:#85d4f2!important}
:host .nav-group-head,:host .nav-group-label{display:none!important}
:host .content-head:has(.content-tab),:host .content-tab{display:none!important}
:host .panels,:host .panel-col,:host .panels-stacked{gap:10px!important}
:host .panel:has(.panel-tiles){background:none!important;border:0!important;box-shadow:none!important}
:host .panel:has(.panel-tiles)>.panel-head{display:none!important}
:host .panel-tiles{grid-template-columns:repeat(5,minmax(0,1fr))!important;gap:max(8px,1.8vh) max(8px,1.4vw)!important;padding:2px 6px!important}
:host .tile,:host .tile.is-on{min-height:max(64px,14.5vh)!important;border:2px solid #15161f!important;border-radius:7px!important;background:linear-gradient(180deg,#42455f,#383b52 40%,#31344a)!important;box-shadow:0 3px 0 #0c0d14,inset 0 1px rgb(255 255 255 / .12)!important;transform:skew(-4deg)!important}
:host .tile:before{display:none!important}
:host .tile.is-on{box-shadow:0 3px 0 #0c0d14,inset 0 0 0 2px #5fe01c!important}
:host .tile-toggle{gap:3px!important;padding:8px 10px 8px 12px!important;justify-content:flex-start!important;transform:skew(4deg)}
:host .tile-name{font:400 max(14px,3.3vh)/1.05 var(--font-brand)!important;color:#ffd447!important;-webkit-text-stroke:1.2px #1a1200!important;text-shadow:0 2px 0 #1a1200!important;white-space:nowrap;overflow:hidden;max-width:100%}
:host .tile-desc{color:#eef0f6;font:700 max(8px,1.5vh)/1.25 var(--font);overflow:hidden;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;max-width:100%}
:host .tile-pill,:host .tile-glyph{display:none!important}
:host .tile-side{position:absolute!important;right:2px;bottom:2px;width:26px;height:26px;flex:none!important;border:0!important;opacity:.45}:host .tile-side-btn+.tile-side-btn{display:none}
:host .window>.topbar{order:9!important;position:static!important;justify-content:space-between!important;margin:6px 40px 10px!important;padding:4px!important;border:0!important;border-radius:28px!important;background:#0f1923!important;box-shadow:0 2px 6px #0006!important;gap:4px!important}
:host .topbar .brand,:host .topbar .action-icon,:host .topbar .config-chevron-box,:host .topbar .config-icon{display:none!important}
:host .topbar .config,:host .topbar .action{flex:1 1 0!important;width:auto!important;min-width:0!important;height:max(34px,8vh)!important;padding:0 14px!important;display:flex!important;align-items:center!important;justify-content:center!important;transform:none!important;background:none!important;border:0!important;border-radius:24px!important;box-shadow:none!important;color:#d8dbe2!important;font:400 max(13px,2.9vh)/1 var(--font-brand)!important;text-transform:uppercase!important}
:host .topbar .config:first-of-type{color:#f5cf3a!important;background:#1c2f3e!important}
:host .topbar-actions{display:contents!important}
:host .topbar .action-label{display:inline!important;color:inherit!important;font:inherit!important}
:host .topbar .action-close{position:absolute!important;flex:none!important;top:10px!important;right:12px!important;width:44px!important;height:44px!important;padding:0!important;border-radius:50%!important;background:rgb(20 24 36 / .8)!important;border:0!important}
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
        elif 'season' not in sel and not re.search(r'data-theme="(?!dark)', sel):
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
new = 'n("span",{class:"tile-name"},i),n("small",{class:"tile-desc"},(e?.description?l(e.description):this.explanation(t))||""),s)'
assert js.count(old) == 1
js = js.replace(old, new).replace('/* Local menu client */\n', '', 1)
for trim in (", including any changes you've made", " - reverted to the previous scale"):
    assert js.count(trim) == 1
    js = js.replace(trim, '', 1)
jb = js.encode()
jb += b' ' * (bn - len(jb))
assert len(jb) == bn
d[bi:bi + bn] = jb
open(DST, 'wb').write(d)
print('ok')
