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
:host .scrim{background:transparent!important;padding:0!important}
:host .window,:host([data-theme]) .window{background:rgb(255 255 255 / .1)!important;-webkit-backdrop-filter:blur(6px);backdrop-filter:blur(6px);border:2px solid rgb(255 255 255 / .12)!important;border-radius:26px!important;box-shadow:0 0 0 1px rgb(0 0 0 / .18)!important;overflow:visible!important;width:87vw!important;height:97.5vh!important;max-width:none!important;max-height:none!important}:host([expanded]) .window{transform:translate(var(--window-drag-x,0px),var(--window-drag-y,0px))!important}:host .window:after{display:none!important}
:host .window-body,:host([right-handed]) .window-body{display:flex!important;flex-direction:column!important;grid-template-columns:none!important;min-height:0}
:host .sidebar,:host([right-handed]) .sidebar{height:auto!important;min-height:max-content!important;flex:0 0 auto!important;order:0!important;flex-direction:row!important;align-items:center!important;width:auto!important;min-width:0!important;max-width:none!important;background:none!important;border:0!important;padding:3.4vh 1vw 3.2vh 2.3vw!important;gap:1.1vw!important;overflow-x:auto!important;overflow-y:visible!important}
:host .sidebar>*{height:auto!important;min-height:max-content!important;align-items:center!important;flex:0 0 auto!important;display:flex!important;flex-direction:row!important;flex-wrap:nowrap!important;gap:1.1vw!important;background:none!important;border:0!important;padding:0 0 5px!important;margin:0!important;overflow:visible!important}
:host .content,:host([right-handed]) .content{order:1!important;background:none!important;border:0!important}
:host .sidebar .nav-item{--b:#5ffc0a;--t:#a4ef5a;--e:#227d04;flex:0 0 auto!important;display:flex!important;flex-direction:row!important;align-items:center!important;gap:7px!important;height:max(32px,7.8vh)!important;min-height:0!important;min-width:0!important;width:10.2vw!important;padding:0 .6vw!important;margin:0 .1vw!important;justify-content:center!important;border:2px solid #111216!important;border-radius:6px!important;transform:skew(-16deg)!important;color:#fff!important;font:400 max(12px,2.9vh)/1 var(--font-brand)!important;-webkit-text-stroke:1.2px #1a1a1a!important;text-shadow:0 2px 0 #1a1a1a!important;background:linear-gradient(110deg,rgb(255 255 255 / .28) 0,rgb(255 255 255 / .08) 32%,transparent 45%),var(--b)!important;box-shadow:0 2px 0 #111216,inset 0 3px var(--t),inset 0 -5px var(--e)!important;opacity:1!important;outline:0!important}
:host .sidebar .nav-item>*{transform:skew(16deg)}
:host .sidebar .nav-item:nth-child(6n+2){--b:#aae3f4;--t:#dcf8ff;--e:#5f9fb2}
:host .sidebar .nav-item:nth-child(6n+3){--b:#eb1a39;--t:#f6606b;--e:#8a0310}
:host .sidebar .nav-item:nth-child(6n+4){--b:#0aaee3;--t:#6ad6f7;--e:#075a7a}
:host .sidebar .nav-item:nth-child(6n+5){--b:#97439f;--t:#ce5aef;--e:#6a1870}
:host .sidebar .nav-item:nth-child(6n){--b:#f78d45;--t:#fdb27c;--e:#8f3c08}
:host .sidebar .nav-page-icon{display:none!important}:host .sidebar .nav-name:before{content:"";display:inline-block;vertical-align:middle;width:max(18px,4.4vh);height:max(18px,4.4vh);margin-right:.5vw;filter:drop-shadow(0 1px 0 #0006)}:host .sidebar .nav-item:nth-child(6n+1) .nav-name:before{background:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Crect x='1.5' y='9' width='29' height='14' rx='7' fill='%23fff' stroke='%23111' stroke-width='2'/%3E%3Ccircle cx='9' cy='16' r='3.3' fill='%232c8f0c'/%3E%3Ccircle cx='16' cy='16' r='3.3' fill='%232c8f0c'/%3E%3Ccircle cx='23' cy='16' r='3.3' fill='%232c8f0c'/%3E%3C/svg%3E") center/contain no-repeat!important}:host .sidebar .nav-item:nth-child(6n+2) .nav-name:before{background:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Cpath d='M5 9c0-3 2.5-5 5.5-5h9c4 0 7 3 7 7v6c0 3-2 5-5 5H12l-3 4H6l1-5c-1.3-.8-2-2.3-2-4z' fill='%23e8f4ff' stroke='%23111' stroke-width='2'/%3E%3Cpath d='M10 9h10' stroke='%237fa9c9' stroke-width='2'/%3E%3Ccircle cx='23' cy='22' r='6' fill='%23c8402a' stroke='%23111' stroke-width='2'/%3E%3C/svg%3E") center/contain no-repeat!important}:host .sidebar .nav-item:nth-child(6n+3) .nav-name:before{background:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Cpath d='M16 2l11 4v9c0 7-5 12-11 15C10 27 5 22 5 15V6z' fill='%236e0b17' stroke='%23111' stroke-width='2'/%3E%3Cpath d='M16 6l7 3v6c0 5-3 8-7 10-4-2-7-5-7-10V9z' fill='%23a3122a'/%3E%3C/svg%3E") center/contain no-repeat!important}:host .sidebar .nav-item:nth-child(6n+4) .nav-name:before{background:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Cpath d='M13.5 2h5l.8 3.6 2.6 1.1 3.1-2 3.5 3.5-2 3.1 1.1 2.6 3.6.8v5l-3.6.8-1.1 2.6 2 3.1-3.5 3.5-3.1-2-2.6 1.1-.8 3.6h-5l-.8-3.6-2.6-1.1-3.1 2-3.5-3.5 2-3.1-1.1-2.6L1 18.5v-5l3.6-.8 1.1-2.6-2-3.1 3.5-3.5 3.1 2 2.6-1.1z' fill='%23fff' stroke='%23111' stroke-width='1.8'/%3E%3Ccircle cx='16' cy='16' r='4.5' fill='%231a8bd9' stroke='%23111' stroke-width='1.8'/%3E%3C/svg%3E") center/contain no-repeat!important}:host .sidebar .nav-item:nth-child(6n+5) .nav-name:before{background:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Crect x='2' y='7' width='17' height='17' rx='5' transform='rotate%28-12 10 15%29' fill='%23ff5ab4' stroke='%23111' stroke-width='2'/%3E%3Crect x='13' y='8' width='17' height='17' rx='5' transform='rotate%2810 21 16%29' fill='%23ffd23a' stroke='%23111' stroke-width='2'/%3E%3Ccircle cx='18.5' cy='15' r='1.6' fill='%23111'/%3E%3Ccircle cx='24.5' cy='16' r='1.6' fill='%23111'/%3E%3Ccircle cx='7' cy='15' r='1.6' fill='%23111'/%3E%3Ccircle cx='12' cy='14' r='1.6' fill='%23111'/%3E%3C/svg%3E") center/contain no-repeat!important}:host .sidebar .nav-item:nth-child(6n) .nav-name:before{background:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Cpath d='M16 2l12 7v14l-12 7-12-7V9z' fill='%23ffb24a' stroke='%23111' stroke-width='2'/%3E%3Cpath d='M16 2l12 7-12 7-12-7z' fill='%23ffd07a'/%3E%3Cpath d='M16 16v14' stroke='%23b85d0a' stroke-width='2'/%3E%3C/svg%3E") center/contain no-repeat!important}:host .content-head:has(.config-profile){display:none!important}
:host .sidebar .nav-name{display:inline-flex!important;align-items:center;color:#fff!important;font:inherit!important;white-space:nowrap}
:host .sidebar .nav-count{display:none!important}
:host .sidebar .nav-search{flex:0 0 auto!important;width:max(38px,8.6vh)!important;height:max(38px,8.6vh)!important;min-height:0!important;margin:0 0 0 1.2vw!important;border-radius:50%!important;border:3px solid #4a5068!important;background:radial-gradient(circle at 50% 40%,#2c3346,#171b27)!important;box-shadow:0 2px 0 #0c0d14!important;position:relative!important}:host .sidebar .nav-search>*{display:none!important}:host .sidebar .nav-search:before{content:"";position:absolute;left:24%;top:16%;width:46%;height:46%;border-radius:50%;background:radial-gradient(circle at 35% 30%,#f2fdff 0,#9fe3ff 30%,#2f9fe0 75%);box-shadow:0 0 0 2px #10131c}:host .sidebar .nav-search:after{content:"";position:absolute;left:14%;top:58%;width:26%;height:12%;border-radius:3px;background:#10131c;transform:rotate(-45deg)}
:host .nav-group-head,:host .nav-group-label{display:none!important}
:host .content-head:has(.content-tab),:host .content-tab{display:none!important}
:host .panels,:host .panel-col{gap:10px!important}:host .panels-stacked:has(.panel-tiles){display:grid!important;grid-template-columns:repeat(5,minmax(0,1fr))!important;gap:max(10px,3vh) max(8px,1vw)!important;padding:0 1.5vw 0 2vw!important}:host .panels-stacked .panel:has(.panel-tiles),:host .panels-stacked .panel-tiles{display:contents!important}
:host .panel:has(.panel-tiles){background:none!important;border:0!important;box-shadow:none!important}
:host .panel:has(.panel-tiles)>.panel-head{display:none!important}
:host .panel-tiles{grid-template-columns:repeat(5,minmax(0,1fr))!important;gap:max(8px,1.8vh) max(8px,1.4vw)!important;padding:2px 6px!important}
:host .tile,:host .tile.is-on{min-height:max(64px,13.5vh)!important;border:2px solid #15161f!important;border-radius:6px!important;background:linear-gradient(180deg,#41455c,#3a3e55)!important;box-shadow:0 3px 0 #0c0d14,inset 0 1px rgb(255 255 255 / .1)!important;transform:skew(-7deg)!important}
:host .tile:before{display:none!important}
:host .tile.is-on{box-shadow:0 3px 0 #0c0d14,inset 0 0 0 2px #5fe01c!important}
:host .tile-toggle{gap:3px!important;padding:8px 10px 8px 12px!important;justify-content:flex-start!important;transform:skew(7deg)}
:host .tile-name{font:400 max(14px,3.6vh)/1.1 var(--font-brand)!important;color:transparent!important;background:linear-gradient(172deg,#fffbe0 0,#fff0a8 22%,#ffd968 48%,#f6b93a 72%,#e0901f 100%);-webkit-background-clip:text;background-clip:text;-webkit-text-stroke:0!important;text-shadow:none!important;filter:drop-shadow(1.2px 0 0 #000) drop-shadow(-1.2px 0 0 #000) drop-shadow(0 -1.2px 0 #000) drop-shadow(0 2.4px 0 #000);white-space:nowrap;overflow:hidden;max-width:100%;padding-bottom:2px}
:host .tile-desc{color:#a3a6b3;font:600 max(7px,1.3vh)/1.3 var(--font);text-shadow:0 1px 0 #0008;overflow:hidden;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;max-width:100%}
:host .tile-pill,:host .tile-glyph{display:none!important}
:host .tile-side{position:absolute!important;right:2px;bottom:2px;width:26px;height:26px;flex:none!important;border:0!important;opacity:.45}:host .tile-side-btn+.tile-side-btn{display:none}
:host .window>.topbar{order:9!important;position:static!important;justify-content:space-between!important;margin:1vh 4vw 1vh!important;padding:4px!important;border:0!important;border-radius:28px!important;background:#0f1923!important;box-shadow:0 2px 6px #0006!important;gap:4px!important}
:host .topbar .brand,:host .topbar .action-icon,:host .topbar .config-chevron-box,:host .topbar .config-icon{display:none!important}
:host .topbar .config,:host .topbar .action{flex:1 1 0!important;width:auto!important;min-width:0!important;height:max(36px,9.3vh)!important;padding:0 14px!important;display:flex!important;align-items:center!important;justify-content:center!important;transform:none!important;background:none!important;border:0!important;border-radius:24px!important;box-shadow:none!important;color:#c1c3cd!important;font:400 max(13px,3.1vh)/1 var(--font-brand)!important;text-transform:uppercase!important}
:host .topbar .config:first-of-type{background:#1c2f3e!important}:host .topbar .config:first-of-type .config-label{color:#f6d465!important;-webkit-text-stroke:0;filter:drop-shadow(1px 0 0 #000) drop-shadow(-1px 0 0 #000) drop-shadow(0 -1px 0 #000) drop-shadow(0 2px 0 #000)}
:host .topbar-actions{display:contents!important}
:host .topbar .action-label{display:inline!important;color:inherit!important;font:inherit!important}
:host .topbar .action-close{position:absolute!important;flex:none!important;top:2.8vh!important;right:2.6vw!important;width:max(36px,8vh)!important;height:max(36px,8vh)!important;padding:0!important;border-radius:50%!important;background:rgb(20 24 36 / .8)!important;border:0!important}
:host .topbar .action-close .action-icon{display:block!important;width:55%!important;height:55%!important;stroke-width:3.4;color:#fff!important;filter:grayscale(1) brightness(3)}
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
def rep1(a, b):
    global js
    assert js.count(a) == 1, a[:60]
    js = js.replace(a, b, 1)

def obj_end(src, start):
    depth, i, q = 0, start, None
    while True:
        c = src[i]
        if q:
            if c == '\\': i += 1
            elif c == q: q = None
        elif c in '"\'`': q = c
        elif c == '{': depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0: return i + 1
        i += 1

# free space: Arabic UI falls back to English
xs = js.index(',Xn={') + 4
js = js[:xs] + '{}' + js[obj_end(js, xs):]
rep1('ar:Xn,', 'ar:Vn,')

rep1('go=[{id:"battle",labelKey:"category.battle",iconName:Ue.battle},{id:"visuals",labelKey:"category.visuals",iconName:Ue.visuals},{id:"maintenance",labelKey:"category.miscellaneous.short",iconName:Ue.maintenance}]',
     'go=[{id:"allmods",labelKey:"All Mods",iconName:"other"},{id:"combat",labelKey:"Combat",iconName:"combat"},{id:"battle",labelKey:"Battle",iconName:"battle"},{id:"visuals",labelKey:"Visual",iconName:"visuals"},{id:"lobby",labelKey:"Lobby",iconName:"other"},{id:"maintenance",labelKey:"Misc",iconName:"utility"}]')
a0 = js.index('var lt={battle:')
a1 = js.index(',ke=new Set(K)', a0)
js = js[:a0] + ('var lt={allmods:"All Mods",combat:"Combat",battle:"Battle",visuals:"Visual",lobby:"Lobby",maintenance:"Misc"},'
    'qs=["combat","battle","visuals","lobby","maintenance"],'
    'he={allmods:[{id:"all",labelKey:"All Mods"}],combat:[{id:"aim",labelKey:"category.combat"}],'
    'battle:[{id:"utility",labelKey:"category.utility"},{id:"exploits",labelKey:"category.exploits"}],'
    'visuals:[{id:"players",labelKey:"subcategory.players"},{id:"enemyhud",labelKey:"subcategory.enemyhud"},{id:"objects",labelKey:"subcategory.objects"},{id:"camera",labelKey:"subcategory.camera"}],'
    'lobby:[{id:"automation",labelKey:"category.automation"},{id:"general",labelKey:"general.title"}],'
    'maintenance:[{id:"gui",labelKey:"category.gui"},{id:"fps",labelKey:"settings.section.fps"},{id:"privacy",labelKey:"settings.section.privacy"},{id:"limited",labelKey:"subcategory.limited"}]}') + js[a1:]
s0 = js.index('switch(`${o}.${t}`){case"battle.combat"')
s1 = js.index('default:return[]}', s0)
js = js[:s0] + ('switch(`${o}.${t}`){case"allmods.all":return Kr();case"combat.aim":return Vs;case"battle.utility":return Gs;case"battle.exploits":return Ys;'
    'case"visuals.players":return js;case"visuals.enemyhud":return Js;case"visuals.objects":return Xs;case"visuals.camera":return Qs;'
    'case"lobby.automation":return zs;case"lobby.general":return[...Ai,...Ws,...Ti];'
    'case"maintenance.gui":return Zs;case"maintenance.fps":return Ks;case"maintenance.privacy":return Us;case"maintenance.limited":return en;') + js[s1:]
rep1('`${t}.${e}`=="maintenance.general"', '`${t}.${e}`=="lobby.general"')
rep1('I.initialModuleId:"battle",', 'I.initialModuleId:"allmods",')
jb = js.encode()
print('js free bytes', bn - len(jb))
jb += b' ' * (bn - len(jb))
assert len(jb) == bn
d[bi:bi + bn] = jb
open(DST, 'wb').write(d)
print('ok')
