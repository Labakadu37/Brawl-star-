.class public final synthetic Llocal/bsre/filter/TransportDiagnostics$0;
.super Ljava/lang/Object;
.source "D8$$SyntheticClass"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation runtime Lcom/android/tools/r8/annotations/LambdaMethod;
    holder = "Llocal/bsre/filter/TransportDiagnostics;"
    method = "lambda$start$0"
    proto = "(Llocal/bsre/filter/UdpRelay$Platform;)V"
.end annotation


# instance fields
.field public final synthetic f$0:Llocal/bsre/filter/UdpRelay$Platform;


# direct methods
.method public synthetic constructor <init>(Llocal/bsre/filter/UdpRelay$Platform;)V
    .locals 0

    .line 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Llocal/bsre/filter/TransportDiagnostics$0;->f$0:Llocal/bsre/filter/UdpRelay$Platform;

    return-void
.end method


# virtual methods
.method public final run()V
    .locals 1

    .line 0
    iget-object v0, p0, Llocal/bsre/filter/TransportDiagnostics$0;->f$0:Llocal/bsre/filter/UdpRelay$Platform;

    invoke-static {v0}, Llocal/bsre/filter/TransportDiagnostics;->lambda$start$0(Llocal/bsre/filter/UdpRelay$Platform;)V

    return-void
.end method
