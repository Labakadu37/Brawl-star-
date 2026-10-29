const {chromium}=require('playwright');const fs=require('fs');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411}});
const errs=[];p.on('pageerror',e=>errs.push(e.message));
const names={autoDodge:'Auto Dodge',killAura:'Kill Aura',autoAttack:'Auto Attack',aimbot:'Aimbot',autoHyper:'Auto Hypercharge',autoRecall:'Auto Recall',hideGadget:'Hide Gadget',hideHover:'Hide Hover',hideHyper:'Hide Hypercharge',hideAim:'Hide Aim',xray:'Xray',grayMod:'Gray Mod',naniMod:'Nani Mod',shadeMod:'Shade Mod',hitboxes:'Hitboxes',tracers:'Tracers',rangeIndicator:'Range Indicator',ghostMarker:'Ghost Marker',ghostPlayers:'Ghost Players',ghostTraps:'Show Traps'};
const en={};for(const k in names)en['module.'+k]=names[k];
await p.addInitScript(en=>{window.__MENU_CONTENT__={locales:{en}}},en);
const bg='data:image/jpeg;base64,'+fs.readFileSync('out/bg_phone.jpg').toString('base64');
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(600);
await p.evaluate(bg=>{document.documentElement.style.setProperty('background','url('+bg+') center/cover','important');window.bsMenu.setExpanded(true)},bg);await p.waitForTimeout(900);
await p.screenshot({path:'out/phone.png'});
console.log(await p.evaluate(()=>[...document.querySelector('bs-menu').shadowRoot.querySelectorAll('.tile-desc')].slice(0,4).map(e=>e.textContent).join(' | ')));
for(let i=0;i<8;i++){await p.screenshot({path:'out/gs'+i+'.png',clip:{x:90,y:110,width:330,height:40}});await p.waitForTimeout(230)}
console.log('errors',errs.length?errs:'none');await b.close()})();
