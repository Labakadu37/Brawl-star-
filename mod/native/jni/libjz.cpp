// libjz - JZS Brawl native signature stub
// Loaded by com.jzs.brawl.JZSInit at boot.
// Kept intentionally minimal: identity + version stamp only.

#include <jni.h>
#include <android/log.h>
#include <string.h>

#define JZS_TAG      "JZS"
#define JZS_MOD_NAME "JZS Brawl"
#define JZS_VERSION  "JZS V6.0"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  JZS_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* /*reserved*/) {
    LOGI("libjz loaded - %s %s", JZS_MOD_NAME, JZS_VERSION);
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_jzs_brawl_JZSInit_nativeVersion(JNIEnv* env, jclass /*clazz*/) {
    return env->NewStringUTF(JZS_VERSION);
}

// ---------------------------------------------------------------------------
// Match server info (shown under the badge as "ip:port")
//
// Walks this process's own file descriptors and reads the peer address of
// the game's sockets with getpeername(). Nothing is hooked or patched: it is
// the same info `netstat` would show for the app's own connections.
//   - UDP socket with a remote peer  -> battle server ("U ip:port")
//   - TCP socket to port 9339        -> lobby server  ("T ip:port")
// DNS (53), NTP (123) and QUIC/HTTPS (443) sockets are ignored.
// ---------------------------------------------------------------------------

#include <arpa/inet.h>
#include <dirent.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <unistd.h>

static bool jz_peer(int fd, char* out, size_t outLen, int* port) {
    sockaddr_storage ss;
    socklen_t len = sizeof(ss);
    if (getpeername(fd, reinterpret_cast<sockaddr*>(&ss), &len) != 0) return false;

    if (ss.ss_family == AF_INET) {
        auto* a = reinterpret_cast<sockaddr_in*>(&ss);
        if (ntohl(a->sin_addr.s_addr) >> 24 == 127) return false;
        inet_ntop(AF_INET, &a->sin_addr, out, outLen);
        *port = ntohs(a->sin_port);
        return true;
    }
    if (ss.ss_family == AF_INET6) {
        auto* a = reinterpret_cast<sockaddr_in6*>(&ss);
        if (IN6_IS_ADDR_LOOPBACK(&a->sin6_addr)) return false;
        if (IN6_IS_ADDR_V4MAPPED(&a->sin6_addr)) {
            in_addr v4;
            memcpy(&v4, &a->sin6_addr.s6_addr[12], 4);
            if (ntohl(v4.s_addr) >> 24 == 127) return false;
            inet_ntop(AF_INET, &v4, out, outLen);
        } else {
            inet_ntop(AF_INET6, &a->sin6_addr, out, outLen);
        }
        *port = ntohs(a->sin6_port);
        return true;
    }
    return false;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_jzs_brawl_JZSNetInfo_nativeServer(JNIEnv* env, jclass /*clazz*/) {
    char battle[80] = "";
    char lobby[80] = "";

    DIR* dir = opendir("/proc/self/fd");
    if (dir != nullptr) {
        dirent* e;
        char link[64];
        char path[64];
        while ((e = readdir(dir)) != nullptr && battle[0] == '\0') {
            if (e->d_name[0] < '0' || e->d_name[0] > '9') continue;
            snprintf(path, sizeof(path), "/proc/self/fd/%s", e->d_name);
            ssize_t n = readlink(path, link, sizeof(link) - 1);
            if (n <= 0) continue;
            link[n] = '\0';
            if (strncmp(link, "socket:", 7) != 0) continue;

            int fd = atoi(e->d_name);
            int type = 0;
            socklen_t tl = sizeof(type);
            if (getsockopt(fd, SOL_SOCKET, SO_TYPE, &type, &tl) != 0) continue;

            char ip[INET6_ADDRSTRLEN];
            int port = 0;
            if (!jz_peer(fd, ip, sizeof(ip), &port)) continue;

            if (type == SOCK_DGRAM && port != 53 && port != 123 && port != 443) {
                snprintf(battle, sizeof(battle), strchr(ip, ':') ? "U [%s]:%d" : "U %s:%d", ip, port);
            } else if (type == SOCK_STREAM && port == 9339 && lobby[0] == '\0') {
                snprintf(lobby, sizeof(lobby), strchr(ip, ':') ? "T [%s]:%d" : "T %s:%d", ip, port);
            }
        }
        closedir(dir);
    }
    return env->NewStringUTF(battle[0] ? battle : lobby);
}
