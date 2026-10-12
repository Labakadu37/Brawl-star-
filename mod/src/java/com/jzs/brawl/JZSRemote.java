package com.jzs.brawl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Remote control of the mod, driven by the Discord bot in bot/.
 *
 * On every game launch we download remote/status.json from the GitHub repo
 * (the bot rewrites that file when you run /miseajour, /maintenance, ...):
 *
 *   - MOD_BUILD < min_build  -> blocking "mise a jour" dialog + Discord link
 *   - maintenance == true    -> blocking maintenance dialog
 *   - announcement_id is new -> one-time announcement popup
 *   - badge_text non empty   -> replaces the overlay text
 *
 * The last good status is cached in SharedPreferences so a blocked build
 * stays blocked even if the download fails (airplane mode, DNS block...).
 */
public final class JZSRemote {

    /** Bump this every time you publish a new APK, then run /miseajour <build>. */
    public static final int MOD_BUILD = 1;

    static final String STATUS_URL =
            "https://raw.githubusercontent.com/Labakadu37/Brawl-star-/HEAD/remote/status.json";

    private static final String PREFS = "jzs_remote";
    private static final String KEY_STATUS = "status";
    private static final String KEY_SEEN_ANNOUNCE = "seen_announcement";

    private static volatile boolean sBlocked;

    private JZSRemote() {}

    /** Mod menu code must check this before doing anything. */
    public static boolean isModAllowed() {
        return !sBlocked;
    }

    public static void check(final Activity activity) {
        // Apply the cached status immediately, then refresh from the network.
        String cached = prefs(activity).getString(KEY_STATUS, null);
        if (cached != null) {
            apply(activity, cached);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String body = download(STATUS_URL + "?t=" + System.currentTimeMillis());
                    new JSONObject(body); // validate before caching
                    prefs(activity).edit().putString(KEY_STATUS, body).apply();
                    apply(activity, body);
                } catch (Throwable t) {
                    android.util.Log.w("JZS", "remote status fetch failed: " + t);
                }
            }
        }, "jzs-remote").start();
    }

    private static void apply(final Activity activity, String json) {
        final JSONObject s;
        try {
            s = new JSONObject(json);
        } catch (Throwable t) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    applyOnUi(activity, s);
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "remote status apply failed", t);
                }
            }
        });
    }

    private static AlertDialog sDialog;

    private static void applyOnUi(final Activity activity, JSONObject s) {
        if (activity.isFinishing()) return;

        String link = s.optString("update_link", "https://discord.gg/jzbrawl");
        int minBuild = s.optInt("min_build", 0);

        if (MOD_BUILD < minBuild) {
            sBlocked = true;
            showBlocking(activity, "Mise a jour disponible",
                    s.optString("update_message", "Une mise a jour est disponible.")
                            + "\n\n" + link, link);
            return;
        }
        if (s.optBoolean("maintenance", false)) {
            sBlocked = true;
            showBlocking(activity, "Maintenance",
                    s.optString("maintenance_message", "Le mod est en maintenance.")
                            + "\n\n" + link, link);
            return;
        }

        // Unblocked (e.g. /annulermaj or /maintenance off since last launch).
        sBlocked = false;
        if (sDialog != null && sDialog.isShowing()) {
            sDialog.dismiss();
            sDialog = null;
        }

        JZSOverlay.setText(s.optString("badge_text", ""));

        int announceId = s.optInt("announcement_id", 0);
        String announce = s.optString("announcement", "");
        SharedPreferences p = prefs(activity);
        if (announceId > 0 && !announce.isEmpty()
                && p.getInt(KEY_SEEN_ANNOUNCE, 0) != announceId) {
            p.edit().putInt(KEY_SEEN_ANNOUNCE, announceId).apply();
            new AlertDialog.Builder(activity)
                    .setTitle(JZSOverlay.MOD_NAME)
                    .setMessage(announce)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private static void showBlocking(final Activity activity, String title,
                                     String message, final String link) {
        JZSOverlay.setText("JZS - " + title);
        if (sDialog != null && sDialog.isShowing()) {
            sDialog.setTitle(title);
            sDialog.setMessage(message);
            return;
        }
        sDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Rejoindre le Discord", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        openLink(activity, link);
                        activity.finishAffinity();
                    }
                })
                .setNegativeButton("Quitter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        activity.finishAffinity();
                    }
                })
                .show();
    }

    private static void openLink(Context ctx, String link) {
        try {
            if (!link.startsWith("http")) link = "https://" + link;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Throwable t) {
            Toast.makeText(ctx, link, Toast.LENGTH_LONG).show();
        }
    }

    private static String download(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setUseCaches(false);
        c.setRequestProperty("Cache-Control", "no-cache");
        try {
            if (c.getResponseCode() != 200) {
                throw new IllegalStateException("HTTP " + c.getResponseCode());
            }
            InputStream in = c.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            c.disconnect();
        }
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
