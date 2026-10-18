.class Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;
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
    iput-object p1, p0, Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;->act:Landroid/app/Activity;
    iput-object p2, p0, Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;->dev:Ljava/lang/String;
    iput-object p3, p0, Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;->onSuccess:Ljava/lang/Runnable;
    return-void
.end method

# virtual methods
.method public run()V
    .registers 5
    iget-object v0, p0, Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;->act:Landroid/app/Activity;
    const-string v1, "Erreur de connexion au serveur JZS"
    const/4 v2, 0x1
    invoke-static {v0, v1, v2}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
    move-result-object v1
    invoke-virtual {v1}, Landroid/widget/Toast;->show()V

    iget-object v1, p0, Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;->dev:Ljava/lang/String;
    iget-object v2, p0, Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;->onSuccess:Ljava/lang/Runnable;
    invoke-static {v0, v1, v2}, Lcom/jzs/brawl/JZSKeyAuth;->showDialog(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/Runnable;)V
    return-void
.end method
