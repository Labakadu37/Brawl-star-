.class Lcom/jzs/brawl/JZSModMenu$6;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->makeFeatureCard(Landroid/content/Context;FLjava/lang/String;Ljava/lang/String;)Landroid/view/View;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$key:Ljava/lang/String;


# direct methods
.method constructor <init>(Ljava/lang/String;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 549
    iput-object p1, p0, Lcom/jzs/brawl/JZSModMenu$6;->val$key:Ljava/lang/String;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .registers 3

    .line 551
    invoke-virtual {p1}, Landroid/view/View;->getContext()Landroid/content/Context;

    move-result-object p1

    const/16 v0, 0x1e

    # invokes: Lcom/jzs/brawl/JZSModMenu;->vibrate(Landroid/content/Context;I)V
    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->access$200(Landroid/content/Context;I)V

    .line 552
    iget-object p1, p0, Lcom/jzs/brawl/JZSModMenu$6;->val$key:Ljava/lang/String;

    # invokes: Lcom/jzs/brawl/JZSModMenu;->isFeatureOn(Ljava/lang/String;)Z
    invoke-static {p1}, Lcom/jzs/brawl/JZSModMenu;->access$800(Ljava/lang/String;)Z

    move-result p1

    .line 553
    xor-int/lit8 p1, p1, 0x1

    iget-object v0, p0, Lcom/jzs/brawl/JZSModMenu$6;->val$key:Ljava/lang/String;

    # invokes: Lcom/jzs/brawl/JZSModMenu;->setFeature(Ljava/lang/String;Z)V
    invoke-static {v0, p1}, Lcom/jzs/brawl/JZSModMenu;->access$900(Ljava/lang/String;Z)V

    .line 554
    # invokes: Lcom/jzs/brawl/JZSModMenu;->refreshToggleStates()V
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$1000()V

    .line 555
    return-void
.end method
