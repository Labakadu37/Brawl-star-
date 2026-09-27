'use strict';

let TOKEN = null;
let ME = null;

const $ = (id) => document.getElementById(id);

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
function toast(msg, ok = true) {
  const t = $('toast');
  t.textContent = msg;
  t.className = 'toast ' + (ok ? 'ok' : 'err');
  setTimeout(() => t.classList.add('hidden'), 2600);
}

function avatarUrl(u) {
  if (u.avatar) {
    const ext = u.avatar.startsWith('a_') ? 'gif' : 'png';
    return `https://cdn.discordapp.com/avatars/${u.id}/${u.avatar}.${ext}?size=128`;
  }
  const idx = u.discriminator && u.discriminator !== '0'
    ? Number(u.discriminator) % 5
    : Number((BigInt(u.id) >> 22n) % 6n);
  return `https://cdn.discordapp.com/embed/avatars/${idx}.png`;
}

function bannerFrom(u) {
  if (u.banner) {
    const ext = u.banner.startsWith('a_') ? 'gif' : 'png';
    return `url(https://cdn.discordapp.com/banners/${u.id}/${u.banner}.${ext}?size=480)`;
  }
  if (u.accent_color != null) return '#' + u.accent_color.toString(16).padStart(6, '0');
  return 'var(--blurple)';
}

function nitroLabel(type) {
  return ({ 0: 'Aucun', 1: 'Nitro Classic', 2: 'Nitro', 3: 'Nitro Basic' })[type] || 'Aucun';
}

// ---------------------------------------------------------------------------
// Render account preview
// ---------------------------------------------------------------------------
function renderMe(u) {
  ME = u;
  $('avatar').src = avatarUrl(u);
  const b = bannerFrom(u);
  $('banner').style.background = b.startsWith('url') ? b + ' center/cover' : b;
  $('displayName').textContent = u.global_name || u.username;
  $('username').textContent = u.discriminator && u.discriminator !== '0'
    ? `${u.username}#${u.discriminator}` : '@' + u.username;
  $('userId').textContent = u.id;
  $('email').textContent = u.email || '—';
  $('phone').textContent = u.phone || '—';
  $('mfa').textContent = u.mfa_enabled ? 'Activé' : 'Désactivé';
  $('nitro').textContent = nitroLabel(u.premium_type);

  // prefill profile fields
  $('globalName').value = u.global_name || '';
  $('bio').value = u.bio || '';
  $('bioCount').textContent = (u.bio || '').length;
  if (u.accent_color != null) $('accent').value = '#' + u.accent_color.toString(16).padStart(6, '0');
}

function applyStatusDot(status) {
  $('statusDot').className = 'status-dot ' + (status || 'online');
}

// ---------------------------------------------------------------------------
// Login
// ---------------------------------------------------------------------------
async function doLogin(token) {
  $('loginError').textContent = '';
  try {
    const me = await window.api.login(token);
    TOKEN = token;
    renderMe(me);
    $('login').classList.add('hidden');
    $('app').classList.remove('hidden');
    if ($('remember').checked) await window.api.saveToken(token);
  } catch (e) {
    $('loginError').textContent = e.message === '401: Unauthorized' || e.status === 401
      ? 'Token invalide.' : ('Échec : ' + e.message);
  }
}

$('loginBtn').addEventListener('click', () => {
  const t = $('token').value.trim();
  if (!t) { $('loginError').textContent = 'Entre un token.'; return; }
  doLogin(t);
});
$('token').addEventListener('keydown', (e) => { if (e.key === 'Enter') $('loginBtn').click(); });
$('reveal').addEventListener('click', () => {
  const i = $('token');
  i.type = i.type === 'password' ? 'text' : 'password';
});

$('logout').addEventListener('click', async () => {
  await window.api.logout();
  await window.api.clearToken();
  TOKEN = null; ME = null;
  $('token').value = '';
  $('app').classList.add('hidden');
  $('login').classList.remove('hidden');
});

// auto-fill remembered token
(async () => {
  const saved = await window.api.loadToken();
  if (saved) {
    $('token').value = saved;
    $('remember').checked = true;
    doLogin(saved);
  }
})();

// ---------------------------------------------------------------------------
// Tabs
// ---------------------------------------------------------------------------
document.querySelectorAll('.tab').forEach((tab) => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach((t) => t.classList.remove('active'));
    document.querySelectorAll('.panel').forEach((p) => p.classList.remove('active'));
    tab.classList.add('active');
    document.querySelector(`.panel[data-panel="${tab.dataset.tab}"]`).classList.add('active');
  });
});

// ---------------------------------------------------------------------------
// Presence: status
// ---------------------------------------------------------------------------
let currentStatus = 'online';
let currentCustom = null; // { text, emoji }

function buildActivities(extra) {
  const acts = [];
  if (currentCustom && (currentCustom.text || currentCustom.emoji)) {
    const a = { type: 4, name: 'Custom Status', state: currentCustom.text || '' };
    if (currentCustom.emoji) a.emoji = { name: currentCustom.emoji };
    acts.push(a);
  }
  if (extra) acts.push(extra);
  return acts;
}

let streamActivity = null;

async function pushPresence() {
  const activities = buildActivities(streamActivity);
  try {
    await window.api.setPresence(TOKEN, { status: currentStatus, activities, afk: false, since: 0 });
    applyStatusDot(currentStatus);
  } catch (e) {
    toast('Erreur présence : ' + e.message, false);
  }
}

document.querySelectorAll('.status-btn').forEach((btn) => {
  btn.addEventListener('click', async () => {
    document.querySelectorAll('.status-btn').forEach((b) => b.classList.remove('selected'));
    btn.classList.add('selected');
    currentStatus = btn.dataset.status;
    await pushPresence();
    toast('Statut : ' + btn.textContent.trim());
  });
});

$('saveCustom').addEventListener('click', async () => {
  const text = $('customText').value.trim();
  const emoji = $('customEmoji').value.trim();
  currentCustom = (text || emoji) ? { text, emoji } : null;
  // Custom status is also persisted server-side via settings so it sticks.
  try {
    await window.api.updateSettings(TOKEN, {
      custom_status: currentCustom
        ? { text: text || null, emoji_name: emoji || null }
        : null
    });
  } catch (_) { /* settings patch may vary; presence still applies */ }
  await pushPresence();
  $('customStatusPreview').textContent = currentCustom
    ? `${emoji ? emoji + ' ' : ''}${text}` : '';
  toast('Statut personnalisé appliqué');
});

$('setStream').addEventListener('click', async () => {
  const name = $('streamName').value.trim() || 'Streaming';
  const url = $('streamUrl').value.trim();
  if (!/^https:\/\/(www\.)?(twitch\.tv|youtube\.com|youtu\.be)\//i.test(url)) {
    toast('URL Twitch/YouTube requise pour le badge Streaming', false);
    return;
  }
  streamActivity = { type: 1, name, url };
  await pushPresence();
  toast('Activité Streaming définie');
});

$('clearActivity').addEventListener('click', async () => {
  streamActivity = null;
  await pushPresence();
  toast('Activité retirée');
});

// ---------------------------------------------------------------------------
// Profile
// ---------------------------------------------------------------------------
$('bio').addEventListener('input', () => { $('bioCount').textContent = $('bio').value.length; });

function profileMsg(text, ok) {
  const m = $('profileMsg');
  m.textContent = text; m.className = 'msg ' + (ok ? 'ok' : 'err');
}

$('saveGlobalName').addEventListener('click', async () => {
  try {
    const u = await window.api.updateProfile(TOKEN, { global_name: $('globalName').value.trim() });
    renderMe(u); profileMsg('Nom affiché mis à jour.', true); toast('Nom enregistré');
  } catch (e) { profileMsg('Erreur : ' + e.message, false); }
});

$('saveBio').addEventListener('click', async () => {
  try {
    const u = await window.api.updateProfile(TOKEN, { bio: $('bio').value });
    renderMe(u); profileMsg('Bio mise à jour.', true); toast('Bio enregistrée');
  } catch (e) { profileMsg('Erreur : ' + e.message, false); }
});

$('saveAccent').addEventListener('click', async () => {
  try {
    const int = parseInt($('accent').value.replace('#', ''), 16);
    const u = await window.api.updateProfile(TOKEN, { accent_color: int });
    renderMe(u); profileMsg('Couleur enregistrée.', true); toast('Couleur enregistrée');
  } catch (e) { profileMsg('Erreur : ' + e.message, false); }
});

// ---------------------------------------------------------------------------
// Security: password change
// ---------------------------------------------------------------------------
function securityMsg(text, ok) {
  const m = $('securityMsg');
  m.textContent = text; m.className = 'msg ' + (ok ? 'ok' : 'err');
}

$('savePass').addEventListener('click', async () => {
  const cur = $('curPass').value;
  const np = $('newPass').value;
  const np2 = $('newPass2').value;
  if (!cur || !np) { securityMsg('Remplis les champs.', false); return; }
  if (np !== np2) { securityMsg('Les nouveaux mots de passe ne correspondent pas.', false); return; }
  if (np.length < 6) { securityMsg('Minimum 6 caractères.', false); return; }
  try {
    const u = await window.api.updateProfile(TOKEN, { password: cur, new_password: np });
    securityMsg('Mot de passe changé. Le token a été régénéré — reconnecte-toi.', true);
    $('curPass').value = $('newPass').value = $('newPass2').value = '';
    // token is now invalid; force logout shortly
    setTimeout(async () => {
      await window.api.logout(); await window.api.clearToken();
      $('app').classList.add('hidden'); $('login').classList.remove('hidden');
      $('token').value = ''; $('loginError').textContent = 'Reconnecte-toi avec le nouveau token.';
    }, 2500);
  } catch (e) { securityMsg('Erreur : ' + e.message, false); }
});
