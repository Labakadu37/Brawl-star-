.class public final Llocal/bsre/filter/UdpRelay;
.super Ljava/lang/Object;
.source "UdpRelay.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Llocal/bsre/filter/UdpRelay$Frame;,
        Llocal/bsre/filter/UdpRelay$Endpoint;,
        Llocal/bsre/filter/UdpRelay$Platform;
    }
.end annotation


# static fields
.field private static final IDLE_MILLIS:I = 0x7530

.field private static final MAX_DESTINATIONS:I = 0x8

.field private static final MAX_PAYLOAD:I = 0xffe3


# direct methods
.method private constructor <init>()V
    .locals 0

    .line 28
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method static read(Ljava/io/DataInputStream;)Llocal/bsre/filter/UdpRelay$Frame;
    .locals 7
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 44
    invoke-virtual {p0}, Ljava/io/DataInputStream;->readUnsignedShort()I

    move-result v0

    .line 45
    invoke-virtual {p0}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v1

    .line 46
    invoke-virtual {p0}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v2

    .line 47
    const v3, 0xffe3

    if-gt v0, v3, :cond_9

    .line 49
    const/4 v3, 0x3

    const/4 v4, 0x0

    const/4 v5, 0x1

    if-ne v2, v3, :cond_0

    move v3, v5

    goto :goto_0

    :cond_0
    move v3, v4

    .line 50
    :goto_0
    const/4 v6, 0x4

    if-ne v2, v5, :cond_1

    goto :goto_1

    .line 51
    :cond_1
    if-ne v2, v6, :cond_2

    const/16 v6, 0x10

    goto :goto_1

    .line 52
    :cond_2
    if-eqz v3, :cond_8

    invoke-virtual {p0}, Ljava/io/DataInputStream;->readUnsignedByte()I

    move-result v6

    .line 54
    :goto_1
    if-eqz v6, :cond_7

    const/16 v2, 0xfd

    if-gt v6, v2, :cond_7

    add-int/lit8 v2, v6, 0x6

    add-int/2addr v2, v3

    if-ne v1, v2, :cond_7

    .line 57
    new-array v1, v6, [B

    .line 58
    invoke-virtual {p0, v1}, Ljava/io/DataInputStream;->readFully([B)V

    .line 59
    if-eqz v3, :cond_4

    :goto_2
    if-ge v4, v6, :cond_4

    aget-byte v2, v1, v4

    .line 60
    and-int/lit16 v2, v2, 0xff

    .line 61
    const/16 v5, 0x21

    if-lt v2, v5, :cond_3

    const/16 v5, 0x7e

    if-gt v2, v5, :cond_3

    .line 59
    add-int/lit8 v4, v4, 0x1

    goto :goto_2

    .line 61
    :cond_3
    new-instance p0, Ljava/io/IOException;

    const-string v0, "Invalid UDP domain"

    invoke-direct {p0, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p0

    .line 63
    :cond_4
    if-eqz v3, :cond_5

    new-instance v2, Ljava/lang/String;

    sget-object v4, Ljava/nio/charset/StandardCharsets;->US_ASCII:Ljava/nio/charset/Charset;

    invoke-direct {v2, v1, v4}, Ljava/lang/String;-><init>([BLjava/nio/charset/Charset;)V

    goto :goto_3

    .line 64
    :cond_5
    invoke-static {v1}, Ljava/net/InetAddress;->getByAddress([B)Ljava/net/InetAddress;

    move-result-object v1

    invoke-virtual {v1}, Ljava/net/InetAddress;->getHostAddress()Ljava/lang/String;

    move-result-object v2

    .line 65
    :goto_3
    invoke-virtual {p0}, Ljava/io/DataInputStream;->readUnsignedShort()I

    move-result v1

    .line 66
    if-eqz v1, :cond_6

    .line 67
    new-array v0, v0, [B

    .line 68
    invoke-virtual {p0, v0}, Ljava/io/DataInputStream;->readFully([B)V

    .line 69
    new-instance p0, Llocal/bsre/filter/UdpRelay$Frame;

    invoke-direct {p0, v2, v1, v3, v0}, Llocal/bsre/filter/UdpRelay$Frame;-><init>(Ljava/lang/String;IZ[B)V

    return-object p0

    .line 66
    :cond_6
    new-instance p0, Ljava/io/IOException;

    const-string v0, "Invalid UDP destination port"

    invoke-direct {p0, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p0

    .line 55
    :cond_7
    new-instance p0, Ljava/io/IOException;

    const-string v0, "Invalid UDP frame header"

    invoke-direct {p0, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p0

    .line 53
    :cond_8
    new-instance p0, Ljava/io/IOException;

    const-string v0, "Unsupported UDP address type"

    invoke-direct {p0, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p0

    .line 47
    :cond_9
    new-instance p0, Ljava/io/IOException;

    const-string v0, "UDP payload too large"

    invoke-direct {p0, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p0
.end method

.method public static serve(Ljava/net/Socket;Llocal/bsre/filter/GamePolicy;Llocal/bsre/filter/UdpRelay$Platform;)V
    .locals 12
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 125
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 126
    const/16 v1, 0x7530

    invoke-virtual {p0, v1}, Ljava/net/Socket;->setSoTimeout(I)V

    .line 127
    new-instance v1, Ljava/io/DataInputStream;

    invoke-virtual {p0}, Ljava/net/Socket;->getInputStream()Ljava/io/InputStream;

    move-result-object v2

    invoke-direct {v1, v2}, Ljava/io/DataInputStream;-><init>(Ljava/io/InputStream;)V

    .line 128
    new-instance v2, Ljava/io/DataOutputStream;

    invoke-virtual {p0}, Ljava/net/Socket;->getOutputStream()Ljava/io/OutputStream;

    move-result-object v3

    invoke-direct {v2, v3}, Ljava/io/DataOutputStream;-><init>(Ljava/io/OutputStream;)V

    .line 129
    const/16 v3, 0xa

    new-array v3, v3, [B

    fill-array-data v3, :array_0

    invoke-virtual {v2, v3}, Ljava/io/DataOutputStream;->write([B)V

    .line 130
    invoke-virtual {v2}, Ljava/io/DataOutputStream;->flush()V

    .line 131
    const/4 v3, 0x0

    move v4, v3

    .line 133
    :goto_0
    :try_start_0
    invoke-virtual {p0}, Ljava/net/Socket;->isClosed()Z

    move-result v5
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    if-nez v5, :cond_9

    .line 135
    :try_start_1
    invoke-static {v1}, Llocal/bsre/filter/UdpRelay;->read(Ljava/io/DataInputStream;)Llocal/bsre/filter/UdpRelay$Frame;

    move-result-object v5
    :try_end_1
    .catch Ljava/io/EOFException; {:try_start_1 .. :try_end_1} :catch_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 136
    nop

    .line 137
    :try_start_2
    iget-object v6, v5, Llocal/bsre/filter/UdpRelay$Frame;->host:Ljava/lang/String;

    iget v7, v5, Llocal/bsre/filter/UdpRelay$Frame;->port:I

    iget-boolean v8, v5, Llocal/bsre/filter/UdpRelay$Frame;->domain:Z

    invoke-virtual {p1, v6, v7, v8}, Llocal/bsre/filter/GamePolicy;->allows(Ljava/lang/String;IZ)Z

    move-result v6
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    const-string v7, ":"

    if-nez v6, :cond_1

    .line 138
    add-int/lit8 v6, v4, 0x1

    const/4 v8, 0x5

    if-ge v4, v8, :cond_0

    :try_start_3
    sget-object v4, Ljava/lang/System;->err:Ljava/io/PrintStream;

    new-instance v8, Ljava/lang/StringBuilder;

    invoke-direct {v8}, Ljava/lang/StringBuilder;-><init>()V

    const-string v9, "BSRETransport DENY UDP "

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    iget-object v9, v5, Llocal/bsre/filter/UdpRelay$Frame;->host:Ljava/lang/String;

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    iget v5, v5, Llocal/bsre/filter/UdpRelay$Frame;->port:I

    invoke-virtual {v7, v5}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 133
    :cond_0
    move v4, v6

    goto :goto_0

    .line 141
    :cond_1
    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    iget-object v8, v5, Llocal/bsre/filter/UdpRelay$Frame;->host:Ljava/lang/String;

    invoke-virtual {v6, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    iget v8, v5, Llocal/bsre/filter/UdpRelay$Frame;->port:I

    invoke-virtual {v6, v8}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    .line 142
    nop

    .line 143
    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v8

    add-int/lit8 v8, v8, -0x1

    const/4 v9, 0x0

    :goto_1
    if-ltz v8, :cond_4

    .line 144
    invoke-interface {v0, v8}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Llocal/bsre/filter/UdpRelay$Endpoint;

    .line 145
    iget-boolean v11, v10, Llocal/bsre/filter/UdpRelay$Endpoint;->alive:Z

    if-nez v11, :cond_2

    invoke-virtual {v10}, Llocal/bsre/filter/UdpRelay$Endpoint;->close()V

    invoke-interface {v0, v8}, Ljava/util/List;->remove(I)Ljava/lang/Object;

    goto :goto_2

    .line 146
    :cond_2
    iget-object v11, v10, Llocal/bsre/filter/UdpRelay$Endpoint;->key:Ljava/lang/String;

    invoke-virtual {v11, v6}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v11

    if-eqz v11, :cond_3

    move-object v9, v10

    .line 143
    :cond_3
    :goto_2
    add-int/lit8 v8, v8, -0x1

    goto :goto_1

    .line 148
    :cond_4
    if-nez v9, :cond_8

    .line 149
    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v8

    const/16 v9, 0x8

    if-ge v8, v9, :cond_7

    .line 150
    iget-object v8, v5, Llocal/bsre/filter/UdpRelay$Frame;->host:Ljava/lang/String;

    iget-boolean v9, v5, Llocal/bsre/filter/UdpRelay$Frame;->domain:Z

    invoke-interface {p2, v8, v9}, Llocal/bsre/filter/UdpRelay$Platform;->resolve(Ljava/lang/String;Z)[Ljava/net/InetAddress;

    move-result-object v8

    .line 151
    array-length v9, v8

    if-eqz v9, :cond_6

    .line 152
    aget-object v8, v8, v3

    .line 153
    invoke-virtual {v8}, Ljava/net/InetAddress;->getHostAddress()Ljava/lang/String;

    move-result-object v9

    iget v10, v5, Llocal/bsre/filter/UdpRelay$Frame;->port:I

    invoke-virtual {p1, v9, v10, v3}, Llocal/bsre/filter/GamePolicy;->allows(Ljava/lang/String;IZ)Z

    move-result v9

    if-nez v9, :cond_5

    goto/16 :goto_0

    .line 154
    :cond_5
    invoke-interface {p2}, Llocal/bsre/filter/UdpRelay$Platform;->openDatagram()Ljava/net/DatagramSocket;

    move-result-object v9
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    .line 156
    :try_start_4
    iget v10, v5, Llocal/bsre/filter/UdpRelay$Frame;->port:I

    invoke-virtual {v9, v8, v10}, Ljava/net/DatagramSocket;->connect(Ljava/net/InetAddress;I)V

    .line 157
    const/16 v8, 0x3e8

    invoke-virtual {v9, v8}, Ljava/net/DatagramSocket;->setSoTimeout(I)V

    .line 158
    new-instance v8, Llocal/bsre/filter/UdpRelay$Endpoint;

    invoke-direct {v8, v6, v9, v2}, Llocal/bsre/filter/UdpRelay$Endpoint;-><init>(Ljava/lang/String;Ljava/net/DatagramSocket;Ljava/io/DataOutputStream;)V

    .line 159
    invoke-interface {v0, v8}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 160
    iget-object v6, v8, Llocal/bsre/filter/UdpRelay$Endpoint;->receiver:Ljava/lang/Thread;

    invoke-virtual {v6}, Ljava/lang/Thread;->start()V
    :try_end_4
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_4} :catch_0
    .catchall {:try_start_4 .. :try_end_4} :catchall_0

    .line 164
    nop

    .line 165
    :try_start_5
    sget-object v6, Ljava/lang/System;->err:Ljava/io/PrintStream;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v10, "BSRETransport ALLOW UDP "

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    iget-object v10, v5, Llocal/bsre/filter/UdpRelay$Frame;->host:Ljava/lang/String;

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    iget v9, v5, Llocal/bsre/filter/UdpRelay$Frame;->port:I

    invoke-virtual {v7, v9}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v6, v7}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    move-object v9, v8

    goto :goto_3

    .line 161
    :catch_0
    move-exception p1

    .line 162
    invoke-virtual {v9}, Ljava/net/DatagramSocket;->close()V

    .line 163
    new-instance p2, Ljava/io/IOException;

    const-string v1, "UDP socket setup failed"

    invoke-direct {p2, v1, p1}, Ljava/io/IOException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    throw p2

    .line 151
    :cond_6
    new-instance p1, Ljava/io/IOException;

    const-string p2, "No UDP address"

    invoke-direct {p1, p2}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 149
    :cond_7
    new-instance p1, Ljava/io/IOException;

    const-string p2, "UDP destination limit reached"

    invoke-direct {p1, p2}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 167
    :cond_8
    :goto_3
    iget-object v6, v9, Llocal/bsre/filter/UdpRelay$Endpoint;->socket:Ljava/net/DatagramSocket;

    new-instance v7, Ljava/net/DatagramPacket;

    iget-object v8, v5, Llocal/bsre/filter/UdpRelay$Frame;->payload:[B

    iget-object v5, v5, Llocal/bsre/filter/UdpRelay$Frame;->payload:[B

    array-length v5, v5

    invoke-direct {v7, v8, v5}, Ljava/net/DatagramPacket;-><init>([BI)V

    invoke-virtual {v6, v7}, Ljava/net/DatagramSocket;->send(Ljava/net/DatagramPacket;)V
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_0

    .line 168
    goto/16 :goto_0

    .line 136
    :catch_1
    move-exception p1

    .line 171
    :cond_9
    invoke-virtual {p0}, Ljava/net/Socket;->close()V

    .line 172
    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object p0

    :goto_4
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    move-result p1

    if-eqz p1, :cond_a

    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Llocal/bsre/filter/UdpRelay$Endpoint;

    invoke-virtual {p1}, Llocal/bsre/filter/UdpRelay$Endpoint;->close()V

    goto :goto_4

    .line 173
    :cond_a
    nop

    .line 174
    return-void

    .line 171
    :catchall_0
    move-exception p1

    invoke-virtual {p0}, Ljava/net/Socket;->close()V

    .line 172
    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object p0

    :goto_5
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    move-result p2

    if-eqz p2, :cond_b

    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object p2

    check-cast p2, Llocal/bsre/filter/UdpRelay$Endpoint;

    invoke-virtual {p2}, Llocal/bsre/filter/UdpRelay$Endpoint;->close()V

    goto :goto_5

    .line 173
    :cond_b
    throw p1

    :array_0
    .array-data 1
        0x5t
        0x0t
        0x0t
        0x1t
        0x0t
        0x0t
        0x0t
        0x0t
        0x0t
        0x0t
    .end array-data
.end method

.method static write(Ljava/io/DataOutputStream;Ljava/net/DatagramPacket;)V
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 73
    invoke-virtual {p1}, Ljava/net/DatagramPacket;->getAddress()Ljava/net/InetAddress;

    move-result-object v0

    invoke-virtual {v0}, Ljava/net/InetAddress;->getAddress()[B

    move-result-object v0

    .line 74
    monitor-enter p0

    .line 75
    :try_start_0
    invoke-virtual {p1}, Ljava/net/DatagramPacket;->getLength()I

    move-result v1

    invoke-virtual {p0, v1}, Ljava/io/DataOutputStream;->writeShort(I)V

    .line 76
    array-length v1, v0

    add-int/lit8 v1, v1, 0x6

    invoke-virtual {p0, v1}, Ljava/io/DataOutputStream;->writeByte(I)V

    .line 77
    array-length v1, v0

    const/4 v2, 0x4

    if-ne v1, v2, :cond_0

    const/4 v2, 0x1

    :cond_0
    invoke-virtual {p0, v2}, Ljava/io/DataOutputStream;->writeByte(I)V

    .line 78
    invoke-virtual {p0, v0}, Ljava/io/DataOutputStream;->write([B)V

    .line 79
    invoke-virtual {p1}, Ljava/net/DatagramPacket;->getPort()I

    move-result v0

    invoke-virtual {p0, v0}, Ljava/io/DataOutputStream;->writeShort(I)V

    .line 80
    invoke-virtual {p1}, Ljava/net/DatagramPacket;->getData()[B

    move-result-object v0

    invoke-virtual {p1}, Ljava/net/DatagramPacket;->getOffset()I

    move-result v1

    invoke-virtual {p1}, Ljava/net/DatagramPacket;->getLength()I

    move-result p1

    invoke-virtual {p0, v0, v1, p1}, Ljava/io/DataOutputStream;->write([BII)V

    .line 81
    invoke-virtual {p0}, Ljava/io/DataOutputStream;->flush()V

    .line 82
    monitor-exit p0

    .line 83
    return-void

    .line 82
    :catchall_0
    move-exception p1

    monitor-exit p0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw p1
.end method
