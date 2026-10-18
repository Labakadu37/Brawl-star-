.class Lcom/jzs/brawl/JZSKeyAuth$UIError;
.super Ljava/lang/Object;
.implements Ljava/lang/Runnable;

# instance fields
.field private tv:Landroid/widget/TextView;
.field private msg:Ljava/lang/String;

# direct methods
.method constructor <init>(Landroid/widget/TextView;Ljava/lang/String;)V
    .registers 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/jzs/brawl/JZSKeyAuth$UIError;->tv:Landroid/widget/TextView;
    iput-object p2, p0, Lcom/jzs/brawl/JZSKeyAuth$UIError;->msg:Ljava/lang/String;
    return-void
.end method

# virtual methods
.method public run()V
    .registers 3
    iget-object v0, p0, Lcom/jzs/brawl/JZSKeyAuth$UIError;->tv:Landroid/widget/TextView;
    const v1, -0xaab
    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setTextColor(I)V
    iget-object v1, p0, Lcom/jzs/brawl/JZSKeyAuth$UIError;->msg:Ljava/lang/String;
    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    return-void
.end method
