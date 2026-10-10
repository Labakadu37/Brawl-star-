.class Lcom/jzs/brawl/JZSModMenu$4;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->addSidebarTab(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;II)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$group:Ljava/lang/String;


# direct methods
.method constructor <init>(Ljava/lang/String;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 357
    iput-object p1, p0, Lcom/jzs/brawl/JZSModMenu$4;->val$group:Ljava/lang/String;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .registers 3

    .line 359
    invoke-virtual {p1}, Landroid/view/View;->getContext()Landroid/content/Context;

    move-result-object p1

    const/16 v0, 0x14

    # invokes: Lcom/jzs/brawl/JZSModMenu;->vibrate(Landroid/content/Context;I)V
    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->access$200(Landroid/content/Context;I)V

    .line 360
    iget-object p1, p0, Lcom/jzs/brawl/JZSModMenu$4;->val$group:Ljava/lang/String;

    # setter for: Lcom/jzs/brawl/JZSModMenu;->sCurrentGroup:Ljava/lang/String;
    invoke-static {p1}, Lcom/jzs/brawl/JZSModMenu;->access$302(Ljava/lang/String;)Ljava/lang/String;

    .line 361
    const-string p1, ""

    # setter for: Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;
    invoke-static {p1}, Lcom/jzs/brawl/JZSModMenu;->access$402(Ljava/lang/String;)Ljava/lang/String;

    .line 362
    # invokes: Lcom/jzs/brawl/JZSModMenu;->refreshContent()V
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$500()V

    .line 363
    # invokes: Lcom/jzs/brawl/JZSModMenu;->refreshSidebar()V
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$600()V

    .line 364
    # invokes: Lcom/jzs/brawl/JZSModMenu;->refreshSubTabs()V
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->access$700()V

    .line 365
    return-void
.end method
