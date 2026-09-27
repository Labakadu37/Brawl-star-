'use strict';

const { app, BrowserWindow, ipcMain, safeStorage } = require('electron');
const path = require('path');
const fs = require('fs');
const WebSocket = require('ws');

const API = 'https://discord.com/api/v10';
const TOKEN_FILE = path.join(app.getPath('userData'), 'token.bin');

let win = null;

// ---------------------------------------------------------------------------
// Gateway connection: used only to broadcast presence (status / activity),
// which the REST API cannot set. Kept alive while the app is open.
// ---------------------------------------------------------------------------
let gw = null;
let gwHeartbeat = null;
let gwSeq = null;
let currentToken = null;
let desiredPresence = null; // { status, activities }

function log(...a) {
  console.log('[main]', ...a);
}

function closeGateway() {
  if (gwHeartbeat) {
    clearInterval(gwHeartbeat);
    gwHeartbeat = null;
  }
  if (gw) {
    try { gw.close(); } catch (_) {}
    gw = null;
  }
  gwSeq = null;
}

function sendPresence() {
  if (!gw || gw.readyState !== WebSocket.OPEN || !desiredPresence) return;
  gw.send(JSON.stringify({
    op: 3,
    d: {
      since: 0,
      activities: desiredPresence.activities || [],
      status: desiredPresence.status || 'online',
      afk: false
    }
  }));
}

function openGateway(token) {
  closeGateway();
  currentToken = token;
  gw = new WebSocket('wss://gateway.discord.gg/?v=10&encoding=json');

  gw.on('message', (raw) => {
    let payload;
    try { payload = JSON.parse(raw.toString()); } catch (_) { return; }
    const { op, d, s, t } = payload;
    if (s != null) gwSeq = s;

    if (op === 10) {
      const interval = d.heartbeat_interval;
      gwHeartbeat = setInterval(() => {
        if (gw && gw.readyState === WebSocket.OPEN) {
          gw.send(JSON.stringify({ op: 1, d: gwSeq }));
        }
      }, interval);
      gw.send(JSON.stringify({
        op: 2,
        d: {
          token: currentToken,
          intents: 0,
          properties: { os: 'windows', browser: 'discord-account-manager', device: 'desktop' },
          presence: desiredPresence || { status: 'online', activities: [], afk: false, since: 0 }
        }
      }));
    } else if (op === 0 && t === 'READY') {
      sendPresence();
    } else if (op === 1) {
      gw.send(JSON.stringify({ op: 1, d: gwSeq }));
    }
  });

  gw.on('close', (code) => {
    log('gateway closed', code);
    if (gwHeartbeat) { clearInterval(gwHeartbeat); gwHeartbeat = null; }
  });

  gw.on('error', (e) => log('gateway error', e.message));
}

// ---------------------------------------------------------------------------
// REST helpers
// ---------------------------------------------------------------------------
async function discord(token, method, endpoint, body) {
  const res = await fetch(API + endpoint, {
    method,
    headers: {
      'Authorization': token,
      'Content-Type': 'application/json'
    },
    body: body ? JSON.stringify(body) : undefined
  });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (_) { data = text; }
  if (!res.ok) {
    const msg = (data && data.message) ? data.message : ('HTTP ' + res.status);
    const err = new Error(msg);
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

// ---------------------------------------------------------------------------
// Token persistence (encrypted at rest with the OS keychain when available)
// ---------------------------------------------------------------------------
function saveToken(token) {
  try {
    if (safeStorage.isEncryptionAvailable()) {
      fs.writeFileSync(TOKEN_FILE, safeStorage.encryptString(token));
    } else {
      fs.writeFileSync(TOKEN_FILE, Buffer.from('plain:' + token, 'utf8'));
    }
  } catch (e) { log('saveToken failed', e.message); }
}

function loadToken() {
  try {
    if (!fs.existsSync(TOKEN_FILE)) return null;
    const buf = fs.readFileSync(TOKEN_FILE);
    if (buf.slice(0, 6).toString() === 'plain:') return buf.slice(6).toString('utf8');
    if (safeStorage.isEncryptionAvailable()) return safeStorage.decryptString(buf);
    return null;
  } catch (e) { log('loadToken failed', e.message); return null; }
}

function clearToken() {
  try { if (fs.existsSync(TOKEN_FILE)) fs.unlinkSync(TOKEN_FILE); } catch (_) {}
}

// ---------------------------------------------------------------------------
// IPC
// ---------------------------------------------------------------------------
ipcMain.handle('login', async (_e, token) => {
  const me = await discord(token, 'GET', '/users/@me');
  openGateway(token);
  return me;
});

ipcMain.handle('get-me', async (_e, token) => discord(token, 'GET', '/users/@me'));

ipcMain.handle('get-settings', async (_e, token) =>
  discord(token, 'GET', '/users/@me/settings'));

ipcMain.handle('update-profile', async (_e, token, body) =>
  discord(token, 'PATCH', '/users/@me', body));

ipcMain.handle('update-settings', async (_e, token, body) =>
  discord(token, 'PATCH', '/users/@me/settings', body));

ipcMain.handle('set-presence', async (_e, token, presence) => {
  desiredPresence = presence;
  if (!gw || gw.readyState !== WebSocket.OPEN) openGateway(token);
  else sendPresence();
  return { ok: true };
});

ipcMain.handle('save-token', (_e, token) => { saveToken(token); return true; });
ipcMain.handle('load-token', () => loadToken());
ipcMain.handle('clear-token', () => { clearToken(); closeGateway(); return true; });
ipcMain.handle('logout', () => { closeGateway(); currentToken = null; return true; });

// ---------------------------------------------------------------------------
// Window
// ---------------------------------------------------------------------------
function createWindow() {
  win = new BrowserWindow({
    width: 1100,
    height: 720,
    minWidth: 900,
    minHeight: 600,
    backgroundColor: '#1e1f22',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });
  win.setMenuBarVisibility(false);
  win.loadFile(path.join(__dirname, 'src', 'index.html'));
}

app.whenReady().then(createWindow);
app.on('window-all-closed', () => { closeGateway(); app.quit(); });
