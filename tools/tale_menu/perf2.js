const {chromium}=require('playwright');
(async()=>{for(const f of ['menu_old.html','menu.html','menu_old.html','menu.html']){const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411},deviceScaleFactor:2.6});
const c=await p.context().newCDPSession(p);await c.send('Emulation.setCPUThrottlingRate',{rate:6});await c.send('Performance.enable');
await p.goto('file://'+process.cwd()+'/out/'+f);await p.waitForTimeout(1500);
const g=async()=>Object.fromEntries((await c.send('Performance.getMetrics')).metrics.map(m=>[m.name,m.value]));
const a=await g();await p.evaluate(()=>window.bsMenu.setExpanded(true));await p.waitForTimeout(3000);const z=await g();
await p.evaluate(()=>window.bsMenu.setExpanded(false));await p.waitForTimeout(500);const y=await g();await p.waitForTimeout(3000);const x=await g();
const d=(u,v,k)=>((v[k]-u[k])*1000).toFixed(0);
console.log(f.padEnd(14),'OPEN 3s: task',d(a,z,'TaskDuration'),'ms style',d(a,z,'RecalcStyleDuration'),'layout',d(a,z,'LayoutDuration'),'| CLOSED 3s: task',d(y,x,'TaskDuration'),'ms');await b.close()}})();
