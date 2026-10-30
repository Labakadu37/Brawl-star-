require('dotenv').config();
const { REST, Routes } = require('discord.js');
const rest = new REST().setToken(process.env.DISCORD_TOKEN);
rest.put(Routes.applicationCommands(process.env.DISCORD_CLIENT_ID), { body: [] })
  .then(() => console.log('Vieilles commandes globales effacees'))
  .catch(e => console.error('Erreur:', e.message));
