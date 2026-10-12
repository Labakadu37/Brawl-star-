.class Lcom/jzs/brawl/JZSRemote$2;
.super Ljava/lang/Object;
.source "JZSRemote.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/jzs/brawl/JZSRemote;->apply(Lorg/json/JSONObject;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$annBody:Ljava/lang/String;

.field final synthetic val$annId:Ljava/lang/String;

.field final synthetic val$annTitle:Ljava/lang/String;

.field final synthetic val$body:Ljava/lang/String;

.field final synthetic val$btn:Ljava/lang/String;

.field final synthetic val$locked:Z

.field final synthetic val$title:Ljava/lang/String;

.field final synthetic val$url:Ljava/lang/String;


# direct methods
.method constructor <init>(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    iput-boolean p1, p0, Lcom/jzs/brawl/JZSRemote$2;->val$locked:Z

    iput-object p2, p0, Lcom/jzs/brawl/JZSRemote$2;->val$title:Ljava/lang/String;

    iput-object p3, p0, Lcom/jzs/brawl/JZSRemote$2;->val$body:Ljava/lang/String;

    iput-object p4, p0, Lcom/jzs/brawl/JZSRemote$2;->val$btn:Ljava/lang/String;

    iput-object p5, p0, Lcom/jzs/brawl/JZSRemote$2;->val$url:Ljava/lang/String;

    iput-object p6, p0, Lcom/jzs/brawl/JZSRemote$2;->val$annId:Ljava/lang/String;

    iput-object p7, p0, Lcom/jzs/brawl/JZSRemote$2;->val$annTitle:Ljava/lang/String;

    iput-object p8, p0, Lcom/jzs/brawl/JZSRemote$2;->val$annBody:Ljava/lang/String;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .locals 4

    iget-boolean v0, p0, Lcom/jzs/brawl/JZSRemote$2;->val$locked:Z

    if-eqz v0, :cond_0

    iget-object v0, p0, Lcom/jzs/brawl/JZSRemote$2;->val$title:Ljava/lang/String;

    iget-object v1, p0, Lcom/jzs/brawl/JZSRemote$2;->val$body:Ljava/lang/String;

    iget-object v2, p0, Lcom/jzs/brawl/JZSRemote$2;->val$btn:Ljava/lang/String;

    iget-object v3, p0, Lcom/jzs/brawl/JZSRemote$2;->val$url:Ljava/lang/String;

    invoke-static {v0, v1, v2, v3}, Lcom/jzs/brawl/JZSRemote;->access$100(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

    goto :goto_0

    :cond_0
    invoke-static {}, Lcom/jzs/brawl/JZSRemote;->access$200()V

    :goto_0
    iget-object v0, p0, Lcom/jzs/brawl/JZSRemote$2;->val$annId:Ljava/lang/String;

    invoke-virtual {v0}, Ljava/lang/String;->length()I

    move-result v0

    if-lez v0, :cond_1

    iget-object v0, p0, Lcom/jzs/brawl/JZSRemote$2;->val$annId:Ljava/lang/String;

    invoke-static {}, Lcom/jzs/brawl/JZSRemote;->access$300()Landroid/content/SharedPreferences;

    move-result-object v1

    const-string v2, ""

    const-string v3, "seen_ann"

    invoke-interface {v1, v3, v2}, Landroid/content/SharedPreferences;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_1

    invoke-static {}, Lcom/jzs/brawl/JZSRemote;->access$300()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v0

    iget-object v1, p0, Lcom/jzs/brawl/JZSRemote$2;->val$annId:Ljava/lang/String;

    invoke-interface {v0, v3, v1}, Landroid/content/SharedPreferences$Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;

    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences$Editor;->apply()V

    iget-object v0, p0, Lcom/jzs/brawl/JZSRemote$2;->val$annTitle:Ljava/lang/String;

    iget-object v1, p0, Lcom/jzs/brawl/JZSRemote$2;->val$annBody:Ljava/lang/String;

    invoke-static {v0, v1}, Lcom/jzs/brawl/JZSRemote;->access$400(Ljava/lang/String;Ljava/lang/String;)V

    :cond_1
    return-void
.end method
