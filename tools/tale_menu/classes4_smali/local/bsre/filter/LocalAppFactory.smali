.class public final Llocal/bsre/filter/LocalAppFactory;
.super Landroid/app/AppComponentFactory;


# instance fields
.field private blocked:Z


# direct methods
.method public constructor <init>()V
    .locals 3

    invoke-direct {p0}, Landroid/app/AppComponentFactory;-><init>()V

    invoke-static {}, Landroid/app/Application;->getProcessName()Ljava/lang/String;

    move-result-object v0

    const-string v1, ":bsre_filter"

    invoke-virtual {v0, v1}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    :cond_0
    invoke-static {}, Llocal/bsre/filter/NetworkGate;->awaitReady()Z

    move-result v0

    if-nez v0, :cond_1

    const/4 v0, 0x1

    iput-boolean v0, p0, Llocal/bsre/filter/LocalAppFactory;->blocked:Z

    const-string v0, "BSREFilter"

    const-string v1, "Recovery gate: original application and components withheld"

    invoke-static {v0, v1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    :cond_1
    invoke-static {}, Llocal/bsre/filter/NetworkGate;->watch()V

    :try_start_0
    const-string v0, "androidx.core.app.CoreComponentFactory"

    const-class v1, Llocal/bsre/filter/LocalAppFactory;

    invoke-virtual {v1}, Ljava/lang/Class;->getClassLoader()Ljava/lang/ClassLoader;

    move-result-object v1

    const/4 v2, 0x1

    invoke-static {v0, v2, v1}, Ljava/lang/Class;->forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;
    :try_end_0
    .catch Ljava/lang/ClassNotFoundException; {:try_start_0 .. :try_end_0} :catch_0

    return-void

    :catch_0
    move-exception v0

    new-instance v1, Ljava/lang/IllegalStateException;

    const-string v2, "Original component factory absent"

    invoke-direct {v1, v2, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    throw v1
.end method


# virtual methods
.method public instantiateActivity(Ljava/lang/ClassLoader;Ljava/lang/String;Landroid/content/Intent;)Landroid/app/Activity;
    .locals 1

    iget-boolean v0, p0, Llocal/bsre/filter/LocalAppFactory;->blocked:Z

    if-eqz v0, :cond_0

    new-instance v0, Llocal/bsre/filter/RecoveryActivity;

    invoke-direct {v0}, Llocal/bsre/filter/RecoveryActivity;-><init>()V

    return-object v0

    :cond_0
    invoke-super {p0, p1, p2, p3}, Landroid/app/AppComponentFactory;->instantiateActivity(Ljava/lang/ClassLoader;Ljava/lang/String;Landroid/content/Intent;)Landroid/app/Activity;

    move-result-object v0

    return-object v0
.end method

.method public instantiateApplication(Ljava/lang/ClassLoader;Ljava/lang/String;)Landroid/app/Application;
    .locals 2

    iget-boolean v0, p0, Llocal/bsre/filter/LocalAppFactory;->blocked:Z

    if-nez v0, :cond_0

    invoke-static {}, Landroid/app/Application;->getProcessName()Ljava/lang/String;

    move-result-object v0

    const-string v1, ":bsre_filter"

    invoke-virtual {v0, v1}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z

    move-result v0

    if-nez v0, :cond_0

    invoke-static {}, Llocal/bsre/filter/NetworkGate;->isReady()Z

    move-result v0

    if-nez v0, :cond_1

    const/4 v0, 0x1

    iput-boolean v0, p0, Llocal/bsre/filter/LocalAppFactory;->blocked:Z

    :cond_0
    new-instance v0, Landroid/app/Application;

    invoke-direct {v0}, Landroid/app/Application;-><init>()V

    return-object v0

    :cond_1
    invoke-super {p0, p1, p2}, Landroid/app/AppComponentFactory;->instantiateApplication(Ljava/lang/ClassLoader;Ljava/lang/String;)Landroid/app/Application;

    move-result-object v0

    return-object v0
.end method

.method public instantiateProvider(Ljava/lang/ClassLoader;Ljava/lang/String;)Landroid/content/ContentProvider;
    .locals 1

    iget-boolean v0, p0, Llocal/bsre/filter/LocalAppFactory;->blocked:Z

    if-eqz v0, :cond_0

    new-instance v0, Llocal/bsre/filter/RecoveryProvider;

    invoke-direct {v0}, Llocal/bsre/filter/RecoveryProvider;-><init>()V

    return-object v0

    :cond_0
    invoke-super {p0, p1, p2}, Landroid/app/AppComponentFactory;->instantiateProvider(Ljava/lang/ClassLoader;Ljava/lang/String;)Landroid/content/ContentProvider;

    move-result-object v0

    return-object v0
.end method

.method public instantiateReceiver(Ljava/lang/ClassLoader;Ljava/lang/String;Landroid/content/Intent;)Landroid/content/BroadcastReceiver;
    .locals 1

    iget-boolean v0, p0, Llocal/bsre/filter/LocalAppFactory;->blocked:Z

    if-eqz v0, :cond_0

    new-instance v0, Llocal/bsre/filter/RecoveryReceiver;

    invoke-direct {v0}, Llocal/bsre/filter/RecoveryReceiver;-><init>()V

    return-object v0

    :cond_0
    invoke-super {p0, p1, p2, p3}, Landroid/app/AppComponentFactory;->instantiateReceiver(Ljava/lang/ClassLoader;Ljava/lang/String;Landroid/content/Intent;)Landroid/content/BroadcastReceiver;

    move-result-object v0

    return-object v0
.end method

.method public instantiateService(Ljava/lang/ClassLoader;Ljava/lang/String;Landroid/content/Intent;)Landroid/app/Service;
    .locals 1

    iget-boolean v0, p0, Llocal/bsre/filter/LocalAppFactory;->blocked:Z

    if-eqz v0, :cond_0

    new-instance v0, Llocal/bsre/filter/RecoveryService;

    invoke-direct {v0}, Llocal/bsre/filter/RecoveryService;-><init>()V

    return-object v0

    :cond_0
    invoke-super {p0, p1, p2, p3}, Landroid/app/AppComponentFactory;->instantiateService(Ljava/lang/ClassLoader;Ljava/lang/String;Landroid/content/Intent;)Landroid/app/Service;

    move-result-object v0

    return-object v0
.end method
