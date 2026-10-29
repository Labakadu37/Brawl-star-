const {chromium}=require('playwright');const fs=require('fs');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:1079,height:538},deviceScaleFactor:2});
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(800);
const bg='data:image/jpeg;base64,'+fs.readFileSync('/root/.claude/uploads/b3bddd68-5cb9-5bf7-b51b-9e0c225caa68/cb1fbe54-image.jpg').toString('base64');
await p.evaluate(bg=>{document.documentElement.style.setProperty('background','url('+bg+') 0 0/1079px 538px','important');const r=document.querySelector('bs-menu').shadowRoot;const L=r.querySelector('.launcher');L.style.setProperty('left','90px','important');L.style.setProperty('top','14px','important');r.querySelector('.jz-wm').style.display='none';L.getAnimations({subtree:true}).forEach(a=>{a.pause();a.currentTime=3000})},bg);
await p.waitForTimeout(300);await p.screenshot({path:'out/lshot.png',clip:{x:0,y:8,width:180,height:56}});await b.close()})();
