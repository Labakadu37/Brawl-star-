package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public final class JZSOverlay {

    public static final String MOD_NAME = "JZS Brawl";
    public static final String MOD_VERSION = "JZS V1 (lastest)";

    private static TextView sBadge;

    private JZSOverlay() {}

    public static void attach(final Activity activity) {
        if (activity == null || sBadge != null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    installBadge(activity);
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "overlay attach failed", t);
                }
            }
        });
    }

    private static void installBadge(Activity activity) {
        Context ctx = activity;

        TextView tv = new TextView(ctx);
        tv.setText(MOD_VERSION);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setShadowLayer(4f, 0f, 0f, Color.BLACK);
        tv.setPadding(dp(ctx, 10), dp(ctx, 6), dp(ctx, 10), dp(ctx, 6));
        tv.setBackgroundColor(Color.argb(160, 0, 0, 0));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.topMargin = dp(ctx, 24);
        lp.leftMargin = dp(ctx, 12);
        tv.setLayoutParams(lp);

        View decor = activity.getWindow().getDecorView();
        if (decor instanceof ViewGroup) {
            FrameLayout host = new FrameLayout(ctx);
            host.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            host.addView(tv);

            activity.getWindow().addContentView(host, new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            tv.bringToFront();
        }

        sBadge = tv;
        android.util.Log.i("JZS", MOD_NAME + " overlay attached: " + MOD_VERSION);
    }

    private static int dp(Context ctx, int value) {
        float d = ctx.getResources().getDisplayMetrics().density;
        return (int) (value * d + 0.5f);
    }
}
