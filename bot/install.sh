#!/bin/bash
# Usage: sudo bash install.sh ton-domaine.com
set -e
DOMAIN="$1"
[ -z "$DOMAIN" ] && { echo "Usage: sudo bash install.sh ton-domaine.com"; exit 1; }
DIR="$(cd "$(dirname "$0")" && pwd)"
apt-get update
apt-get install -y curl nginx certbot python3-certbot-nginx build-essential
if ! command -v node >/dev/null || [ "$(node -v | cut -c2-3)" -lt 20 ]; then
  curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
  apt-get install -y nodejs
fi
cd "$DIR" && npm install --omit=dev
[ -f .env ] || { cp .env.example .env; echo ">>> Remplis $DIR/.env puis relance ce script"; exit 1; }
PORT="$(grep -E '^PORT=' .env | cut -d= -f2 | tr -d ' \r')"
PORT="${PORT:-3000}"
cat > /etc/systemd/system/jzs-key.service <<EOS
[Unit]
Description=JZS key server
After=network.target
[Service]
WorkingDirectory=$DIR
ExecStart=$(command -v node) index.js
Restart=always
[Install]
WantedBy=multi-user.target
EOS
cat > /etc/nginx/sites-available/jzs-key <<EOS
server {
  listen 80;
  server_name $DOMAIN;
  location / { proxy_pass http://127.0.0.1:$PORT; proxy_set_header X-Forwarded-For \$remote_addr; }
}
EOS
ln -sf /etc/nginx/sites-available/jzs-key /etc/nginx/sites-enabled/jzs-key
nginx -t && systemctl reload nginx
certbot --nginx -d "$DOMAIN" --non-interactive --agree-tos --register-unsafely-without-email --redirect
systemctl daemon-reload && systemctl enable --now jzs-key
sleep 3
echo; echo "===== CLE PUBLIQUE A ENVOYER ====="; cat "$DIR/public.txt"; echo
