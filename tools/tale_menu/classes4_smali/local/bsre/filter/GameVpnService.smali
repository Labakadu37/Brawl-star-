.class public final Llocal/bsre/filter/GameVpnService;
.super Landroid/net/VpnService;
.source "GameVpnService.java"


# static fields
.field private static final TAG:Ljava/lang/String; = "BSREFilter"


# instance fields
.field private gate:Landroid/net/LocalServerSocket;

.field private proxy:Llocal/bsre/filter/SocksProxy;

.field private volatile ready:Z

.field private tunnel:Landroid/os/ParcelFileDescriptor;


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 23
    invoke-direct {p0}, Landroid/net/VpnService;-><init>()V

    return-void
.end method

.method private startFilter()V
    .locals 7
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Exception;
        }
    .end annotation

    .line 64
    invoke-virtual {p0}, Llocal/bsre/filter/GameVpnService;->getAssets()Landroid/content/res/AssetManager;

    move-result-object v0

    const-string v1, "bsre-filter-policy.json"

    invoke-virtual {v0, v1}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v0

    .line 65
    :try_start_0
    new-instance v1, Ljava/io/ByteArrayOutputStream;

    invoke-direct {v1}, Ljava/io/ByteArrayOutputStream;-><init>()V

    .line 66
    const/16 v2, 0x1000

    new-array v2, v2, [B

    .line 68
    :goto_0
    invoke-virtual {v0, v2}, Ljava/io/InputStream;->read([B)I

    move-result v3

    const/4 v4, -0x1

    const/4 v5, 0x0

    if-eq v3, v4, :cond_1

    .line 69
    invoke-virtual {v1}, Ljava/io/ByteArrayOutputStream;->size()I

    move-result v4

    add-int/2addr v4, v3

    const/16 v6, 0x4000

    if-gt v4, v6, :cond_0

    .line 70
    invoke-virtual {v1, v2, v5, v3}, Ljava/io/ByteArrayOutputStream;->write([BII)V

    goto :goto_0

    .line 69
    :cond_0
    new-instance v1, Ljava/io/IOException;

    const-string v2, "Policy too large"

    invoke-direct {v1, v2}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw v1

    .line 72
    :cond_1
    invoke-virtual {v1}, Ljava/io/ByteArrayOutputStream;->toByteArray()[B

    move-result-object v1
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_2

    .line 73
    if-eqz v0, :cond_2

    invoke-virtual {v0}, Ljava/io/InputStream;->close()V

    .line 74
    :cond_2
    new-instance v0, Lorg/json/JSONObject;

    new-instance v2, Ljava/lang/String;

    sget-object v3, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-direct {v2, v1, v3}, Ljava/lang/String;-><init>([BLjava/nio/charset/Charset;)V

    invoke-direct {v0, v2}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    .line 75
    new-instance v1, Llocal/bsre/filter/GamePolicy;

    const-string v2, "hosts"

    invoke-virtual {v0, v2}, Lorg/json/JSONObject;->getJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;

    move-result-object v2

    invoke-static {v2}, Llocal/bsre/filter/GameVpnService;->strings(Lorg/json/JSONArray;)[Ljava/lang/String;

    move-result-object v2

    .line 76
    const-string v3, "addresses"

    invoke-virtual {v0, v3}, Lorg/json/JSONObject;->getJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;

    move-result-object v3

    invoke-static {v3}, Llocal/bsre/filter/GameVpnService;->strings(Lorg/json/JSONArray;)[Ljava/lang/String;

    move-result-object v3

    const-string v4, "port"

    invoke-virtual {v0, v4}, Lorg/json/JSONObject;->getInt(Ljava/lang/String;)I

    move-result v0

    invoke-direct {v1, v2, v3, v0}, Llocal/bsre/filter/GamePolicy;-><init>([Ljava/lang/String;[Ljava/lang/String;I)V

    .line 77
    new-instance v0, Llocal/bsre/filter/SocksProxy;

    invoke-direct {v0, p0, v1}, Llocal/bsre/filter/SocksProxy;-><init>(Llocal/bsre/filter/GameVpnService;Llocal/bsre/filter/GamePolicy;)V

    iput-object v0, p0, Llocal/bsre/filter/GameVpnService;->proxy:Llocal/bsre/filter/SocksProxy;

    .line 78
    iget-object v0, p0, Llocal/bsre/filter/GameVpnService;->proxy:Llocal/bsre/filter/SocksProxy;

    invoke-virtual {v0}, Llocal/bsre/filter/SocksProxy;->start()V

    .line 79
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "tunnel:\n  mtu: 1500\n  ipv4: 10.77.0.1\n  ipv6: \'fd77::1\'\nsocks5:\n  udp: tcp\n  address: 127.0.0.1\n  port: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->proxy:Llocal/bsre/filter/SocksProxy;

    .line 80
    invoke-virtual {v1}, Llocal/bsre/filter/SocksProxy;->port()I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, "\n  username: bsre\n  password: \'"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->proxy:Llocal/bsre/filter/SocksProxy;

    .line 81
    invoke-virtual {v1}, Llocal/bsre/filter/SocksProxy;->password()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, "\'\nmapdns:\n  address: 10.77.0.2\n  port: 53\n  network: 198.18.0.0\n  netmask: 255.255.0.0\n  cache-size: 256\nmisc:\n  max-session-count: 128\n  connect-timeout: 10000\n  tcp-read-write-timeout: 300000\n  udp-read-write-timeout: 30000\n  log-file: stderr\n  log-level: warn\n"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 87
    new-instance v1, Ljava/io/File;

    invoke-virtual {p0}, Llocal/bsre/filter/GameVpnService;->getFilesDir()Ljava/io/File;

    move-result-object v2

    const-string v3, "bsre-filter.yml"

    invoke-direct {v1, v2, v3}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 88
    new-instance v2, Ljava/io/FileOutputStream;

    invoke-direct {v2, v1}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;)V

    .line 89
    :try_start_1
    sget-object v3, Ljava/nio/charset/StandardCharsets;->US_ASCII:Ljava/nio/charset/Charset;

    invoke-virtual {v0, v3}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    move-result-object v0

    invoke-virtual {v2, v0}, Ljava/io/FileOutputStream;->write([B)V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 90
    invoke-virtual {v2}, Ljava/io/FileOutputStream;->close()V

    .line 91
    new-instance v0, Landroid/net/VpnService$Builder;

    invoke-direct {v0, p0}, Landroid/net/VpnService$Builder;-><init>(Landroid/net/VpnService;)V

    const-string v2, "Brawl Stars : filtre JZS"

    invoke-virtual {v0, v2}, Landroid/net/VpnService$Builder;->setSession(Ljava/lang/String;)Landroid/net/VpnService$Builder;

    move-result-object v0

    .line 92
    const/16 v2, 0x5dc

    invoke-virtual {v0, v2}, Landroid/net/VpnService$Builder;->setMtu(I)Landroid/net/VpnService$Builder;

    move-result-object v0

    const-string v2, "10.77.0.1"

    const/16 v3, 0x20

    invoke-virtual {v0, v2, v3}, Landroid/net/VpnService$Builder;->addAddress(Ljava/lang/String;I)Landroid/net/VpnService$Builder;

    move-result-object v0

    const-string v2, "fd77::1"

    const/16 v3, 0x80

    invoke-virtual {v0, v2, v3}, Landroid/net/VpnService$Builder;->addAddress(Ljava/lang/String;I)Landroid/net/VpnService$Builder;

    move-result-object v0

    .line 93
    const-string v2, "0.0.0.0"

    invoke-virtual {v0, v2, v5}, Landroid/net/VpnService$Builder;->addRoute(Ljava/lang/String;I)Landroid/net/VpnService$Builder;

    move-result-object v0

    const-string v2, "::"

    invoke-virtual {v0, v2, v5}, Landroid/net/VpnService$Builder;->addRoute(Ljava/lang/String;I)Landroid/net/VpnService$Builder;

    move-result-object v0

    const-string v2, "10.77.0.2"

    invoke-virtual {v0, v2}, Landroid/net/VpnService$Builder;->addDnsServer(Ljava/lang/String;)Landroid/net/VpnService$Builder;

    move-result-object v0

    .line 94
    invoke-virtual {p0}, Llocal/bsre/filter/GameVpnService;->getPackageName()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v0, v2}, Landroid/net/VpnService$Builder;->addAllowedApplication(Ljava/lang/String;)Landroid/net/VpnService$Builder;

    move-result-object v0

    invoke-virtual {v0}, Landroid/net/VpnService$Builder;->establish()Landroid/os/ParcelFileDescriptor;

    move-result-object v0

    iput-object v0, p0, Llocal/bsre/filter/GameVpnService;->tunnel:Landroid/os/ParcelFileDescriptor;

    .line 95
    iget-object v0, p0, Llocal/bsre/filter/GameVpnService;->tunnel:Landroid/os/ParcelFileDescriptor;

    if-eqz v0, :cond_4

    .line 96
    invoke-virtual {v1}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v0

    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->tunnel:Landroid/os/ParcelFileDescriptor;

    invoke-virtual {v1}, Landroid/os/ParcelFileDescriptor;->getFd()I

    move-result v1

    invoke-static {v0, v1}, Lhev/htproxy/TProxyService;->TProxyStartService(Ljava/lang/String;I)Z

    move-result v0

    if-eqz v0, :cond_3

    .line 98
    new-instance v0, Landroid/net/LocalServerSocket;

    const-string v1, "com.supercell.brawlstars.bsre.filter.v1"

    invoke-direct {v0, v1}, Landroid/net/LocalServerSocket;-><init>(Ljava/lang/String;)V

    iput-object v0, p0, Llocal/bsre/filter/GameVpnService;->gate:Landroid/net/LocalServerSocket;

    .line 99
    const/4 v0, 0x1

    iput-boolean v0, p0, Llocal/bsre/filter/GameVpnService;->ready:Z

    .line 100
    new-instance v1, Ljava/lang/Thread;

    new-instance v2, Llocal/bsre/filter/GameVpnService$$ExternalSyntheticLambda0;

    invoke-direct {v2, p0}, Llocal/bsre/filter/GameVpnService$$ExternalSyntheticLambda0;-><init>(Llocal/bsre/filter/GameVpnService;)V

    const-string v3, "BSRE-filter-gate"

    invoke-direct {v1, v2, v3}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;Ljava/lang/String;)V

    .line 114
    invoke-virtual {v1, v0}, Ljava/lang/Thread;->setDaemon(Z)V

    .line 115
    invoke-virtual {v1}, Ljava/lang/Thread;->start()V

    .line 116
    const-string v0, "BSREFilter"

    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->proxy:Llocal/bsre/filter/SocksProxy;

    invoke-static {v1}, Llocal/bsre/filter/TransportDiagnostics;->start(Llocal/bsre/filter/UdpRelay$Platform;)V

    const-string v1, "VPN established: known BSRE destinations denied; other game TCP allowed"

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 117
    return-void

    .line 97
    :cond_3
    new-instance v0, Ljava/io/IOException;

    const-string v1, "TUN engine refused startup"

    invoke-direct {v0, v1}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 95
    :cond_4
    new-instance v0, Ljava/io/IOException;

    const-string v1, "VPN approval absent"

    invoke-direct {v0, v1}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 88
    :catchall_0
    move-exception v0

    :try_start_2
    invoke-virtual {v2}, Ljava/io/FileOutputStream;->close()V
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_1

    goto :goto_1

    :catchall_1
    move-exception v1

    invoke-virtual {v0, v1}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V

    :goto_1
    throw v0

    .line 64
    :catchall_2
    move-exception v1

    if-eqz v0, :cond_5

    :try_start_3
    invoke-virtual {v0}, Ljava/io/InputStream;->close()V
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_3

    goto :goto_2

    :catchall_3
    move-exception v0

    invoke-virtual {v1, v0}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V

    :cond_5
    :goto_2
    throw v1
.end method

.method private static strings(Lorg/json/JSONArray;)[Ljava/lang/String;
    .locals 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Exception;
        }
    .end annotation

    .line 57
    invoke-virtual {p0}, Lorg/json/JSONArray;->length()I

    move-result v0

    new-array v1, v0, [Ljava/lang/String;

    .line 58
    const/4 v2, 0x0

    :goto_0
    if-ge v2, v0, :cond_0

    invoke-virtual {p0, v2}, Lorg/json/JSONArray;->getString(I)Ljava/lang/String;

    move-result-object v3

    aput-object v3, v1, v2

    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 59
    :cond_0
    return-object v1
.end method


# virtual methods
.method synthetic lambda$startFilter$0$local-bsre-filter-GameVpnService()V
    .locals 5

    .line 101
    nop

    :goto_0
    iget-object v0, p0, Llocal/bsre/filter/GameVpnService;->gate:Landroid/net/LocalServerSocket;

    if-eqz v0, :cond_8

    .line 102
    :try_start_0
    iget-object v0, p0, Llocal/bsre/filter/GameVpnService;->gate:Landroid/net/LocalServerSocket;

    invoke-virtual {v0}, Landroid/net/LocalServerSocket;->accept()Landroid/net/LocalSocket;

    move-result-object v0
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0
    .catch Ljava/lang/RuntimeException; {:try_start_0 .. :try_end_0} :catch_0

    .line 103
    const/16 v1, 0x2ee

    :try_start_1
    invoke-virtual {v0, v1}, Landroid/net/LocalSocket;->setSoTimeout(I)V

    .line 104
    invoke-virtual {v0}, Landroid/net/LocalSocket;->getPeerCredentials()Landroid/net/Credentials;

    move-result-object v1

    invoke-virtual {v1}, Landroid/net/Credentials;->getUid()I

    move-result v1

    invoke-static {}, Landroid/os/Process;->myUid()I

    move-result v2
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    if-eq v1, v2, :cond_1

    .line 109
    if-eqz v0, :cond_0

    :try_start_2
    invoke-virtual {v0}, Landroid/net/LocalSocket;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_0
    .catch Ljava/lang/RuntimeException; {:try_start_2 .. :try_end_2} :catch_0

    .line 104
    :cond_0
    goto :goto_0

    .line 105
    :cond_1
    :try_start_3
    invoke-virtual {v0}, Landroid/net/LocalSocket;->getInputStream()Ljava/io/InputStream;

    move-result-object v1

    invoke-virtual {v1}, Ljava/io/InputStream;->read()I

    move-result v1
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    const/16 v2, 0x47

    if-eq v1, v2, :cond_3

    .line 109
    if-eqz v0, :cond_2

    :try_start_4
    invoke-virtual {v0}, Landroid/net/LocalSocket;->close()V
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_0
    .catch Ljava/lang/RuntimeException; {:try_start_4 .. :try_end_4} :catch_0

    .line 105
    :cond_2
    goto :goto_0

    .line 106
    :cond_3
    :try_start_5
    iget-boolean v1, p0, Llocal/bsre/filter/GameVpnService;->ready:Z

    const/4 v2, 0x1

    const/4 v3, 0x0

    if-eqz v1, :cond_4

    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->proxy:Llocal/bsre/filter/SocksProxy;

    invoke-virtual {v1}, Llocal/bsre/filter/SocksProxy;->isRunning()Z

    move-result v1

    if-eqz v1, :cond_4

    invoke-static {}, Lhev/htproxy/TProxyService;->TProxyIsRunning()Z

    move-result v1

    if-eqz v1, :cond_4

    .line 107
    invoke-static {p0}, Landroid/net/VpnService;->prepare(Landroid/content/Context;)Landroid/content/Intent;

    move-result-object v1

    if-nez v1, :cond_4

    move v1, v2

    goto :goto_1

    :cond_4
    move v1, v3

    .line 108
    :goto_1
    invoke-virtual {v0}, Landroid/net/LocalSocket;->getOutputStream()Ljava/io/OutputStream;

    move-result-object v4

    if-eqz v1, :cond_5

    goto :goto_2

    :cond_5
    move v2, v3

    :goto_2
    invoke-virtual {v4, v2}, Ljava/io/OutputStream;->write(I)V
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_0

    .line 109
    if-eqz v0, :cond_7

    :try_start_6
    invoke-virtual {v0}, Landroid/net/LocalSocket;->close()V
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_0
    .catch Ljava/lang/RuntimeException; {:try_start_6 .. :try_end_6} :catch_0

    goto :goto_4

    .line 102
    :catchall_0
    move-exception v1

    if-eqz v0, :cond_6

    :try_start_7
    invoke-virtual {v0}, Landroid/net/LocalSocket;->close()V
    :try_end_7
    .catchall {:try_start_7 .. :try_end_7} :catchall_1

    goto :goto_3

    :catchall_1
    move-exception v0

    :try_start_8
    invoke-virtual {v1, v0}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V

    :cond_6
    :goto_3
    throw v1
    :try_end_8
    .catch Ljava/io/IOException; {:try_start_8 .. :try_end_8} :catch_0
    .catch Ljava/lang/RuntimeException; {:try_start_8 .. :try_end_8} :catch_0

    .line 109
    :catch_0
    move-exception v0

    .line 110
    iget-boolean v1, p0, Llocal/bsre/filter/GameVpnService;->ready:Z

    if-eqz v1, :cond_7

    const-string v1, "BSREFilter"

    const-string v2, "Gate request failed"

    invoke-static {v1, v2, v0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 111
    :cond_7
    :goto_4
    goto :goto_0

    .line 113
    :cond_8
    return-void
.end method

.method public onCreate()V
    .locals 5

    .line 31
    invoke-super {p0}, Landroid/net/VpnService;->onCreate()V

    .line 32
    const-class v0, Landroid/app/NotificationManager;

    invoke-virtual {p0, v0}, Llocal/bsre/filter/GameVpnService;->getSystemService(Ljava/lang/Class;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/app/NotificationManager;

    .line 33
    new-instance v1, Landroid/app/NotificationChannel;

    const-string v2, "Filtre r\u00e9seau du jeu"

    const/4 v3, 0x2

    const-string v4, "bsre_filter"

    invoke-direct {v1, v4, v2, v3}, Landroid/app/NotificationChannel;-><init>(Ljava/lang/String;Ljava/lang/CharSequence;I)V

    invoke-virtual {v0, v1}, Landroid/app/NotificationManager;->createNotificationChannel(Landroid/app/NotificationChannel;)V

    .line 35
    new-instance v0, Landroid/content/Intent;

    const-class v1, Llocal/bsre/filter/FilterActivity;

    invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    const/high16 v1, 0x4000000

    const/4 v2, 0x0

    invoke-static {p0, v2, v0, v1}, Landroid/app/PendingIntent;->getActivity(Landroid/content/Context;ILandroid/content/Intent;I)Landroid/app/PendingIntent;

    move-result-object v0

    .line 37
    new-instance v1, Landroid/app/Notification$Builder;

    invoke-direct {v1, p0, v4}, Landroid/app/Notification$Builder;-><init>(Landroid/content/Context;Ljava/lang/String;)V

    .line 38
    const v2, 0x108002f

    invoke-virtual {v1, v2}, Landroid/app/Notification$Builder;->setSmallIcon(I)Landroid/app/Notification$Builder;

    move-result-object v1

    .line 39
    const-string v2, "JZS : blocage s\u00e9lectif"

    invoke-virtual {v1, v2}, Landroid/app/Notification$Builder;->setContentTitle(Ljava/lang/CharSequence;)Landroid/app/Notification$Builder;

    move-result-object v1

    .line 40
    const-string v2, "JZS actif : Supercell accessible"

    invoke-virtual {v1, v2}, Landroid/app/Notification$Builder;->setContentText(Ljava/lang/CharSequence;)Landroid/app/Notification$Builder;

    move-result-object v1

    .line 41
    invoke-virtual {v1, v0}, Landroid/app/Notification$Builder;->setContentIntent(Landroid/app/PendingIntent;)Landroid/app/Notification$Builder;

    move-result-object v0

    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Landroid/app/Notification$Builder;->setOngoing(Z)Landroid/app/Notification$Builder;

    move-result-object v0

    invoke-virtual {v0}, Landroid/app/Notification$Builder;->build()Landroid/app/Notification;

    move-result-object v0

    .line 37
    const/16 v1, 0x1be3    # 1.0004E-41f

    invoke-virtual {p0, v1, v0}, Llocal/bsre/filter/GameVpnService;->startForeground(ILandroid/app/Notification;)V

    .line 42
    return-void
.end method

.method public onDestroy()V
    .locals 2

    .line 125
    const/4 v0, 0x0

    iput-boolean v0, p0, Llocal/bsre/filter/GameVpnService;->ready:Z

    .line 127
    iget-object v0, p0, Llocal/bsre/filter/GameVpnService;->gate:Landroid/net/LocalServerSocket;

    if-eqz v0, :cond_0

    :try_start_0
    iget-object v0, p0, Llocal/bsre/filter/GameVpnService;->gate:Landroid/net/LocalServerSocket;

    invoke-virtual {v0}, Landroid/net/LocalServerSocket;->close()V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 128
    :cond_0
    :goto_0
    const/4 v0, 0x0

    iput-object v0, p0, Llocal/bsre/filter/GameVpnService;->gate:Landroid/net/LocalServerSocket;

    .line 129
    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->proxy:Llocal/bsre/filter/SocksProxy;

    if-eqz v1, :cond_1

    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->proxy:Llocal/bsre/filter/SocksProxy;

    invoke-virtual {v1}, Llocal/bsre/filter/SocksProxy;->close()V

    .line 130
    :cond_1
    :try_start_1
    invoke-static {}, Lhev/htproxy/TProxyService;->TProxyStopService()Z
    :try_end_1
    .catch Ljava/lang/LinkageError; {:try_start_1 .. :try_end_1} :catch_1

    goto :goto_1

    :catch_1
    move-exception v1

    .line 131
    :goto_1
    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->tunnel:Landroid/os/ParcelFileDescriptor;

    if-eqz v1, :cond_2

    :try_start_2
    iget-object v1, p0, Llocal/bsre/filter/GameVpnService;->tunnel:Landroid/os/ParcelFileDescriptor;

    invoke-virtual {v1}, Landroid/os/ParcelFileDescriptor;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_2

    goto :goto_2

    :catch_2
    move-exception v1

    .line 132
    :cond_2
    :goto_2
    iput-object v0, p0, Llocal/bsre/filter/GameVpnService;->tunnel:Landroid/os/ParcelFileDescriptor;

    .line 133
    invoke-super {p0}, Landroid/net/VpnService;->onDestroy()V

    .line 134
    return-void
.end method

.method public onRevoke()V
    .locals 1

    .line 120
    const/4 v0, 0x0

    iput-boolean v0, p0, Llocal/bsre/filter/GameVpnService;->ready:Z

    .line 121
    invoke-virtual {p0}, Llocal/bsre/filter/GameVpnService;->stopSelf()V

    .line 122
    return-void
.end method

.method public onStartCommand(Landroid/content/Intent;II)I
    .locals 0

    .line 45
    iget-object p1, p0, Llocal/bsre/filter/GameVpnService;->tunnel:Landroid/os/ParcelFileDescriptor;

    if-nez p1, :cond_0

    .line 46
    :try_start_0
    invoke-direct {p0}, Llocal/bsre/filter/GameVpnService;->startFilter()V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0
    .catch Ljava/lang/LinkageError; {:try_start_0 .. :try_end_0} :catch_0

    .line 51
    goto :goto_0

    .line 47
    :catch_0
    move-exception p1

    .line 49
    const/4 p2, 0x0

    iput-boolean p2, p0, Llocal/bsre/filter/GameVpnService;->ready:Z

    .line 50
    const-string p2, "BSREFilter"

    const-string p3, "Filter initialization failed; game launch refused"

    invoke-static {p2, p3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 53
    :cond_0
    :goto_0
    const/4 p1, 0x1

    return p1
.end method
