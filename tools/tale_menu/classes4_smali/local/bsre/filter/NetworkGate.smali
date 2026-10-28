.class public final Llocal/bsre/filter/NetworkGate;
.super Ljava/lang/Object;
.source "NetworkGate.java"


# static fields
.field static final ADDRESS:Ljava/lang/String; = "com.supercell.brawlstars.bsre.filter.v1"


# direct methods
.method private constructor <init>()V
    .locals 0

    .line 11
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static awaitReady()Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method

.method public static isReady()Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method

.method static synthetic lambda$watch$0()V
    .locals 2

    .line 28
    nop

    :cond_0
    :goto_0
    const-wide/16 v0, 0x96

    :try_start_0
    invoke-static {v0, v1}, Ljava/lang/Thread;->sleep(J)V
    :try_end_0
    .catch Ljava/lang/InterruptedException; {:try_start_0 .. :try_end_0} :catch_0

    .line 29
    goto :goto_1

    :catch_0
    move-exception v0

    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/Thread;->interrupt()V

    .line 30
    :goto_1
    invoke-static {}, Llocal/bsre/filter/NetworkGate;->isReady()Z

    move-result v0

    if-nez v0, :cond_0

    invoke-static {}, Landroid/os/Process;->myPid()I

    move-result v0

    invoke-static {v0}, Landroid/os/Process;->killProcess(I)V

    goto :goto_0
.end method

.method static watch()V
    .locals 0

    return-void
.end method
