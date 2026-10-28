.class public final Llocal/bsre/filter/FilterActivity;
.super Landroid/app/Activity;
.source "FilterActivity.java"


# instance fields
.field private attempts:I

.field private final handler:Landroid/os/Handler;

.field private final poll:Ljava/lang/Runnable;

.field private status:Landroid/widget/TextView;


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 10
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    .line 11
    new-instance v0, Landroid/os/Handler;

    invoke-direct {v0}, Landroid/os/Handler;-><init>()V

    iput-object v0, p0, Llocal/bsre/filter/FilterActivity;->handler:Landroid/os/Handler;

    .line 14
    new-instance v0, Llocal/bsre/filter/FilterActivity$1;

    invoke-direct {v0, p0}, Llocal/bsre/filter/FilterActivity$1;-><init>(Llocal/bsre/filter/FilterActivity;)V

    iput-object v0, p0, Llocal/bsre/filter/FilterActivity;->poll:Ljava/lang/Runnable;

    return-void
.end method

.method static synthetic access$004(Llocal/bsre/filter/FilterActivity;)I
    .locals 1

    .line 10
    iget v0, p0, Llocal/bsre/filter/FilterActivity;->attempts:I

    add-int/lit8 v0, v0, 0x1

    iput v0, p0, Llocal/bsre/filter/FilterActivity;->attempts:I

    return v0
.end method

.method static synthetic access$100(Llocal/bsre/filter/FilterActivity;)Landroid/os/Handler;
    .locals 0

    .line 10
    iget-object p0, p0, Llocal/bsre/filter/FilterActivity;->handler:Landroid/os/Handler;

    return-object p0
.end method

.method static synthetic access$200(Llocal/bsre/filter/FilterActivity;)Landroid/widget/TextView;
    .locals 0

    .line 10
    iget-object p0, p0, Llocal/bsre/filter/FilterActivity;->status:Landroid/widget/TextView;

    return-object p0
.end method

.method private prepareModules()Z
    .locals 6

    :try_start_0
    invoke-virtual {p0}, Llocal/bsre/filter/FilterActivity;->getDataDir()Ljava/io/File;

    move-result-object v0

    invoke-virtual {p0}, Llocal/bsre/filter/FilterActivity;->getAssets()Landroid/content/res/AssetManager;

    move-result-object v1

    const-string v2, "bsre-local/modules.properties"

    invoke-virtual {v1, v2}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v2

    const-string v3, "bsre-local/load.lebronjs"

    invoke-virtual {v1, v3}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v3

    const-string v4, "bsre-local/load.midnight"

    invoke-virtual {v1, v4}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v4

    invoke-static {v0, v2, v3, v4}, Llocal/bsre/filter/ModuleInstaller;->install(Ljava/io/File;Ljava/io/InputStream;Ljava/io/InputStream;Ljava/io/InputStream;)V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    const/4 v0, 0x1

    return v0

    :catch_0
    move-exception v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "Local module preparation failed: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/Exception;->getMessage()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    iget-object v1, p0, Llocal/bsre/filter/FilterActivity;->status:Landroid/widget/TextView;

    invoke-virtual {v1, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    const/4 v0, 0x0

    return v0
.end method

.method private startFilter()V
    .locals 2

    iget-object v0, p0, Llocal/bsre/filter/FilterActivity;->handler:Landroid/os/Handler;

    iget-object v1, p0, Llocal/bsre/filter/FilterActivity;->poll:Ljava/lang/Runnable;

    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    return-void
.end method


# virtual methods
.method protected onActivityResult(IILandroid/content/Intent;)V
    .locals 0

    .line 41
    invoke-super {p0, p1, p2, p3}, Landroid/app/Activity;->onActivityResult(IILandroid/content/Intent;)V

    .line 42
    const/16 p3, 0x47

    if-ne p1, p3, :cond_0

    const/4 p1, -0x1

    if-ne p2, p1, :cond_0

    invoke-direct {p0}, Llocal/bsre/filter/FilterActivity;->startFilter()V

    goto :goto_0

    .line 43
    :cond_0
    iget-object p1, p0, Llocal/bsre/filter/FilterActivity;->status:Landroid/widget/TextView;

    const-string p2, "Sans l'autorisation VPN locale, JZS ne peut pas lancer le jeu."

    invoke-virtual {p1, p2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 44
    :goto_0
    return-void
.end method

.method public onCreate(Landroid/os/Bundle;)V
    .locals 2

    .line 29
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    .line 30
    new-instance p1, Landroid/widget/TextView;

    invoke-direct {p1, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    iput-object p1, p0, Llocal/bsre/filter/FilterActivity;->status:Landroid/widget/TextView;

    .line 31
    iget-object p1, p0, Llocal/bsre/filter/FilterActivity;->status:Landroid/widget/TextView;

    const/high16 v0, 0x41900000    # 18.0f

    invoke-virtual {p1, v0}, Landroid/widget/TextView;->setTextSize(F)V

    .line 32
    iget-object p1, p0, Llocal/bsre/filter/FilterActivity;->status:Landroid/widget/TextView;

    const/16 v0, 0x20

    const/16 v1, 0x30

    invoke-virtual {p1, v0, v1, v0, v0}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 33
    iget-object p1, p0, Llocal/bsre/filter/FilterActivity;->status:Landroid/widget/TextView;

    const-string v0, "Pr\u00e9paration des modules et du filtre JZS"

    invoke-virtual {p1, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 34
    iget-object p1, p0, Llocal/bsre/filter/FilterActivity;->status:Landroid/widget/TextView;

    invoke-virtual {p0, p1}, Llocal/bsre/filter/FilterActivity;->setContentView(Landroid/view/View;)V

    .line 35
    invoke-direct {p0}, Llocal/bsre/filter/FilterActivity;->prepareModules()Z

    move-result p1

    if-nez p1, :cond_0

    return-void

    :cond_0
    const/4 p1, 0x0

    .line 36
    if-eqz p1, :cond_1

    const/16 v0, 0x47

    invoke-virtual {p0, p1, v0}, Llocal/bsre/filter/FilterActivity;->startActivityForResult(Landroid/content/Intent;I)V

    goto :goto_0

    .line 37
    :cond_1
    invoke-direct {p0}, Llocal/bsre/filter/FilterActivity;->startFilter()V

    .line 38
    :goto_0
    return-void
.end method

.method public onDestroy()V
    .locals 2

    .line 52
    iget-object v0, p0, Llocal/bsre/filter/FilterActivity;->handler:Landroid/os/Handler;

    iget-object v1, p0, Llocal/bsre/filter/FilterActivity;->poll:Ljava/lang/Runnable;

    invoke-virtual {v0, v1}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 53
    invoke-super {p0}, Landroid/app/Activity;->onDestroy()V

    .line 54
    return-void
.end method
