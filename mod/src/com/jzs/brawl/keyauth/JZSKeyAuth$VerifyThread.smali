.class Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;
.super Ljava/lang/Object;
.implements Ljava/lang/Runnable;

# instance fields
.field act:Landroid/app/Activity;
.field key:Ljava/lang/String;
.field dev:Ljava/lang/String;
.field activate:Z
.field onSuccess:Ljava/lang/Runnable;
.field statusView:Landroid/widget/TextView;
.field dialog:Landroid/app/AlertDialog;

# direct methods
.method constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

# virtual methods
.method public run()V
    .registers 10

    :try_start
    # Build JSON body: {"key":"...","device":"...","nonce":"..."}
    new-instance v0, Ljava/lang/StringBuilder;
    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "{\"key\":\""
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    iget-object v1, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->key:Ljava/lang/String;
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v1, "\",\"device\":\""
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    iget-object v1, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->dev:Ljava/lang/String;
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v1, "\",\"nonce\":\""
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    # Generate nonce
    invoke-static {}, Ljava/util/UUID;->randomUUID()Ljava/util/UUID;
    move-result-object v1
    invoke-virtual {v1}, Ljava/util/UUID;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v2, 0x0
    const/16 v3, 0x10
    invoke-virtual {v1, v2, v3}, Ljava/lang/String;->substring(II)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v1, "\"}"
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0
    # v0 = json body string

    # Build URL
    new-instance v1, Ljava/lang/StringBuilder;
    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V
    const-string v2, "https://api.jzbrawl.fr/api/"
    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->activate:Z
    if-eqz v2, :check_endpoint
    const-string v2, "activate"
    goto :append_endpoint
    :check_endpoint
    const-string v2, "check"
    :append_endpoint
    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v1

    # Open connection
    new-instance v2, Ljava/net/URL;
    invoke-direct {v2, v1}, Ljava/net/URL;-><init>(Ljava/lang/String;)V
    invoke-virtual {v2}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;
    move-result-object v2
    check-cast v2, Ljava/net/HttpURLConnection;
    # v2 = HttpURLConnection

    const-string v3, "POST"
    invoke-virtual {v2, v3}, Ljava/net/HttpURLConnection;->setRequestMethod(Ljava/lang/String;)V
    const-string v3, "Content-Type"
    const-string v4, "application/json"
    invoke-virtual {v2, v3, v4}, Ljava/net/HttpURLConnection;->setRequestProperty(Ljava/lang/String;Ljava/lang/String;)V
    const/16 v3, 0x2710
    invoke-virtual {v2, v3}, Ljava/net/HttpURLConnection;->setConnectTimeout(I)V
    invoke-virtual {v2, v3}, Ljava/net/HttpURLConnection;->setReadTimeout(I)V
    const/4 v3, 0x1
    invoke-virtual {v2, v3}, Ljava/net/HttpURLConnection;->setDoOutput(Z)V

    # Write body
    invoke-virtual {v2}, Ljava/net/HttpURLConnection;->getOutputStream()Ljava/io/OutputStream;
    move-result-object v3
    const-string v4, "UTF-8"
    invoke-virtual {v0, v4}, Ljava/lang/String;->getBytes(Ljava/lang/String;)[B
    move-result-object v4
    invoke-virtual {v3, v4}, Ljava/io/OutputStream;->write([B)V
    invoke-virtual {v3}, Ljava/io/OutputStream;->close()V

    # Read response
    invoke-virtual {v2}, Ljava/net/HttpURLConnection;->getResponseCode()I
    move-result v3
    # v3 = response code

    const/16 v4, 0x190
    if-lt v3, v4, :use_input_stream
    invoke-virtual {v2}, Ljava/net/HttpURLConnection;->getErrorStream()Ljava/io/InputStream;
    move-result-object v3
    goto :read_stream
    :use_input_stream
    invoke-virtual {v2}, Ljava/net/HttpURLConnection;->getInputStream()Ljava/io/InputStream;
    move-result-object v3

    :read_stream
    new-instance v4, Ljava/io/BufferedReader;
    new-instance v5, Ljava/io/InputStreamReader;
    invoke-direct {v5, v3}, Ljava/io/InputStreamReader;-><init>(Ljava/io/InputStream;)V
    invoke-direct {v4, v5}, Ljava/io/BufferedReader;-><init>(Ljava/io/Reader;)V

    new-instance v5, Ljava/lang/StringBuilder;
    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    :read_loop
    invoke-virtual {v4}, Ljava/io/BufferedReader;->readLine()Ljava/lang/String;
    move-result-object v6
    if-eqz v6, :read_done
    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    goto :read_loop

    :read_done
    invoke-virtual {v4}, Ljava/io/BufferedReader;->close()V
    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v3
    # v3 = response body string

    # Parse status from JSON response
    const-string v4, "\"status\":\"ok\""
    invoke-virtual {v3, v4}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v4
    if-eqz v4, :check_revoked

    # STATUS OK - post success to UI thread
    const/4 v4, 0x1
    sput-boolean v4, Lcom/jzs/brawl/JZSKeyAuth;->unlocked:Z

    # Save key to SharedPreferences
    iget-object v4, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->act:Landroid/app/Activity;
    const-string v5, "jzs_key"
    const/4 v6, 0x0
    invoke-virtual {v4, v5, v6}, Landroid/app/Activity;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;
    move-result-object v4
    invoke-interface {v4}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;
    move-result-object v4
    const-string v5, "key"
    iget-object v6, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->key:Ljava/lang/String;
    invoke-interface {v4, v5, v6}, Landroid/content/SharedPreferences$Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;
    invoke-interface {v4}, Landroid/content/SharedPreferences$Editor;->apply()V

    # Run onSuccess on UI thread
    new-instance v4, Landroid/os/Handler;
    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
    move-result-object v5
    invoke-direct {v4, v5}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
    iget-object v5, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->onSuccess:Ljava/lang/Runnable;
    invoke-virtual {v4, v5}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    return-void

    :check_revoked
    const-string v4, "\"status\":\"revoked\""
    invoke-virtual {v3, v4}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v4
    if-eqz v4, :check_used
    const-string v4, "Clé révoquée !"
    goto :show_error

    :check_used
    const-string v4, "\"status\":\"used\""
    invoke-virtual {v3, v4}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v4
    if-eqz v4, :check_rate
    const-string v4, "Clé déjà utilisée sur un autre appareil !"
    goto :show_error

    :check_rate
    const-string v4, "\"status\":\"rate\""
    invoke-virtual {v3, v4}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v4
    if-eqz v4, :invalid
    const-string v4, "Trop de tentatives, réessaye plus tard."
    goto :show_error

    :invalid
    const-string v4, "Clé invalide !"

    :show_error
    # v4 = error message string
    iget-object v5, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->statusView:Landroid/widget/TextView;
    if-eqz v5, :error_no_status

    # Has status view - show error in dialog
    new-instance v6, Lcom/jzs/brawl/JZSKeyAuth$UIError;
    invoke-direct {v6, v5, v4}, Lcom/jzs/brawl/JZSKeyAuth$UIError;-><init>(Landroid/widget/TextView;Ljava/lang/String;)V
    new-instance v7, Landroid/os/Handler;
    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
    move-result-object v8
    invoke-direct {v7, v8}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
    invoke-virtual {v7, v6}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z
    return-void

    :error_no_status
    # No status view (background check) - clear saved key, show dialog
    iget-object v5, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->act:Landroid/app/Activity;
    const-string v6, "jzs_key"
    const/4 v7, 0x0
    invoke-virtual {v5, v6, v7}, Landroid/app/Activity;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;
    move-result-object v5
    invoke-interface {v5}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;
    move-result-object v5
    const-string v6, "key"
    invoke-interface {v5, v6}, Landroid/content/SharedPreferences$Editor;->remove(Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;
    invoke-interface {v5}, Landroid/content/SharedPreferences$Editor;->apply()V

    const/4 v5, 0x0
    sput-boolean v5, Lcom/jzs/brawl/JZSKeyAuth;->unlocked:Z

    # Show dialog on UI thread
    new-instance v5, Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;
    iget-object v6, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->act:Landroid/app/Activity;
    iget-object v7, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->dev:Ljava/lang/String;
    iget-object v8, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->onSuccess:Ljava/lang/Runnable;
    invoke-direct {v5, v6, v7, v8}, Lcom/jzs/brawl/JZSKeyAuth$ShowDialogRunnable;-><init>(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/Runnable;)V

    new-instance v6, Landroid/os/Handler;
    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
    move-result-object v7
    invoke-direct {v6, v7}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
    invoke-virtual {v6, v5}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    return-void

    :try_end
    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch_ex

    :catch_ex
    # Network error
    iget-object v0, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->statusView:Landroid/widget/TextView;
    if-eqz v0, :net_error_no_status

    # Has status view - show error
    const-string v1, "Erreur réseau, réessaye."
    new-instance v2, Lcom/jzs/brawl/JZSKeyAuth$UIError;
    invoke-direct {v2, v0, v1}, Lcom/jzs/brawl/JZSKeyAuth$UIError;-><init>(Landroid/widget/TextView;Ljava/lang/String;)V
    new-instance v3, Landroid/os/Handler;
    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
    move-result-object v4
    invoke-direct {v3, v4}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
    invoke-virtual {v3, v2}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z
    return-void

    :net_error_no_status
    # No status view - toast + show dialog
    new-instance v0, Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;
    iget-object v1, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->act:Landroid/app/Activity;
    iget-object v2, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->dev:Ljava/lang/String;
    iget-object v3, p0, Lcom/jzs/brawl/JZSKeyAuth$VerifyThread;->onSuccess:Ljava/lang/Runnable;
    invoke-direct {v0, v1, v2, v3}, Lcom/jzs/brawl/JZSKeyAuth$ToastAndDialog;-><init>(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/Runnable;)V

    new-instance v1, Landroid/os/Handler;
    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
    move-result-object v2
    invoke-direct {v1, v2}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
    invoke-virtual {v1, v0}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    return-void
.end method
