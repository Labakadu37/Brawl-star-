package com.jzs.brawl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class JZSKeyAuth {
    private static final String TAG = "JZS";
    private static final String API = "https://api.jzbrawl.fr/api/";
    private static final String PREF = "jzs_key";
    private static final String K_KEY = "key";
    private static final String K_DEV = "device";
    private static boolean unlocked = false;

    public static boolean isUnlocked() {
        return unlocked;
    }

    public static void gate(Activity act, Runnable onSuccess) {
        if (unlocked) {
            onSuccess.run();
            return;
        }
        SharedPreferences sp = act.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String saved = sp.getString(K_KEY, null);
        String dev = getDevice(act, sp);
        if (saved != null && !saved.isEmpty()) {
            verify(act, saved, dev, false, onSuccess);
        } else {
            showDialog(act, dev, onSuccess);
        }
    }

    private static String getDevice(Context ctx, SharedPreferences sp) {
        String d = sp.getString(K_DEV, null);
        if (d != null && !d.isEmpty()) return d;
        try {
            d = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            d = null;
        }
        if (d == null || d.isEmpty()) d = UUID.randomUUID().toString().substring(0, 16);
        sp.edit().putString(K_DEV, d).apply();
        return d;
    }

    private static void showDialog(Activity act, String dev, Runnable onSuccess) {
        LinearLayout layout = new LinearLayout(act);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        TextView label = new TextView(act);
        label.setText("Entrez votre clé JZS :");
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(16f);
        label.setPadding(0, 0, 0, 20);
        layout.addView(label);

        EditText input = new EditText(act);
        input.setHint("JZS-XXXX-XXXX-XXXX");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFF888888);
        input.setBackgroundColor(0xFF2A2A2A);
        input.setPadding(30, 25, 30, 25);
        layout.addView(input);

        final TextView status = new TextView(act);
        status.setText("");
        status.setTextColor(0xFFFF5555);
        status.setTextSize(13f);
        status.setPadding(0, 15, 0, 0);
        layout.addView(status);

        AlertDialog dialog = new AlertDialog.Builder(act)
            .setTitle("🔑 JZS Key")
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("Valider", null)
            .setNegativeButton("Quitter", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    act.finish();
                }
            })
            .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                String key = input.getText().toString().trim().toUpperCase();
                if (key.isEmpty()) {
                    status.setText("Entrez une clé !");
                    return;
                }
                status.setTextColor(0xFFAAAAFF);
                status.setText("Vérification...");
                verify(act, key, dev, true, new Runnable() {
                    public void run() {
                        dialog.dismiss();
                        onSuccess.run();
                    }
                }, status);
            }
        });
    }

    private static void verify(Activity act, String key, String dev, boolean activate, Runnable onSuccess) {
        verify(act, key, dev, activate, onSuccess, null);
    }

    private static void verify(Activity act, String key, String dev, boolean activate, Runnable onSuccess, TextView status) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String nonce = UUID.randomUUID().toString().substring(0, 16);
                    String endpoint = activate ? "activate" : "check";
                    String body = "{\"key\":\"" + key + "\",\"device\":\"" + dev + "\",\"nonce\":\"" + nonce + "\"}";

                    URL url = new URL(API + endpoint);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setRequestProperty("Content-Type", "application/json");
                    c.setConnectTimeout(10000);
                    c.setReadTimeout(10000);
                    c.setDoOutput(true);

                    OutputStream os = c.getOutputStream();
                    os.write(body.getBytes("UTF-8"));
                    os.close();

                    int code = c.getResponseCode();
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                        code >= 400 ? c.getErrorStream() : c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    String resp = sb.toString();

                    boolean ok = resp.contains("\"status\":\"ok\"");
                    String st = "invalid";
                    if (resp.contains("\"status\":\"ok\"")) st = "ok";
                    else if (resp.contains("\"status\":\"revoked\"")) st = "revoked";
                    else if (resp.contains("\"status\":\"used\"")) st = "used";
                    else if (resp.contains("\"status\":\"rate\"")) st = "rate";

                    final String fst = st;
                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        public void run() {
                            if ("ok".equals(fst)) {
                                unlocked = true;
                                SharedPreferences sp = act.getSharedPreferences(PREF, Context.MODE_PRIVATE);
                                sp.edit().putString(K_KEY, key).apply();
                                Log.i(TAG, "Key validated: " + key.substring(0, 7) + "...");
                                onSuccess.run();
                            } else {
                                String msg;
                                if ("revoked".equals(fst)) msg = "Clé révoquée !";
                                else if ("used".equals(fst)) msg = "Clé déjà utilisée sur un autre appareil !";
                                else if ("rate".equals(fst)) msg = "Trop de tentatives, réessaye plus tard.";
                                else msg = "Clé invalide !";

                                if (status != null) {
                                    status.setTextColor(0xFFFF5555);
                                    status.setText(msg);
                                } else {
                                    SharedPreferences sp = act.getSharedPreferences(PREF, Context.MODE_PRIVATE);
                                    sp.edit().remove(K_KEY).apply();
                                    unlocked = false;
                                    showDialog(act, dev, onSuccess);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Key check failed: " + e.getMessage());
                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        public void run() {
                            if (status != null) {
                                status.setTextColor(0xFFFF5555);
                                status.setText("Erreur réseau, réessaye.");
                            } else {
                                Toast.makeText(act, "Erreur de connexion au serveur JZS", Toast.LENGTH_LONG).show();
                                showDialog(act, dev, onSuccess);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    public static void invalidate(Context ctx) {
        unlocked = false;
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().remove(K_KEY).apply();
    }
}
