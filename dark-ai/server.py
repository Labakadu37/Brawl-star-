#!/usr/bin/env python3
"""DARK AI - serveur local.

Sert l'interface web (dossier web/) et relaie les requêtes de chat vers
Ollama (http://127.0.0.1:11434). Aucune dépendance : Python 3.8+ suffit.
"""
import json
import os
import sys
import urllib.error
import urllib.request
import webbrowser
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer

HOST = "127.0.0.1"
PORT = int(os.environ.get("DARK_PORT", "7666"))
OLLAMA = os.environ.get("OLLAMA_HOST_URL", "http://127.0.0.1:11434")
MODEL = os.environ.get("DARK_MODEL", "dark")
WEB_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "web")


class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_DIR, **kwargs)

    def log_message(self, fmt, *args):
        pass

    def _json(self, code, obj):
        body = json.dumps(obj).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/api/models":
            try:
                with urllib.request.urlopen(OLLAMA + "/api/tags", timeout=5) as r:
                    names = [m["name"] for m in json.load(r).get("models", [])]
                self._json(200, {"default": MODEL, "models": names})
            except (urllib.error.URLError, OSError):
                self._json(503, {"error": "Ollama ne répond pas. Lance Ollama puis recharge la page."})
            return
        super().do_GET()

    def do_POST(self):
        if self.path != "/api/chat":
            self.send_error(404)
            return
        length = int(self.headers.get("Content-Length", 0))
        payload = json.loads(self.rfile.read(length) or b"{}")
        req = urllib.request.Request(
            OLLAMA + "/api/chat",
            data=json.dumps({
                "model": payload.get("model") or MODEL,
                "messages": payload.get("messages", []),
                "stream": True,
            }).encode(),
            headers={"Content-Type": "application/json"},
        )
        try:
            upstream = urllib.request.urlopen(req)
        except urllib.error.HTTPError as e:
            self._json(e.code, {"error": e.read().decode(errors="replace")})
            return
        except (urllib.error.URLError, OSError):
            self._json(503, {"error": "Ollama ne répond pas."})
            return

        self.send_response(200)
        self.send_header("Content-Type", "application/x-ndjson")
        self.send_header("Cache-Control", "no-cache")
        self.end_headers()
        try:
            with upstream:
                for line in upstream:
                    self.wfile.write(line)
                    self.wfile.flush()
        except (BrokenPipeError, ConnectionResetError):
            pass  # l'utilisateur a cliqué sur "Stop"


def main():
    server = ThreadingHTTPServer((HOST, PORT), Handler)
    url = "http://%s:%d" % (HOST, PORT)
    print("DARK AI en ligne -> " + url + "   (Ctrl+C pour arrêter)")
    if "--no-browser" not in sys.argv:
        webbrowser.open(url)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
