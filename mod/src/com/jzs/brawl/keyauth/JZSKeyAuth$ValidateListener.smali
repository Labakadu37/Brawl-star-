.class Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;
.super Ljava/lang/Object;
.implements Landroid/view/View$OnClickListener;

# instance fields
.field private act:Landroid/app/Activity;
.field private input:Landroid/widget/EditText;
.field private status:Landroid/widget/TextView;
.field private dev:Ljava/lang/String;
.field private onSuccess:Ljava/lang/Runnable;
.field private dialog:Landroid/app/AlertDialog;

# direct methods
.method constructor <init>(Landroid/app/Activity;Landroid/widget/EditText;Landroid/widget/TextView;Ljava/lang/String;Ljava/lang/Runnable;Landroid/app/AlertDialog;)V
    .registers 7
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->act:Landroid/app/Activity;
    iput-object p2, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->input:Landroid/widget/EditText;
    iput-object p3, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->status:Landroid/widget/TextView;
    iput-object p4, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->dev:Ljava/lang/String;
    iput-object p5, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->onSuccess:Ljava/lang/Runnable;
    iput-object p6, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->dialog:Landroid/app/AlertDialog;
    return-void
.end method

# virtual methods
.method public onClick(Landroid/view/View;)V
    .registers 6
    # Get key text from EditText
    iget-object v0, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->input:Landroid/widget/EditText;
    invoke-virtual {v0}, Landroid/widget/EditText;->getText()Landroid/text/Editable;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/String;->trim()Ljava/lang/String;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/String;->toUpperCase()Ljava/lang/String;
    move-result-object v0

    # Check if empty
    invoke-virtual {v0}, Ljava/lang/String;->isEmpty()Z
    move-result v1
    if-eqz v1, :not_empty

    iget-object v1, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->status:Landroid/widget/TextView;
    const-string v2, "Entrez une clé !"
    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    return-void

    :not_empty
    # Set status to "Verifying..."
    iget-object v1, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->status:Landroid/widget/TextView;
    const v2, -0x5556
    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setTextColor(I)V
    const-string v2, "Vérification..."
    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    # Create VerifyThread
    new-instance v1, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;
    invoke-direct {v1}, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;-><init>()V

    # Set fields
    iget-object v2, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->act:Landroid/app/Activity;
    iput-object v2, v1, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->act:Landroid/app/Activity;
    iput-object v0, v1, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->key:Ljava/lang/String;
    iget-object v2, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->dev:Ljava/lang/String;
    iput-object v2, v1, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->dev:Ljava/lang/String;
    const/4 v2, 0x1
    iput-boolean v2, v1, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->activate:Z
    iget-object v2, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->status:Landroid/widget/TextView;
    iput-object v2, v1, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->statusView:Landroid/widget/TextView;
    iget-object v2, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->dialog:Landroid/app/AlertDialog;
    iput-object v2, v1, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->dialog:Landroid/app/AlertDialog;

    # Create dismiss+success runnable wrapper
    new-instance v2, Lcom/jzs/brawl/JZSKeyAuth$DismissAndRun;
    iget-object v3, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->dialog:Landroid/app/AlertDialog;
    iget-object v4, p0, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;->onSuccess:Ljava/lang/Runnable;
    invoke-direct {v2, v3, v4}, Lcom/jzs/brawl/JZSKeyAuth$DismissAndRun;-><init>(Landroid/app/AlertDialog;Ljava/lang/Runnable;)V
    iput-object v2, v1, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->onSuccess:Ljava/lang/Runnable;

    # Start thread
    new-instance v2, Ljava/lang/Thread;
    invoke-direct {v2, v1}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
    invoke-virtual {v2}, Ljava/lang/Thread;->start()V

    return-void
.end method
