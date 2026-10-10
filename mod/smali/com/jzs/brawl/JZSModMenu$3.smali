.class Lcom/jzs/brawl/JZSModMenu$3;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->addHeaderBtn(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;I)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$isClose:Z


# direct methods
.method constructor <init>(Z)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 266
    iput-boolean p1, p0, Lcom/jzs/brawl/JZSModMenu$3;->val$isClose:Z

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .registers 3

    .line 268
    invoke-virtual {p1}, Landroid/view/View;->getContext()Landroid/content/Context;

    move-result-object p1

    const/16 v0, 0x1e

    # invokes: Lcom/jzs/brawl/JZSModMenu;->vibrate(Landroid/content/Context;I)V
    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->access$200(Landroid/content/Context;I)V

    .line 269
    iget-boolean p1, p0, Lcom/jzs/brawl/JZSModMenu$3;->val$isClose:Z

    if-eqz p1, :cond_10

    # invokes: Lcom/jzs/brawl/JZSModMenu;->togglePanel()V
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$100()V

    .line 270
    :cond_10
    return-void
.end method
