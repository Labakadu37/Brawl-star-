.class public final synthetic Llocal/bsre/filter/GameVpnService$$ExternalSyntheticLambda0;
.super Ljava/lang/Object;
.source "D8$$SyntheticClass"

# interfaces
.implements Ljava/lang/Runnable;


# instance fields
.field public final synthetic f$0:Llocal/bsre/filter/GameVpnService;


# direct methods
.method public synthetic constructor <init>(Llocal/bsre/filter/GameVpnService;)V
    .locals 0

    .line 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Llocal/bsre/filter/GameVpnService$$ExternalSyntheticLambda0;->f$0:Llocal/bsre/filter/GameVpnService;

    return-void
.end method


# virtual methods
.method public final run()V
    .locals 1

    .line 0
    iget-object v0, p0, Llocal/bsre/filter/GameVpnService$$ExternalSyntheticLambda0;->f$0:Llocal/bsre/filter/GameVpnService;

    invoke-virtual {v0}, Llocal/bsre/filter/GameVpnService;->lambda$startFilter$0$local-bsre-filter-GameVpnService()V

    return-void
.end method
