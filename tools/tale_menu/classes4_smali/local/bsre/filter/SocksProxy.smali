.class final Llocal/bsre/filter/SocksProxy;
.super Ljava/lang/Object;
.source "SocksProxy.java"

# interfaces
.implements Ljava/io/Closeable;
.implements Llocal/bsre/filter/UdpRelay$Platform;


# instance fields
.field private final denials:Ljava/util/concurrent/atomic/AtomicInteger;

.field private final listener:Ljava/net/ServerSocket;

.field private final policy:Llocal/bsre/filter/GamePolicy;

.field private volatile running:Z

.field private final secret:Ljava/lang/String;

.field private final service:Llocal/bsre/filter/GameVpnService;

.field private final slots:Ljava/util/concurrent/Semaphore;

.field private final sockets:Ljava/util/Set;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Set<",
            "Ljava/net/Socket;",
            ">;"
        }
    .end annotation
.end field


# direct methods
.method constructor <init>(Llocal/bsre/filter/GameVpnService;Llocal/bsre/filter/GamePolicy;)V
    .locals 7
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 37
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 32
    new-instance v0, Ljava/util/concurrent/Semaphore;

    const/16 v1, 0x40

    invoke-direct {v0, v1}, Ljava/util/concurrent/Semaphore;-><init>(I)V

    iput-object v0, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    .line 33
    new-instance v0, Ljava/util/HashSet;

    invoke-direct {v0}, Ljava/util/HashSet;-><init>()V

    invoke-static {v0}, Ljava/util/Collections;->synchronizedSet(Ljava/util/Set;)Ljava/util/Set;

    move-result-object v0

    iput-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    .line 34
    new-instance v0, Ljava/util/concurrent/atomic/AtomicInteger;

    invoke-direct {v0}, Ljava/util/concurrent/atomic/AtomicInteger;-><init>()V

    iput-object v0, p0, Llocal/bsre/filter/SocksProxy;->denials:Ljava/util/concurrent/atomic/AtomicInteger;

    .line 38
    iput-object p1, p0, Llocal/bsre/filter/SocksProxy;->service:Llocal/bsre/filter/GameVpnService;

    .line 39
    iput-object p2, p0, Llocal/bsre/filter/SocksProxy;->policy:Llocal/bsre/filter/GamePolicy;

    .line 40
    const/16 p1, 0x18

    new-array p2, p1, [B

    .line 41
    new-instance v0, Ljava/security/SecureRandom;

    invoke-direct {v0}, Ljava/security/SecureRandom;-><init>()V

    invoke-virtual {v0, p2}, Ljava/security/SecureRandom;->nextBytes([B)V

    .line 42
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    .line 43
    const/4 v2, 0x0

    move v3, v2

    :goto_0
    if-ge v3, p1, :cond_0

    aget-byte v4, p2, v3

    sget-object v5, Ljava/util/Locale;->ROOT:Ljava/util/Locale;

    and-int/lit16 v4, v4, 0xff

    invoke-static {v4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    filled-new-array {v4}, [Ljava/lang/Object;

    move-result-object v4

    const-string v6, "%02x"

    invoke-static {v5, v6, v4}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v3, v3, 0x1

    goto :goto_0

    .line 44
    :cond_0
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    iput-object p1, p0, Llocal/bsre/filter/SocksProxy;->secret:Ljava/lang/String;

    .line 45
    new-instance p1, Ljava/net/ServerSocket;

    invoke-direct {p1}, Ljava/net/ServerSocket;-><init>()V

    iput-object p1, p0, Llocal/bsre/filter/SocksProxy;->listener:Ljava/net/ServerSocket;

    .line 46
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->listener:Ljava/net/ServerSocket;

    new-instance p2, Ljava/net/InetSocketAddress;

    const/4 v0, 0x4

    new-array v0, v0, [B

    fill-array-data v0, :array_0

    invoke-static {v0}, Ljava/net/InetAddress;->getByAddress([B)Ljava/net/InetAddress;

    move-result-object v0

    invoke-direct {p2, v0, v2}, Ljava/net/InetSocketAddress;-><init>(Ljava/net/InetAddress;I)V

    invoke-virtual {p1, p2, v1}, Ljava/net/ServerSocket;->bind(Ljava/net/SocketAddress;I)V

    .line 47
    return-void

    nop

    :array_0
    .array-data 1
        0x7ft
        0x0t
        0x0t
        0x1t
    .end array-data
.end method

.method private authenticate(Ljava/io/DataInputStream;Ljava/io/OutputStream;)Z
    .locals 9
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 86
    invoke-virtual {p1}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v0

    const/4 v1, 0x0

    const/4 v2, 0x5

    if-eq v0, v2, :cond_0

    return v1

    .line 87
    :cond_0
    invoke-virtual {p1}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v0

    .line 88
    if-eqz v0, :cond_8

    const/16 v3, 0x20

    if-le v0, v3, :cond_1

    goto/16 :goto_5

    .line 89
    :cond_1
    invoke-static {p1, v0}, Llocal/bsre/filter/SocksProxy;->field(Ljava/io/DataInputStream;I)[B

    move-result-object v0

    .line 90
    nop

    .line 91
    array-length v3, v0

    move v4, v1

    move v5, v4

    :goto_0
    const/4 v6, 0x2

    const/4 v7, 0x1

    if-ge v4, v3, :cond_3

    aget-byte v8, v0, v4

    if-ne v8, v6, :cond_2

    goto :goto_1

    :cond_2
    move v7, v1

    :goto_1
    or-int/2addr v5, v7

    add-int/lit8 v4, v4, 0x1

    goto :goto_0

    .line 92
    :cond_3
    new-array v0, v6, [B

    aput-byte v2, v0, v1

    if-eqz v5, :cond_4

    move v2, v6

    goto :goto_2

    :cond_4
    const/4 v2, -0x1

    :goto_2
    aput-byte v2, v0, v7

    invoke-virtual {p2, v0}, Ljava/io/OutputStream;->write([B)V

    .line 93
    invoke-virtual {p2}, Ljava/io/OutputStream;->flush()V

    .line 94
    if-eqz v5, :cond_7

    invoke-virtual {p1}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v0

    if-eq v0, v7, :cond_5

    goto :goto_4

    .line 95
    :cond_5
    invoke-virtual {p1}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v0

    invoke-static {p1, v0}, Llocal/bsre/filter/SocksProxy;->field(Ljava/io/DataInputStream;I)[B

    move-result-object v0

    .line 96
    invoke-virtual {p1}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v2

    invoke-static {p1, v2}, Llocal/bsre/filter/SocksProxy;->field(Ljava/io/DataInputStream;I)[B

    move-result-object p1

    .line 97
    const-string v2, "bsre"

    sget-object v3, Ljava/nio/charset/StandardCharsets;->US_ASCII:Ljava/nio/charset/Charset;

    invoke-virtual {v2, v3}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    move-result-object v2

    invoke-static {v0, v2}, Ljava/security/MessageDigest;->isEqual([B[B)Z

    move-result v0

    if-eqz v0, :cond_6

    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->secret:Ljava/lang/String;

    sget-object v2, Ljava/nio/charset/StandardCharsets;->US_ASCII:Ljava/nio/charset/Charset;

    .line 98
    invoke-virtual {v0, v2}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    move-result-object v0

    invoke-static {p1, v0}, Ljava/security/MessageDigest;->isEqual([B[B)Z

    move-result p1

    if-eqz p1, :cond_6

    move p1, v7

    goto :goto_3

    :cond_6
    move p1, v1

    .line 99
    :goto_3
    new-array v0, v6, [B

    aput-byte v7, v0, v1

    xor-int/lit8 v1, p1, 0x1

    aput-byte v1, v0, v7

    invoke-virtual {p2, v0}, Ljava/io/OutputStream;->write([B)V

    .line 100
    invoke-virtual {p2}, Ljava/io/OutputStream;->flush()V

    .line 101
    return p1

    .line 94
    :cond_7
    :goto_4
    return v1

    .line 88
    :cond_8
    :goto_5
    return v1
.end method

.method private connect(Ljava/lang/String;IZ)Ljava/net/Socket;
    .locals 6
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 119
    invoke-direct {p0}, Llocal/bsre/filter/SocksProxy;->physicalNetwork()Landroid/net/Network;

    move-result-object v0

    .line 120
    if-eqz p3, :cond_0

    invoke-virtual {v0, p1}, Landroid/net/Network;->getAllByName(Ljava/lang/String;)[Ljava/net/InetAddress;

    move-result-object p1

    goto :goto_0

    .line 121
    :cond_0
    invoke-static {p1}, Ljava/net/InetAddress;->getByName(Ljava/lang/String;)Ljava/net/InetAddress;

    move-result-object p1

    filled-new-array {p1}, [Ljava/net/InetAddress;

    move-result-object p1

    .line 122
    :goto_0
    new-instance p3, Ljava/io/IOException;

    const-string v1, "No game address"

    invoke-direct {p3, v1}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    .line 123
    array-length v1, p1

    const/4 v2, 0x0

    move v3, v2

    :goto_1
    if-ge v3, v1, :cond_2

    aget-object p3, p1, v3

    .line 124
    new-instance v4, Ljava/net/Socket;

    invoke-direct {v4}, Ljava/net/Socket;-><init>()V

    .line 127
    :try_start_0
    new-instance v5, Ljava/net/InetSocketAddress;

    invoke-direct {v5, v2}, Ljava/net/InetSocketAddress;-><init>(I)V

    invoke-virtual {v4, v5}, Ljava/net/Socket;->bind(Ljava/net/SocketAddress;)V

    .line 129
    iget-object v5, p0, Llocal/bsre/filter/SocksProxy;->service:Llocal/bsre/filter/GameVpnService;

    invoke-virtual {v5, v4}, Llocal/bsre/filter/GameVpnService;->protect(Ljava/net/Socket;)Z

    move-result v5

    if-eqz v5, :cond_1

    .line 130
    invoke-virtual {v0, v4}, Landroid/net/Network;->bindSocket(Ljava/net/Socket;)V

    .line 131
    const v5, 0x493e0

    invoke-virtual {v4, v5}, Ljava/net/Socket;->setSoTimeout(I)V

    .line 132
    const/4 v5, 0x1

    invoke-virtual {v4, v5}, Ljava/net/Socket;->setTcpNoDelay(Z)V

    .line 133
    new-instance v5, Ljava/net/InetSocketAddress;

    invoke-direct {v5, p3, p2}, Ljava/net/InetSocketAddress;-><init>(Ljava/net/InetAddress;I)V

    const/16 p3, 0x2710

    invoke-virtual {v4, v5, p3}, Ljava/net/Socket;->connect(Ljava/net/SocketAddress;I)V

    .line 134
    iget-object p3, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {p3, v4}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 135
    return-object v4

    .line 129
    :cond_1
    new-instance p3, Ljava/io/IOException;

    const-string v5, "VPN socket protection refused"

    invoke-direct {p3, v5}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p3
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    .line 136
    :catch_0
    move-exception p3

    .line 137
    nop

    .line 138
    :try_start_1
    invoke-virtual {v4}, Ljava/net/Socket;->close()V
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_1

    goto :goto_2

    :catch_1
    move-exception v4

    .line 123
    :goto_2
    add-int/lit8 v3, v3, 0x1

    goto :goto_1

    .line 141
    :cond_2
    throw p3
.end method

.method private static field(Ljava/io/DataInputStream;I)[B
    .locals 0
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 75
    new-array p1, p1, [B

    .line 76
    invoke-virtual {p0, p1}, Ljava/io/DataInputStream;->readFully([B)V

    .line 77
    return-object p1
.end method

.method static synthetic lambda$serve$2(Ljava/net/Socket;Ljava/net/Socket;)V
    .locals 0

    .line 194
    invoke-static {p0, p1}, Llocal/bsre/filter/SocksProxy;->pump(Ljava/net/Socket;Ljava/net/Socket;)V

    return-void
.end method

.method private physicalNetwork()Landroid/net/Network;
    .locals 3

    :try_start_0
    invoke-direct {p0}, Llocal/bsre/filter/SocksProxy;->resolvePhysicalNetwork()Landroid/net/Network;

    move-result-object v0
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    return-object v0

    :catch_0
    move-exception v0

    new-instance v1, Ljava/io/IOException;

    const-string v2, "Physical network lookup failed"

    invoke-direct {v1, v2, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    throw v1
.end method

.method private static pump(Ljava/net/Socket;Ljava/net/Socket;)V
    .locals 0

    invoke-static {p0, p1}, Llocal/bsre/filter/TransportDiagnostics;->pump(Ljava/net/Socket;Ljava/net/Socket;)V

    return-void
.end method

.method private static reply(Ljava/io/OutputStream;I)V
    .locals 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 81
    int-to-byte p1, p1

    const/16 v0, 0xa

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/4 v2, 0x5

    aput-byte v2, v0, v1

    const/4 v3, 0x1

    aput-byte p1, v0, v3

    const/4 p1, 0x2

    aput-byte v1, v0, p1

    const/4 p1, 0x3

    aput-byte v3, v0, p1

    const/4 p1, 0x4

    aput-byte v1, v0, p1

    aput-byte v1, v0, v2

    const/4 p1, 0x6

    aput-byte v1, v0, p1

    const/4 p1, 0x7

    aput-byte v1, v0, p1

    const/16 p1, 0x8

    aput-byte v1, v0, p1

    const/16 p1, 0x9

    aput-byte v1, v0, p1

    invoke-virtual {p0, v0}, Ljava/io/OutputStream;->write([B)V

    .line 82
    invoke-virtual {p0}, Ljava/io/OutputStream;->flush()V

    .line 83
    return-void
.end method

.method private resolvePhysicalNetwork()Landroid/net/Network;
    .locals 8
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 105
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->service:Llocal/bsre/filter/GameVpnService;

    invoke-virtual {v0}, Llocal/bsre/filter/GameVpnService;->getPackageName()Ljava/lang/String;

    move-result-object v1

    const/4 v2, 0x0

    invoke-virtual {v0, v1, v2}, Llocal/bsre/filter/GameVpnService;->createPackageContext(Ljava/lang/String;I)Landroid/content/Context;

    move-result-object v0

    const-class v1, Landroid/net/ConnectivityManager;

    invoke-virtual {v0, v1}, Landroid/content/Context;->getSystemService(Ljava/lang/Class;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/net/ConnectivityManager;

    .line 106
    nop

    .line 107
    invoke-virtual {v0}, Landroid/net/ConnectivityManager;->getAllNetworks()[Landroid/net/Network;

    move-result-object v1

    array-length v2, v1

    const/4 v3, 0x0

    const/4 v4, 0x0

    :goto_0
    if-ge v4, v2, :cond_3

    aget-object v5, v1, v4

    .line 108
    invoke-virtual {v0, v5}, Landroid/net/ConnectivityManager;->getNetworkCapabilities(Landroid/net/Network;)Landroid/net/NetworkCapabilities;

    move-result-object v6

    .line 109
    if-eqz v6, :cond_2

    const/4 v7, 0x4

    invoke-virtual {v6, v7}, Landroid/net/NetworkCapabilities;->hasTransport(I)Z

    move-result v7

    if-nez v7, :cond_2

    .line 110
    const/16 v7, 0xc

    invoke-virtual {v6, v7}, Landroid/net/NetworkCapabilities;->hasCapability(I)Z

    move-result v7

    if-nez v7, :cond_0

    goto :goto_1

    .line 111
    :cond_0
    const/16 v3, 0x10

    invoke-virtual {v6, v3}, Landroid/net/NetworkCapabilities;->hasCapability(I)Z

    move-result v3

    if-eqz v3, :cond_1

    return-object v5

    .line 112
    :cond_1
    move-object v3, v5

    .line 107
    :cond_2
    :goto_1
    add-int/lit8 v4, v4, 0x1

    goto :goto_0

    .line 114
    :cond_3
    if-eqz v3, :cond_4

    .line 115
    return-object v3

    .line 114
    :cond_4
    new-instance v0, Ljava/io/IOException;

    const-string v1, "No physical network"

    invoke-direct {v0, v1}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw v0
.end method

.method private serve(Ljava/net/Socket;)V
    .locals 13

    .line 159
    const-string v0, "BSREFilter"

    .line 160
    nop

    .line 162
    const/16 v1, 0x1f40

    const/4 v2, 0x0

    :try_start_0
    invoke-virtual {p1, v1}, Ljava/net/Socket;->setSoTimeout(I)V

    .line 163
    const/4 v1, 0x1

    invoke-virtual {p1, v1}, Ljava/net/Socket;->setTcpNoDelay(Z)V

    .line 164
    new-instance v3, Ljava/io/DataInputStream;

    invoke-virtual {p1}, Ljava/net/Socket;->getInputStream()Ljava/io/InputStream;

    move-result-object v4

    invoke-direct {v3, v4}, Ljava/io/DataInputStream;-><init>(Ljava/io/InputStream;)V

    .line 165
    invoke-virtual {p1}, Ljava/net/Socket;->getOutputStream()Ljava/io/OutputStream;

    move-result-object v4

    .line 166
    invoke-direct {p0, v3, v4}, Llocal/bsre/filter/SocksProxy;->authenticate(Ljava/io/DataInputStream;Ljava/io/OutputStream;)Z

    move-result v5
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_10
    .catch Ljava/lang/InterruptedException; {:try_start_0 .. :try_end_0} :catch_d
    .catchall {:try_start_0 .. :try_end_0} :catchall_2

    if-nez v5, :cond_0

    .line 204
    :try_start_1
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 205
    :goto_0
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    nop

    .line 210
    nop

    .line 211
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 166
    return-void

    .line 167
    :cond_0
    :try_start_2
    invoke-virtual {v3}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v5
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_10
    .catch Ljava/lang/InterruptedException; {:try_start_2 .. :try_end_2} :catch_d
    .catchall {:try_start_2 .. :try_end_2} :catchall_2

    const/4 v6, 0x5

    if-eq v5, v6, :cond_1

    .line 204
    :try_start_3
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_3
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_1

    goto :goto_1

    :catch_1
    move-exception v0

    .line 205
    :goto_1
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    nop

    .line 210
    nop

    .line 211
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 167
    return-void

    .line 168
    :cond_1
    :try_start_4
    invoke-virtual {v3}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v5

    .line 169
    invoke-virtual {v3}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v6
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_10
    .catch Ljava/lang/InterruptedException; {:try_start_4 .. :try_end_4} :catch_d
    .catchall {:try_start_4 .. :try_end_4} :catchall_2

    if-eqz v6, :cond_2

    .line 204
    :try_start_5
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_2

    goto :goto_2

    :catch_2
    move-exception v0

    .line 205
    :goto_2
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    nop

    .line 210
    nop

    .line 211
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 169
    return-void

    .line 170
    :cond_2
    :try_start_6
    invoke-virtual {v3}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v6

    .line 172
    const/4 v7, 0x3

    const/4 v8, 0x0

    if-ne v6, v7, :cond_3

    move v7, v1

    goto :goto_3

    :cond_3
    move v7, v8

    .line 173
    :goto_3
    const/4 v9, 0x4

    if-ne v6, v1, :cond_4

    invoke-static {v3, v9}, Llocal/bsre/filter/SocksProxy;->field(Ljava/io/DataInputStream;I)[B

    move-result-object v6

    invoke-static {v6}, Ljava/net/InetAddress;->getByAddress([B)Ljava/net/InetAddress;

    move-result-object v6

    invoke-virtual {v6}, Ljava/net/InetAddress;->getHostAddress()Ljava/lang/String;

    move-result-object v6

    goto :goto_7

    .line 174
    :cond_4
    if-ne v6, v9, :cond_5

    const/16 v6, 0x10

    invoke-static {v3, v6}, Llocal/bsre/filter/SocksProxy;->field(Ljava/io/DataInputStream;I)[B

    move-result-object v6

    invoke-static {v6}, Ljava/net/InetAddress;->getByAddress([B)Ljava/net/InetAddress;

    move-result-object v6

    invoke-virtual {v6}, Ljava/net/InetAddress;->getHostAddress()Ljava/lang/String;

    move-result-object v6

    goto :goto_7

    .line 175
    :cond_5
    if-eqz v7, :cond_10

    .line 176
    invoke-virtual {v3}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v6

    .line 177
    if-eqz v6, :cond_f

    const/16 v9, 0xfd

    if-le v6, v9, :cond_6

    goto/16 :goto_f

    .line 178
    :cond_6
    invoke-static {v3, v6}, Llocal/bsre/filter/SocksProxy;->field(Ljava/io/DataInputStream;I)[B

    move-result-object v6

    .line 179
    array-length v9, v6

    move v10, v8

    :goto_4
    if-ge v10, v9, :cond_9

    aget-byte v11, v6, v10
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_10
    .catch Ljava/lang/InterruptedException; {:try_start_6 .. :try_end_6} :catch_d
    .catchall {:try_start_6 .. :try_end_6} :catchall_2

    and-int/lit16 v11, v11, 0xff

    const/16 v12, 0x21

    if-lt v11, v12, :cond_8

    const/16 v12, 0x7e

    if-le v11, v12, :cond_7

    goto :goto_5

    :cond_7
    add-int/lit8 v10, v10, 0x1

    goto :goto_4

    .line 204
    :cond_8
    :goto_5
    :try_start_7
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_7
    .catch Ljava/io/IOException; {:try_start_7 .. :try_end_7} :catch_3

    goto :goto_6

    :catch_3
    move-exception v0

    .line 205
    :goto_6
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    nop

    .line 210
    nop

    .line 211
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 179
    return-void

    .line 180
    :cond_9
    :try_start_8
    new-instance v9, Ljava/lang/String;

    sget-object v10, Ljava/nio/charset/StandardCharsets;->US_ASCII:Ljava/nio/charset/Charset;

    invoke-direct {v9, v6, v10}, Ljava/lang/String;-><init>([BLjava/nio/charset/Charset;)V

    .line 181
    move-object v6, v9

    .line 182
    :goto_7
    invoke-virtual {v3}, Ljava/io/DataInputStream;->readUnsignedShort()I

    move-result v3

    const/4 v9, 0x5

    if-ne v5, v9, :cond_a

    iget-object v10, p0, Llocal/bsre/filter/SocksProxy;->policy:Llocal/bsre/filter/GamePolicy;

    invoke-static {p1, v10, p0}, Llocal/bsre/filter/UdpRelay;->serve(Ljava/net/Socket;Llocal/bsre/filter/GamePolicy;Llocal/bsre/filter/UdpRelay$Platform;)V

    iget-object v10, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v10, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    iget-object v10, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {v10}, Ljava/util/concurrent/Semaphore;->release()V

    return-void
    :try_end_8
    .catch Ljava/io/IOException; {:try_start_8 .. :try_end_8} :catch_10
    .catch Ljava/lang/InterruptedException; {:try_start_8 .. :try_end_8} :catch_d
    .catchall {:try_start_8 .. :try_end_8} :catchall_2

    .line 183
    :cond_a
    const-string v9, ":"

    if-ne v5, v1, :cond_d

    :try_start_9
    iget-object v10, p0, Llocal/bsre/filter/SocksProxy;->policy:Llocal/bsre/filter/GamePolicy;

    invoke-virtual {v10, v6, v3, v7}, Llocal/bsre/filter/GamePolicy;->allows(Ljava/lang/String;IZ)Z

    move-result v10

    if-nez v10, :cond_b

    goto/16 :goto_d

    .line 189
    :cond_b
    invoke-direct {p0, v6, v3, v7}, Llocal/bsre/filter/SocksProxy;->connect(Ljava/lang/String;IZ)Ljava/net/Socket;

    move-result-object v5
    :try_end_9
    .catch Ljava/io/IOException; {:try_start_9 .. :try_end_9} :catch_10
    .catch Ljava/lang/InterruptedException; {:try_start_9 .. :try_end_9} :catch_d
    .catchall {:try_start_9 .. :try_end_9} :catchall_2

    .line 190
    :try_start_a
    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    const-string v10, "ALLOW "

    invoke-virtual {v7, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v0, v3}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 191
    invoke-static {v4, v8}, Llocal/bsre/filter/SocksProxy;->reply(Ljava/io/OutputStream;I)V

    .line 192
    const v3, 0x493e0

    invoke-virtual {p1, v3}, Ljava/net/Socket;->setSoTimeout(I)V

    .line 193
    nop

    .line 194
    new-instance v3, Ljava/lang/Thread;

    new-instance v4, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda1;

    invoke-direct {v4, v5, p1}, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda1;-><init>(Ljava/net/Socket;Ljava/net/Socket;)V

    const-string v6, "BSRE-game-receive"

    invoke-direct {v3, v4, v6}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;Ljava/lang/String;)V
    :try_end_a
    .catch Ljava/io/IOException; {:try_start_a .. :try_end_a} :catch_9
    .catch Ljava/lang/InterruptedException; {:try_start_a .. :try_end_a} :catch_8
    .catchall {:try_start_a .. :try_end_a} :catchall_1

    .line 195
    :try_start_b
    invoke-virtual {v3, v1}, Ljava/lang/Thread;->setDaemon(Z)V

    .line 196
    invoke-virtual {v3}, Ljava/lang/Thread;->start()V

    .line 197
    invoke-static {p1, v5}, Llocal/bsre/filter/SocksProxy;->pump(Ljava/net/Socket;Ljava/net/Socket;)V

    .line 198
    const-wide/32 v1, 0x493e0

    invoke-virtual {v3, v1, v2}, Ljava/lang/Thread;->join(J)V
    :try_end_b
    .catch Ljava/io/IOException; {:try_start_b .. :try_end_b} :catch_7
    .catch Ljava/lang/InterruptedException; {:try_start_b .. :try_end_b} :catch_6
    .catchall {:try_start_b .. :try_end_b} :catchall_0

    .line 204
    :try_start_c
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_c
    .catch Ljava/io/IOException; {:try_start_c .. :try_end_c} :catch_4

    goto :goto_8

    :catch_4
    move-exception v0

    .line 205
    :goto_8
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    if-eqz v5, :cond_c

    .line 207
    :try_start_d
    invoke-virtual {v5}, Ljava/net/Socket;->close()V
    :try_end_d
    .catch Ljava/io/IOException; {:try_start_d .. :try_end_d} :catch_5

    goto :goto_9

    :catch_5
    move-exception p1

    .line 208
    :goto_9
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {p1, v5}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 210
    :cond_c
    goto/16 :goto_15

    .line 204
    :catchall_0
    move-exception v0

    goto :goto_a

    .line 201
    :catch_6
    move-exception v0

    goto :goto_b

    .line 199
    :catch_7
    move-exception v1

    goto :goto_c

    .line 204
    :catchall_1
    move-exception v0

    move-object v3, v2

    :goto_a
    move-object v2, v5

    goto/16 :goto_1a

    .line 201
    :catch_8
    move-exception v0

    move-object v3, v2

    :goto_b
    move-object v2, v5

    goto/16 :goto_12

    .line 199
    :catch_9
    move-exception v1

    move-object v3, v2

    :goto_c
    move-object v2, v5

    goto/16 :goto_16

    .line 184
    :cond_d
    :goto_d
    :try_start_e
    iget-object v1, p0, Llocal/bsre/filter/SocksProxy;->denials:Ljava/util/concurrent/atomic/AtomicInteger;

    invoke-virtual {v1}, Ljava/util/concurrent/atomic/AtomicInteger;->getAndIncrement()I

    move-result v1

    const/16 v7, 0x64

    if-ge v1, v7, :cond_e

    .line 185
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "DENY "

    invoke-virtual {v1, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v3, " command="

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v5}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 186
    :cond_e
    const/4 v1, 0x2

    invoke-static {v4, v1}, Llocal/bsre/filter/SocksProxy;->reply(Ljava/io/OutputStream;I)V
    :try_end_e
    .catch Ljava/io/IOException; {:try_start_e .. :try_end_e} :catch_10
    .catch Ljava/lang/InterruptedException; {:try_start_e .. :try_end_e} :catch_d
    .catchall {:try_start_e .. :try_end_e} :catchall_2

    .line 204
    :try_start_f
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_f
    .catch Ljava/io/IOException; {:try_start_f .. :try_end_f} :catch_a

    goto :goto_e

    :catch_a
    move-exception v0

    .line 205
    :goto_e
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    nop

    .line 210
    nop

    .line 211
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 187
    return-void

    .line 204
    :cond_f
    :goto_f
    :try_start_10
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_10
    .catch Ljava/io/IOException; {:try_start_10 .. :try_end_10} :catch_b

    goto :goto_10

    :catch_b
    move-exception v0

    .line 205
    :goto_10
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    nop

    .line 210
    nop

    .line 211
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 177
    return-void

    .line 181
    :cond_10
    const/16 v1, 0x8

    :try_start_11
    invoke-static {v4, v1}, Llocal/bsre/filter/SocksProxy;->reply(Ljava/io/OutputStream;I)V
    :try_end_11
    .catch Ljava/io/IOException; {:try_start_11 .. :try_end_11} :catch_10
    .catch Ljava/lang/InterruptedException; {:try_start_11 .. :try_end_11} :catch_d
    .catchall {:try_start_11 .. :try_end_11} :catchall_2

    .line 204
    :try_start_12
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_12
    .catch Ljava/io/IOException; {:try_start_12 .. :try_end_12} :catch_c

    goto :goto_11

    :catch_c
    move-exception v0

    .line 205
    :goto_11
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    nop

    .line 210
    nop

    .line 211
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 181
    return-void

    .line 204
    :catchall_2
    move-exception v0

    move-object v3, v2

    goto :goto_1a

    .line 201
    :catch_d
    move-exception v0

    move-object v3, v2

    .line 202
    :goto_12
    :try_start_13
    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/Thread;->interrupt()V
    :try_end_13
    .catchall {:try_start_13 .. :try_end_13} :catchall_3

    .line 204
    :try_start_14
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_14
    .catch Ljava/io/IOException; {:try_start_14 .. :try_end_14} :catch_e

    goto :goto_13

    :catch_e
    move-exception v0

    .line 205
    :goto_13
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    if-eqz v2, :cond_11

    .line 207
    :try_start_15
    invoke-virtual {v2}, Ljava/net/Socket;->close()V
    :try_end_15
    .catch Ljava/io/IOException; {:try_start_15 .. :try_end_15} :catch_f

    goto :goto_14

    :catch_f
    move-exception p1

    .line 208
    :goto_14
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {p1, v2}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 210
    :cond_11
    if-eqz v3, :cond_12

    :goto_15
    invoke-virtual {v3}, Ljava/lang/Thread;->interrupt()V

    .line 211
    :cond_12
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 212
    goto :goto_19

    .line 204
    :catchall_3
    move-exception v0

    goto :goto_1a

    .line 199
    :catch_10
    move-exception v1

    move-object v3, v2

    .line 200
    :goto_16
    :try_start_16
    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "Game connection ended: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v4

    invoke-virtual {v1}, Ljava/io/IOException;->getMessage()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v4

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v0, v4, v1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_16
    .catchall {:try_start_16 .. :try_end_16} :catchall_3

    .line 204
    :try_start_17
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_17
    .catch Ljava/io/IOException; {:try_start_17 .. :try_end_17} :catch_11

    goto :goto_17

    :catch_11
    move-exception v0

    .line 205
    :goto_17
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    if-eqz v2, :cond_13

    .line 207
    :try_start_18
    invoke-virtual {v2}, Ljava/net/Socket;->close()V
    :try_end_18
    .catch Ljava/io/IOException; {:try_start_18 .. :try_end_18} :catch_12

    goto :goto_18

    :catch_12
    move-exception p1

    .line 208
    :goto_18
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {p1, v2}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 210
    :cond_13
    if-eqz v3, :cond_12

    goto :goto_15

    .line 213
    :goto_19
    return-void

    .line 204
    :goto_1a
    :try_start_19
    invoke-virtual {p1}, Ljava/net/Socket;->close()V
    :try_end_19
    .catch Ljava/io/IOException; {:try_start_19 .. :try_end_19} :catch_13

    goto :goto_1b

    :catch_13
    move-exception v1

    .line 205
    :goto_1b
    iget-object v1, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v1, p1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 206
    if-eqz v2, :cond_14

    .line 207
    :try_start_1a
    invoke-virtual {v2}, Ljava/net/Socket;->close()V
    :try_end_1a
    .catch Ljava/io/IOException; {:try_start_1a .. :try_end_1a} :catch_14

    goto :goto_1c

    :catch_14
    move-exception p1

    .line 208
    :goto_1c
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {p1, v2}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 210
    :cond_14
    if-eqz v3, :cond_15

    invoke-virtual {v3}, Ljava/lang/Thread;->interrupt()V

    .line 211
    :cond_15
    iget-object p1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {p1}, Ljava/util/concurrent/Semaphore;->release()V

    .line 212
    throw v0
.end method


# virtual methods
.method public close()V
    .locals 3

    .line 216
    const/4 v0, 0x0

    iput-boolean v0, p0, Llocal/bsre/filter/SocksProxy;->running:Z

    .line 217
    :try_start_0
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->listener:Ljava/net/ServerSocket;

    invoke-virtual {v0}, Ljava/net/ServerSocket;->close()V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 218
    :goto_0
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    monitor-enter v0

    .line 219
    :try_start_1
    iget-object v1, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v1}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v1

    :goto_1
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_0

    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Ljava/net/Socket;
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    :try_start_2
    invoke-virtual {v2}, Ljava/net/Socket;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_1
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    goto :goto_1

    :catch_1
    move-exception v2

    goto :goto_1

    .line 220
    :cond_0
    :try_start_3
    iget-object v1, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v1}, Ljava/util/Set;->clear()V

    .line 221
    monitor-exit v0

    .line 222
    return-void

    .line 221
    :catchall_0
    move-exception v1

    monitor-exit v0
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    throw v1
.end method

.method public connectTcp(Ljava/lang/String;IZ)Ljava/net/Socket;
    .locals 1

    invoke-direct {p0, p1, p2, p3}, Llocal/bsre/filter/SocksProxy;->connect(Ljava/lang/String;IZ)Ljava/net/Socket;

    move-result-object v0

    return-object v0
.end method

.method isRunning()Z
    .locals 1

    .line 51
    iget-boolean v0, p0, Llocal/bsre/filter/SocksProxy;->running:Z

    return v0
.end method

.method synthetic lambda$start$0$local-bsre-filter-SocksProxy(Ljava/net/Socket;)V
    .locals 0

    .line 61
    invoke-direct {p0, p1}, Llocal/bsre/filter/SocksProxy;->serve(Ljava/net/Socket;)V

    return-void
.end method

.method synthetic lambda$start$1$local-bsre-filter-SocksProxy()V
    .locals 3

    .line 56
    nop

    :goto_0
    iget-boolean v0, p0, Llocal/bsre/filter/SocksProxy;->running:Z

    if-eqz v0, :cond_2

    .line 58
    :try_start_0
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->listener:Ljava/net/ServerSocket;

    invoke-virtual {v0}, Ljava/net/ServerSocket;->accept()Ljava/net/Socket;

    move-result-object v0

    .line 59
    iget-object v1, p0, Llocal/bsre/filter/SocksProxy;->slots:Ljava/util/concurrent/Semaphore;

    invoke-virtual {v1}, Ljava/util/concurrent/Semaphore;->tryAcquire()Z

    move-result v1

    if-nez v1, :cond_0

    invoke-virtual {v0}, Ljava/net/Socket;->close()V

    goto :goto_0

    .line 60
    :cond_0
    iget-object v1, p0, Llocal/bsre/filter/SocksProxy;->sockets:Ljava/util/Set;

    invoke-interface {v1, v0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 61
    new-instance v1, Ljava/lang/Thread;

    new-instance v2, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda0;

    invoke-direct {v2, p0, v0}, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda0;-><init>(Llocal/bsre/filter/SocksProxy;Ljava/net/Socket;)V

    const-string v0, "BSRE-game-connection"

    invoke-direct {v1, v2, v0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;Ljava/lang/String;)V

    .line 62
    const/4 v0, 0x1

    invoke-virtual {v1, v0}, Ljava/lang/Thread;->setDaemon(Z)V

    .line 63
    invoke-virtual {v1}, Ljava/lang/Thread;->start()V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_1

    .line 64
    :catch_0
    move-exception v0

    .line 65
    iget-boolean v1, p0, Llocal/bsre/filter/SocksProxy;->running:Z

    if-eqz v1, :cond_1

    const-string v1, "BSREFilter"

    const-string v2, "Proxy accept failed"

    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 66
    :cond_1
    const/4 v0, 0x0

    iput-boolean v0, p0, Llocal/bsre/filter/SocksProxy;->running:Z

    .line 67
    :goto_1
    goto :goto_0

    .line 69
    :cond_2
    return-void
.end method

.method public openDatagram()Ljava/net/DatagramSocket;
    .locals 4

    new-instance v0, Ljava/net/DatagramSocket;

    invoke-direct {v0}, Ljava/net/DatagramSocket;-><init>()V

    :try_start_0
    iget-object v1, p0, Llocal/bsre/filter/SocksProxy;->service:Llocal/bsre/filter/GameVpnService;

    invoke-virtual {v1, v0}, Llocal/bsre/filter/GameVpnService;->protect(Ljava/net/DatagramSocket;)Z

    move-result v1

    if-eqz v1, :cond_0

    invoke-direct {p0}, Llocal/bsre/filter/SocksProxy;->physicalNetwork()Landroid/net/Network;

    move-result-object v1

    invoke-virtual {v1, v0}, Landroid/net/Network;->bindSocket(Ljava/net/DatagramSocket;)V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    return-object v0

    :cond_0
    new-instance v1, Ljava/io/IOException;

    const-string v2, "VPN datagram protection refused"

    invoke-direct {v1, v2}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0}, Ljava/net/DatagramSocket;->close()V

    throw v1

    :catch_0
    move-exception v1

    invoke-virtual {v0}, Ljava/net/DatagramSocket;->close()V

    new-instance v2, Ljava/io/IOException;

    const-string v3, "Physical UDP setup failed"

    invoke-direct {v2, v3, v1}, Ljava/io/IOException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    throw v2
.end method

.method password()Ljava/lang/String;
    .locals 1

    .line 50
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->secret:Ljava/lang/String;

    return-object v0
.end method

.method port()I
    .locals 1

    .line 49
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy;->listener:Ljava/net/ServerSocket;

    invoke-virtual {v0}, Ljava/net/ServerSocket;->getLocalPort()I

    move-result v0

    return v0
.end method

.method public resolve(Ljava/lang/String;Z)[Ljava/net/InetAddress;
    .locals 2

    if-eqz p2, :cond_0

    invoke-direct {p0}, Llocal/bsre/filter/SocksProxy;->physicalNetwork()Landroid/net/Network;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/net/Network;->getAllByName(Ljava/lang/String;)[Ljava/net/InetAddress;

    move-result-object v0

    return-object v0

    :cond_0
    const/4 v0, 0x1

    new-array v0, v0, [Ljava/net/InetAddress;

    invoke-static {p1}, Ljava/net/InetAddress;->getByName(Ljava/lang/String;)Ljava/net/InetAddress;

    move-result-object p1

    const/4 v1, 0x0

    aput-object p1, v0, v1

    return-object v0
.end method

.method start()V
    .locals 4

    .line 54
    const/4 v0, 0x1

    iput-boolean v0, p0, Llocal/bsre/filter/SocksProxy;->running:Z

    .line 55
    new-instance v1, Ljava/lang/Thread;

    new-instance v2, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda2;

    invoke-direct {v2, p0}, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda2;-><init>(Llocal/bsre/filter/SocksProxy;)V

    const-string v3, "BSRE-game-proxy"

    invoke-direct {v1, v2, v3}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;Ljava/lang/String;)V

    .line 70
    invoke-virtual {v1, v0}, Ljava/lang/Thread;->setDaemon(Z)V

    .line 71
    invoke-virtual {v1}, Ljava/lang/Thread;->start()V

    .line 72
    return-void
.end method
