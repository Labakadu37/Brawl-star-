.class Lcom/jzs/brawl/JZSModMenu$5;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->refreshSubTabs()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$sub:Ljava/lang/String;


# direct methods
.method constructor <init>(Ljava/lang/String;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 430
    iput-object p1, p0, Lcom/jzs/brawl/JZSModMenu$5;->val$sub:Ljava/lang/String;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .registers 3

    .line 432
    invoke-virtual {p1}, Landroid/view/View;->getContext()Landroid/content/Context;

    move-result-object p1

    const/16 v0, 0x14

    # invokes: Lcom/jzs/brawl/JZSModMenu;->vibrate(Landroid/content/Context;I)V
    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->access$200(Landroid/content/Context;I)V

    .line 433
    iget-object p1, p0, Lcom/jzs/brawl/JZSModMenu$5;->val$sub:Ljava/lang/String;

    # setter for: Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;
    invoke-static {p1}, Lcom/jzs/brawl/JZSModMenu;->access$402(Ljava/lang/String;)Ljava/lang/String;

    .line 434
    # invokes: Lcom/jzs/brawl/JZSModMenu;->refreshContent()V
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$500()V

    .line 435
    # invokes: Lcom/jzs/brawl/JZSModMenu;->refreshSubTabs()V
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$700()V

    .line 436
    return-void
.end method
