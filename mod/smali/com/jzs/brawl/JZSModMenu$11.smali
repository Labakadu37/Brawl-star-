.class Lcom/jzs/brawl/JZSModMenu$11;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->startRainbow()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# direct methods
.method constructor <init>()V
    .registers 1

    .line 779
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .registers 4

    .line 781
    # getter for: Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1600()Landroid/widget/TextView;

    move-result-object v0

    if-nez v0, :cond_7

    return-void

    .line 782
    :cond_7
    # getter for: Lcom/jzs/brawl/JZSModMenu;->sHue:F
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1700()F

    move-result v0

    const/high16 v1, 0x40000000    # 2.0f

    add-float/2addr v0, v1

    const/high16 v1, 0x43b40000    # 360.0f

    rem-float/2addr v0, v1

    # setter for: Lcom/jzs/brawl/JZSModMenu;->sHue:F
    invoke-static {v0}, Lcom/jzs/brawl/JZSModMenu;->access$1702(F)F

    .line 783
    const/4 v0, 0x3

    new-array v0, v0, [F

    const/4 v1, 0x0

    # getter for: Lcom/jzs/brawl/JZSModMenu;->sHue:F
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1700()F

    move-result v2

    aput v2, v0, v1

    const/4 v1, 0x1

    const v2, 0x3f4ccccd    # 0.8f

    aput v2, v0, v1

    const/4 v1, 0x2

    const/high16 v2, 0x3f800000    # 1.0f

    aput v2, v0, v1

    .line 784
    invoke-static {v0}, Landroid/graphics/Color;->HSVToColor([F)I

    move-result v0

    .line 785
    # getter for: Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1600()Landroid/widget/TextView;

    move-result-object v1

    invoke-virtual {v1, v0}, Landroid/widget/TextView;->setTextColor(I)V

    .line 786
    # getter for: Lcom/jzs/brawl/JZSModMenu;->sHandler:Landroid/os/Handler;
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1800()Landroid/os/Handler;

    move-result-object v0

    const-wide/16 v1, 0x32

    invoke-virtual {v0, p0, v1, v2}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z

    .line 787
    return-void
.end method
