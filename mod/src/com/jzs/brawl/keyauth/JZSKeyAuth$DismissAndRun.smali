.class Lcom/jzs/brawl/JZSKeyAuth$DismissAndRun;
.super Ljava/lang/Object;
.implements Ljava/lang/Runnable;

# instance fields
.field private dialog:Landroid/app/AlertDialog;
.field private inner:Ljava/lang/Runnable;

# direct methods
.method constructor <init>(Landroid/app/AlertDialog;Ljava/lang/Runnable;)V
    .registers 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/jzs/brawl/JZSKeyAuth$DismissAndRun;->dialog:Landroid/app/AlertDialog;
    iput-object p2, p0, Lcom/jzs/brawl/JZSKeyAuth$DismissAndRun;->inner:Ljava/lang/Runnable;
    return-void
.end method

# virtual methods
.method public run()V
    .registers 2
    iget-object v0, p0, Lcom/jzs/brawl/JZSKeyAuth$DismissAndRun;->dialog:Landroid/app/AlertDialog;
    invoke-virtual {v0}, Landroid/app/AlertDialog;->dismiss()V
    iget-object v0, p0, Lcom/jzs/brawl/JZSKeyAuth$DismissAndRun;->inner:Ljava/lang/Runnable;
    invoke-interface {v0}, Ljava/lang/Runnable;->run()V
    return-void
.end method
