#!/data/data/com.termux/files/usr/bin/bash
# Lance le bot JZS Brawl sur Termux et le relance s'il crash.
cd "$(dirname "$0")"
termux-wake-lock 2>/dev/null || true
while true; do
    python bot.py
    echo "Bot arrete, redemarrage dans 5s... (Ctrl+C pour quitter)"
    sleep 5
done
