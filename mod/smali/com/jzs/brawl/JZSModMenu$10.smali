.class Lcom/jzs/brawl/JZSModMenu$10;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->togglePanel()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# direct methods
.method constructor <init>()V
    .registers 1

    .line 686
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .registers 3

    .line 688
    # getter for: Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1300()Landroid/widget/FrameLayout;

    move-result-object v0

    const/16 v1, 0x8

    invoke-virtual {v0, v1}, Landroid/widget/FrameLayout;->setVisibility(I)V

    .line 689
    # getter for: Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1400()Landroid/view/View;

    move-result-object v0

    invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V

    .line 690
    # getter for: Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1500()Landroid/widget/FrameLayout;

    move-result-object v0

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/FrameLayout;->setVisibility(I)V

    .line 691
    return-void
.end method
