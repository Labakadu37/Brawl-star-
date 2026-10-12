package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * Remote kill-switch / announcement client for the JZS Brawl mod.
 *
 * It polls a small JSON file (hosted on GitHub, written by the Discord bot
 * running on Termux) and, when the owner flips the switch, disables the mod
 * menu and shows a non-dismissible "update required" screen pointing to the
 * Discord. It can also push one-off announcements / messages.
 *
 * The last known state is cached in SharedPreferences so that simply turning
 * off the network does not bypass a lock that was already received.
 *
 * Called once from JZSInit.install(Activity) (UI thread).
 */
public final class JZSRemote {

    private static final String TAG = "JZS";

    /** Raw URL of the status file. Uses the slash-free "status" branch. */
    private static final String STATUS_URL =
            "https://raw.githubusercontent.com/Labakadu37/Brawl-star-/status/status.json";

    private static final String DEFAULT_DISCORD = "https://discord.gg/jzbrawl";
    private static final long POLL_MS = 60000L;

    private static boolean sStarted;
    private static WeakReference<Activity> sAct;
    private static Handler sUi;
    private static SharedPreferences sPrefs;
    private static FrameLayout sLockView;

    private JZSRemote() {
    }

    public static void init(Activity a) {
        try {
            if (a == null) {
                return;
            }
            sAct = new WeakReference<Activity>(a);
            sUi = new Handler(Looper.getMainLooper());
            sPrefs = a.getSharedPreferences("jzs_menu", Context.MODE_PRIVATE);

            // Re-apply a previously received lock straight away: killing the
            // network must not re-enable a mod that was remotely disabled.
            if (sPrefs.getBoolean("remote_locked", false)) {
                showLock(sPrefs.getString("remote_title", "Mise a jour disponible"),
                        sPrefs.getString("remote_body", "Une nouvelle version du mod est disponible."),
                        sPrefs.getString("remote_btn", "Rejoindre le Discord"),
                        sPrefs.getString("remote_url", DEFAULT_DISCORD));
            }

            if (sStarted) {
                return;
            }
            sStarted = true;
            Thread t = new Thread(new Runnable() {
                public void run() {
                    loop();
                }
            }, "jzs-remote");
            t.setDaemon(true);
            t.start();
        } catch (Throwable th) {
            Log.w(TAG, "remote init failed: " + th);
        }
    }

    private static void loop() {
        while (true) {
            try {
                apply(fetch());
            } catch (Throwable ignore) {
            }
            try {
                Thread.sleep(POLL_MS);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static JSONObject fetch() throws Exception {
        HttpURLConnection c = (HttpURLConnection)
                new URL(STATUS_URL + "?t=" + System.currentTimeMillis()).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestProperty("Cache-Control", "no-cache");
        c.setRequestProperty("User-Agent", "JZSBrawl");
        try {
            if (c.getResponseCode() != 200) {
                return null;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            r.close();
            return new JSONObject(sb.toString());
        } finally {
            c.disconnect();
        }
    }

    private static void apply(final JSONObject j) {
        if (j == null) {
            return; // missing file or network error -> keep cached state
        }
        final boolean locked = j.optBoolean("locked", false);
        final String title = j.optString("title", "Mise a jour disponible");
        final String body = j.optString("body", "Une nouvelle version du mod est disponible.");
        final String btn = j.optString("button", "Rejoindre le Discord");
        final String url = j.optString("url", DEFAULT_DISCORD);
        final String annId = j.optString("announce_id", "");
        final String annTitle = j.optString("announce_title", "");
        final String annBody = j.optString("announce_body", "");

        sPrefs.edit()
                .putBoolean("remote_locked", locked)
                .putString("remote_title", title)
                .putString("remote_body", body)
                .putString("remote_btn", btn)
                .putString("remote_url", url)
                .apply();

        sUi.post(new Runnable() {
            public void run() {
                if (locked) {
                    showLock(title, body, btn, url);
                } else {
                    hideLock();
                }
                if (annId.length() > 0 && !annId.equals(sPrefs.getString("seen_ann", ""))) {
                    sPrefs.edit().putString("seen_ann", annId).apply();
                    showAnnounce(annTitle, annBody);
                }
            }
        });
    }

    private static Activity act() {
        return sAct == null ? null : sAct.get();
    }

    private static ViewGroup decor(Activity a) {
        return (ViewGroup) a.getWindow().getDecorView();
    }

    /** Hide / restore the mod menu root (field JZSModMenu.j) without a compile dep. */
    private static void setMenuHidden(boolean hide) {
        try {
            Class<?> cls = Class.forName("com.jzs.brawl.JZSModMenu");
            Object v = cls.getField("j").get(null);
            if (v instanceof View) {
                ((View) v).setVisibility(hide ? View.GONE : View.VISIBLE);
            }
        } catch (Throwable ignore) {
        }
    }

    private static void showLock(String title, String body, String btnText, final String url) {
        final Activity a = act();
        if (a == null || sLockView != null) {
            return;
        }
        setMenuHidden(true);

        FrameLayout overlay = new FrameLayout(a);
        overlay.setBackgroundColor(0xF00B0B14);
        overlay.setClickable(true);
        overlay.setFocusable(true);

        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        int pad = dp(a, 28);
        box.setPadding(pad, pad, pad, pad);

        TextView t = new TextView(a);
        t.setText(title);
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(22);
        t.setGravity(Gravity.CENTER);
        box.addView(t);

        TextView b = new TextView(a);
        b.setText(body);
        b.setTextColor(0xFFC9CDD6);
        b.setTextSize(15);
        b.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-2, -2);
        blp.topMargin = dp(a, 14);
        box.addView(b, blp);

        Button join = new Button(a);
        join.setText(btnText);
        join.setAllCaps(false);
        join.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Activity act = act();
                    if (act != null) {
                        act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }
                } catch (Throwable ignore) {
                }
            }
        });
        LinearLayout.LayoutParams jlp = new LinearLayout.LayoutParams(-2, -2);
        jlp.topMargin = dp(a, 22);
        box.addView(join, jlp);

        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(-2, -2);
        clp.gravity = Gravity.CENTER;
        overlay.addView(box, clp);

        decor(a).addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        sLockView = overlay;
    }

    private static void hideLock() {
        setMenuHidden(false);
        if (sLockView != null) {
            try {
                ViewParent p = sLockView.getParent();
                if (p instanceof ViewGroup) {
                    ((ViewGroup) p).removeView(sLockView);
                }
            } catch (Throwable ignore) {
            }
            sLockView = null;
        }
    }

    private static void showAnnounce(String title, String body) {
        final Activity a = act();
        if (a == null) {
            return;
        }
        final FrameLayout ov = new FrameLayout(a);
        ov.setBackgroundColor(0xCC000000);
        ov.setClickable(true);

        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(0xFF1B1E27);
        int pad = dp(a, 24);
        box.setPadding(pad, pad, pad, pad);

        TextView t = new TextView(a);
        t.setText(title.length() > 0 ? title : "JZS Brawl");
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(20);
        box.addView(t);

        TextView b = new TextView(a);
        b.setText(body);
        b.setTextColor(0xFFC9CDD6);
        b.setTextSize(15);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-2, -2);
        blp.topMargin = dp(a, 12);
        box.addView(b, blp);

        Button ok = new Button(a);
        ok.setText("OK");
        ok.setAllCaps(false);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ViewParent p = ov.getParent();
                    if (p instanceof ViewGroup) {
                        ((ViewGroup) p).removeView(ov);
                    }
                } catch (Throwable ignore) {
                }
            }
        });
        LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(-2, -2);
        olp.topMargin = dp(a, 16);
        olp.gravity = Gravity.RIGHT;
        box.addView(ok, olp);

        int w = (int) (a.getResources().getDisplayMetrics().widthPixels * 0.82);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(w, -2);
        clp.gravity = Gravity.CENTER;
        ov.addView(box, clp);

        decor(a).addView(ov, new FrameLayout.LayoutParams(-1, -1));
    }

    private static int dp(Activity a, int v) {
        return (int) (v * a.getResources().getDisplayMetrics().density + 0.5f);
    }
}
