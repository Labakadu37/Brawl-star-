const {chromium}=require('playwright');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411}});
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(600);await p.evaluate(()=>window.bsMenu.setExpanded(true));await p.waitForTimeout(800);
console.log(await p.evaluate(()=>{const r=document.querySelector('bs-menu').shadowRoot;const all=r.getAnimations?r.getAnimations():document.getAnimations();const a=r.getAnimations();return a.map(x=>(x.animationName||'?')+'@'+(x.effect&&x.effect.target?(x.effect.target.className||x.effect.target.tagName):'?')+(x.effect&&x.effect.pseudoElement?x.effect.pseudoElement:'')).join(', ')||'none'}));await b.close()})();
