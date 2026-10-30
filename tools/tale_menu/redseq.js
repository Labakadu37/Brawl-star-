const {chromium}=require('playwright');const fs=require('fs');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411},deviceScaleFactor:3});
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(1500);
const bg='data:image/jpeg;base64,'+fs.readFileSync('out/bg_tgame.jpg').toString('base64');
await p.evaluate(bg=>{document.documentElement.style.setProperty('background','url('+bg+') center/cover','important');document.querySelector('bs-menu').shadowRoot.querySelector('.launcher').style.display='none'},bg);
console.log(await p.evaluate(()=>[...document.querySelector('bs-menu').shadowRoot.querySelectorAll('.jz-c')].map(e=>getComputedStyle(e).animationName+' '+getComputedStyle(e).animationDuration).join(', ')));
let i=0;for(const t of [0,1500,3000,3500,4000,5500]){await p.evaluate(t=>document.querySelector('bs-menu').shadowRoot.querySelectorAll('.jz-c').forEach(e=>e.getAnimations().forEach(a=>{a.pause();a.currentTime=t})),t);await p.waitForTimeout(80);await p.screenshot({path:'out/rq'+(i++)+'.png',clip:{x:0,y:0,width:140,height:16}})}
await b.close()})();
