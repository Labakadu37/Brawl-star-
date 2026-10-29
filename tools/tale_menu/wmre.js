const {chromium}=require('playwright');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411}});
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(1200);
const c=()=>p.evaluate(()=>document.querySelector('bs-menu').shadowRoot.querySelectorAll('.jz-wm').length);
console.log('initial',await c());
await p.evaluate(()=>{const r=document.querySelector('bs-menu').shadowRoot;[...r.children].filter(e=>!e.matches('style')).forEach(e=>e.remove())});
console.log('after wipe',await c());await p.waitForTimeout(900);console.log('after 0.9s',await c());await b.close()})();
