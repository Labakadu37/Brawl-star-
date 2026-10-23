require('dotenv').config();
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const express = require('express');
const {
  Client, GatewayIntentBits, REST, Routes, SlashCommandBuilder,
  EmbedBuilder, MessageFlags, ActionRowBuilder, ButtonBuilder, ButtonStyle,
} = require('discord.js');

const DIR = __dirname;
const ADMIN_ROLES = (process.env.ADMIN_ROLE_ID || '').split(',').map(x => x.trim()).filter(Boolean);
const PRIV = path.join(DIR, 'private.pem');
const PUB = path.join(DIR, 'public.txt');
if (!fs.existsSync(PRIV)) {
  const { privateKey, publicKey } = crypto.generateKeyPairSync('rsa', { modulusLength: 2048 });
  fs.writeFileSync(PRIV, privateKey.export({ type: 'pkcs8', format: 'pem' }), { mode: 0o600 });
  fs.writeFileSync(PUB, publicKey.export({ type: 'spki', format: 'der' }).toString('base64'));
}
const privateKey = crypto.createPrivateKey(fs.readFileSync(PRIV));
console.log('PUBLIC KEY:\n' + fs.readFileSync(PUB, 'utf8') + '\n');

const DBFILE = path.join(DIR, 'keys.json');
let KEYS = {};
if (fs.existsSync(DBFILE)) { try { KEYS = JSON.parse(fs.readFileSync(DBFILE, 'utf8')); } catch { KEYS = {}; } }
let saveTimer = null;
function save() {
  if (saveTimer) return;
  saveTimer = setTimeout(() => {
    saveTimer = null;
    fs.writeFileSync(DBFILE + '.tmp', JSON.stringify(KEYS));
    fs.renameSync(DBFILE + '.tmp', DBFILE);
  }, 100);
}
function allKeys(filter) {
  const rows = Object.values(KEYS);
  if (filter === 'free') return rows.filter(r => !r.device && !r.revoked);
  if (filter === 'used') return rows.filter(r => r.device && !r.revoked);
  if (filter === 'revoked') return rows.filter(r => r.revoked);
  return rows;
}

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
  const row = KEYS[key];
  if (!row) return 'invalid';
  if (row.revoked) return 'revoked';
  const now = Date.now();
  if (!row.device) {
    if (!activate) return 'invalid';
    row.device = device; row.used_at = now; row.last_seen = now; save();
    return 'ok';
  }
  if (row.device !== device) return 'used';
  row.last_seen = now; save();
  return 'ok';
}

const app = express();
app.set('trust proxy', 1);
app.use(express.json({ limit: '2kb' }));
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
    const key = norm(req.body.key), device = String(req.body.device || '').slice(0, 64), nonce = String(req.body.nonce || '').slice(0, 64);
    if (!key || !device || !nonce) return res.status(400).json({ status: 'bad' });
    const status = verify(key, device, nonce, activate);
    res.json({ status, sig: sign(status, key, device, nonce) });
  };
}
app.post('/api/activate', handler(true));
app.post('/api/check', handler(false));
app.get('/', (_, res) => res.send('JZS key server OK'));
app.listen(Number(process.env.PORT || 3000), '0.0.0.0', () => console.log('API sur port', process.env.PORT || 3000));

setInterval(() => { const cutoff = Date.now() - 120000; for (const [ip, ts] of hits) { const f = ts.filter(t => t > cutoff); if (f.length) hits.set(ip, f); else hits.delete(ip); }}, 60000);

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
].map(c => c.setDefaultMemberPermissions('0').toJSON());

const client = new Client({ intents: [GatewayIntentBits.Guilds] });
client.once('ready', async () => {
  const rest = new REST().setToken(process.env.DISCORD_TOKEN);
  try { await rest.put(Routes.applicationCommands(process.env.DISCORD_CLIENT_ID), { body: commands }); console.log('Commandes enregistrees'); }
  catch (e) { console.error('Enreg. echoue:', e.message); }
  if (!ADMIN_ROLES.length) console.warn('ADMIN_ROLE_ID non defini : seul le proprietaire du serveur peut gerer les cles.');
  console.log('Bot connecte:', client.user.tag);
});

const ts = t => t ? `<t:${Math.floor(t / 1000)}:R>` : '-';
function allowed(i) {
  if (!i.guild || !i.member) return false;
  if (i.guild.ownerId && i.user && i.guild.ownerId === i.user.id) return true;
  if (!ADMIN_ROLES.length) return false;
  const r = i.member.roles;
  if (r && r.cache && typeof r.cache.has === 'function') return ADMIN_ROLES.some(id => r.cache.has(id));
  return Array.isArray(i.member.roles) && ADMIN_ROLES.some(id => i.member.roles.includes(id));
}
const PAGE = 10;
function keyLine(r) {
  const e = r.revoked ? 'X' : r.device ? 'O' : '.';
  return `${e} \`${r.key}\``;
}
function buildKeysEmbed(filtre, page) {
  const rows = allKeys(filtre).sort((a, b) => b.created_at - a.created_at);
  const total = rows.length;
  const pages = Math.max(1, Math.ceil(total / PAGE));
  page = Math.min(Math.max(0, page), pages - 1);
  const slice = rows.slice(page * PAGE, (page + 1) * PAGE);
  const names = { all: 'Toutes', free: 'Libres', used: 'Utilisees', revoked: 'Revoquees' };
  const e = new EmbedBuilder()
    .setTitle('JZS - Cles (' + (names[filtre] || 'Toutes') + ')')
    .setColor(0xFF2E90)
    .setDescription(slice.length ? slice.map(keyLine).join('\n') : '_Aucune cle._')
    .addFields({ name: 'Resume', value:
      `Total **${allKeys('all').length}** - Libres **${allKeys('free').length}** - Utilisees **${allKeys('used').length}** - Revoquees **${allKeys('revoked').length}**` })
    .setFooter({ text: `Page ${page + 1}/${pages}` });
  const row = new ActionRowBuilder().addComponents(
    new ButtonBuilder().setCustomId(`keys:${filtre}:${page - 1}`).setLabel('<').setStyle(ButtonStyle.Secondary).setDisabled(page <= 0),
    new ButtonBuilder().setCustomId(`keys:${filtre}:${page + 1}`).setLabel('>').setStyle(ButtonStyle.Secondary).setDisabled(page >= pages - 1),
  );
  return { embeds: [e], components: [row] };
}

client.on('interactionCreate', async i => {
  const eph = { flags: MessageFlags.Ephemeral };
  if (i.isButton() && i.customId.startsWith('keys:')) {
    if (!allowed(i)) return i.reply({ content: 'Reserve au staff.', ...eph });
    const [, filtre, pg] = i.customId.split(':');
    return i.update(buildKeysEmbed(filtre, parseInt(pg, 10) || 0));
  }
  if (!i.isChatInputCommand()) return;
  if (!allowed(i)) {
    console.warn(`BLOCKED ${i.user.tag} (${i.user.id}) tried /${i.commandName}`);
    return i.reply({ content: 'Tu n as pas la permission d utiliser ce bot.', ...eph });
  }
  const key = norm(i.options.getString('key'));
  const row = key ? KEYS[key] : null;
  if (key && !row) return i.reply({ content: 'Cle introuvable.', ...eph });
  switch (i.commandName) {
    case 'genkey': {
      const n = i.options.getInteger('nombre') || 1;
      const list = [];
      for (let k = 0; k < n; k++) {
        const v = newKey();
        KEYS[v] = { key: v, created_at: Date.now(), created_by: i.user.id, device: null, used_at: null, last_seen: null, revoked: false };
        list.push(v);
      }
      save();
      const e = new EmbedBuilder().setTitle(`${n} cle(s) generee(s)`).setColor(0xFF2E90)
        .setDescription('```\n' + list.join('\n') + '\n```');
      return i.reply({ embeds: [e], ...eph });
    }
    case 'revokekey':
      row.revoked = true; save();
      return i.reply({ content: `\`${key}\` revoquee. L utilisateur perd l acces sous 2 min.`, ...eph });
    case 'unrevokekey':
      row.revoked = false; save();
      return i.reply({ content: `\`${key}\` reactivee.`, ...eph });
    case 'resetkey':
      row.device = null; row.used_at = null; save();
      return i.reply({ content: `\`${key}\` deliee, utilisable sur un nouveau telephone.`, ...eph });
    case 'keyinfo': {
      const st = row.revoked ? 'Revoquee' : row.device ? 'Utilisee' : 'Libre';
      const e = new EmbedBuilder().setTitle(key).setColor(row.revoked ? 0xe74c3c : row.device ? 0x2ecc71 : 0x95a5a6).addFields(
        { name: 'Statut', value: st, inline: true },
        { name: 'Creee', value: ts(row.created_at), inline: true },
        { name: 'Par', value: row.created_by ? `<@${row.created_by}>` : '-', inline: true },
        { name: 'Activee', value: ts(row.used_at), inline: true },
        { name: 'Vue', value: ts(row.last_seen), inline: true },
        { name: 'Appareil', value: row.device ? `\`${row.device}\`` : '-', inline: true });
      return i.reply({ embeds: [e], ...eph });
    }
    case 'keys':
      return i.reply({ ...buildKeysEmbed(i.options.getString('filtre') || 'all', 0), ...eph });
  }
});
client.login(process.env.DISCORD_TOKEN).catch(e => console.error('Login echoue:', e.message));
