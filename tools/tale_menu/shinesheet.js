const {chromium}=require('playwright');const fs=require('fs');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411},deviceScaleFactor:3});
const errs=[];p.on('pageerror',e=>errs.push(e.message));
await p.addInitScript(()=>{window.__MENU_CONTENT__={locales:{en:{'module.autoDodge':'Auto Dodge','module.killAura':'Kill Aura'}}}});
const bg='data:image/jpeg;base64,'+fs.readFileSync('out/bg_tgame.jpg').toString('base64');
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(700);
await p.evaluate(bg=>{document.documentElement.style.setProperty('background','url('+bg+') center/cover','important');window.bsMenu.setExpanded(true)},bg);await p.waitForTimeout(800);
const R='document.querySelector("bs-menu").shadowRoot';
const rect=async sel=>p.evaluate(([R,sel])=>{const e=eval(R).querySelector(sel);const r=e.getBoundingClientRect();return {x:r.x,y:r.y,width:r.width,height:r.height}},[R,sel]);
const freeze=async(sel,t,cls)=>p.evaluate(([R,sel,t,cls])=>{const e=eval(R).querySelector(sel);if(cls)e.classList.add(cls);e.getAnimations({subtree:true}).forEach(a=>{a.pause();a.currentTime=t})},[R,sel,t,cls]);
const shots=[];
async function series(name,sel,times,cls,pad=4){for(const t of times){await freeze(sel,t,cls);await p.waitForTimeout(60);const r=await rect(sel);const f='out/ss_'+name+'_'+t+'.png';await p.screenshot({path:f,clip:{x:r.x-pad,y:r.y-pad,width:r.width+2*pad,height:r.height+2*pad}});shots.push(f)}}
// stop the JS wave from interfering
await p.evaluate(()=>{const id=setInterval(()=>{},1e6);for(let i=0;i<id;i++)clearInterval(i)});
await series('tab','.nav-item',[0,500,900,1300,1700],null);
await series('title','.jz-tw',[0,250,500,750,1000],'jz-go');
await p.evaluate(()=>window.bsMenu.setExpanded(false));await p.waitForTimeout(400);
await series('launch','.launcher .float-shell',[0,500,900,1300,1700],null,6);
await series('wmg','.jz-g',[0,350,650,950,1300],'jz-go');
await series('wmr','.jz-r',[0,350,650,950,1300],'jz-go');
console.log(JSON.stringify(shots));console.log('errors',errs.length?errs:'none');await b.close()})();
