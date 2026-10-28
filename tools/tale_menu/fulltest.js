const {chromium}=require('playwright');
(async()=>{const b=await chromium.launch({args:['--autoplay-policy=no-user-gesture-required']});const p=await b.newPage({viewport:{width:1079,height:538}});
const errs=[];p.on('pageerror',e=>errs.push(e.message));p.on('console',m=>{if(m.type()==='error')errs.push('console: '+m.text())});
const res={};const ok=(k,v)=>{res[k]=v;console.log((v===true||(typeof v==='string'&&!v.startsWith('FAIL'))?'OK  ':'??  ')+k+': '+v)};
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(700);
await p.evaluate(()=>{window.__played=0;const o=HTMLMediaElement.prototype.play;HTMLMediaElement.prototype.play=function(){window.__played++;return o.call(this).catch(()=>{})}});
const R='document.querySelector("bs-menu").shadowRoot';
const tap=async sel=>p.evaluate(([R,sel])=>{const r=eval(R);const e=typeof sel==='string'?r.querySelector(sel):null;if(!e)return false;for(const t of['pointerdown','pointerup'])e.dispatchEvent(new PointerEvent(t,{bubbles:true,composed:true}));e.click();return true},[R,sel]);
ok('launcher present',await p.evaluate(R=>!!eval(R).querySelector('.launcher'),R));
await p.evaluate(()=>{window.__sent=[];const o=window.prompt;window.prompt=x=>{window.__sent.push(x)}});await p.evaluate(()=>{Object.defineProperty(navigator,"userAgent",{get:()=>"Android"})});await tap('.launcher .float-btn');console.log("bridge:",await p.evaluate(()=>JSON.stringify(window.__sent)));await p.waitForTimeout(600);
ok('menu opens via launcher',await p.evaluate(()=>document.querySelector('bs-menu').hasAttribute('expanded')));
if(!(await p.evaluate(()=>document.querySelector('bs-menu').hasAttribute('expanded')))){await p.evaluate(()=>window.bsMenu.setExpanded(true));await p.waitForTimeout(500)}
const tabs=await p.evaluate(R=>[...eval(R).querySelectorAll('.nav-item')].map(n=>n.textContent.trim()),R);ok('tabs',tabs.join(', '));
for(let i=0;i<tabs.length;i++){await p.evaluate(([R,i])=>{const n=eval(R).querySelectorAll('.nav-item')[i];for(const t of['pointerdown','pointerup'])n.dispatchEvent(new PointerEvent(t,{bubbles:true,composed:true}));n.click()},[R,i]);await p.waitForTimeout(500);
 const c=await p.evaluate(R=>eval(R).querySelectorAll('.tile').length,R);ok('tab '+tabs[i]+' tiles',c>0?String(c):'FAIL 0')}
await p.evaluate(([R])=>{const n=eval(R).querySelectorAll('.nav-item')[0];n.click()},[R]);await p.waitForTimeout(400);
const before=await p.evaluate(()=>window.__played);
await tap('.tile .tile-toggle');await p.waitForTimeout(250);
ok('tile turns on',await p.evaluate(R=>eval(R).querySelector('.tile').classList.contains('is-on'),R));
ok('floater shown',await p.evaluate(R=>{const f=eval(R).querySelector('.jz-floater');return f?f.textContent:'FAIL none'},R));
ok('press animation class',await p.evaluate(R=>!!eval(R).querySelector('.tile.jz-press'),R));
ok('click sound played',(await p.evaluate(()=>window.__played))>before?'yes':'FAIL no');
ok('title green when on',await p.evaluate(R=>getComputedStyle(eval(R).querySelector('.tile.is-on .tile-name')).backgroundImage.includes('141, 255')||getComputedStyle(eval(R).querySelector('.tile.is-on .tile-name')).backgroundImage.slice(0,60),R));
await tap('.tile .tile-toggle');await p.waitForTimeout(250);
ok('tile turns off + disabled msg',await p.evaluate(R=>!eval(R).querySelector('.tile').classList.contains('is-on')&&(eval(R).querySelector('.jz-floater')?.textContent||''),R));
await p.waitForTimeout(1900);ok('floater removed after anim',await p.evaluate(R=>!eval(R).querySelector('.jz-floater'),R));
await tap('.nav-search');await p.waitForTimeout(400);
const si=await p.evaluate(R=>{const i=eval(R).querySelector('.search-input,input[type=search],.nav-search-input');return i?i.className:null},R);ok('search input',si||'FAIL none');
if(si){await p.evaluate(R=>{const i=eval(R).querySelector('.search-input,input[type=search]');i.focus();i.value='aim';i.dispatchEvent(new Event('input',{bubbles:true}))},R);await p.waitForTimeout(400);
 ok('search results',await p.evaluate(R=>[...eval(R).querySelectorAll('.tile-name')].map(e=>e.textContent).join(',')||'FAIL none',R));}
for(const a of ['.action-friends','.action-settings','.config']){await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(500);await p.evaluate(()=>window.bsMenu.setExpanded(true));await p.waitForTimeout(400);
 const t=await tap('.topbar '+a);await p.waitForTimeout(600);ok('open '+a,t?await p.evaluate(R=>{const m=[...eval(R).querySelectorAll('.mini.is-open .mini-head-title,.mini.is-open .mini-title')].map(e=>e.textContent).filter(Boolean)[0];return m||'FAIL no popup'},R):'FAIL missing')}
await tap('.action-close');await p.waitForTimeout(500);ok('close button closes menu',await p.evaluate(()=>!document.querySelector('bs-menu').hasAttribute('expanded')));
ok('JS errors',errs.length?'FAIL '+errs.join(' || '):'none');
await b.close()})();
