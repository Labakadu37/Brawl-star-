.class public final synthetic Llocal/bsre/filter/UdpRelay$Endpoint$0;
.super Ljava/lang/Object;
.source "D8$$SyntheticClass"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation runtime Lcom/android/tools/r8/annotations/LambdaMethod;
    holder = "Llocal/bsre/filter/UdpRelay$Endpoint;"
    method = "lambda$new$0"
    proto = "(Ljava/net/DatagramSocket;Ljava/io/DataOutputStream;)V"
.end annotation


# instance fields
.field public final synthetic f$0:Llocal/bsre/filter/UdpRelay$Endpoint;

.field public final synthetic f$1:Ljava/net/DatagramSocket;

.field public final synthetic f$2:Ljava/io/DataOutputStream;


# direct methods
.method public synthetic constructor <init>(Llocal/bsre/filter/UdpRelay$Endpoint;Ljava/net/DatagramSocket;Ljava/io/DataOutputStream;)V
    .locals 0

    .line 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Llocal/bsre/filter/UdpRelay$Endpoint$0;->f$0:Llocal/bsre/filter/UdpRelay$Endpoint;

    iput-object p2, p0, Llocal/bsre/filter/UdpRelay$Endpoint$0;->f$1:Ljava/net/DatagramSocket;

    iput-object p3, p0, Llocal/bsre/filter/UdpRelay$Endpoint$0;->f$2:Ljava/io/DataOutputStream;

    return-void
.end method


# virtual methods
.method public final run()V
    .locals 3

    .line 0
    iget-object v0, p0, Llocal/bsre/filter/UdpRelay$Endpoint$0;->f$0:Llocal/bsre/filter/UdpRelay$Endpoint;

    iget-object v1, p0, Llocal/bsre/filter/UdpRelay$Endpoint$0;->f$1:Ljava/net/DatagramSocket;

    iget-object v2, p0, Llocal/bsre/filter/UdpRelay$Endpoint$0;->f$2:Ljava/io/DataOutputStream;

    invoke-virtual {v0, v1, v2}, Llocal/bsre/filter/UdpRelay$Endpoint;->lambda$new$0$local-bsre-filter-UdpRelay$Endpoint(Ljava/net/DatagramSocket;Ljava/io/DataOutputStream;)V

    return-void
.end method
