.class Lcom/jzs/brawl/JZSKeyAuth$QuitListener;
.super Ljava/lang/Object;
.implements Landroid/content/DialogInterface$OnClickListener;

# instance fields
.field private act:Landroid/app/Activity;

# direct methods
.method constructor <init>(Landroid/app/Activity;)V
    .registers 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/jzs/brawl/JZSKeyAuth$QuitListener;->act:Landroid/app/Activity;
    return-void
.end method

# virtual methods
.method public onClick(Landroid/content/DialogInterface;I)V
    .registers 3
    iget-object v0, p0, Lcom/jzs/brawl/JZSKeyAuth$QuitListener;->act:Landroid/app/Activity;
    invoke-virtual {v0}, Landroid/app/Activity;->finish()V
    return-void
.end method
