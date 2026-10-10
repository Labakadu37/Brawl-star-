.class public final Lcom/jzs/brawl/JZSInit;
.super Ljava/lang/Object;
.source "JZSInit.java"


# static fields
.field private static sLoaded:Z


# direct methods
.method private constructor <init>()V
    .registers 1

    .line 9
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static declared-synchronized install(Landroid/app/Activity;)V
    .registers 6

    const-class v0, Lcom/jzs/brawl/JZSInit;

    monitor-enter v0

    .line 12
    :try_start_3
    sget-boolean v1, Lcom/jzs/brawl/JZSInit;->sLoaded:Z
    :try_end_5
    .catchall {:try_start_3 .. :try_end_5} :catchall_3c

    if-nez v1, :cond_34

    .line 14
    :try_start_7
    const-string v1, "jz"

    invoke-static {v1}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
    :try_end_c
    .catchall {:try_start_7 .. :try_end_c} :catchall_d

    .line 17
    goto :goto_2a

    .line 15
    :catchall_d
    move-exception v1

    .line 16
    :try_start_e
    const-string v2, "JZS"

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "libjz.so not loaded (non-fatal): "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v1}, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v2, v1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 18
    :goto_2a
    const/4 v1, 0x1

    sput-boolean v1, Lcom/jzs/brawl/JZSInit;->sLoaded:Z

    .line 19
    const-string v1, "JZS"

    const-string v2, "JZS Brawl init - 5.2"

    invoke-static {v1, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 21
    :cond_34
    invoke-static {p0}, Lcom/jzs/brawl/JZSOverlay;->attach(Landroid/app/Activity;)V

    .line 22
    invoke-static {p0}, Lcom/jzs/brawl/JZSModMenu;->attach(Landroid/app/Activity;)V
    :try_end_3a
    .catchall {:try_start_e .. :try_end_3a} :catchall_3c

    .line 23
    monitor-exit v0

    return-void

    .line 11
    :catchall_3c
    move-exception p0

    monitor-exit v0

    throw p0
.end method

.method public static native nativeVersion()Ljava/lang/String;
.end method
