const {chromium}=require('playwright');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:891,height:411}});
const errs=[];p.on('pageerror',e=>errs.push(e.message));
await p.addInitScript(()=>{window.__sent=[];window.prompt=x=>{window.__sent.push(x)};Object.defineProperty(navigator,'userAgent',{get:()=>'Android'})});
await p.goto('file://'+process.cwd()+'/out/menu.html');await p.waitForTimeout(500);
await p.evaluate(()=>{const m=document.querySelector('bs-menu');m.state.set('hud','enabled',true);m.state.set('quickAccess','enabled',true);window.__sent=[]});
await p.waitForTimeout(3300);
console.log(await p.evaluate(()=>{const m=document.querySelector('bs-menu');return 'hud='+m.state.isEnabled('hud')+' quick='+m.state.isEnabled('quickAccess')+' sent='+JSON.stringify(window.__sent.filter(x=>x.includes('module-value')))}));
console.log('errors',errs.length?errs:'none');await b.close()})();
