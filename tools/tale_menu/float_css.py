from urllib.parse import quote
def uri(svg): return 'url("data:image/svg+xml,'+quote(svg,safe=" =:/'.,-()")+'")'
BADGE=("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><defs>"
 "<linearGradient id='r' x1='0' y1='0' x2='1' y2='1'><stop offset='0' stop-color='#e27cff'/><stop offset='.5' stop-color='#b44cf0'/><stop offset='1' stop-color='#8a36e0'/></linearGradient>"
 "<radialGradient id='d' cx='.45' cy='.35' r='.7'><stop offset='0' stop-color='#8a5cff'/><stop offset='1' stop-color='#5b2fd0'/></radialGradient></defs>"
 "<circle cx='50' cy='50' r='48' fill='url(#r)'/><circle cx='50' cy='50' r='43.5' fill='#120a1e'/><circle cx='50' cy='50' r='37.5' fill='url(#d)'/>"
 "<path d='M27 8l2 4 4 2-4 2-2 4-2-4-4-2 4-2z' fill='#ffe26a'/><path d='M14 22l1.5 3 3 1.5-3 1.5-1.5 3-1.5-3-3-1.5 3-1.5z' fill='#fff'/>"
 "<path d='M84 70l1.6 3.2 3.2 1.6-3.2 1.6-1.6 3.2-1.6-3.2-3.2-1.6 3.2-1.6z' fill='#ffe26a'/><path d='M74 84l1.4 2.8 2.8 1.4-2.8 1.4-1.4 2.8-1.4-2.8-2.8-1.4 2.8-1.4z' fill='#7fe8ff'/></svg>")
PLUS="<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'><circle cx='20' cy='20' r='19' fill='#f42a4b'/><path d='M20 11v18M11 20h18' stroke='#fff' stroke-width='5' stroke-linecap='round'/></svg>"
CSS=(
 ":host .launcher,:host([chunky-buttons]) .launcher{width:max(58px,19vh)!important;min-width:0!important;height:max(58px,19vh)!important}"
 ":host .launcher .float-btn{width:100%!important;height:100%!important;padding:0!important;border:0!important;background:none!important;box-shadow:none!important;position:relative!important;border-radius:50%!important}"
 ":host .launcher .float-shell{display:grid!important;place-items:center!important;width:100%!important;height:100%!important;padding:0!important;border:0!important;border-radius:50%!important;background:"+uri(BADGE)+" center/100% no-repeat!important;box-shadow:0 3px 6px #0006!important}"
 ":host .launcher .float-icon,:host .launcher .float-label{display:none!important}"
 ":host .launcher .float-shell:after{content:'JZS';font:400 max(18px,7.4vh)/1 var(--font-brand);letter-spacing:-.02em;transform:skew(-9deg) translateY(-3%);color:transparent;background:linear-gradient(90deg,#fff 0 63%,#2fa4ff 63% 100%);-webkit-background-clip:text;background-clip:text;filter:drop-shadow(1.4px 0 0 #000) drop-shadow(-1.4px 0 0 #000) drop-shadow(0 -1.4px 0 #000) drop-shadow(0 3px 0 #000)}"
 ":host .launcher .float-btn:after{content:'';position:absolute;left:31%;bottom:-11%;width:44%;height:44%;background:"+uri(PLUS)+" center/100% no-repeat;filter:drop-shadow(0 2px 2px #0005);pointer-events:none}"
 ":host .quick-wrap .float-shell,:host .xray-wrap .float-shell,:host .spectators-wrap .float-shell{border-radius:50%!important;background:rgb(255 255 255 / .2)!important;border:2px solid rgb(255 255 255 / .28)!important;box-shadow:0 2px 8px #0003!important;-webkit-backdrop-filter:blur(4px);backdrop-filter:blur(4px);color:#fff!important}"
 ":host .xray-wrap .float-label,:host .spectators-wrap .float-label{display:none!important}"
 ":host .xray-wrap,:host .spectators-wrap{width:max(44px,11vh)!important;min-width:0!important;height:max(44px,11vh)!important}"
 ":host .xray-wrap .float-btn,:host .spectators-wrap .float-btn{width:100%!important;height:100%!important;padding:0!important;background:none!important;border:0!important;box-shadow:none!important}"
 ":host .xray-wrap .float-shell,:host .spectators-wrap .float-shell{width:100%!important;height:100%!important;display:grid!important;place-items:center!important;padding:0!important}"
 ":host .xray-wrap .float-icon,:host .spectators-wrap .float-icon{width:55%!important;height:55%!important;color:#fff!important;stroke-width:2.6}"
 ":host .jz-press{animation:jzp .22s cubic-bezier(.3,1.6,.6,1)!important}"
 "@keyframes jzp{0%{scale:1}35%{scale:.88}100%{scale:1}}"
)
