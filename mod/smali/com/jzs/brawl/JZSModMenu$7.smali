.class Lcom/jzs/brawl/JZSModMenu$7;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->makeSliderCard(Landroid/content/Context;F[Ljava/lang/String;)Landroid/view/View;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$s:[Ljava/lang/String;

.field final synthetic val$valText:Landroid/widget/TextView;


# direct methods
.method constructor <init>([Ljava/lang/String;Landroid/widget/TextView;)V
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 609
    iput-object p1, p0, Lcom/jzs/brawl/JZSModMenu$7;->val$s:[Ljava/lang/String;

    iput-object p2, p0, Lcom/jzs/brawl/JZSModMenu$7;->val$valText:Landroid/widget/TextView;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .registers 6

    .line 611
    invoke-virtual {p1}, Landroid/view/View;->getContext()Landroid/content/Context;

    move-result-object p1

    const/16 v0, 0x14

    # invokes: Lcom/jzs/brawl/JZSModMenu;->vibrate(Landroid/content/Context;I)V
    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->access$200(Landroid/content/Context;I)V

    .line 612
    iget-object p1, p0, Lcom/jzs/brawl/JZSModMenu$7;->val$s:[Ljava/lang/String;

    const/4 v0, 0x1

    aget-object p1, p1, v0

    iget-object v1, p0, Lcom/jzs/brawl/JZSModMenu$7;->val$s:[Ljava/lang/String;

    const/4 v2, 0x6

    aget-object v1, v1, v2

    invoke-static {v1}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v1

    # invokes: Lcom/jzs/brawl/JZSModMenu;->getIntPref(Ljava/lang/String;I)I
    invoke-static {p1, v1}, Lcom/jzs/brawl/JZSModMenu;->access$1100(Ljava/lang/String;I)I

    move-result p1

    .line 613
    iget-object v1, p0, Lcom/jzs/brawl/JZSModMenu$7;->val$s:[Ljava/lang/String;

    const/4 v2, 0x3

    aget-object v1, v1, v2

    invoke-static {v1}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v1

    iget-object v2, p0, Lcom/jzs/brawl/JZSModMenu$7;->val$s:[Ljava/lang/String;

    const/4 v3, 0x5

    aget-object v2, v2, v3

    invoke-static {v2}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v2

    sub-int/2addr p1, v2

    invoke-static {v1, p1}, Ljava/lang/Math;->max(II)I

    move-result p1

    .line 614
    iget-object v1, p0, Lcom/jzs/brawl/JZSModMenu$7;->val$s:[Ljava/lang/String;

    aget-object v0, v1, v0

    # invokes: Lcom/jzs/brawl/JZSModMenu;->setIntPref(Ljava/lang/String;I)V
    invoke-static {v0, p1}, Lcom/jzs/brawl/JZSModMenu;->access$1200(Ljava/lang/String;I)V

    .line 615
    iget-object v0, p0, Lcom/jzs/brawl/JZSModMenu$7;->val$valText:Landroid/widget/TextView;

    invoke-static {p1}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object p1

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 616
    return-void
.end method
