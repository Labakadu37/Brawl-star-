package com.jzs.brawl;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Small line in the top-left corner showing the server the game is connected to:
 *   "PARTIE  1.2.3.4:5678"  while a battle is running (UDP)
 *   "LOBBY   1.2.3.4:9339"  in the menus (TCP)
 * Hidden when there is no connection. Refreshed once per second off the UI thread.
 *
 * {@link #TOP_DP} decides where it sits: put it just under the text that is already
 * in the corner (JZS badge / green text).
 */
public final class JZSNetInfo {

    /** Distance from the top of the screen, in dp. Raise it if it overlaps the text above. */
    public static int TOP_DP = 58;
    public static int LEFT_DP = 12;

    private static final long REFRESH_MS = 1000L;
    private static final int GREEN = 0xFF5CFF8A;
    private static final int LOBBY = 0xFF85CFFC;

    private static TextView sLine;
    private static Handler sWorker;
    private static boolean sEnabled = true;
    private static String sLast = "";

    private JZSNetInfo() {}

    public static void attach(final Activity activity) {
        if (activity == null || sLine != null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    build(activity);
                    startPolling();
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "net info attach failed", t);
                }
            }
        });
    }

    /** Show / hide the line (e.g. from a toggle in the menu). */
    public static void setEnabled(boolean enabled) {
        sEnabled = enabled;
        final TextView line = sLine;
        if (line != null) {
            line.post(new Runnable() {
                @Override
                public void run() {
                    line.setVisibility(sEnabled && !sLast.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    private static void build(Activity activity) {
        TextView tv = new TextView(activity);
        tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        tv.setTextColor(GREEN);
        tv.setShadowLayer(3f, 0f, 0f, Color.BLACK);
        tv.setPadding(JZSUi.dpi(activity, 8), JZSUi.dpi(activity, 3),
                JZSUi.dpi(activity, 8), JZSUi.dpi(activity, 3));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(140, 0, 0, 0));
        bg.setCornerRadius(JZSUi.dp(activity, 6));
        tv.setBackground(bg);
        tv.setVisibility(View.GONE);
        // Never steal touches from the game.
        tv.setClickable(false);
        tv.setFocusable(false);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.topMargin = JZSUi.dpi(activity, TOP_DP);
        lp.leftMargin = JZSUi.dpi(activity, LEFT_DP);

        FrameLayout host = new FrameLayout(activity);
        host.addView(tv, lp);
        activity.getWindow().addContentView(host, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sLine = tv;
    }

    private static void startPolling() {
        if (sWorker != null) return;
        HandlerThread thread = new HandlerThread("jzs-netinfo");
        thread.start();
        sWorker = new Handler(thread.getLooper());
        final Handler ui = new Handler(Looper.getMainLooper());
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                String raw = "";
                try {
                    raw = nativeServer();
                } catch (Throwable t) {
                    // libjz.so missing or too old: nothing to show
                }
                final String now = raw == null ? "" : raw;
                if (!now.equals(sLast)) {
                    sLast = now;
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            render(now);
                        }
                    });
                }
                sWorker.postDelayed(this, REFRESH_MS);
            }
        });
    }

    private static void render(String raw) {
        TextView tv = sLine;
        if (tv == null) return;
        if (raw.length() < 3 || !sEnabled) {
            tv.setVisibility(View.GONE);
            return;
        }
        boolean battle = raw.charAt(0) == 'U';
        String label = battle ? JZSUi.tr("MATCH ", "PARTIE") : "LOBBY ";
        tv.setText(label + "  " + raw.substring(2));
        tv.setTextColor(battle ? GREEN : LOBBY);
        tv.setVisibility(View.VISIBLE);
    }

    /** "U ip:port" for a battle server, "T ip:port" for the lobby, "" when not connected. */
    private static native String nativeServer();
}
