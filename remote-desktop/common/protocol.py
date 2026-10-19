"""Wire protocol shared by the admin and client apps.

Discovery uses UDP broadcast on the local network so the admin finds
running clients without typing an address or a code.

Control traffic uses a single TCP connection, in both directions. Each
message is length-prefixed:

    [1 byte type][4 bytes big-endian payload length][payload bytes]
"""

import json
import socket
import struct
import time

# ---- Network defaults -------------------------------------------------

DISCOVERY_PORT = 47899      # UDP: admin broadcasts here, clients answer
CONTROL_PORT = 47900        # TCP: client listens, admin connects
DISCOVERY_MAGIC = "RDESK_DISCOVER_V1"
DISCOVERY_REPLY = "RDESK_HERE_V1"
DEFAULT_TOKEN = "change-moi"

# ---- Message types ----------------------------------------------------

MSG_HELLO = 1        # admin  -> client : {"token": "...", "admin", "mode"}
MSG_WELCOME = 2      # client -> admin  : {"name": "...", "width": W, "height": H}
MSG_DENIED = 3       # client -> admin  : {"reason": "..."}
MSG_FRAME = 4        # client -> admin  : JPEG bytes
MSG_INPUT = 5        # admin  -> client : {"kind": "...", ...}
MSG_BYE = 6          # either way       : polite disconnect
MSG_COMMAND = 7      # admin  -> client : {"cmd": "message|lock|sleep|...", ...}

MAX_PAYLOAD = 32 * 1024 * 1024

_HEADER = struct.Struct(">BI")


def send_msg(sock, msg_type, payload=b""):
    if isinstance(payload, (dict, list)):
        payload = json.dumps(payload).encode("utf-8")
    elif isinstance(payload, str):
        payload = payload.encode("utf-8")
    sock.sendall(_HEADER.pack(msg_type, len(payload)) + payload)


def _recv_exactly(sock, n):
    buf = bytearray()
    while len(buf) < n:
        chunk = sock.recv(min(65536, n - len(buf)))
        if not chunk:
            raise ConnectionError("connexion fermée")
        buf.extend(chunk)
    return bytes(buf)


def recv_msg(sock):
    msg_type, length = _HEADER.unpack(_recv_exactly(sock, _HEADER.size))
    if length > MAX_PAYLOAD:
        raise ConnectionError("message trop gros (%d octets)" % length)
    payload = _recv_exactly(sock, length) if length else b""
    return msg_type, payload


def decode_json(payload):
    return json.loads(payload.decode("utf-8")) if payload else {}


def local_ip():
    """Best guess at this machine's LAN address (no packet is sent)."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("10.255.255.255", 1))
        return s.getsockname()[0]
    except OSError:
        return "127.0.0.1"
    finally:
        s.close()


# ---- Discovery ----------------------------------------------------------

def discovery_responder(name, tcp_port, stop_event):
    """Client side: answer admin broadcasts until stop_event is set."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(("", DISCOVERY_PORT))
    s.settimeout(0.5)
    reply = json.dumps({"magic": DISCOVERY_REPLY, "name": name,
                        "port": tcp_port}).encode("utf-8")
    try:
        while not stop_event.is_set():
            try:
                data, addr = s.recvfrom(4096)
            except socket.timeout:
                continue
            if data.decode("utf-8", "ignore").strip() == DISCOVERY_MAGIC:
                s.sendto(reply, addr)
    finally:
        s.close()


def discover_clients(timeout=1.5, extra_hosts=None):
    """Admin side: probe the LAN (and any saved remote hosts) for clients.

    extra_hosts is a list of hostnames/IPs to probe directly by unicast —
    used for PCs reached over a VPN like Tailscale, where UDP broadcast
    does not travel. Returns [{"name", "ip", "port"}, ...].
    """
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.settimeout(0.3)
    found = {}

    # Probe several targets so discovery works whether the client is on
    # another machine (global + subnet-directed broadcast) or on this very
    # machine for testing (loopback).
    targets = {"255.255.255.255", "127.0.0.1"}
    ip = local_ip()
    if ip.count(".") == 3 and not ip.startswith("127."):
        targets.add(ip.rsplit(".", 1)[0] + ".255")

    # Saved remote PCs (e.g. Tailscale peers) are probed directly.
    unicast = set()
    for host in (extra_hosts or []):
        host = host.strip()
        if not host:
            continue
        try:
            unicast.add(socket.gethostbyname(host))
        except OSError:
            pass

    try:
        magic = DISCOVERY_MAGIC.encode("utf-8")
        for target in targets | unicast:
            try:
                s.sendto(magic, (target, DISCOVERY_PORT))
            except OSError:
                pass
        deadline = time.time() + timeout
        while time.time() < deadline:
            try:
                data, addr = s.recvfrom(4096)
            except socket.timeout:
                continue
            try:
                info = json.loads(data.decode("utf-8"))
            except ValueError:
                continue
            if info.get("magic") != DISCOVERY_REPLY:
                continue
            port = int(info.get("port", CONTROL_PORT))
            found[(addr[0], port)] = {"name": info.get("name", addr[0]),
                                      "ip": addr[0], "port": port}
    finally:
        s.close()

    # When admin and client run on the same machine, the client answers
    # both the loopback probe and its own LAN address: drop the loopback
    # copy if the same client also replied from a real address.
    real_names = {c["name"] for c in found.values()
                  if not c["ip"].startswith("127.")}
    clients = [c for c in found.values()
               if not (c["ip"].startswith("127.") and c["name"] in real_names)]
    return sorted(clients, key=lambda c: c["name"].lower())
