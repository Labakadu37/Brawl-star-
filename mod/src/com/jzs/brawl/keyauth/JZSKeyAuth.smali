.class public Lcom/jzs/brawl/JZSKeyAuth;
.super Ljava/lang/Object;

# static fields
.field static unlocked:Z

# direct methods
.method public static isUnlocked()Z
    .registers 1
    sget-boolean v0, Lcom/jzs/brawl/JZSKeyAuth;->unlocked:Z
    return v0
.end method

.method public static gate(Landroid/app/Activity;Ljava/lang/Runnable;)V
    .registers 5
    # p0 = activity, p1 = onSuccess runnable

    # If already unlocked, run immediately
    sget-boolean v0, Lcom/jzs/brawl/JZSKeyAuth;->unlocked:Z
    if-eqz v0, :check_saved

    invoke-interface {p1}, Ljava/lang/Runnable;->run()V
    return-void

    :check_saved
    # Get SharedPreferences
    const-string v0, "jzs_key"
    const/4 v1, 0x0
    invoke-virtual {p0, v0, v1}, Landroid/app/Activity;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;
    move-result-object v0

    # Get saved key
    const-string v1, "key"
    const/4 v2, 0x0
    invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v1

    # Get device ID
    invoke-static {p0, v0}, Lcom/jzs/brawl/JZSKeyAuth;->getDevice(Landroid/content/Context;Landroid/content/SharedPreferences;)Ljava/lang/String;
    move-result-object v2

    # If saved key exists, verify it
    if-eqz v1, :show_dialog
    invoke-virtual {v1}, Ljava/lang/String;->isEmpty()Z
    move-result v3
    if-nez v3, :show_dialog

    # Verify saved key (activate=false)
    const/4 v3, 0x0
    invoke-static {p0, v1, v2, v3, p1}, Lcom/jzs/brawl/JZSKeyAuth;->verify(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Runnable;)V
    return-void

    :show_dialog
    invoke-static {p0, v2, p1}, Lcom/jzs/brawl/JZSKeyAuth;->showDialog(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/Runnable;)V
    return-void
.end method

.method private static getDevice(Landroid/content/Context;Landroid/content/SharedPreferences;)Ljava/lang/String;
    .registers 5
    # p0 = context, p1 = sharedprefs

    # Check if device ID is saved
    const-string v0, "device"
    const/4 v1, 0x0
    invoke-interface {p1, v0, v1}, Landroid/content/SharedPreferences;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0

    if-eqz v0, :gen_device
    invoke-virtual {v0}, Ljava/lang/String;->isEmpty()Z
    move-result v1
    if-nez v1, :gen_device
    return-object v0

    :gen_device
    :try_start
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;
    move-result-object v1
    const-string v2, "android_id"
    invoke-static {v1, v2}, Landroid/provider/Settings$Secure;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    :try_end
    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch_gen

    if-nez v0, :save_device
    :catch_gen
    invoke-static {}, Ljava/util/UUID;->randomUUID()Ljava/util/UUID;
    move-result-object v1
    invoke-virtual {v1}, Ljava/util/UUID;->toString()Ljava/lang/String;
    move-result-object v0
    const/4 v1, 0x0
    const/16 v2, 0x10
    invoke-virtual {v0, v1, v2}, Ljava/lang/String;->substring(II)Ljava/lang/String;
    move-result-object v0

    :save_device
    invoke-interface {p1}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;
    move-result-object v1
    const-string v2, "device"
    invoke-interface {v1, v2, v0}, Landroid/content/SharedPreferences$Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;
    invoke-interface {v1}, Landroid/content/SharedPreferences$Editor;->apply()V
    return-object v0
.end method

.method static showDialog(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/Runnable;)V
    .registers 15
    # p0 = activity, p1 = device, p2 = onSuccess

    # Create LinearLayout
    new-instance v0, Landroid/widget/LinearLayout;
    invoke-direct {v0, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V
    const/4 v1, 0x1
    invoke-virtual {v0, v1}, Landroid/widget/LinearLayout;->setOrientation(I)V
    const/16 v1, 0x3c
    const/16 v2, 0x28
    invoke-virtual {v0, v1, v2, v1, v2}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    # Create label
    new-instance v1, Landroid/widget/TextView;
    invoke-direct {v1, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V
    const-string v2, "Entrez votre clé JZS :"
    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    const v2, -0x1
    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setTextColor(I)V
    const/high16 v2, 0x41800000    # 16.0f
    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setTextSize(F)V
    invoke-virtual {v0, v1}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    # Create EditText
    new-instance v1, Landroid/widget/EditText;
    invoke-direct {v1, p0}, Landroid/widget/EditText;-><init>(Landroid/content/Context;)V
    const-string v2, "JZS-XXXX-XXXX-XXXX"
    invoke-virtual {v1, v2}, Landroid/widget/EditText;->setHint(Ljava/lang/CharSequence;)V
    const/4 v2, 0x1
    invoke-virtual {v1, v2}, Landroid/widget/EditText;->setSingleLine(Z)V
    const/16 v2, 0x1001
    invoke-virtual {v1, v2}, Landroid/widget/EditText;->setInputType(I)V
    const v2, -0x1
    invoke-virtual {v1, v2}, Landroid/widget/EditText;->setTextColor(I)V
    const v2, -0x777778
    invoke-virtual {v1, v2}, Landroid/widget/EditText;->setHintTextColor(I)V
    const v2, -0xd5d5d6
    invoke-virtual {v1, v2}, Landroid/widget/EditText;->setBackgroundColor(I)V
    const/16 v2, 0x1e
    const/16 v3, 0x19
    invoke-virtual {v1, v2, v3, v2, v3}, Landroid/widget/EditText;->setPadding(IIII)V
    invoke-virtual {v0, v1}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    # Create status TextView
    new-instance v2, Landroid/widget/TextView;
    invoke-direct {v2, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V
    const-string v3, ""
    invoke-virtual {v2, v3}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    const v3, -0xaab
    invoke-virtual {v2, v3}, Landroid/widget/TextView;->setTextColor(I)V
    const/high16 v3, 0x41500000    # 13.0f
    invoke-virtual {v2, v3}, Landroid/widget/TextView;->setTextSize(F)V
    const/4 v3, 0x0
    const/16 v4, 0xf
    invoke-virtual {v2, v3, v4, v3, v3}, Landroid/widget/TextView;->setPadding(IIII)V
    invoke-virtual {v0, v2}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    # Build AlertDialog
    new-instance v3, Landroid/app/AlertDialog$Builder;
    invoke-direct {v3, p0}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V

    const-string v4, "🔑 JZS Key"
    invoke-virtual {v3, v4}, Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;

    invoke-virtual {v3, v0}, Landroid/app/AlertDialog$Builder;->setView(Landroid/view/View;)Landroid/app/AlertDialog$Builder;

    const/4 v4, 0x0
    invoke-virtual {v3, v4}, Landroid/app/AlertDialog$Builder;->setCancelable(Z)Landroid/app/AlertDialog$Builder;

    const-string v4, "Valider"
    const/4 v0, 0x0
    invoke-virtual {v3, v4, v0}, Landroid/app/AlertDialog$Builder;->setPositiveButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    const-string v4, "Quitter"
    new-instance v0, Lcom/jzs/brawl/JZSKeyAuth$QuitListener;
    invoke-direct {v0, p0}, Lcom/jzs/brawl/JZSKeyAuth$QuitListener;-><init>(Landroid/app/Activity;)V
    invoke-virtual {v3, v4, v0}, Landroid/app/AlertDialog$Builder;->setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    invoke-virtual {v3}, Landroid/app/AlertDialog$Builder;->create()Landroid/app/AlertDialog;
    move-result-object v3

    invoke-virtual {v3}, Landroid/app/AlertDialog;->show()V

    # Set positive button listener (override to not dismiss on click)
    const/4 v4, -0x1
    invoke-virtual {v3, v4}, Landroid/app/AlertDialog;->getButton(I)Landroid/widget/Button;
    move-result-object v4

    new-instance v5, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;
    move-object v6, p0
    move-object v7, v1
    move-object v8, v2
    move-object v9, p1
    move-object v10, p2
    move-object v11, v3
    invoke-direct/range {v5 .. v11}, Lcom/jzs/brawl/JZSKeyAuth$ValidateListener;-><init>(Landroid/app/Activity;Landroid/widget/EditText;Landroid/widget/TextView;Ljava/lang/String;Ljava/lang/Runnable;Landroid/app/AlertDialog;)V
    invoke-virtual {v4, v5}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    return-void
.end method

.method static verify(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Runnable;)V
    .registers 6
    # p0=act, p1=key, p2=dev, p3=activate, p4=onSuccess
    # Start verify thread with no status TextView
    new-instance v0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;
    const/4 v1, 0x0
    invoke-direct/range {v0 .. v0}, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;-><init>()V

    # Set fields
    iput-object p0, v0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->act:Landroid/app/Activity;
    iput-object p1, v0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->key:Ljava/lang/String;
    iput-object p2, v0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->dev:Ljava/lang/String;
    iput-boolean p3, v0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->activate:Z
    iput-object p4, v0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->onSuccess:Ljava/lang/Runnable;

    new-instance v1, Ljava/lang/Thread;
    invoke-direct {v1, v0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
    invoke-virtual {v1}, Ljava/lang/Thread;->start()V

    return-void
.end method

.method public static invalidate(Landroid/content/Context;)V
    .registers 4
    const/4 v0, 0x0
    sput-boolean v0, Lcom/jzs/brawl/JZSKeyAuth;->unlocked:Z

    const-string v0, "jzs_key"
    const/4 v1, 0x0
    invoke-virtual {p0, v0, v1}, Landroid/content/Context;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;
    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;
    move-result-object v1
    const-string v2, "key"
    invoke-interface {v1, v2}, Landroid/content/SharedPreferences$Editor;->remove(Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;
    invoke-interface {v1}, Landroid/content/SharedPreferences$Editor;->apply()V
    return-void
.end method
