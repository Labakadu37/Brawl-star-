.class public interface abstract Llocal/bsre/filter/UdpRelay$Platform;
.super Ljava/lang/Object;
.source "UdpRelay.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Llocal/bsre/filter/UdpRelay;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x609
    name = "Platform"
.end annotation


# virtual methods
.method public abstract connectTcp(Ljava/lang/String;IZ)Ljava/net/Socket;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation
.end method

.method public abstract openDatagram()Ljava/net/DatagramSocket;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation
.end method

.method public abstract resolve(Ljava/lang/String;Z)[Ljava/net/InetAddress;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation
.end method
