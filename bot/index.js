require('dotenv').config();
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const express = require('express');
const Database = require('better-sqlite3');
const {
  Client, GatewayIntentBits, REST, Routes, SlashCommandBuilder,
  EmbedBuilder, MessageFlags, ActionRowBuilder, ButtonBuilder, ButtonStyle,
} = require('discord.js');

const DIR = __dirname;
const ADMIN_ROLE = String(process.env.ADMIN_ROLE_ID || '1555694710582939791').trim();
const PRIV = path.join(DIR, 'private.pem');
const PUB = path.join(DIR, 'public.txt');
if (!fs.existsSync(PRIV)) {
  const { privateKey, publicKey } = crypto.generateKeyPairSync('rsa', { modulusLength: 2048 });
  fs.writeFileSync(PRIV, privateKey.export({ type: 'pkcs8', format: 'pem' }), { mode: 0o600 });
  fs.writeFileSync(PUB, publicKey.export({ type: 'spki', format: 'der' }).toString('base64'));
}
const privateKey = crypto.createPrivateKey(fs.readFileSync(PRIV));
console.log('PUBLIC KEY:\n' + fs.readFileSync(PUB, 'utf8') + '\n');

const db = new Database(path.join(DIR, 'keys.db'));
db.exec(`CREATE TABLE IF NOT EXISTS keys (
  key TEXT PRIMARY KEY, created_at INTEGER NOT NULL, created_by TEXT,
  device TEXT, used_at INTEGER, last_seen INTEGER, revoked INTEGER NOT NULL DEFAULT 0)`);
db.exec('CREATE TABLE IF NOT EXISTS join_roles (guild TEXT PRIMARY KEY, role TEXT NOT NULL)');

const CHARS = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
function newKey() {
  const part = () => Array.from(crypto.randomBytes(4), b => CHARS[b % CHARS.length]).join('');
  return `JZS-${part()}-${part()}-${part()}`;
}
const norm = k => String(k || '').trim().toUpperCase();
function sign(status, key, device, nonce) {
  return crypto.sign('sha256', Buffer.from(`${status}|${key}|${device}|${nonce}`), privateKey).toString('base64');
}
function verify(key, device, nonce, activate) {
  const row = db.prepare('SELECT * FROM keys WHERE key = ?').get(key);
  if (!row) return 'invalid';
  if (row.revoked) return 'revoked';
  const now = Date.now();
  if (!row.device) {
    if (!activate) return 'invalid';
    db.prepare('UPDATE keys SET device=?, used_at=?, last_seen=? WHERE key=?').run(device, now, now, key);
    return 'ok';
  }
  if (row.device !== device) return 'used';
  db.prepare('UPDATE keys SET last_seen=? WHERE key=?').run(now, key);
  return 'ok';
}

const app = express();
app.set('trust proxy', 1);
// The in-game menu is a WebView with an opaque origin: allow any origin and accept
// text/plain bodies so the request stays "simple" and needs no CORS preflight.
app.use('/api', (req, res, next) => {
  res.set({ 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Methods': 'POST, OPTIONS', 'Access-Control-Allow-Headers': 'Content-Type' });
  if (req.method === 'OPTIONS') return res.sendStatus(204);
  next();
});
app.use(express.json({ limit: '2kb', type: () => true }));
const hits = new Map();
app.use('/api', (req, res, next) => {
  const ip = req.ip, now = Date.now();
  const h = (hits.get(ip) || []).filter(t => now - t < 60000);
  h.push(now); hits.set(ip, h);
  if (h.length > 30) return res.status(429).json({ status: 'rate' });
  next();
});
function handler(activate) {
  return (req, res) => {
    const body = req.body && typeof req.body === 'object' ? req.body : {};
    const key = norm(body.key), device = String(body.device || '').slice(0, 64), nonce = String(body.nonce || '').slice(0, 64);
    if (!key || !device || !nonce) return res.status(400).json({ status: 'bad' });
    const status = verify(key, device, nonce, activate);
    res.json({ status, sig: sign(status, key, device, nonce) });
  };
}
app.post('/api/activate', handler(true));
app.post('/api/check', handler(false));
app.get('/', (_, res) => res.send('JZS key server OK'));
const PORT = Number(process.env.PORT || 3000);
const HOST = (process.env.HOST || '127.0.0.1').trim();
app.listen(PORT, HOST, () => console.log(`API sur http://${HOST}:${PORT}`));

const commands = [
  new SlashCommandBuilder().setName('genkey').setDescription('Generer des cles')
    .addIntegerOption(o => o.setName('nombre').setDescription('Combien (1-25)').setMinValue(1).setMaxValue(25)),
  new SlashCommandBuilder().setName('revokekey').setDescription('Revoquer une cle (bloque l utilisateur)')
    .addStringOption(o => o.setName('key').setDescription('La cle').setRequired(true)),
  new SlashCommandBuilder().setName('unrevokekey').setDescription('Reactiver une cle revoquee')
    .addStringOption(o => o.setName('key').setDescription('La cle').setRequired(true)),
  new SlashCommandBuilder().setName('resetkey').setDescription('Delier la cle du telephone')
    .addStringOption(o => o.setName('key').setDescription('La cle').setRequired(true)),
  new SlashCommandBuilder().setName('keyinfo').setDescription('Infos sur une cle')
    .addStringOption(o => o.setName('key').setDescription('La cle').setRequired(true)),
  new SlashCommandBuilder().setName('keys').setDescription('Liste des cles')
    .addStringOption(o => o.setName('filtre').setDescription('Quel type').addChoices(
      { name: 'Toutes', value: 'all' }, { name: 'Libres', value: 'free' },
      { name: 'Utilisees', value: 'used' }, { name: 'Revoquees', value: 'revoked' })),
  new SlashCommandBuilder().setName('rolejoin').setDescription('Role donne automatiquement aux nouveaux membres')
    .addSubcommand(s => s.setName('set').setDescription('Choisir le role')
      .addRoleOption(o => o.setName('role').setDescription('Le role a donner').setRequired(true)))
    .addSubcommand(s => s.setName('off').setDescription('Desactiver le role automatique'))
    .addSubcommand(s => s.setName('info').setDescription('Voir le role actuel')),
].map(c => c.toJSON());

// GuildMembers is a privileged intent: enable "Server Members Intent" in the Developer Portal.
const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMembers] });

client.on('guildMemberAdd', async member => {
  const row = db.prepare('SELECT role FROM join_roles WHERE guild = ?').get(member.guild.id);
  if (!row) return;
  try { await member.roles.add(row.role, 'rolejoin'); }
  catch (e) { console.error(`rolejoin: impossible de donner ${row.role} a ${member.user.tag}:`, e.message); }
});

async function roleJoin(i) {
  const eph = { flags: MessageFlags.Ephemeral };
  const sub = i.options.getSubcommand();
  if (sub === 'off') {
    db.prepare('DELETE FROM join_roles WHERE guild = ?').run(i.guildId);
    return i.reply({ content: '✅ Role automatique desactive.', ...eph });
  }
  if (sub === 'info') {
    const row = db.prepare('SELECT role FROM join_roles WHERE guild = ?').get(i.guildId);
    return i.reply({ content: row ? `Role automatique : <@&${row.role}>` : 'Aucun role automatique.', ...eph });
  }
  const role = i.options.getRole('role');
  const me = await i.guild.members.fetchMe();
  if (role.managed || role.id === i.guildId) return i.reply({ content: '❌ Ce role ne peut pas etre donne.', ...eph });
  if (!me.permissions.has('ManageRoles')) return i.reply({ content: '❌ Le bot n a pas la permission **Gerer les roles**.', ...eph });
  if (me.roles.highest.comparePositionTo(role) <= 0)
    return i.reply({ content: `❌ Le role du bot doit etre **au-dessus** de ${role} dans Parametres du serveur > Roles.`, ...eph });
  db.prepare('INSERT INTO join_roles (guild, role) VALUES (?, ?) ON CONFLICT(guild) DO UPDATE SET role = excluded.role').run(i.guildId, role.id);
  return i.reply({ content: `✅ Les nouveaux membres recevront ${role}.`, ...eph });
}
client.once('ready', async () => {
  const rest = new REST().setToken(process.env.DISCORD_TOKEN);
  const appId = String(process.env.DISCORD_CLIENT_ID || '').trim();
  const guildId = String(process.env.DISCORD_GUILD_ID || '').trim();
  // Guild commands show up instantly; global ones can take up to an hour.
  const route = guildId ? Routes.applicationGuildCommands(appId, guildId) : Routes.applicationCommands(appId);
  try { await rest.put(route, { body: commands }); console.log('Commandes enregistrees', guildId ? `(serveur ${guildId})` : '(global)'); }
  catch (e) { console.error('Enreg. echoue:', e.message); }
  console.log('Bot connecte:', client.user.tag);
});

const ts = t => t ? `<t:${Math.floor(t / 1000)}:R>` : '—';
function allowed(i) {
  if (!i.member) return false;
  const p = i.member.permissions;
  if (p && typeof p.has === 'function' && p.has('Administrator')) return true;
  const r = i.member.roles;
  if (r && r.cache && r.cache.has(ADMIN_ROLE)) return true;
  return Array.isArray(i.member.roles) && i.member.roles.includes(ADMIN_ROLE);
}
const PAGE = 10;
const WHERE = { all: '', free: 'WHERE device IS NULL AND revoked=0', used: 'WHERE device IS NOT NULL AND revoked=0', revoked: 'WHERE revoked=1' };
function keyLine(r) {
  const e = r.revoked ? '⛔' : r.device ? '🟢' : '⚪';
  return `${e} \`${r.key}\``;
}
function buildKeysEmbed(filtre, page) {
  const w = WHERE[filtre] || '';
  const total = db.prepare(`SELECT COUNT(*) n FROM keys ${w}`).get().n;
  const pages = Math.max(1, Math.ceil(total / PAGE));
  page = Math.min(Math.max(0, page), pages - 1);
  const rows = db.prepare(`SELECT * FROM keys ${w} ORDER BY created_at DESC LIMIT ? OFFSET ?`).all(PAGE, page * PAGE);
  const c = q => db.prepare(`SELECT COUNT(*) n FROM keys ${q}`).get().n;
  const names = { all: 'Toutes', free: 'Libres', used: 'Utilisees', revoked: 'Revoquees' };
  const e = new EmbedBuilder()
    .setTitle('🔑 JZS — Clés (' + (names[filtre] || 'Toutes') + ')')
    .setColor(0x5865F2)
    .setDescription(rows.length ? rows.map(keyLine).join('\n') : '_Aucune clé._')
    .addFields({ name: 'Résumé', value:
      `Total **${c('')}** · ⚪ Libres **${c(WHERE.free)}** · 🟢 Utilisées **${c(WHERE.used)}** · ⛔ Révoquées **${c(WHERE.revoked)}**` })
    .setFooter({ text: `Page ${page + 1}/${pages}` });
  const row = new ActionRowBuilder().addComponents(
    new ButtonBuilder().setCustomId(`keys:${filtre}:${page - 1}`).setLabel('◀').setStyle(ButtonStyle.Secondary).setDisabled(page <= 0),
    new ButtonBuilder().setCustomId(`keys:${filtre}:${page + 1}`).setLabel('▶').setStyle(ButtonStyle.Secondary).setDisabled(page >= pages - 1),
  );
  return { embeds: [e], components: [row] };
}

client.on('interactionCreate', async i => {
  const eph = { flags: MessageFlags.Ephemeral };
  if (i.isButton() && i.customId.startsWith('keys:')) {
    if (!allowed(i)) return i.reply({ content: '⛔ Reserve au staff.', ...eph });
    const [, filtre, pg] = i.customId.split(':');
    return i.update(buildKeysEmbed(filtre, parseInt(pg, 10) || 0));
  }
  if (!i.isChatInputCommand()) return;
  if (!allowed(i)) return i.reply({ content: '⛔ Tu n as pas la permission d utiliser ce bot.', ...eph });
  if (i.commandName === 'rolejoin') return roleJoin(i);
  const key = norm(i.options.getString('key'));
  const row = key ? db.prepare('SELECT * FROM keys WHERE key = ?').get(key) : null;
  if (key && !row) return i.reply({ content: '❌ Cle introuvable.', ...eph });
  switch (i.commandName) {
    case 'genkey': {
      const n = i.options.getInteger('nombre') || 1;
      const ins = db.prepare('INSERT INTO keys (key, created_at, created_by) VALUES (?, ?, ?)');
      const list = [];
      for (let k = 0; k < n; k++) { const v = newKey(); ins.run(v, Date.now(), i.user.id); list.push(v); }
      const e = new EmbedBuilder().setTitle(`🔑 ${n} clé(s) générée(s)`).setColor(0x2ecc71)
        .setDescription('```\n' + list.join('\n') + '\n```');
      return i.reply({ embeds: [e], ...eph });
    }
    case 'revokekey':
      db.prepare('UPDATE keys SET revoked=1 WHERE key=?').run(key);
      return i.reply({ content: `⛔ \`${key}\` révoquée. L utilisateur perd l accès sous 2 min.`, ...eph });
    case 'unrevokekey':
      db.prepare('UPDATE keys SET revoked=0 WHERE key=?').run(key);
      return i.reply({ content: `✅ \`${key}\` réactivée.`, ...eph });
    case 'resetkey':
      db.prepare('UPDATE keys SET device=NULL, used_at=NULL WHERE key=?').run(key);
      return i.reply({ content: `♻️ \`${key}\` déliée, utilisable sur un nouveau téléphone.`, ...eph });
    case 'keyinfo': {
      const st = row.revoked ? '⛔ Révoquée' : row.device ? '🟢 Utilisée' : '⚪ Libre';
      const e = new EmbedBuilder().setTitle(key).setColor(row.revoked ? 0xe74c3c : row.device ? 0x2ecc71 : 0x95a5a6).addFields(
        { name: 'Statut', value: st, inline: true },
        { name: 'Créée', value: ts(row.created_at), inline: true },
        { name: 'Par', value: row.created_by ? `<@${row.created_by}>` : '—', inline: true },
        { name: 'Activée', value: ts(row.used_at), inline: true },
        { name: 'Vue', value: ts(row.last_seen), inline: true },
        { name: 'Appareil', value: row.device ? `\`${row.device}\`` : '—', inline: true });
      return i.reply({ embeds: [e], ...eph });
    }
    case 'keys':
      return i.reply({ ...buildKeysEmbed(i.options.getString('filtre') || 'all', 0), ...eph });
  }
});
client.login(process.env.DISCORD_TOKEN).catch(e => console.error('Login echoue:', e.message));
