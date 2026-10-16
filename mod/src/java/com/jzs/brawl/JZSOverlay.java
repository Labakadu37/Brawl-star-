package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
    private static TextView sSettingsBtn;
    private static FrameLayout sHost;

    private JZSOverlay() {}

    public static void attach(final Activity activity) {
        if (activity == null || sBadge != null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JZSTheme.load(activity);
                    installOverlay(activity);
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "overlay attach failed", t);
                }
            }
        });
    }

    private static void installOverlay(final Activity activity) {
        Context ctx = activity;
        int t = JZSTheme.current();

        sHost = new FrameLayout(ctx);
        sHost.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // ── Badge (top-left) ──
        sBadge = new TextView(ctx);
        sBadge.setText(MOD_VERSION);
        applyBadgeTheme(ctx, sBadge, t);

        FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.gravity = Gravity.TOP | Gravity.START;
        badgeLp.topMargin = dp(ctx, 24);
        badgeLp.leftMargin = dp(ctx, 12);
        sBadge.setLayoutParams(badgeLp);
        sHost.addView(sBadge);

        // ── Settings button (top-right) ──
        sSettingsBtn = new TextView(ctx);
        sSettingsBtn.setText("⚙");
        applySettingsBtnTheme(ctx, sSettingsBtn, t);

        FrameLayout.LayoutParams settingsLp = new FrameLayout.LayoutParams(
                dp(ctx, 36), dp(ctx, 36));
        settingsLp.gravity = Gravity.TOP | Gravity.END;
        settingsLp.topMargin = dp(ctx, 24);
        settingsLp.rightMargin = dp(ctx, 12);
        sSettingsBtn.setLayoutParams(settingsLp);
        sSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                JZSSettingsPanel.toggle();
            }
        });
        sHost.addView(sSettingsBtn);

        // ── Settings panel ──
        JZSSettingsPanel.init(activity, sHost);

        // ── Theme change listener ──
        JZSTheme.setListener(new JZSTheme.Listener() {
            @Override
            public void onThemeChanged(final int themeId) {
                activity.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        applyBadgeTheme(activity, sBadge, themeId);
                        applySettingsBtnTheme(activity, sSettingsBtn, themeId);
                        JZSSettingsPanel.rebuild();
                    }
                });
            }
        });

        // add to window
        activity.getWindow().addContentView(sHost, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        sHost.bringToFront();

        android.util.Log.i("JZS", MOD_NAME + " overlay attached: " + MOD_VERSION);
    }

    private static void applyBadgeTheme(Context ctx, TextView tv, int t) {
        tv.setTextColor(JZSTheme.textPrimary(t));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setShadowLayer(
                t == JZSTheme.THEME_GLASS ? 6f : 4f,
                0f, 0f,
                t == JZSTheme.THEME_GLASS ? Color.argb(80, 255, 255, 255) : Color.BLACK);
        tv.setPadding(dp(ctx, 10), dp(ctx, 6), dp(ctx, 10), dp(ctx, 6));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(JZSTheme.badgeBg(t));
        bg.setCornerRadius(dp(ctx, JZSTheme.cornerRadiusDp(t)));
        int bw = JZSTheme.borderWidthDp(t);
        if (bw > 0) bg.setStroke(dp(ctx, bw), JZSTheme.borderColor(t));
        tv.setBackground(bg);
    }

    private static void applySettingsBtnTheme(Context ctx, TextView tv, int t) {
        tv.setTextColor(JZSTheme.textPrimary(t));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        tv.setGravity(Gravity.CENTER);
        tv.setShadowLayer(
                t == JZSTheme.THEME_GLASS ? 6f : 4f,
                0f, 0f,
                t == JZSTheme.THEME_GLASS ? Color.argb(80, 255, 255, 255) : Color.BLACK);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(JZSTheme.badgeBg(t));
        bg.setCornerRadius(dp(ctx, JZSTheme.cornerRadiusDp(t)));
        int bw = JZSTheme.borderWidthDp(t);
        if (bw > 0) bg.setStroke(dp(ctx, bw), JZSTheme.borderColor(t));
        tv.setBackground(bg);
    }

    private static int dp(Context ctx, int value) {
        float d = ctx.getResources().getDisplayMetrics().density;
        return (int) (value * d + 0.5f);
    }
}
