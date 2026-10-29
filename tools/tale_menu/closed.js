const {chromium}=require('playwright');const fs=require('fs');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411}});
const bg='data:image/jpeg;base64,'+fs.readFileSync('out/bg_phone.jpg').toString('base64');
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(800);
await p.evaluate(bg=>{document.documentElement.style.setProperty('background','url('+bg+') center/cover','important')},bg);await p.waitForTimeout(400);
console.log(await p.evaluate(()=>{const r=document.querySelector('bs-menu').shadowRoot;return [...r.children].filter(e=>e.tagName!=='STYLE').map(e=>{const b=e.getBoundingClientRect();return e.className+' '+Math.round(b.x)+','+Math.round(b.y)+' '+Math.round(b.width)+'x'+Math.round(b.height)+' vis='+getComputedStyle(e).visibility+' op='+getComputedStyle(e).opacity+' txt='+e.textContent.slice(0,60)}).join('\n')}));
await p.screenshot({path:'out/closed.png'});await b.close()})();
