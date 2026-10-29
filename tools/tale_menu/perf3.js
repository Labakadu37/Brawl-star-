const {chromium}=require('playwright');
const V={none:'',noTitleShine:'.jz-sh{display:none!important}',noTabShine:'.sidebar .nav-item:after{display:none!important}',noWM:'.jz-wm{display:none!important}',noAll:'.jz-sh,.jz-wm,.sidebar .nav-item:after,.launcher .float-shell:before{display:none!important}'};
(async()=>{for(const [k,css] of Object.entries(V)){const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411},deviceScaleFactor:2.6});
const c=await p.context().newCDPSession(p);await c.send('Emulation.setCPUThrottlingRate',{rate:6});await c.send('Performance.enable');
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(1200);
await p.evaluate(css=>{const s=document.createElement('style');s.textContent=css.replace(/(^|\})([^{}]+)\{/g,(m,a,sel)=>a+sel.split(',').map(x=>':host '+x.trim()).join(',')+'{');document.querySelector('bs-menu').shadowRoot.append(s)},css);
const g=async()=>Object.fromEntries((await c.send('Performance.getMetrics')).metrics.map(m=>[m.name,m.value]));
await p.evaluate(()=>window.bsMenu.setExpanded(true));await p.waitForTimeout(800);const a=await g();await p.waitForTimeout(3000);const z=await g();
console.log(k.padEnd(14),'open steady 3s: task',((z.TaskDuration-a.TaskDuration)*1000).toFixed(0),'style',((z.RecalcStyleDuration-a.RecalcStyleDuration)*1000).toFixed(0));await b.close()}})();
