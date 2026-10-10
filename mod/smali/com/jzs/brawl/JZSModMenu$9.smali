.class Lcom/jzs/brawl/JZSModMenu$9;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->buildFab(Landroid/app/Activity;F)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# direct methods
.method constructor <init>()V
    .registers 1

    .line 668
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .registers 3

    .line 670
    invoke-virtual {p1}, Landroid/view/View;->getContext()Landroid/content/Context;

    move-result-object p1

    const/16 v0, 0x28

    # invokes: Lcom/jzs/brawl/JZSModMenu;->vibrate(Landroid/content/Context;I)V
    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->access$200(Landroid/content/Context;I)V

    .line 671
    # invokes: Lcom/jzs/brawl/JZSModMenu;->togglePanel()V
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$100()V

    .line 672
    return-void
.end method
