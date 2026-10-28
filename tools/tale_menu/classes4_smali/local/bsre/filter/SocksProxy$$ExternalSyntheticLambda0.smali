.class public final synthetic Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda0;
.super Ljava/lang/Object;
.source "D8$$SyntheticClass"

# interfaces
.implements Ljava/lang/Runnable;


# instance fields
.field public final synthetic f$0:Llocal/bsre/filter/SocksProxy;

.field public final synthetic f$1:Ljava/net/Socket;


# direct methods
.method public synthetic constructor <init>(Llocal/bsre/filter/SocksProxy;Ljava/net/Socket;)V
    .locals 0

    .line 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda0;->f$0:Llocal/bsre/filter/SocksProxy;

    iput-object p2, p0, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda0;->f$1:Ljava/net/Socket;

    return-void
.end method


# virtual methods
.method public final run()V
    .locals 2

    .line 0
    iget-object v0, p0, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda0;->f$0:Llocal/bsre/filter/SocksProxy;

    iget-object v1, p0, Llocal/bsre/filter/SocksProxy$$ExternalSyntheticLambda0;->f$1:Ljava/net/Socket;

    invoke-virtual {v0, v1}, Llocal/bsre/filter/SocksProxy;->lambda$start$0$local-bsre-filter-SocksProxy(Ljava/net/Socket;)V

    return-void
.end method
