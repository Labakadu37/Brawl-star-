package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public final class JZSModMenu {

    // ─── Colors ───
    private static final int COL_BG_DARK     = 0xFF1A1A2E;
    private static final int COL_BG_PANEL    = 0xF01A1A2E;
    private static final int COL_BG_CARD     = 0xFF16213E;
    private static final int COL_BG_SIDEBAR  = 0xFF0F3460;
    private static final int COL_BG_HEADER   = 0xFF0A0A1A;
    private static final int COL_BORDER      = 0x30FFFFFF;
    private static final int COL_TEXT        = 0xFFE4E4E4;
    private static final int COL_TEXT_DIM    = 0xFF7B8794;
    private static final int COL_ACCENT      = 0xFFE94560;
    private static final int COL_ON          = 0xFF00CC66;
    private static final int COL_OFF         = 0xFF7B8794;
    private static final int COL_BTN_GREEN   = 0xFF00CC66;
    private static final int COL_BTN_YELLOW  = 0xFFFFC107;
    private static final int COL_BTN_BLUE    = 0xFF2196F3;
    private static final int COL_BTN_RED     = 0xFFE94560;
    private static final int COL_BTN_ORANGE  = 0xFFFF6B35;
    private static final int COL_OVERLAY_DIM = 0x98000000;
    private static final int COL_SIDEBAR_SEL = 0xFF1A1A40;
    private static final int COL_TAB_SEL     = 0x40E94560;
    private static final int COL_3D_SHADOW   = 0xFF0D0D1F;
    private static final int COL_3D_HIGHLIGHT= 0x20FFFFFF;

    // ─── Feature definitions ───
    static final String[][] FEATURES = {
        // {name, key, group, subgroup}
        {"Aimbot",           "mod_aimbot",         "Battle", "Combat"},
        {"Auto Aim",         "mod_autoaim",        "Battle", "Combat"},
        {"Auto Fire",        "mod_autofire",        "Battle", "Combat"},
        {"Wall Hack",        "mod_wallhack",        "Battle", "Combat"},
        {"Speed Hack",       "mod_speedhack",       "Battle", "Utility"},
        {"God Mode",         "mod_godmode",         "Battle", "Utility"},
        {"Unlimited Ammo",   "mod_unlimitedammo",   "Battle", "Utility"},
        {"Auto Dodge",       "mod_autododge",       "Battle", "Automation"},
        {"Auto Play",        "mod_autoplay",        "Battle", "Automation"},
        {"No Cooldown",      "mod_nocooldown",      "Battle", "Exploits"},
        {"Zoom Hack",        "mod_zoomhack",        "Visuals", "Camera"},
        {"Show HP",          "mod_showhp",          "Visuals", "Camera"},
        {"No Fog",           "mod_nofog",           "Visuals", "Effects"},
        {"Custom Skins",     "mod_customskins",     "Visuals", "Effects"},
        {"Rainbow Mode",     "mod_rainbow",         "Visuals", "Effects"},
        {"FPS Counter",      "mod_fpscounter",      "Visuals", "HUD"},
        {"Debug Info",       "mod_debuginfo",       "Visuals", "HUD"},
        {"Anti Ban",         "mod_antiban",         "Misc", "Security"},
        {"Spoof Device",     "mod_spoofdevice",     "Misc", "Security"},
    };

    static final String[][] SLIDERS = {
        // {name, key, group, min, max, step, default}
        {"Speed Value",  "val_speed",   "Battle",  "50", "300", "10", "100"},
        {"Zoom Level",   "val_zoom",    "Visuals", "50", "500", "10", "100"},
        {"FPS Limit",    "val_fps",     "Visuals", "30", "120", "10", "60"},
    };

    private static final String[] GROUPS = {"Battle", "Visuals", "Misc"};
    private static final int[] GROUP_COLORS = {COL_BTN_RED, COL_BTN_BLUE, COL_BTN_YELLOW};

    // ─── State ───
    private static Activity sActivity;
    private static boolean sAttached;
    private static FrameLayout sRootFrame;
    private static FrameLayout sPanel;
    private static View sDimBg;
    private static LinearLayout sBody;
    private static LinearLayout sSidebar;
    private static LinearLayout sSubTabs;
    private static FrameLayout sFab;
    private static TextView sFabText;
    private static String sCurrentGroup = "Battle";
    private static String sCurrentSubGroup = "";
    private static ArrayList<Object[]> sToggleViews = new ArrayList<>();
    private static ArrayList<Object[]> sSliderViews = new ArrayList<>();
    private static Handler sHandler = new Handler(Looper.getMainLooper());
    private static Runnable sRainbow;
    private static float sHue = 0f;

    private JZSModMenu() {}

    // ─── Entry point ───
    public static void attach(final Activity activity) {
        if (sAttached && sActivity == activity) return;
        sAttached = true;
        sActivity = activity;
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                try { build(activity); } catch (Throwable t) {
                    android.util.Log.e("JZS", "ModMenu build failed", t);
                }
            }
        });
    }

    // ─── Build entire UI ───
    private static void build(Activity act) {
        float d = act.getResources().getDisplayMetrics().density;
        int sw = act.getResources().getDisplayMetrics().widthPixels;
        int sh = act.getResources().getDisplayMetrics().heightPixels;

        sRootFrame = new FrameLayout(act);
        sRootFrame.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        // Dim background
        sDimBg = new View(act);
        sDimBg.setBackgroundColor(COL_OVERLAY_DIM);
        sDimBg.setVisibility(View.GONE);
        sDimBg.setClickable(true);
        sDimBg.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { togglePanel(); }
        });
        sRootFrame.addView(sDimBg, new FrameLayout.LayoutParams(-1, -1));

        // Main panel
        sPanel = new FrameLayout(act);
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        panelBg.setColors(new int[]{COL_BG_PANEL, 0xF0111128, 0xF00A0A1A});
        panelBg.setCornerRadius(dp(d, 12));
        panelBg.setStroke(dp(d, 1), COL_BORDER);
        sPanel.setBackground(panelBg);
        sPanel.setElevation(dp(d, 8));
        sPanel.setClickable(true);
        sPanel.setVisibility(View.GONE);

        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(
            (int)(sw * 0.82f), (int)(sh * 0.85f));
        panelLp.gravity = Gravity.CENTER;
        sRootFrame.addView(sPanel, panelLp);

        LinearLayout mainCol = new LinearLayout(act);
        mainCol.setOrientation(LinearLayout.VERTICAL);
        sPanel.addView(mainCol, new FrameLayout.LayoutParams(-1, -1));

        // Header
        buildHeader(act, mainCol, d);

        // Divider
        addDivider(act, mainCol, d, COL_ACCENT);

        // Content: sidebar + body
        LinearLayout contentRow = new LinearLayout(act);
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(-1, 0, 1f);
        mainCol.addView(contentRow, contentLp);

        // Sidebar
        buildSidebar(act, contentRow, d);

        // Right side: subtabs + feature grid
        LinearLayout rightCol = new LinearLayout(act);
        rightCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -1, 1f);
        contentRow.addView(rightCol, rightLp);

        buildSubTabs(act, rightCol, d);
        buildBody(act, rightCol, d);

        // FAB (floating action button)
        buildFab(act, d);

        // Add to activity
        ViewGroup root = (ViewGroup) act.findViewById(android.R.id.content);
        root.addView(sRootFrame);

        refreshContent();
        startRainbow();
    }

    // ─── Header ───
    private static void buildHeader(Activity act, LinearLayout parent, float d) {
        LinearLayout header = new LinearLayout(act);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(d, 14), dp(d, 10), dp(d, 14), dp(d, 10));

        GradientDrawable headerBg = new GradientDrawable();
        headerBg.setColor(COL_BG_HEADER);
        headerBg.setCornerRadii(new float[]{
            dpf(d,12), dpf(d,12), dpf(d,12), dpf(d,12), 0, 0, 0, 0});
        header.setBackground(headerBg);

        // Logo badge
        TextView logo = new TextView(act);
        logo.setText("JZS");
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(2, dpf(d, 5.5f));
        logo.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        logo.setGravity(Gravity.CENTER);
        logo.setPadding(dp(d, 8), dp(d, 3), dp(d, 8), dp(d, 3));
        GradientDrawable logoBg = new GradientDrawable();
        logoBg.setColor(COL_ACCENT);
        logoBg.setCornerRadius(dpf(d, 6));
        logo.setBackground(logoBg);
        header.addView(logo);

        // Title "JZS Brawl"
        sFabText = new TextView(act);
        sFabText.setText("JZS Brawl");
        sFabText.setTextColor(Color.WHITE);
        sFabText.setTextSize(2, dpf(d, 5.5f));
        sFabText.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        sFabText.setPadding(dp(d, 8), 0, 0, 0);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, -2, 1f);
        header.addView(sFabText, titleLp);

        // Version badge
        TextView ver = makeLabel(act, "v5.2", dpf(d, 3.2f), COL_TEXT_DIM);
        ver.setPadding(dp(d, 6), dp(d, 2), dp(d, 6), dp(d, 2));
        GradientDrawable verBg = new GradientDrawable();
        verBg.setColor(0x20FFFFFF);
        verBg.setCornerRadius(dpf(d, 4));
        ver.setBackground(verBg);
        header.addView(ver);

        // Action buttons
        addHeaderBtn(act, header, d, "⚙", COL_BTN_YELLOW);   // Settings ⚙
        addHeaderBtn(act, header, d, "✎", COL_BTN_BLUE);     // Edit ✎
        addHeaderBtn(act, header, d, "✖", COL_BTN_RED);      // Close ✖

        parent.addView(header);
    }

    private static void addHeaderBtn(Activity act, LinearLayout parent, float d,
                                     String icon, int color) {
        TextView btn = new TextView(act);
        btn.setText(icon);
        btn.setTextSize(2, dpf(d, 4.5f));
        btn.setGravity(Gravity.CENTER);
        btn.setTextColor(Color.WHITE);

        int size = dp(d, 28);
        GradientDrawable bg = make3dButton(color, dpf(d, 6));
        btn.setBackground(bg);
        btn.setPadding(0, 0, 0, 0);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.leftMargin = dp(d, 5);
        parent.addView(btn, lp);

        final boolean isClose = icon.equals("✖");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                vibrate(v.getContext(), 30);
                if (isClose) togglePanel();
            }
        });
    }

    // ─── Sidebar ───
    private static void buildSidebar(Activity act, LinearLayout parent, float d) {
        ScrollView sv = new ScrollView(act);
        sv.setFillViewport(true);
        sv.setVerticalScrollBarEnabled(false);

        sSidebar = new LinearLayout(act);
        sSidebar.setOrientation(LinearLayout.VERTICAL);
        sSidebar.setPadding(dp(d, 4), dp(d, 8), dp(d, 4), dp(d, 8));

        GradientDrawable sbBg = new GradientDrawable();
        sbBg.setColor(0xFF111130);
        sSidebar.setBackground(sbBg);

        for (int i = 0; i < GROUPS.length; i++) {
            addSidebarTab(act, sSidebar, d, GROUPS[i], GROUP_COLORS[i], getGroupCount(GROUPS[i]));
        }

        sv.addView(sSidebar, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(dp(d, 80), -1);
        parent.addView(sv, svLp);
    }

    private static void addSidebarTab(Activity act, LinearLayout parent, float d,
                                      final String group, int color, int count) {
        LinearLayout tab = new LinearLayout(act);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(dp(d, 6), dp(d, 12), dp(d, 6), dp(d, 12));

        boolean selected = group.equals(sCurrentGroup);
        GradientDrawable tabBg = new GradientDrawable();
        tabBg.setColor(selected ? COL_SIDEBAR_SEL : 0x00000000);
        tabBg.setCornerRadius(dpf(d, 8));
        tab.setBackground(tabBg);

        // Color accent bar
        View accent = new View(act);
        GradientDrawable accentBg = new GradientDrawable();
        accentBg.setColor(color);
        accentBg.setCornerRadius(dpf(d, 2));
        accent.setBackground(accentBg);
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(dp(d, 24), dp(d, 3));
        accentLp.bottomMargin = dp(d, 6);
        tab.addView(accent, accentLp);

        // Icon (using first letter as icon placeholder)
        TextView icon = new TextView(act);
        icon.setText(group.substring(0, 1));
        icon.setTextSize(2, dpf(d, 5f));
        icon.setTextColor(selected ? Color.WHITE : COL_TEXT_DIM);
        icon.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        icon.setGravity(Gravity.CENTER);
        tab.addView(icon);

        // Label
        TextView label = makeLabel(act, group, dpf(d, 3f), selected ? Color.WHITE : COL_TEXT_DIM);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(-2, -2);
        lblLp.topMargin = dp(d, 2);
        tab.addView(label, lblLp);

        // Badge count
        if (count > 0) {
            TextView badge = new TextView(act);
            badge.setText(String.valueOf(count));
            badge.setTextSize(2, dpf(d, 2.6f));
            badge.setTextColor(Color.WHITE);
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dp(d, 5), dp(d, 1), dp(d, 5), dp(d, 1));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(color);
            badgeBg.setCornerRadius(dpf(d, 8));
            badge.setBackground(badgeBg);
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(-2, -2);
            badgeLp.topMargin = dp(d, 4);
            tab.addView(badge, badgeLp);
        }

        LinearLayout.LayoutParams tabLp = new LinearLayout.LayoutParams(-1, -2);
        tabLp.bottomMargin = dp(d, 4);
        parent.addView(tab, tabLp);

        tab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                vibrate(v.getContext(), 20);
                sCurrentGroup = group;
                sCurrentSubGroup = "";
                refreshContent();
                refreshSidebar();
                refreshSubTabs();
            }
        });
    }

    // ─── Sub-category tabs ───
    private static void buildSubTabs(Activity act, LinearLayout parent, float d) {
        HorizontalScrollView hsv = new HorizontalScrollView(act);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setPadding(dp(d, 8), dp(d, 6), dp(d, 8), dp(d, 6));

        GradientDrawable hsvBg = new GradientDrawable();
        hsvBg.setColor(0xFF111128);
        hsv.setBackground(hsvBg);

        sSubTabs = new LinearLayout(act);
        sSubTabs.setOrientation(LinearLayout.HORIZONTAL);
        sSubTabs.setGravity(Gravity.CENTER_VERTICAL);
        hsv.addView(sSubTabs, new LinearLayout.LayoutParams(-2, -2));

        parent.addView(hsv, new LinearLayout.LayoutParams(-1, -2));
        refreshSubTabs();
    }

    private static void refreshSubTabs() {
        if (sSubTabs == null || sActivity == null) return;
        sSubTabs.removeAllViews();
        float d = sActivity.getResources().getDisplayMetrics().density;

        // Get unique subgroups for current group
        ArrayList<String> subs = new ArrayList<>();
        for (String[] f : FEATURES) {
            if (f[2].equals(sCurrentGroup) && !subs.contains(f[3])) subs.add(f[3]);
        }
        for (String[] s : SLIDERS) {
            if (s[2].equals(sCurrentGroup)) {
                String sub = s.length > 3 ? s[2] : "General";
                if (!subs.contains(sub)) subs.add(sub);
            }
        }

        // "All" tab
        subs.add(0, "All");

        if (sCurrentSubGroup.isEmpty()) sCurrentSubGroup = "All";

        for (final String sub : subs) {
            TextView tab = new TextView(sActivity);
            tab.setText(sub);
            tab.setTextSize(2, dpf(d, 3.4f));
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(d, 12), dp(d, 6), dp(d, 12), dp(d, 6));

            boolean sel = sub.equals(sCurrentSubGroup);
            tab.setTextColor(sel ? Color.WHITE : COL_TEXT_DIM);
            GradientDrawable tabBg = new GradientDrawable();
            tabBg.setColor(sel ? COL_TAB_SEL : 0x00000000);
            tabBg.setCornerRadius(dpf(d, 6));
            if (sel) tabBg.setStroke(dp(d, 1), COL_ACCENT);
            tab.setBackground(tabBg);
            tab.setTypeface(Typeface.create("sans-serif-medium", sel ? Typeface.BOLD : Typeface.NORMAL));

            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-2, -2);
            tlp.rightMargin = dp(d, 6);
            sSubTabs.addView(tab, tlp);

            tab.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    vibrate(v.getContext(), 20);
                    sCurrentSubGroup = sub;
                    refreshContent();
                    refreshSubTabs();
                }
            });
        }
    }

    // ─── Body / Feature grid ───
    private static void buildBody(Activity act, LinearLayout parent, float d) {
        ScrollView sv = new ScrollView(act);
        sv.setFillViewport(true);
        sv.setVerticalScrollBarEnabled(false);
        sv.setPadding(dp(d, 8), dp(d, 4), dp(d, 8), dp(d, 8));

        sBody = new LinearLayout(act);
        sBody.setOrientation(LinearLayout.VERTICAL);
        sv.addView(sBody, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(-1, 0, 1f);
        parent.addView(sv, svLp);
    }

    private static void refreshContent() {
        if (sBody == null || sActivity == null) return;
        sBody.removeAllViews();
        sToggleViews.clear();
        sSliderViews.clear();

        float d = sActivity.getResources().getDisplayMetrics().density;
        boolean showAll = sCurrentSubGroup.equals("All") || sCurrentSubGroup.isEmpty();

        // Toggle features - 2 column grid
        LinearLayout row = null;
        int col = 0;
        for (String[] f : FEATURES) {
            if (!f[2].equals(sCurrentGroup)) continue;
            if (!showAll && !f[3].equals(sCurrentSubGroup)) continue;

            if (col % 2 == 0) {
                row = new LinearLayout(sActivity);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
                rowLp.bottomMargin = dp(d, 4);
                sBody.addView(row, rowLp);
            }

            View card = makeFeatureCard(sActivity, d, f[0], f[1]);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(0, -2, 1f);
            cardLp.leftMargin = dp(d, 2);
            cardLp.rightMargin = dp(d, 2);
            row.addView(card, cardLp);
            col++;
        }
        // Fill incomplete row
        if (row != null && col % 2 == 1) {
            View spacer = new View(sActivity);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 0, 1f);
            sp.leftMargin = dp(d, 2);
            sp.rightMargin = dp(d, 2);
            row.addView(spacer, sp);
        }

        // Slider controls
        for (String[] s : SLIDERS) {
            if (!s[2].equals(sCurrentGroup)) continue;
            View slider = makeSliderCard(sActivity, d, s);
            LinearLayout.LayoutParams slLp = new LinearLayout.LayoutParams(-1, -2);
            slLp.bottomMargin = dp(d, 4);
            sBody.addView(slider, slLp);
        }

        refreshToggleStates();
    }

    // ─── Feature card ───
    private static View makeFeatureCard(Context ctx, float d, final String name, final String key) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(d, 10), dp(d, 10), dp(d, 8), dp(d, 10));

        GradientDrawable cardBg = make3dCard(d);
        card.setBackground(cardBg);

        // Feature name
        TextView label = new TextView(ctx);
        label.setText(name);
        label.setTextColor(COL_TEXT);
        label.setTextSize(2, dpf(d, 3.6f));
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        label.setSingleLine(true);
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(0, -2, 1f);
        card.addView(label, lblLp);

        // ON/OFF badge
        final TextView badge = new TextView(ctx);
        boolean isOn = isFeatureOn(key);
        updateBadge(badge, isOn, d);

        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(-2, -2);
        badgeLp.leftMargin = dp(d, 4);
        card.addView(badge, badgeLp);

        // Settings gear
        TextView gear = new TextView(ctx);
        gear.setText("⚙");
        gear.setTextSize(2, dpf(d, 4.2f));
        gear.setTextColor(COL_TEXT_DIM);
        gear.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams gearLp = new LinearLayout.LayoutParams(dp(d, 22), dp(d, 22));
        gearLp.leftMargin = dp(d, 4);
        card.addView(gear, gearLp);

        sToggleViews.add(new Object[]{badge, key});

        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                vibrate(v.getContext(), 30);
                boolean newState = !isFeatureOn(key);
                setFeature(key, newState);
                refreshToggleStates();
            }
        });

        return card;
    }

    private static void updateBadge(TextView badge, boolean isOn, float d) {
        badge.setText(isOn ? "ON" : "OFF");
        badge.setTextSize(2, dpf(d, 2.8f));
        badge.setTextColor(Color.WHITE);
        badge.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(d, 6), dp(d, 2), dp(d, 6), dp(d, 2));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(isOn ? COL_ON : COL_OFF);
        bg.setCornerRadius(dpf(d, 4));
        badge.setBackground(bg);
    }

    // ─── Slider card ───
    private static View makeSliderCard(Context ctx, float d, final String[] s) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(d, 10), dp(d, 8), dp(d, 10), dp(d, 8));

        GradientDrawable cardBg = make3dCard(d);
        card.setBackground(cardBg);

        // Label
        TextView label = makeLabel(ctx, s[0], dpf(d, 3.6f), COL_TEXT);
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(0, -2, 1f);
        card.addView(label, lblLp);

        // Minus button
        int min = Integer.parseInt(s[3]);
        int max = Integer.parseInt(s[4]);
        int step = Integer.parseInt(s[5]);
        int def = Integer.parseInt(s[6]);

        final TextView valText = new TextView(ctx);
        int curVal = getIntPref(s[1], def);
        valText.setText(String.valueOf(curVal));
        valText.setTextSize(2, dpf(d, 4f));
        valText.setTextColor(COL_TEXT);
        valText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        valText.setGravity(Gravity.CENTER);
        valText.setMinWidth(dp(d, 40));

        TextView minus = make3dMiniButton(ctx, d, "−", COL_BTN_RED);
        TextView plus = make3dMiniButton(ctx, d, "+", COL_BTN_GREEN);

        minus.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                vibrate(v.getContext(), 20);
                int cur = getIntPref(s[1], Integer.parseInt(s[6]));
                int nv = Math.max(Integer.parseInt(s[3]), cur - Integer.parseInt(s[5]));
                setIntPref(s[1], nv);
                valText.setText(String.valueOf(nv));
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                vibrate(v.getContext(), 20);
                int cur = getIntPref(s[1], Integer.parseInt(s[6]));
                int nv = Math.min(Integer.parseInt(s[4]), cur + Integer.parseInt(s[5]));
                setIntPref(s[1], nv);
                valText.setText(String.valueOf(nv));
            }
        });

        int btnSize = dp(d, 26);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(btnSize, btnSize);
        btnLp.leftMargin = dp(d, 4);

        card.addView(minus, new LinearLayout.LayoutParams(btnSize, btnSize));
        card.addView(valText);

        LinearLayout.LayoutParams plusLp = new LinearLayout.LayoutParams(btnSize, btnSize);
        plusLp.leftMargin = dp(d, 4);
        card.addView(plus, plusLp);

        sSliderViews.add(new Object[]{valText, s});

        return card;
    }

    // ─── FAB (floating open button) ───
    private static void buildFab(Activity act, float d) {
        sFab = new FrameLayout(act);
        sFab.setClickable(true);

        int fabSize = dp(d, 48);
        GradientDrawable fabBg = make3dButton(COL_ACCENT, dpf(d, 24));
        sFab.setBackground(fabBg);
        sFab.setElevation(dpf(d, 6));

        TextView fabIcon = new TextView(act);
        fabIcon.setText("JZS");
        fabIcon.setTextColor(Color.WHITE);
        fabIcon.setTextSize(2, dpf(d, 3.8f));
        fabIcon.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        fabIcon.setGravity(Gravity.CENTER);
        sFab.addView(fabIcon, new FrameLayout.LayoutParams(-1, -1));

        FrameLayout.LayoutParams fabLp = new FrameLayout.LayoutParams(fabSize, fabSize);
        fabLp.gravity = Gravity.BOTTOM | Gravity.START;
        fabLp.leftMargin = dp(d, 12);
        fabLp.bottomMargin = dp(d, 12);
        sRootFrame.addView(sFab, fabLp);

        sFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                vibrate(v.getContext(), 40);
                togglePanel();
            }
        });

        // Start with alpha animation
        sFab.setAlpha(0f);
        sFab.animate().alpha(1f).setDuration(500).start();
    }

    // ─── Panel toggle ───
    private static void togglePanel() {
        if (sPanel == null) return;
        boolean isVisible = sPanel.getVisibility() == View.VISIBLE;
        if (isVisible) {
            sPanel.animate().alpha(0f).scaleX(0.9f).scaleY(0.9f).setDuration(200)
                .withEndAction(new Runnable() {
                    @Override public void run() {
                        sPanel.setVisibility(View.GONE);
                        sDimBg.setVisibility(View.GONE);
                        sFab.setVisibility(View.VISIBLE);
                    }
                }).start();
        } else {
            sPanel.setVisibility(View.VISIBLE);
            sDimBg.setVisibility(View.VISIBLE);
            sFab.setVisibility(View.GONE);
            sPanel.setAlpha(0f);
            sPanel.setScaleX(0.9f);
            sPanel.setScaleY(0.9f);
            sPanel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start();
        }
    }

    // ─── Refresh helpers ───
    private static void refreshSidebar() {
        if (sSidebar == null || sActivity == null) return;
        sSidebar.removeAllViews();
        float d = sActivity.getResources().getDisplayMetrics().density;
        for (int i = 0; i < GROUPS.length; i++) {
            addSidebarTab(sActivity, sSidebar, d, GROUPS[i], GROUP_COLORS[i], getGroupCount(GROUPS[i]));
        }
    }

    private static void refreshToggleStates() {
        if (sActivity == null) return;
        float d = sActivity.getResources().getDisplayMetrics().density;
        for (Object[] pair : sToggleViews) {
            TextView badge = (TextView) pair[0];
            String key = (String) pair[1];
            boolean on = isFeatureOn(key);
            updateBadge(badge, on, d);
        }
    }

    private static int getGroupCount(String group) {
        int c = 0;
        for (String[] f : FEATURES) {
            if (f[2].equals(group)) c++;
        }
        return c;
    }

    // ─── 3D button effect ───
    private static GradientDrawable make3dButton(int baseColor, float radius) {
        GradientDrawable bg = new GradientDrawable();
        int r = Color.red(baseColor), g = Color.green(baseColor), b = Color.blue(baseColor);
        int lighter = Color.argb(255, Math.min(255, r+30), Math.min(255, g+30), Math.min(255, b+30));
        int darker = Color.argb(255, Math.max(0, r-40), Math.max(0, g-40), Math.max(0, b-40));
        bg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        bg.setColors(new int[]{lighter, baseColor, darker});
        bg.setCornerRadius(radius);
        bg.setStroke(1, 0x20FFFFFF);
        return bg;
    }

    private static GradientDrawable make3dCard(float d) {
        GradientDrawable bg = new GradientDrawable();
        bg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        bg.setColors(new int[]{0xFF1E2746, COL_BG_CARD, 0xFF0E1530});
        bg.setCornerRadius(dpf(d, 8));
        bg.setStroke(dp(d, 1), 0x18FFFFFF);
        return bg;
    }

    private static TextView make3dMiniButton(Context ctx, float d, String text, int color) {
        TextView btn = new TextView(ctx);
        btn.setText(text);
        btn.setTextSize(2, dpf(d, 4.2f));
        btn.setTextColor(Color.WHITE);
        btn.setGravity(Gravity.CENTER);
        btn.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        btn.setBackground(make3dButton(color, dpf(d, 6)));
        return btn;
    }

    // ─── Divider ───
    private static void addDivider(Context ctx, LinearLayout parent, float d, int color) {
        View div = new View(ctx);
        GradientDrawable divBg = new GradientDrawable();
        divBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        divBg.setColors(new int[]{0x00000000, color, 0x00000000});
        div.setBackground(divBg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Math.max(1, dp(d, 1)));
        parent.addView(div, lp);
    }

    // ─── Rainbow title effect ───
    private static void startRainbow() {
        sRainbow = new Runnable() {
            @Override public void run() {
                if (sFabText == null) return;
                sHue = (sHue + 2f) % 360f;
                float[] hsv = {sHue, 0.8f, 1f};
                int c = Color.HSVToColor(hsv);
                sFabText.setTextColor(c);
                sHandler.postDelayed(this, 50);
            }
        };
        sHandler.post(sRainbow);
    }

    // ─── Settings persistence (file-based, compatible with ht64) ───
    private static ArrayList<String> sSettingLines;

    private static ArrayList<String> loadSettings() {
        if (sSettingLines != null) return sSettingLines;
        sSettingLines = new ArrayList<>();
        if (sActivity == null) return sSettingLines;
        try {
            File f = new File(sActivity.getFilesDir(), "jzs_mod_settings.txt");
            if (!f.exists()) return sSettingLines;
            FileInputStream fis = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) > 0) bos.write(buf, 0, n);
            fis.close();
            String[] lines = bos.toString("UTF-8").split("\n");
            for (String line : lines) {
                String t = line.trim();
                if (t.length() > 0) sSettingLines.add(t);
            }
        } catch (Exception e) {}
        return sSettingLines;
    }

    private static void saveSettings() {
        if (sActivity == null || sSettingLines == null) return;
        try {
            File f = new File(sActivity.getFilesDir(), "jzs_mod_settings.txt");
            f.getParentFile().mkdirs();
            StringBuilder sb = new StringBuilder();
            for (String line : sSettingLines) {
                sb.append(line).append('\n');
            }
            FileOutputStream fos = new FileOutputStream(f, false);
            fos.write(sb.toString().getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {}
    }

    private static boolean isFeatureOn(String key) {
        ArrayList<String> lines = loadSettings();
        for (String line : lines) {
            if (line.equals(key + "=1")) return true;
            if (line.equals(key + "=0")) return false;
        }
        return false;
    }

    private static void setFeature(String key, boolean on) {
        ArrayList<String> lines = loadSettings();
        String entry = key + "=" + (on ? "1" : "0");
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(key + "=")) {
                lines.set(i, entry);
                saveSettings();
                return;
            }
        }
        lines.add(entry);
        saveSettings();
    }

    private static int getIntPref(String key, int def) {
        ArrayList<String> lines = loadSettings();
        for (String line : lines) {
            if (line.startsWith(key + "=")) {
                try { return Integer.parseInt(line.substring(key.length() + 1).trim()); }
                catch (Exception e) { return def; }
            }
        }
        return def;
    }

    private static void setIntPref(String key, int val) {
        ArrayList<String> lines = loadSettings();
        String entry = key + "=" + val;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(key + "=")) {
                lines.set(i, entry);
                saveSettings();
                return;
            }
        }
        lines.add(entry);
        saveSettings();
    }

    // ─── Utilities ───
    private static int dp(float density, int value) {
        return (int)(value * density + 0.5f);
    }

    private static float dpf(float density, float value) {
        return value * density;
    }

    private static TextView makeLabel(Context ctx, String text, float size, int color) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextSize(2, size);
        tv.setTextColor(color);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return tv;
    }

    private static void vibrate(Context ctx, int ms) {
        try {
            Vibrator v = (Vibrator) ctx.getSystemService("vibrator");
            if (v != null) v.vibrate(VibrationEffect.createOneShot(ms, 40));
        } catch (Throwable t) {}
    }

    // ─── Public API ───
    public static void show(Context ctx) {
        if (ctx instanceof Activity) {
            attach((Activity) ctx);
        }
    }
}
