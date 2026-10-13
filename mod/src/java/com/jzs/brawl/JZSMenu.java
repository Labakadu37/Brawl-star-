package com.jzs.brawl;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public final class JZSMenu {

    public static final String MOD_VERSION = "JZS Brawl V5.5";
    public static final String MOD_TAG = "JZS V5.5";

    private static JZSMenu sInstance;

    private final Activity mActivity;
    private final JZSConfig mConfig;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private FrameLayout mRoot;
    private View mFab;
    private FrameLayout mPanel;
    private LinearLayout mContent;
    private int mActiveTab = 0;

    private long mLastFrameTime;
    private int mFrameCount;
    private int mCurrentFps;
    private TextView mFpsView;
    private boolean mFpsRunning;

    private static final String[] TAB_LABELS = {
            "⚡ FEATURES", "🎨 VISUAL", "ℹ️ INFO", "⚙️ SETTINGS"
    };

    private JZSMenu(Activity activity) {
        mActivity = activity;
        mConfig = JZSConfig.get(activity);
    }

    public static void init(Activity activity) {
        if (sInstance != null) return;
        sInstance = new JZSMenu(activity);
        sInstance.build();
    }

    public static JZSMenu getInstance() {
        return sInstance;
    }

    public JZSConfig config() {
        return mConfig;
    }

    public int currentFps() {
        return mCurrentFps;
    }

    private void build() {
        mRoot = new FrameLayout(mActivity);
        mRoot.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        buildFab();
        buildPanel();

        mRoot.addView(mPanel);
        mRoot.addView(mFab);

        mActivity.getWindow().addContentView(mRoot,
                new WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        mPanel.setVisibility(View.GONE);
        mPanel.setAlpha(0f);
        mPanel.setScaleX(0.8f);
        mPanel.setScaleY(0.8f);

        startFpsCounter();
    }

    private void buildFab() {
        int size = dp(56);
        TextView fab = new TextView(mActivity);
        fab.setText("JZS");
        fab.setTextColor(JZSTheme.NAVY);
        fab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        fab.setTypeface(Typeface.DEFAULT_BOLD);
        fab.setGravity(Gravity.CENTER);
        fab.setBackground(JZSTheme.circle(JZSTheme.GOLD));
        fab.setElevation(dp(8));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        int savedX = mConfig.getInt(JZSConfig.KEY_MENU_BTN_X, dp(12));
        int savedY = mConfig.getInt(JZSConfig.KEY_MENU_BTN_Y, dp(120));
        lp.leftMargin = savedX;
        lp.topMargin = savedY;
        fab.setLayoutParams(lp);

        fab.setOnTouchListener(new FabTouchListener());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePanel();
            }
        });

        mFab = fab;
    }

    private void buildPanel() {
        int panelW = dp(320);
        int panelH = dp(420);
        mPanel = new FrameLayout(mActivity);
        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(panelW, panelH);
        panelLp.gravity = Gravity.CENTER;
        mPanel.setLayoutParams(panelLp);
        mPanel.setBackground(JZSTheme.panel());
        mPanel.setElevation(dp(12));
        mPanel.setClickable(true);

        LinearLayout col = new LinearLayout(mActivity);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        col.addView(buildHeader());
        col.addView(buildDivider());
        col.addView(buildTabBar());
        col.addView(buildDivider());

        ScrollView scroll = new ScrollView(mActivity);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        mContent = new LinearLayout(mActivity);
        mContent.setOrientation(LinearLayout.VERTICAL);
        mContent.setPadding(dp(12), dp(8), dp(12), dp(12));
        mContent.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        scroll.addView(mContent);

        col.addView(scroll);
        col.addView(buildFooter());

        mPanel.addView(col);

        populateTab(0);
    }

    private View buildHeader() {
        FrameLayout header = new FrameLayout(mActivity);
        header.setBackground(JZSTheme.headerGradient());
        header.setPadding(dp(16), dp(12), dp(12), dp(10));
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout left = new LinearLayout(mActivity);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.START | Gravity.CENTER_VERTICAL));

        TextView title = new TextView(mActivity);
        title.setText(MOD_VERSION);
        title.setTextColor(JZSTheme.GOLD);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setShadowLayer(4f, 0f, 2f, JZSTheme.SHADOW);
        left.addView(title);

        TextView sub = new TextView(mActivity);
        sub.setText("Mod Menu • by JZS");
        sub.setTextColor(JZSTheme.TEXT_DIM);
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        left.addView(sub);

        header.addView(left);

        TextView close = new TextView(mActivity);
        close.setText("✕");
        close.setTextColor(JZSTheme.RED);
        close.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        close.setTypeface(Typeface.DEFAULT_BOLD);
        close.setPadding(dp(8), dp(4), dp(8), dp(4));
        FrameLayout.LayoutParams closeLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.TOP);
        close.setLayoutParams(closeLp);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePanel();
            }
        });
        header.addView(close);

        return header;
    }

    private View buildTabBar() {
        HorizontalScrollView hsv = new HorizontalScrollView(mActivity);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        hsv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        hsv.setPadding(dp(8), dp(6), dp(8), dp(6));

        LinearLayout row = new LinearLayout(mActivity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int idx = i;
            TextView tab = new TextView(mActivity);
            tab.setText(TAB_LABELS[i]);
            tab.setTextColor(i == mActiveTab ? JZSTheme.WHITE : JZSTheme.TEXT_DIM);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tab.setTypeface(Typeface.DEFAULT_BOLD);
            tab.setPadding(dp(12), dp(6), dp(12), dp(6));
            tab.setBackground(i == mActiveTab ? JZSTheme.tabActive() : JZSTheme.tabInactive());
            tab.setTag("tab_" + i);

            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.rightMargin = dp(4);
            tab.setLayoutParams(tlp);

            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchTab(idx);
                }
            });
            row.addView(tab);
        }

        hsv.addView(row);
        return hsv;
    }

    private View buildFooter() {
        LinearLayout footer = new LinearLayout(mActivity);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(dp(8), dp(6), dp(8), dp(8));
        footer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        mFpsView = new TextView(mActivity);
        mFpsView.setText("FPS: --");
        mFpsView.setTextColor(JZSTheme.GREEN);
        mFpsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        mFpsView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        footer.addView(mFpsView);

        TextView spacer = new TextView(mActivity);
        spacer.setText("  •  ");
        spacer.setTextColor(JZSTheme.DIVIDER);
        spacer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        footer.addView(spacer);

        TextView ver = new TextView(mActivity);
        ver.setText("drive.jzbrawl.fr");
        ver.setTextColor(JZSTheme.TEXT_DIM);
        ver.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        footer.addView(ver);

        return footer;
    }

    private View buildDivider() {
        View div = new View(mActivity);
        div.setBackgroundColor(JZSTheme.DIVIDER);
        div.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return div;
    }

    private void switchTab(int idx) {
        if (idx == mActiveTab) return;
        mActiveTab = idx;

        ViewGroup tabBar = (ViewGroup) ((HorizontalScrollView) mPanel.findViewWithTag("tab_0")
                .getParent()).getChildAt(0);
        for (int i = 0; i < TAB_LABELS.length; i++) {
            View t = tabBar.getChildAt(i);
            if (t instanceof TextView) {
                ((TextView) t).setTextColor(i == idx ? JZSTheme.WHITE : JZSTheme.TEXT_DIM);
                t.setBackground(i == idx ? JZSTheme.tabActive() : JZSTheme.tabInactive());
            }
        }
        populateTab(idx);
    }

    private void populateTab(int idx) {
        mContent.removeAllViews();
        switch (idx) {
            case 0: populateFeatures(); break;
            case 1: populateVisual();   break;
            case 2: populateInfo();     break;
            case 3: populateSettings(); break;
        }
    }

    private void populateFeatures() {
        addSectionTitle("GAMEPLAY");
        addToggle("Zoom Étendu", "Vue plus large en jeu",
                JZSConfig.KEY_ZOOM_ENABLED, false);
        addSlider("Niveau de Zoom", JZSConfig.KEY_ZOOM_LEVEL, 50, 200, 100, "%");
        addToggle("Show Hitboxes", "Afficher les zones de collision",
                JZSConfig.KEY_SHOW_HITBOX, false);
        addToggle("Anti-Aliasing", "Lissage des bords",
                JZSConfig.KEY_ANTI_ALIASING, false);

        addSectionTitle("PERFORMANCE");
        addToggle("Compteur FPS", "Afficher les FPS en overlay",
                JZSConfig.KEY_FPS_COUNTER, true);
        addToggle("Afficher Ping", "Latence réseau",
                JZSConfig.KEY_SHOW_PING, false);
    }

    private void populateVisual() {
        addSectionTitle("OVERLAY");
        addToggle("Badge JZS", "Afficher le badge du mod",
                JZSConfig.KEY_SHOW_BADGE, true);
        addToggle("Mode Sombre HUD", "Assombrir le HUD du jeu",
                JZSConfig.KEY_DARK_HUD, false);
        addToggle("Grille de Debug", "Afficher la grille d'overlay",
                JZSConfig.KEY_SHOW_GRID, false);

        addSectionTitle("INFORMATIONS");
        addToggle("Coordonnées Joueur", "Position X/Y en overlay",
                JZSConfig.KEY_SHOW_COORDS, false);
        addToggle("Info Mémoire", "Utilisation RAM",
                JZSConfig.KEY_SHOW_MEMORY, false);
        addToggle("Niveau Batterie", "Pourcentage batterie",
                JZSConfig.KEY_SHOW_BATTERY, false);
    }

    private void populateInfo() {
        addSectionTitle("MOD");
        addInfoRow("Version", MOD_VERSION);
        addInfoRow("Build", "V5.5-release");
        addInfoRow("Site", "drive.jzbrawl.fr");
        addInfoRow("Développeur", "JZS Team");

        addSectionTitle("JEU");
        addInfoRow("Jeu", "Brawl Stars");
        addInfoRow("Version Jeu", "v69.252");
        addInfoRow("Package", "com.supercell.brawlstars");

        addSectionTitle("APPAREIL");
        addInfoRow("Modèle", Build.MODEL);
        addInfoRow("Android", "API " + Build.VERSION.SDK_INT);
        addInfoRow("Fabricant", Build.MANUFACTURER);

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long totalMb = rt.maxMemory() / (1024 * 1024);
        addInfoRow("Mémoire", usedMb + " / " + totalMb + " MB");
    }

    private void populateSettings() {
        addSectionTitle("MENU");
        addSlider("Opacité Menu", JZSConfig.KEY_MENU_OPACITY, 30, 100, 90, "%");

        addSectionTitle("DONNÉES");
        addButton("Réinitialiser Config", JZSTheme.RED, new Runnable() {
            @Override
            public void run() {
                mConfig.resetAll();
                populateTab(mActiveTab);
            }
        });

        addSectionTitle("À PROPOS");
        addInfoRow("Mod", "JZS Brawl V5.5");
        addInfoRow("Créateur", "JZS");
        addInfoRow("Site", "drive.jzbrawl.fr");
        addInfoRow("Discord", "JZS Community");
    }

    private void addSectionTitle(String text) {
        TextView tv = new TextView(mActivity);
        tv.setText(text);
        tv.setTextColor(JZSTheme.GOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(dp(4), dp(12), 0, dp(4));
        tv.setLetterSpacing(0.1f);
        mContent.addView(tv);
    }

    private void addToggle(String label, String desc, final String key, boolean defaultVal) {
        LinearLayout row = new LinearLayout(mActivity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.setBackground(JZSTheme.pill(JZSTheme.SURFACE, JZSTheme.CORNER_SM));
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(4);
        row.setLayoutParams(rowLp);

        LinearLayout textCol = new LinearLayout(mActivity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView lbl = new TextView(mActivity);
        lbl.setText(label);
        lbl.setTextColor(JZSTheme.WHITE);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(lbl);

        if (desc != null) {
            TextView d = new TextView(mActivity);
            d.setText(desc);
            d.setTextColor(JZSTheme.TEXT_DIM);
            d.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
            textCol.addView(d);
        }
        row.addView(textCol);

        final boolean isOn = mConfig.getBool(key, defaultVal);
        final View toggle = buildToggle(isOn);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newVal = !mConfig.getBool(key, false);
                mConfig.setBool(key, newVal);
                animateToggle(toggle, newVal);
                onFeatureChanged(key, newVal);
            }
        });
        row.addView(toggle);

        mContent.addView(row);
    }

    private View buildToggle(boolean on) {
        FrameLayout frame = new FrameLayout(mActivity);
        int w = dp(44);
        int h = dp(24);
        frame.setLayoutParams(new LinearLayout.LayoutParams(w, h));

        View track = new View(mActivity);
        track.setBackground(JZSTheme.pill(on ? JZSTheme.TOGGLE_ON : JZSTheme.TOGGLE_OFF, dp(12)));
        track.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        track.setTag("track");
        frame.addView(track);

        View knob = new View(mActivity);
        int knobSize = dp(20);
        FrameLayout.LayoutParams klp = new FrameLayout.LayoutParams(knobSize, knobSize);
        klp.gravity = Gravity.CENTER_VERTICAL;
        klp.leftMargin = on ? (w - knobSize - dp(2)) : dp(2);
        klp.topMargin = dp(2);
        knob.setLayoutParams(klp);
        knob.setBackground(JZSTheme.circle(JZSTheme.TOGGLE_KNOB));
        knob.setElevation(dp(2));
        knob.setTag("knob");
        frame.addView(knob);

        return frame;
    }

    private void animateToggle(View toggle, boolean on) {
        View track = toggle.findViewWithTag("track");
        View knob = toggle.findViewWithTag("knob");
        if (track == null || knob == null) return;

        int w = dp(44);
        int knobSize = dp(20);
        int targetMargin = on ? (w - knobSize - dp(2)) : dp(2);

        track.setBackground(JZSTheme.pill(on ? JZSTheme.TOGGLE_ON : JZSTheme.TOGGLE_OFF, dp(12)));

        FrameLayout.LayoutParams klp = (FrameLayout.LayoutParams) knob.getLayoutParams();
        final int startMargin = klp.leftMargin;
        final int delta = targetMargin - startMargin;
        final View fKnob = knob;

        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(200);
        anim.setInterpolator(new OvershootInterpolator(1.5f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) fKnob.getLayoutParams();
                p.leftMargin = startMargin + (int) (delta * f);
                fKnob.setLayoutParams(p);
            }
        });
        anim.start();
    }

    private void addSlider(String label, final String key, int min, int max, int def,
                           final String suffix) {
        LinearLayout col = new LinearLayout(mActivity);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(8), dp(6), dp(8), dp(8));
        col.setBackground(JZSTheme.pill(JZSTheme.SURFACE, JZSTheme.CORNER_SM));
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        colLp.bottomMargin = dp(4);
        col.setLayoutParams(colLp);

        LinearLayout top = new LinearLayout(mActivity);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView lbl = new TextView(mActivity);
        lbl.setText(label);
        lbl.setTextColor(JZSTheme.WHITE);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        lbl.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(lbl);

        int cur = mConfig.getInt(key, def);
        final TextView valView = new TextView(mActivity);
        valView.setText(cur + suffix);
        valView.setTextColor(JZSTheme.BLUE);
        valView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        valView.setTypeface(Typeface.DEFAULT_BOLD);
        top.addView(valView);

        col.addView(top);

        SeekBar seek = new SeekBar(mActivity);
        seek.setMax(max - min);
        seek.setProgress(cur - min);
        seek.setPadding(0, dp(4), 0, 0);
        seek.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        final int fMin = min;
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int val = fMin + progress;
                valView.setText(val + suffix);
                mConfig.setInt(key, val);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
        col.addView(seek);

        mContent.addView(col);
    }

    private void addInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(mActivity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(8), dp(6), dp(8), dp(6));
        row.setBackground(JZSTheme.pill(JZSTheme.SURFACE, JZSTheme.CORNER_SM));
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(4);
        row.setLayoutParams(rowLp);

        TextView lbl = new TextView(mActivity);
        lbl.setText(label);
        lbl.setTextColor(JZSTheme.TEXT_DIM);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        lbl.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(lbl);

        TextView val = new TextView(mActivity);
        val.setText(value);
        val.setTextColor(JZSTheme.WHITE);
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        val.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(val);

        mContent.addView(row);
    }

    private void addButton(String label, int color, final Runnable action) {
        TextView btn = new TextView(mActivity);
        btn.setText(label);
        btn.setTextColor(JZSTheme.WHITE);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(16), dp(10), dp(16), dp(10));
        btn.setBackground(JZSTheme.pill(color, JZSTheme.CORNER_SM));
        btn.setElevation(dp(2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(4);
        btn.setLayoutParams(lp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
            }
        });
        mContent.addView(btn);
    }

    private void togglePanel() {
        boolean opening = mPanel.getVisibility() != View.VISIBLE;
        if (opening) {
            mPanel.setVisibility(View.VISIBLE);
            mPanel.animate()
                    .alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(250)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        } else {
            mPanel.animate()
                    .alpha(0f).scaleX(0.8f).scaleY(0.8f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mPanel.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    private void onFeatureChanged(String key, boolean value) {
        if (JZSConfig.KEY_FPS_COUNTER.equals(key)) {
            JZSOverlay overlay = JZSOverlay.getInstance();
            if (overlay != null) overlay.refreshOverlays();
        } else if (JZSConfig.KEY_SHOW_BADGE.equals(key)) {
            JZSOverlay overlay = JZSOverlay.getInstance();
            if (overlay != null) overlay.refreshOverlays();
        }
    }

    private void startFpsCounter() {
        mFpsRunning = true;
        mLastFrameTime = System.nanoTime();
        mFrameCount = 0;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mFpsRunning) return;
                mFrameCount++;
                long now = System.nanoTime();
                long elapsed = now - mLastFrameTime;
                if (elapsed >= 1_000_000_000L) {
                    mCurrentFps = (int) (mFrameCount * 1_000_000_000L / elapsed);
                    mFrameCount = 0;
                    mLastFrameTime = now;
                    if (mFpsView != null) {
                        mFpsView.setText("FPS: " + mCurrentFps);
                        mFpsView.setTextColor(mCurrentFps >= 50 ? JZSTheme.GREEN
                                : mCurrentFps >= 30 ? JZSTheme.GOLD : JZSTheme.RED);
                    }
                    JZSOverlay overlay = JZSOverlay.getInstance();
                    if (overlay != null) overlay.updateFps(mCurrentFps);
                }
                mHandler.postDelayed(this, 16);
            }
        });
    }

    private class FabTouchListener implements View.OnTouchListener {
        private float startX, startY;
        private int origLeft, origTop;
        private boolean dragging;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = e.getRawX();
                    startY = e.getRawY();
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
                    origLeft = lp.leftMargin;
                    origTop = lp.topMargin;
                    dragging = false;
                    return false;

                case MotionEvent.ACTION_MOVE:
                    float dx = e.getRawX() - startX;
                    float dy = e.getRawY() - startY;
                    if (!dragging && (Math.abs(dx) > dp(8) || Math.abs(dy) > dp(8))) {
                        dragging = true;
                    }
                    if (dragging) {
                        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) v.getLayoutParams();
                        p.leftMargin = origLeft + (int) dx;
                        p.topMargin = origTop + (int) dy;
                        v.setLayoutParams(p);
                        return true;
                    }
                    return false;

                case MotionEvent.ACTION_UP:
                    if (dragging) {
                        FrameLayout.LayoutParams fp = (FrameLayout.LayoutParams) v.getLayoutParams();
                        mConfig.setInt(JZSConfig.KEY_MENU_BTN_X, fp.leftMargin);
                        mConfig.setInt(JZSConfig.KEY_MENU_BTN_Y, fp.topMargin);
                        return true;
                    }
                    return false;
            }
            return false;
        }
    }

    private int dp(int value) {
        float d = mActivity.getResources().getDisplayMetrics().density;
        return (int) (value * d + 0.5f);
    }
}
