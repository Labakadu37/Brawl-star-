.class public final Lcom/jzs/brawl/JZSInit;
.super Ljava/lang/Object;

# Generated smali for com.jzs.brawl.JZSInit
# Regenerate from JZSInit.java with:
#   javac -source 8 -target 8 -d out mod/src/java/com/jzs/brawl/JZSInit.java
#   d8 --output out/classes.dex out/com/jzs/brawl/JZSInit.class
#   baksmali d out/classes.dex -o out/smali
# Then copy out/smali/com/jzs/brawl/JZSInit.smali here.

.field private static sLoaded:Z

.method static constructor <clinit>()V
    .registers 1
    const/4 v0, 0x0
    sput-boolean v0, Lcom/jzs/brawl/JZSInit;->sLoaded:Z
    return-void
.end method

.method private constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static declared-synchronized install(Landroid/app/Activity;)V
    .registers 3
    const-class v0, Lcom/jzs/brawl/JZSInit;
    monitor-enter v0

    :try_start
    sget-boolean v1, Lcom/jzs/brawl/JZSInit;->sLoaded:Z
    if-nez v1, :attach

    const-string v1, "jz"
    :try_load_start
    invoke-static {v1}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
    :try_load_end
    goto :mark_loaded
    .catch Ljava/lang/Throwable; {:try_load_start .. :try_load_end} :load_failed

    :load_failed
    move-exception v2
    const-string v1, "JZS"
    const-string v2, "libjz.so not loaded (non-fatal)"
    invoke-static {v1, v2}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    :mark_loaded
    const/4 v1, 0x1
    sput-boolean v1, Lcom/jzs/brawl/JZSInit;->sLoaded:Z

    :attach
    invoke-static {p0}, Lcom/jzs/brawl/JZSOverlay;->attach(Landroid/app/Activity;)V
    monitor-exit v0
    return-void
    :try_end
    .catchall {:try_start .. :try_end} :sync_fail

    :sync_fail
    move-exception v1
    monitor-exit v0
    throw v1
.end method

.method public static native nativeVersion()Ljava/lang/String;
.end method
