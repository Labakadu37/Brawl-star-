package com.jzs.brawl;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * JZS Brawl mod menu — pure JZS build, zero HernBrawl code.
 * Glass-morphism cyan neon theme, draggable orb, tabbed panel, animated toggles.
 */
public final class JzsModMenu {

    public static final String VERSION = "JZS V2 (final)";

    /* === palette === */
    private static final int BG_SCRIM     = 0xB0000000;
    private static final int PANEL_TOP    = 0xE60A1628;
    private static final int PANEL_BOT    = 0xE6081222;
    private static final int STROKE_CYAN  = 0xFF00E5FF;
    private static final int ACCENT_CYAN  = 0xFF00ACC1;
    private static final int ACCENT_SOFT  = 0xFF4DD0E1;
    private static final int TXT_PRIMARY  = 0xFFE0F7FA;
    private static final int TXT_SECOND   = 0xFF80DEEA;
    private static final int TXT_MUTED    = 0xFF607D8B;
    private static final int ROW_BG       = 0x30101C30;
    private static final int ROW_BG_ON    = 0x3000E5FF;
    private static final int ORB_CORE     = 0xFF00E5FF;
    private static final int ORB_GLOW     = 0x6600E5FF;

    private static Activity sActivity;
    private static FrameLayout sRoot;
    private static View sOrb;
    private static View sPanel;
    private static boolean sPanelOpen;
    private static int sActiveTab;

    /* === feature catalog === */
    private static final class Feature {
        final String tab, flag, label, desc;
        Feature(String tab, String flag, String label, String desc) {
            this.tab = tab; this.flag = flag; this.label = label; this.desc = desc;
        }
    }

    private static final String[] TABS = { "Combat", "Visual", "Misc" };

    private static final Feature[] FEATURES = {
        new Feature("Combat", "jzaim",      "Aimbot",           "Auto-aim at closest enemy"),
        new Feature("Combat", "jzspinner",  "Spinner",          "Rotate shots in a circle"),
        new Feature("Combat", "jzdodge",    "Dodge Bullets",    "Predict and avoid projectiles"),
        new Feature("Combat", "jztrigger",  "Trigger Bot",      "Auto-fire when enemy in range"),
        new Feature("Combat", "jzgadget",   "Gadget Hack",      "Reduce gadget cooldown"),
        new Feature("Combat", "jzsuper",    "Super Ready",      "Alert when super is up"),

        new Feature("Visual", "jzaura",     "Aura Wall",        "ESP wall around enemies"),
        new Feature("Visual", "jzhaz",      "Hazard ESP",       "Show mines, gas, bombs"),
        new Feature("Visual", "jzbox",      "Box ESP",          "Draw boxes around entities"),
        new Feature("Visual", "jzline",     "Tracers",          "Lines from you to enemies"),
        new Feature("Visual", "jzhp",       "HP Bars",          "Enemy HP overlay"),
        new Feature("Visual", "jzname",     "Name Tags",        "Show enemy brawler name"),
        new Feature("Visual", "jzrange",    "Range Circle",     "Draw your attack range"),

        new Feature("Misc",   "jzstealth",  "Stealth",          "Hide position from radar"),
        new Feature("Misc",   "jzfps",      "FPS Unlock",       "Remove 60 FPS cap"),
        new Feature("Misc",   "jzwater",    "Watermark",        "Show JZS watermark"),
        new Feature("Misc",   "jzdiag",     "Debug Diag",       "Write diag.txt log"),
        new Feature("Misc",   "jzfast",     "Fast Reload",      "Reduce reload animation"),
    };

    private JzsModMenu() {}

    public static void attach(Activity activity) {
        if (activity == null) return;
        sActivity = activity;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                try { buildOrb(); } catch (Throwable ignored) {}
            }
        });
    }

    /* ===================== ORB ===================== */

    private static void buildOrb() {
        if (sActivity == null || sOrb != null) return;
        final Context ctx = sActivity;

        sRoot = new FrameLayout(ctx);
        FrameLayout.LayoutParams rootLp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT);

        ViewGroup decor = (ViewGroup) sActivity.getWindow().getDecorView();
        decor.addView(sRoot, rootLp);

        final int size = dp(56);
        sOrb = new View(ctx) {
            @Override
            protected void onDraw(Canvas c) {
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;
                float r  = Math.min(cx, cy);
                Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
                glow.setShader(new RadialGradient(cx, cy, r, ORB_GLOW, 0x0000E5FF, Shader.TileMode.CLAMP));
                c.drawCircle(cx, cy, r, glow);
                Paint core = new Paint(Paint.ANTI_ALIAS_FLAG);
                core.setShader(new RadialGradient(cx, cy, r * 0.6f, 0xFFFFFFFF, ORB_CORE, Shader.TileMode.CLAMP));
                c.drawCircle(cx, cy, r * 0.55f, core);
                Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
                ring.setStyle(Paint.Style.STROKE);
                ring.setStrokeWidth(dp(1.5f));
                ring.setColor(STROKE_CYAN);
                c.drawCircle(cx, cy, r * 0.75f, ring);
                Paint t = new Paint(Paint.ANTI_ALIAS_FLAG);
                t.setColor(0xFF001F2E);
                t.setTextAlign(Paint.Align.CENTER);
                t.setFakeBoldText(true);
                t.setTextSize(dp(12));
                t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
                c.drawText("JZS", cx, cy + dp(4), t);
            }
        };

        FrameLayout.LayoutParams orbLp = new FrameLayout.LayoutParams(size, size);
        orbLp.gravity = Gravity.TOP | Gravity.START;
        orbLp.leftMargin = dp(16);
        orbLp.topMargin  = dp(80);
        sOrb.setLayoutParams(orbLp);
        sRoot.addView(sOrb);
        sOrb.setElevation(dp(12));

        // drag + tap
        sOrb.setOnTouchListener(new DragTap() {
            @Override protected void onTap() { togglePanel(); }
        });
    }

    /* ===================== PANEL ===================== */

    private static void togglePanel() {
        if (sPanelOpen) closePanel(); else openPanel();
    }

    private static void openPanel() {
        if (sActivity == null || sPanel != null) return;
        final Context ctx = sActivity;
        sPanelOpen = true;

        final FrameLayout scrim = new FrameLayout(ctx);
        scrim.setBackgroundColor(BG_SCRIM);
        scrim.setAlpha(0f);
        scrim.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { closePanel(); }
        });
        FrameLayout.LayoutParams scrimLp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT);
        sRoot.addView(scrim, scrimLp);

        // panel container
        LinearLayout panel = new LinearLayout(ctx);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(makePanelBg());
        panel.setPadding(dp(16), dp(14), dp(16), dp(14));
        panel.setElevation(dp(24));
        panel.setOnClickListener(null); // swallow

        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(
            dp(340), FrameLayout.LayoutParams.WRAP_CONTENT);
        panelLp.gravity = Gravity.CENTER;
        panel.setLayoutParams(panelLp);

        // header
        panel.addView(buildHeader(ctx));
        // tabs
        panel.addView(buildTabs(ctx, panel));
        // content
        final ScrollView sv = new ScrollView(ctx);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(360)));
        sv.setFillViewport(true);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(buildContent(ctx, TABS[sActiveTab]));
        panel.addView(sv);
        // footer
        panel.addView(buildFooter(ctx));

        scrim.addView(panel);
        sPanel = scrim;

        scrim.animate().alpha(1f).setDuration(180).setInterpolator(new DecelerateInterpolator()).start();
        panel.setScaleX(0.92f); panel.setScaleY(0.92f); panel.setAlpha(0f);
        panel.animate().scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(220).setInterpolator(new DecelerateInterpolator()).start();
    }

    private static void closePanel() {
        if (sPanel == null) return;
        final View p = sPanel;
        sPanel = null;
        sPanelOpen = false;
        p.animate().alpha(0f).setDuration(160).withEndAction(new Runnable() {
            public void run() { if (p.getParent() != null) ((ViewGroup) p.getParent()).removeView(p); }
        }).start();
    }

    private static GradientDrawable makePanelBg() {
        GradientDrawable g = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{ PANEL_TOP, PANEL_BOT });
        g.setCornerRadius(dp(22));
        g.setStroke(dp(1), STROKE_CYAN);
        return g;
    }

    /* ===================== HEADER / TABS / FOOTER ===================== */

    private static View buildHeader(Context ctx) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(10));

        TextView title = new TextView(ctx);
        title.setText("JZS");
        title.setTextColor(STROKE_CYAN);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        title.setShadowLayer(dp(8), 0, 0, 0x8000E5FF);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(tLp);
        row.addView(title);

        TextView ver = new TextView(ctx);
        ver.setText(VERSION);
        ver.setTextColor(TXT_MUTED);
        ver.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        row.addView(ver);

        TextView close = new TextView(ctx);
        close.setText("  ✕");
        close.setTextColor(TXT_SECOND);
        close.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        close.setPadding(dp(12), 0, 0, 0);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { closePanel(); }
        });
        row.addView(close);

        return row;
    }

    private static LinearLayout buildTabs(final Context ctx, final LinearLayout panel) {
        final LinearLayout bar = new LinearLayout(ctx);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackground(roundBg(0x40000000, dp(14), 0, 0));
        bar.setPadding(dp(4), dp(4), dp(4), dp(4));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.bottomMargin = dp(10);
        bar.setLayoutParams(blp);

        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            final TextView t = new TextView(ctx);
            t.setText(TABS[i]);
            t.setGravity(Gravity.CENTER);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            t.setPadding(dp(10), dp(8), dp(10), dp(8));
            boolean sel = (idx == sActiveTab);
            t.setTextColor(sel ? 0xFF001F2E : TXT_SECOND);
            t.setBackground(sel ? roundBg(ACCENT_CYAN, dp(10), 0, 0) : null);
            t.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            t.setLayoutParams(lp);
            t.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sActiveTab = idx;
                    // rebuild
                    closePanel();
                    openPanel();
                }
            });
            bar.addView(t);
        }
        return bar;
    }

    private static LinearLayout buildContent(Context ctx, String tab) {
        LinearLayout list = new LinearLayout(ctx);
        list.setOrientation(LinearLayout.VERTICAL);
        for (Feature f : FEATURES) {
            if (!f.tab.equals(tab)) continue;
            list.addView(buildRow(ctx, f));
        }
        if (tab.equals("Misc")) list.addView(buildNameEditor(ctx));
        return list;
    }

    private static View buildRow(final Context ctx, final Feature f) {
        final LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rlp.bottomMargin = dp(8);
        row.setLayoutParams(rlp);

        final boolean[] state = { JzsConfig.get(f.flag) };
        row.setBackground(roundBg(state[0] ? ROW_BG_ON : ROW_BG, dp(14),
                                   state[0] ? ACCENT_CYAN : 0x30FFFFFF, dp(1)));

        LinearLayout texts = new LinearLayout(ctx);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams txLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        texts.setLayoutParams(txLp);

        TextView tv = new TextView(ctx);
        tv.setText(f.label);
        tv.setTextColor(TXT_PRIMARY);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        texts.addView(tv);

        TextView desc = new TextView(ctx);
        desc.setText(f.desc);
        desc.setTextColor(TXT_MUTED);
        desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        texts.addView(desc);

        row.addView(texts);

        final ToggleSwitch sw = new ToggleSwitch(ctx);
        sw.setChecked(state[0]);
        LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(dp(44), dp(24));
        swLp.leftMargin = dp(12);
        sw.setLayoutParams(swLp);
        row.addView(sw);

        View.OnClickListener click = new View.OnClickListener() {
            public void onClick(View v) {
                state[0] = !state[0];
                JzsConfig.set(f.flag, state[0]);
                sw.setChecked(state[0]);
                row.setBackground(roundBg(state[0] ? ROW_BG_ON : ROW_BG, dp(14),
                                           state[0] ? ACCENT_CYAN : 0x30FFFFFF, dp(1)));
            }
        };
        row.setOnClickListener(click);
        sw.setOnClickListener(click);
        return row;
    }

    private static View buildNameEditor(final Context ctx) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackground(roundBg(ROW_BG, dp(14), 0x30FFFFFF, dp(1)));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = dp(4);
        row.setLayoutParams(rlp);

        TextView lbl = new TextView(ctx);
        lbl.setText("Spoof Name");
        lbl.setTextColor(TXT_PRIMARY);
        lbl.setTypeface(null, Typeface.BOLD);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        row.addView(lbl);

        final EditText et = new EditText(ctx);
        et.setText(JzsConfig.readString("name.txt", ""));
        et.setHint("username...");
        et.setHintTextColor(TXT_MUTED);
        et.setTextColor(TXT_PRIMARY);
        et.setBackground(null);
        et.setPadding(0, dp(6), 0, dp(6));
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean has) {
                if (!has) JzsConfig.writeString(ctx, "name.txt", et.getText().toString());
            }
        });
        row.addView(et);
        return row;
    }

    private static View buildFooter(Context ctx) {
        TextView tv = new TextView(ctx);
        tv.setText("● engine online  ·  " + FEATURES.length + " features  ·  jzs");
        tv.setTextColor(TXT_MUTED);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(12), 0, 0);
        return tv;
    }

    /* ===================== TOGGLE SWITCH ===================== */

    private static final class ToggleSwitch extends View {
        private boolean on;
        private float progress; // 0..1
        private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint thumb = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glow  = new Paint(Paint.ANTI_ALIAS_FLAG);

        ToggleSwitch(Context c) { super(c); }

        void setChecked(boolean v) {
            if (v == on) return;
            on = v;
            ValueAnimator a = ValueAnimator.ofFloat(progress, v ? 1f : 0f);
            a.setDuration(180);
            a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator va) {
                    progress = (float) va.getAnimatedValue();
                    invalidate();
                }
            });
            a.start();
        }

        @Override protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            float r = h / 2f;
            int bg = blend(0xFF2A3039, ACCENT_CYAN, progress);
            track.setColor(bg);
            c.drawRoundRect(new RectF(0, 0, w, h), r, r, track);
            if (progress > 0.1f) {
                glow.setColor(ACCENT_CYAN);
                glow.setAlpha((int)(progress * 90));
                glow.setMaskFilter(null);
                c.drawRoundRect(new RectF(-dp(2), -dp(2), w + dp(2), h + dp(2)), r, r, glow);
                track.setColor(bg);
                c.drawRoundRect(new RectF(0, 0, w, h), r, r, track);
            }
            float cx = r + (w - 2 * r) * progress;
            thumb.setColor(0xFFFFFFFF);
            c.drawCircle(cx, r, r - dp(3), thumb);
        }

        private int blend(int c1, int c2, float t) {
            int a = (int)(Color.alpha(c1) + (Color.alpha(c2) - Color.alpha(c1)) * t);
            int rr = (int)(Color.red(c1)   + (Color.red(c2)   - Color.red(c1))   * t);
            int gg = (int)(Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t);
            int bb = (int)(Color.blue(c1)  + (Color.blue(c2)  - Color.blue(c1))  * t);
            return Color.argb(a, rr, gg, bb);
        }
    }

    /* ===================== HELPERS ===================== */

    private static GradientDrawable roundBg(int fill, int radius, int stroke, int strokeW) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(radius);
        if (strokeW > 0) g.setStroke(strokeW, stroke);
        return g;
    }

    private static int dp(float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
            sActivity.getResources().getDisplayMetrics());
    }

    /* ===================== DRAG + TAP ===================== */

    private static abstract class DragTap implements View.OnTouchListener {
        private float downX, downY, startX, startY;
        private boolean moved;

        protected abstract void onTap();

        @Override public boolean onTouch(View v, MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = e.getRawX(); downY = e.getRawY();
                    startX = v.getX();  startY = v.getY();
                    moved = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = e.getRawX() - downX, dy = e.getRawY() - downY;
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true;
                    v.setX(startX + dx);
                    v.setY(startY + dy);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!moved) onTap();
                    return true;
            }
            return false;
        }
    }
}
