package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class JZSSettingsPanel {

    private static FrameLayout sRoot;
    private static LinearLayout sPanel;
    private static ScrollView sScroll;
    private static boolean sVisible;
    private static Activity sActivity;

    private JZSSettingsPanel() {}

    public static void init(Activity activity, FrameLayout host) {
        sActivity = activity;
        Context ctx = activity;
        int t = JZSTheme.current();

        sRoot = new FrameLayout(ctx);
        sRoot.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        sRoot.setVisibility(View.GONE);

        // dim overlay
        View dim = new View(ctx);
        dim.setBackgroundColor(Color.argb(100, 0, 0, 0));
        dim.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        dim.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { hide(); }
        });
        sRoot.addView(dim);

        buildPanel(ctx, t);
        host.addView(sRoot);
    }

    private static void buildPanel(Context ctx, int t) {
        if (sScroll != null && sScroll.getParent() != null) {
            ((ViewGroup) sScroll.getParent()).removeView(sScroll);
        }

        int panelW = dp(ctx, 280);

        sPanel = new LinearLayout(ctx);
        sPanel.setOrientation(LinearLayout.VERTICAL);
        sPanel.setBackground(panelDrawable(ctx, t));
        sPanel.setPadding(dp(ctx, 16), dp(ctx, 14), dp(ctx, 16), dp(ctx, 20));
        sPanel.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (Build.VERSION.SDK_INT >= 21) {
            sPanel.setElevation(dp(ctx, 10));
        }

        // ── Header: ⚙ Paramètres ──
        TextView header = new TextView(ctx);
        header.setText("⚙  Paramètres");
        header.setTextColor(JZSTheme.textPrimary(t));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setGravity(Gravity.CENTER);
        header.setShadowLayer(JZSTheme.textShadowRadius(t), 0, 0, JZSTheme.textShadowColor(t));
        header.setPadding(0, 0, 0, dp(ctx, 10));
        sPanel.addView(header);

        // ── Divider ──
        sPanel.addView(makeDivider(ctx, t));

        // ── Section: Thèmes ──
        TextView section = new TextView(ctx);
        section.setText("THÈMES");
        section.setTextColor(JZSTheme.textSecondary(t));
        section.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        section.setTypeface(Typeface.DEFAULT_BOLD);
        section.setLetterSpacing(0.15f);
        section.setPadding(dp(ctx, 4), dp(ctx, 10), 0, dp(ctx, 8));
        sPanel.addView(section);

        // ── Theme: Dark (default) ──
        sPanel.addView(makeThemeRow(ctx, t, JZSTheme.THEME_DARK,
                "Dark", "Thème par défaut sombre"));

        // ── Theme: Glass ──
        sPanel.addView(makeThemeRow(ctx, t, JZSTheme.THEME_GLASS,
                "Glass", "Glassmorphism translucide"));

        // ── Divider ──
        sPanel.addView(makeDivider(ctx, t));

        // ── Info footer ──
        TextView footer = new TextView(ctx);
        footer.setText("JZS Brawl • " + JZSOverlay.MOD_VERSION);
        footer.setTextColor(JZSTheme.textSecondary(t));
        footer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(ctx, 10), 0, 0);
        footer.setAlpha(0.6f);
        sPanel.addView(footer);

        // wrap in scroll
        sScroll = new ScrollView(ctx);
        FrameLayout.LayoutParams scrollLp = new FrameLayout.LayoutParams(
                panelW, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollLp.gravity = Gravity.TOP | Gravity.END;
        scrollLp.topMargin = dp(ctx, 56);
        scrollLp.rightMargin = dp(ctx, 12);
        scrollLp.bottomMargin = dp(ctx, 40);
        sScroll.setLayoutParams(scrollLp);
        sScroll.setVerticalScrollBarEnabled(false);
        sScroll.addView(sPanel);

        sRoot.addView(sScroll);
    }

    private static View makeThemeRow(final Context ctx, int currentTheme,
                                     final int themeId, String name, String desc) {
        boolean active = (themeId == currentTheme);

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));

        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setShape(GradientDrawable.RECTANGLE);
        rowBg.setCornerRadius(dp(ctx, JZSTheme.cornerRadiusDp(currentTheme)));
        if (active) {
            rowBg.setColor(JZSTheme.sidebarBtnActiveBg(currentTheme));
            rowBg.setStroke(dp(ctx, 1),
                    currentTheme == JZSTheme.THEME_GLASS
                            ? Color.argb(100, 255, 255, 255)
                            : Color.argb(40, 255, 255, 255));
        } else {
            rowBg.setColor(JZSTheme.cardBg(currentTheme));
            rowBg.setStroke(JZSTheme.borderWidthDp(currentTheme) > 0 ? dp(ctx, 1) : 0,
                    JZSTheme.borderColor(currentTheme));
        }
        row.setBackground(rowBg);

        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(ctx, 8);
        row.setLayoutParams(rowLp);

        // color preview dots
        View dot1 = colorDot(ctx,
                themeId == JZSTheme.THEME_GLASS
                        ? Color.argb(60, 255, 255, 255)
                        : Color.argb(255, 51, 51, 51));
        View dot2 = colorDot(ctx,
                themeId == JZSTheme.THEME_GLASS
                        ? Color.argb(100, 255, 255, 255)
                        : Color.argb(255, 42, 42, 42));

        LinearLayout.LayoutParams dotLp1 = new LinearLayout.LayoutParams(dp(ctx, 14), dp(ctx, 14));
        dotLp1.rightMargin = dp(ctx, 5);
        dot1.setLayoutParams(dotLp1);

        LinearLayout.LayoutParams dotLp2 = new LinearLayout.LayoutParams(dp(ctx, 14), dp(ctx, 14));
        dotLp2.rightMargin = dp(ctx, 12);
        dot2.setLayoutParams(dotLp2);

        // text column
        LinearLayout textCol = new LinearLayout(ctx);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView label = new TextView(ctx);
        label.setText(name);
        label.setTextColor(JZSTheme.textPrimary(currentTheme));
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        label.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        if (active) {
            label.setShadowLayer(JZSTheme.textShadowRadius(currentTheme), 0, 0,
                    JZSTheme.textShadowColor(currentTheme));
        }
        textCol.addView(label);

        TextView sub = new TextView(ctx);
        sub.setText(desc);
        sub.setTextColor(JZSTheme.textSecondary(currentTheme));
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        sub.setPadding(0, dp(ctx, 2), 0, 0);
        textCol.addView(sub);

        row.addView(dot1);
        row.addView(dot2);
        row.addView(textCol);

        if (active) {
            TextView check = new TextView(ctx);
            check.setText("✓");
            check.setTextColor(currentTheme == JZSTheme.THEME_GLASS
                    ? Color.WHITE : Color.parseColor("#4CAF50"));
            check.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            check.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            checkLp.leftMargin = dp(ctx, 8);
            check.setLayoutParams(checkLp);
            row.addView(check);
        }

        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                JZSTheme.apply(ctx, themeId);
            }
        });

        return row;
    }

    public static void toggle() {
        if (sVisible) hide(); else show();
    }

    public static boolean isVisible() { return sVisible; }

    public static void rebuild() {
        if (sActivity == null || sRoot == null) return;
        final Context ctx = sActivity;
        final int t = JZSTheme.current();
        sActivity.runOnUiThread(new Runnable() {
            @Override public void run() {
                buildPanel(ctx, t);
                if (sVisible) {
                    sRoot.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private static void show() {
        if (sRoot == null) return;
        sRoot.setVisibility(View.VISIBLE);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(200);
        sRoot.startAnimation(fadeIn);

        AnimationSet set = new AnimationSet(true);
        TranslateAnimation slide = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0.3f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f);
        slide.setDuration(250);
        AlphaAnimation panelFade = new AlphaAnimation(0f, 1f);
        panelFade.setDuration(250);
        set.addAnimation(slide);
        set.addAnimation(panelFade);
        sScroll.startAnimation(set);

        sVisible = true;
    }

    private static void hide() {
        if (sRoot == null) return;

        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(150);
        fadeOut.setFillAfter(true);
        sRoot.startAnimation(fadeOut);
        sRoot.postDelayed(new Runnable() {
            @Override public void run() {
                if (sRoot != null) {
                    sRoot.setVisibility(View.GONE);
                    sRoot.clearAnimation();
                }
            }
        }, 160);

        sVisible = false;
    }

    // ── Helpers ──

    private static View makeDivider(Context ctx, int t) {
        View div = new View(ctx);
        div.setBackgroundColor(JZSTheme.dividerColor(t));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 1));
        lp.topMargin = dp(ctx, 4);
        lp.bottomMargin = dp(ctx, 4);
        div.setLayoutParams(lp);
        return div;
    }

    private static View colorDot(Context ctx, int color) {
        View dot = new View(ctx);
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        gd.setStroke(dp(ctx, 1), Color.argb(60, 255, 255, 255));
        dot.setBackground(gd);
        return dot;
    }

    private static GradientDrawable panelDrawable(Context ctx, int t) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(JZSTheme.panelBg(t));
        gd.setCornerRadius(dp(ctx, JZSTheme.panelCornerDp(t)));
        int bw = JZSTheme.borderWidthDp(t);
        if (bw > 0) {
            gd.setStroke(dp(ctx, bw), JZSTheme.borderColor(t));
        }
        return gd;
    }

    private static int dp(Context ctx, int value) {
        float d = ctx.getResources().getDisplayMetrics().density;
        return (int) (value * d + 0.5f);
    }
}
