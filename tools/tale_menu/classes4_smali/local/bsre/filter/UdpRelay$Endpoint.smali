.class final Llocal/bsre/filter/UdpRelay$Endpoint;
.super Ljava/lang/Object;
.source "UdpRelay.java"

# interfaces
.implements Ljava/lang/AutoCloseable;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Llocal/bsre/filter/UdpRelay;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1a
    name = "Endpoint"
.end annotation


# instance fields
.field volatile alive:Z

.field final key:Ljava/lang/String;

.field final receiver:Ljava/lang/Thread;

.field final socket:Ljava/net/DatagramSocket;


# direct methods
.method constructor <init>(Ljava/lang/String;Ljava/net/DatagramSocket;Ljava/io/DataOutputStream;)V
    .locals 2

    .line 91
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 89
    const/4 v0, 0x1

    iput-boolean v0, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->alive:Z

    .line 92
    iput-object p1, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->key:Ljava/lang/String;

    .line 93
    iput-object p2, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->socket:Ljava/net/DatagramSocket;

    .line 94
    new-instance p1, Ljava/lang/Thread;

    new-instance v1, Llocal/bsre/filter/UdpRelay$Endpoint$0;

    invoke-direct {v1, p0, p2, p3}, Llocal/bsre/filter/UdpRelay$Endpoint$0;-><init>(Llocal/bsre/filter/UdpRelay$Endpoint;Ljava/net/DatagramSocket;Ljava/io/DataOutputStream;)V

    const-string p2, "BSRE-udp-receive"

    invoke-direct {p1, v1, p2}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;Ljava/lang/String;)V

    iput-object p1, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->receiver:Ljava/lang/Thread;

    .line 112
    iget-object p1, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->receiver:Ljava/lang/Thread;

    invoke-virtual {p1, v0}, Ljava/lang/Thread;->setDaemon(Z)V

    .line 113
    return-void
.end method


# virtual methods
.method public close()V
    .locals 3

    .line 116
    const/4 v0, 0x0

    iput-boolean v0, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->alive:Z

    .line 117
    iget-object v0, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->socket:Ljava/net/DatagramSocket;

    invoke-virtual {v0}, Ljava/net/DatagramSocket;->close()V

    .line 118
    iget-object v0, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->receiver:Ljava/lang/Thread;

    invoke-virtual {v0}, Ljava/lang/Thread;->interrupt()V

    .line 119
    :try_start_0
    iget-object v0, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->receiver:Ljava/lang/Thread;

    const-wide/16 v1, 0x5dc

    invoke-virtual {v0, v1, v2}, Ljava/lang/Thread;->join(J)V
    :try_end_0
    .catch Ljava/lang/InterruptedException; {:try_start_0 .. :try_end_0} :catch_0

    .line 120
    goto :goto_0

    :catch_0
    move-exception v0

    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/Thread;->interrupt()V

    .line 121
    :goto_0
    return-void
.end method

.method synthetic lambda$new$0$local-bsre-filter-UdpRelay$Endpoint(Ljava/net/DatagramSocket;Ljava/io/DataOutputStream;)V
    .locals 8

    .line 95
    const v0, 0xffe3

    new-array v1, v0, [B

    .line 96
    invoke-static {}, Ljava/lang/System;->nanoTime()J

    move-result-wide v2

    .line 97
    :goto_0
    iget-boolean v4, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->alive:Z

    if-eqz v4, :cond_1

    .line 99
    :try_start_0
    new-instance v4, Ljava/net/DatagramPacket;

    invoke-direct {v4, v1, v0}, Ljava/net/DatagramPacket;-><init>([BI)V

    .line 100
    invoke-virtual {p1, v4}, Ljava/net/DatagramSocket;->receive(Ljava/net/DatagramPacket;)V

    .line 101
    invoke-static {p2, v4}, Llocal/bsre/filter/UdpRelay;->write(Ljava/io/DataOutputStream;Ljava/net/DatagramPacket;)V

    .line 102
    invoke-static {}, Ljava/lang/System;->nanoTime()J

    move-result-wide v2
    :try_end_0
    .catch Ljava/net/SocketTimeoutException; {:try_start_0 .. :try_end_0} :catch_1
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_1

    .line 105
    :catch_0
    move-exception p2

    .line 106
    goto :goto_2

    .line 103
    :catch_1
    move-exception v4

    .line 104
    invoke-static {}, Ljava/lang/System;->nanoTime()J

    move-result-wide v4

    sub-long/2addr v4, v2

    const-wide/32 v6, 0xf4240

    div-long/2addr v4, v6

    const-wide/16 v6, 0x7530

    cmp-long v4, v4, v6

    if-lez v4, :cond_0

    goto :goto_2

    .line 107
    :cond_0
    :goto_1
    goto :goto_0

    .line 109
    :cond_1
    :goto_2
    const/4 p2, 0x0

    iput-boolean p2, p0, Llocal/bsre/filter/UdpRelay$Endpoint;->alive:Z

    .line 110
    invoke-virtual {p1}, Ljava/net/DatagramSocket;->close()V

    .line 111
    return-void
.end method
