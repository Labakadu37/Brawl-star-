const {chromium}=require('playwright');const fs=require('fs');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411},deviceScaleFactor:2.6});
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(800);
const bg='data:image/jpeg;base64,'+fs.readFileSync(process.argv[2]).toString('base64');
await p.evaluate(bg=>{document.documentElement.style.setProperty('background','url('+bg+') center/cover','important');const r=document.querySelector('bs-menu').shadowRoot;r.querySelector('.launcher').style.display='none';r.querySelectorAll('.jz-c').forEach(e=>e.getAnimations().forEach(a=>{a.pause();a.currentTime=1500}))},bg);
await p.waitForTimeout(300);await p.screenshot({path:'out/wm.png',clip:{x:0,y:0,width:220,height:46}});await b.close()})();
