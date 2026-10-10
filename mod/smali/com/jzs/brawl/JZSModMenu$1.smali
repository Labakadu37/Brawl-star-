.class Lcom/jzs/brawl/JZSModMenu$1;
.super Ljava/lang/Object;
.source "JZSModMenu.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSModMenu;->attach(Landroid/app/Activity;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$activity:Landroid/app/Activity;


# direct methods
.method constructor <init>(Landroid/app/Activity;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 110
    iput-object p1, p0, Lcom/jzs/brawl/JZSModMenu$1;->val$activity:Landroid/app/Activity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .registers 4

    .line 112
    :try_start_0
    iget-object v0, p0, Lcom/jzs/brawl/JZSModMenu$1;->val$activity:Landroid/app/Activity;

    # invokes: Lcom/jzs/brawl/JZSModMenu;->build(Landroid/app/Activity;)V
    invoke-static {v0}, Lcom/jzs/brawl/JZSModMenu;->access$000(Landroid/app/Activity;)V
    :try_end_5
    .catchall {:try_start_0 .. :try_end_5} :catchall_6

    .line 114
    goto :goto_e

    .line 112
    :catchall_6
    move-exception v0

    .line 113
    const-string v1, "JZS"

    const-string v2, "ModMenu build failed"

    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 115
    :goto_e
    return-void
.end method
