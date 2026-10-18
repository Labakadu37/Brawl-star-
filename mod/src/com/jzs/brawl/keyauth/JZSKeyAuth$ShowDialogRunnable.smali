.class Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;
.super Ljava/lang/Object;
.implements Ljava/lang/Runnable;

# instance fields
.field private act:Landroid/app/Activity;
.field private dev:Ljava/lang/String;
.field private onSuccess:Ljava/lang/Runnable;

# direct methods
.method constructor <init>(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/Runnable;)V
    .registers 4
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;->act:Landroid/app/Activity;
    iput-object p2, p0, Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;->dev:Ljava/lang/String;
    iput-object p3, p0, Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;->onSuccess:Ljava/lang/Runnable;
    return-void
.end method

# virtual methods
.method public run()V
    .registers 4
    iget-object v0, p0, Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;->act:Landroid/app/Activity;
    iget-object v1, p0, Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;->dev:Ljava/lang/String;
    iget-object v2, p0, Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;->onSuccess:Ljava/lang/Runnable;
    invoke-static {v0, v1, v2}, Lcom/jzs/brawl/JZSKeyAuth;->showDialog(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/Runnable;)V
    return-void
.end method
