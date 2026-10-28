import re
from urllib.parse import quote
def uri(svg): return 'url("data:image/svg+xml,'+quote(svg,safe=" =:/'.,-").replace('%23','%23')+'")'
S="<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 28'>"
ICON={
'all': S+"<g stroke='#163d07' stroke-width='2.4'><circle cx='9' cy='14' r='7' fill='#fff'/><circle cx='20' cy='14' r='7' fill='#fff'/><circle cx='31' cy='14' r='7' fill='#fff'/></g><g fill='#fff'><rect x='9' y='9.5' width='22' height='9'/></g><g fill='#2f7d12'><circle cx='9' cy='14' r='2.6'/><circle cx='20' cy='14' r='2.6'/><circle cx='31' cy='14' r='2.6'/></g></svg>",
'combat': S+"<path d='M6 7.5c0-2 1.6-3.5 3.6-3.5h4.2c1.3 0 2.2.8 2.6 1.8l.5 1.2h8.6c2.4 0 4.2 1.9 4.2 4.2v6.6c0 2.3-1.9 4.2-4.2 4.2H10.4C8 22 6 20 6 17.6z' fill='#f4f8fc' stroke='#22324a' stroke-width='2.2' stroke-linejoin='round'/><path d='M11 12h12M11 16.5h10' stroke='#9fb6cc' stroke-width='1.8' stroke-linecap='round'/><circle cx='29' cy='20' r='6' fill='#b9482c' stroke='#3a140a' stroke-width='2'/><circle cx='27.4' cy='18.2' r='1.6' fill='#e07a55'/></svg>",
'battle': S+"<path d='M20 2.5c5 0 9 1.6 11 3.2v8.5c0 6.5-5 10.6-11 12.6-6-2-11-6.1-11-12.6V5.7c2-1.6 6-3.2 11-3.2z' fill='#7d0d1e' stroke='#4a0410' stroke-width='2.2'/><path d='M20 6c3.4 0 6 1 7.4 2v6.2c0 4.3-3.2 7.2-7.4 8.7-4.2-1.5-7.4-4.4-7.4-8.7V8c1.4-1 4-2 7.4-2z' fill='#9b1428'/><path d='M14 9.5c1.5-1 3.6-1.6 6-1.6' stroke='#c94458' stroke-width='1.8' stroke-linecap='round' fill='none'/></svg>",
'visual': S+"<path d='M17.6 2h4.8l.7 3.4 2.6 1.3 3-1.8 3.4 3.4-1.8 3 1.3 2.6 3.4.7v4.8l-3.4.7-1.3 2.6 1.8 3-3.4 3.4-3-1.8-2.6 1.3-.7 3.4h-4.8l-.7-3.4-2.6-1.3-3 1.8-3.4-3.4 1.8-3-1.3-2.6-3.4-.7v-4.8l3.4-.7 1.3-2.6-1.8-3 3.4-3.4 3 1.8 2.6-1.3z' transform='translate(20 14) scale(.86) translate(-20 -16)' fill='#fff' stroke='#0b3a5c' stroke-width='2.4' stroke-linejoin='round'/><circle cx='20' cy='14' r='4.4' fill='#1b2a3e'/></svg>",
'lobby': S+"<g stroke='#5a1446' stroke-width='1.6'><circle cx='8' cy='16' r='3.6' fill='#ff6f9e'/><circle cx='14' cy='9' r='3.2' fill='#ff4f7a'/><circle cx='13.5' cy='20' r='2.8' fill='#ffa0c0'/><circle cx='19' cy='14' r='3' fill='#ff5c8a'/></g><rect x='22' y='6.5' width='14' height='15' rx='3' fill='#ffcf3a' stroke='#6b3d00' stroke-width='2'/><path d='M25 10.5h8M25 14h8M25 17.5h5' stroke='#e39a10' stroke-width='1.6' stroke-linecap='round'/></svg>",
}
SEARCH="<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 48 48'><circle cx='24' cy='24' r='22.5' fill='#20263a' stroke='#4b5269' stroke-width='3'/><circle cx='24' cy='24' r='17' fill='#191e2e'/><path d='M14 35l7.5-7.5' stroke='#0d111a' stroke-width='7' stroke-linecap='round'/><path d='M14 35l7.5-7.5' stroke='#9fb8d0' stroke-width='3.6' stroke-linecap='round'/><circle cx='27.5' cy='20.5' r='9.5' fill='#7fd0f5' stroke='#0d111a' stroke-width='2.4'/><circle cx='27.5' cy='20.5' r='6.8' fill='#a9e4fb'/><ellipse cx='24.5' cy='17.3' rx='3.4' ry='2.3' transform='rotate(-35 24.5 17.3)' fill='#fff'/></svg>"
misc_b64 = open('icons/misc.b64').read()
order=['all','combat','battle','visual','lobby','misc']
sel=['6n+1','6n+2','6n+3','6n+4','6n+5','6n']
rules=[]
for k,s in zip(order,sel):
    bg = 'url("data:image/webp;base64,'+misc_b64+'")' if k=='misc' else uri(ICON[k])
    rules.append(':host .sidebar .nav-item:nth-child(%s) .nav-name:before{background:%s center/contain no-repeat!important}'%(s,bg))
ICON_CSS=''.join(rules)
SEARCH_CSS=':host .sidebar .nav-search{background:'+uri(SEARCH)+' center/100% no-repeat!important;border:0!important;box-shadow:0 2px 0 #0c0d14!important}:host .sidebar .nav-search:before,:host .sidebar .nav-search:after{display:none!important}'
