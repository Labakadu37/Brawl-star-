.class Llocal/bsre/filter/FilterActivity$1;
.super Ljava/lang/Object;
.source "FilterActivity.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Llocal/bsre/filter/FilterActivity;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Llocal/bsre/filter/FilterActivity;


# direct methods
.method constructor <init>(Llocal/bsre/filter/FilterActivity;)V
    .locals 0
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x8010
        }
        names = {
            null
        }
    .end annotation

    .line 14
    iput-object p1, p0, Llocal/bsre/filter/FilterActivity$1;->this$0:Llocal/bsre/filter/FilterActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .locals 4

    .line 16
    invoke-static {}, Llocal/bsre/filter/NetworkGate;->isReady()Z

    move-result v0

    if-eqz v0, :cond_0

    .line 17
    iget-object v0, p0, Llocal/bsre/filter/FilterActivity$1;->this$0:Llocal/bsre/filter/FilterActivity;

    new-instance v1, Landroid/content/Intent;

    invoke-direct {v1}, Landroid/content/Intent;-><init>()V

    iget-object v2, p0, Llocal/bsre/filter/FilterActivity$1;->this$0:Llocal/bsre/filter/FilterActivity;

    invoke-virtual {v2}, Llocal/bsre/filter/FilterActivity;->getPackageName()Ljava/lang/String;

    move-result-object v2

    const-string v3, "com.supercell.brawlstars.GameApp"

    invoke-virtual {v1, v2, v3}, Landroid/content/Intent;->setClassName(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;

    move-result-object v1

    invoke-virtual {v0, v1}, Llocal/bsre/filter/FilterActivity;->startActivity(Landroid/content/Intent;)V

    .line 19
    iget-object v0, p0, Llocal/bsre/filter/FilterActivity$1;->this$0:Llocal/bsre/filter/FilterActivity;

    invoke-virtual {v0}, Llocal/bsre/filter/FilterActivity;->finish()V

    goto :goto_0

    .line 20
    :cond_0
    iget-object v0, p0, Llocal/bsre/filter/FilterActivity$1;->this$0:Llocal/bsre/filter/FilterActivity;

    invoke-static {v0}, Llocal/bsre/filter/FilterActivity;->access$004(Llocal/bsre/filter/FilterActivity;)I

    move-result v0

    const/16 v1, 0x64

    if-ge v0, v1, :cond_1

    .line 21
    iget-object v0, p0, Llocal/bsre/filter/FilterActivity$1;->this$0:Llocal/bsre/filter/FilterActivity;

    invoke-static {v0}, Llocal/bsre/filter/FilterActivity;->access$100(Llocal/bsre/filter/FilterActivity;)Landroid/os/Handler;

    move-result-object v0

    const-wide/16 v1, 0x96

    invoke-virtual {v0, p0, v1, v2}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z

    goto :goto_0

    .line 23
    :cond_1
    iget-object v0, p0, Llocal/bsre/filter/FilterActivity$1;->this$0:Llocal/bsre/filter/FilterActivity;

    invoke-static {v0}, Llocal/bsre/filter/FilterActivity;->access$200(Llocal/bsre/filter/FilterActivity;)Landroid/widget/TextView;

    move-result-object v0

    const-string v1, "Le filtre r\u00e9seau JZS n'a pas d\u00e9marr\u00e9. Jeu arr\u00eat\u00e9. Relance l'application."

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 25
    :goto_0
    return-void
.end method
