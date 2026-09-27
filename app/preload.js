'use strict';

const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('api', {
  login: (token) => ipcRenderer.invoke('login', token),
  getMe: (token) => ipcRenderer.invoke('get-me', token),
  getSettings: (token) => ipcRenderer.invoke('get-settings', token),
  updateProfile: (token, body) => ipcRenderer.invoke('update-profile', token, body),
  updateSettings: (token, body) => ipcRenderer.invoke('update-settings', token, body),
  setPresence: (token, presence) => ipcRenderer.invoke('set-presence', token, presence),
  saveToken: (token) => ipcRenderer.invoke('save-token', token),
  loadToken: () => ipcRenderer.invoke('load-token'),
  clearToken: () => ipcRenderer.invoke('clear-token'),
  logout: () => ipcRenderer.invoke('logout')
});
