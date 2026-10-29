const {chromium}=require('playwright');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411}});
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(600);await p.evaluate(()=>window.bsMenu.setExpanded(true));
const seen=new Set();for(let i=0;i<20;i++){await p.waitForTimeout(150);(await p.evaluate(()=>[...document.querySelector('bs-menu').shadowRoot.querySelectorAll('.jz-tw.jz-go .tile-name')].map(e=>e.textContent))).forEach(x=>seen.add(x))}
console.log('tiles shined in 3s:',seen.size,[...seen].slice(0,6).join(', '));
console.log('wm active anims:',await p.evaluate(()=>document.querySelector('bs-menu').shadowRoot.querySelectorAll('.jz-c.jz-go').length));
console.log('wm zIndex:',await p.evaluate(()=>getComputedStyle(document.querySelector('bs-menu').shadowRoot.querySelector('.jz-wm')).zIndex));
await b.close()})();
