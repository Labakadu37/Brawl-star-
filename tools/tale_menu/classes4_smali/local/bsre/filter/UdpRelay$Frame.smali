.class final Llocal/bsre/filter/UdpRelay$Frame;
.super Ljava/lang/Object;
.source "UdpRelay.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Llocal/bsre/filter/UdpRelay;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x18
    name = "Frame"
.end annotation


# instance fields
.field final domain:Z

.field final host:Ljava/lang/String;

.field final payload:[B

.field final port:I


# direct methods
.method constructor <init>(Ljava/lang/String;IZ[B)V
    .locals 0

    .line 35
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 36
    iput-object p1, p0, Llocal/bsre/filter/UdpRelay$Frame;->host:Ljava/lang/String;

    .line 37
    iput p2, p0, Llocal/bsre/filter/UdpRelay$Frame;->port:I

    .line 38
    iput-boolean p3, p0, Llocal/bsre/filter/UdpRelay$Frame;->domain:Z

    .line 39
    iput-object p4, p0, Llocal/bsre/filter/UdpRelay$Frame;->payload:[B

    .line 40
    return-void
.end method
