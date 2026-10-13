package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Welcome guide ("notice") explaining how the JZS menu works.
 *
 * Shown automatically the first time a given notice version is seen, and can be reopened
 * at any time with {@link #show(Activity)} (e.g. from a "Guide" button in the menu).
 * Bump {@link #NOTICE_VERSION} when the pages change so everyone sees the new guide once.
 */
public final class JZSNotice {

    private static final String PREFS = "jzs_notice";
    private static final String KEY_SEEN = "seen_version";
    private static final int NOTICE_VERSION = 6;

    private static FrameLayout sRoot;

    /** {title EN, title FR, body EN, body FR} */
    private static final String[][] PAGES = {
            {"Welcome to JZS Brawl!", "Bienvenue sur JZS Brawl !",
                    "Thanks for installing JZS Brawl " + JZSOverlay.MOD_VERSION + ".\n"
                            + "This quick guide shows you how the menu works.\n"
                            + "JZS is free, never paid: if someone sold it to you, you got scammed.",
                    "Merci d'avoir installé JZS Brawl " + JZSOverlay.MOD_VERSION + ".\n"
                            + "Ce petit guide t'explique comment marche le menu.\n"
                            + "JZS est gratuit, jamais payant : si on te l'a vendu, tu t'es fait arnaquer."},
            {"Open the menu", "Ouvrir le menu",
                    "Tap the JZS button on screen to open or close the menu.\n"
                            + "Tabs are at the top, the search bar finds any option fast.",
                    "Touche le bouton JZS à l'écran pour ouvrir ou fermer le menu.\n"
                            + "Les onglets sont en haut, la barre de recherche trouve une option en un clin d'œil."},
            {"Move & resize", "Déplacer & redimensionner",
                    "Tap \"Edit GUI\" then drag the menu wherever you like.\n"
                            + "Tap \"Edit GUI\" again to lock it in place.\n"
                            + "In Settings: Menu Scale and Opacity.",
                    "Touche « Modifier » puis glisse le menu où tu veux.\n"
                            + "Touche à nouveau « Modifier » pour le verrouiller.\n"
                            + "Dans Paramètres : Taille du menu et Opacité."},
            {"Language & config", "Langue & config",
                    "Settings → Language: English, Français, Español, Português, Deutsch, Italiano.\n"
                            + "Save your setup with Config → Save, it comes back next launch.",
                    "Paramètres → Langue : English, Français, Español, Português, Deutsch, Italiano.\n"
                            + "Sauvegarde tes réglages avec Config → Sauver, ils reviennent au prochain lancement."},
            {"Your profile", "Ton profil",
                    "Open Profile, enter your player tag (#...) and an API key from "
                            + "developer.brawlstars.com to see your trophies, wins and club.",
                    "Ouvre Profil, entre ton tag joueur (#...) et une clé API de "
                            + "developer.brawlstars.com pour voir tes trophées, victoires et ton club."},
            {"Join the community", "Rejoins la communauté",
                    "News, updates and help on jzbrawl.fr.\n"
                            + "You can reopen this guide anytime from the menu.\n"
                            + "Have fun!",
                    "Actus, mises à jour et aide sur jzbrawl.fr.\n"
                            + "Tu peux rouvrir ce guide quand tu veux depuis le menu.\n"
                            + "Amuse-toi bien !"},
    };

    private JZSNotice() {}

    /** Shows the guide only if this notice version hasn't been seen yet. */
    public static void showIfFirstTime(Activity activity) {
        if (activity == null) return;
        SharedPreferences sp = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (sp.getInt(KEY_SEEN, 0) >= NOTICE_VERSION) return;
        show(activity);
    }

    /** Always shows the guide (no-op if it is already on screen). */
    public static void show(final Activity activity) {
        if (activity == null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sRoot != null) return;
                try {
                    build(activity);
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "notice failed", t);
                }
            }
        });
    }

    private static void build(final Activity activity) {
        final Context ctx = activity;
        final boolean fr = JZSUi.french();

        // Dimmed full-screen backdrop that swallows touches
        final FrameLayout root = new FrameLayout(ctx);
        root.setBackgroundColor(0xB0000000);
        root.setClickable(true);
        root.setAlpha(0f);

        // Card
        final LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = JZSUi.dpi(ctx, 22);
        card.setPadding(pad, JZSUi.dpi(ctx, 18), pad, JZSUi.dpi(ctx, 16));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{JZSUi.CARD, JZSUi.BG_DARK});
        bg.setCornerRadius(JZSUi.dp(ctx, 20));
        bg.setStroke(JZSUi.dpi(ctx, 2), JZSUi.CARD_BORDER);
        card.setBackground(bg);
        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                Math.min(JZSUi.dpi(ctx, 460),
                        ctx.getResources().getDisplayMetrics().widthPixels - JZSUi.dpi(ctx, 32)),
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(card, cardLp);

        // Rainbow stripe at the top of the card
        View stripe = new View(ctx);
        GradientDrawable stripeBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, JZSUi.RAINBOW);
        stripeBg.setCornerRadius(JZSUi.dp(ctx, 3));
        stripe.setBackground(stripeBg);
        LinearLayout.LayoutParams stripeLp = new LinearLayout.LayoutParams(
                JZSUi.dpi(ctx, 64), JZSUi.dpi(ctx, 5));
        stripeLp.bottomMargin = JZSUi.dpi(ctx, 14);
        card.addView(stripe, stripeLp);

        // Page content (badge + title + body), animated on page change
        final LinearLayout content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        card.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final TextView badge = new TextView(ctx);
        badge.setTypeface(JZSUi.titleFont(ctx));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        badge.setTextColor(Color.BLACK);
        badge.setGravity(Gravity.CENTER);
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.OVAL);
        badgeBg.setColor(JZSUi.YELLOW);
        badgeBg.setStroke(JZSUi.dpi(ctx, 3), Color.BLACK);
        badge.setBackground(badgeBg);
        int bs = JZSUi.dpi(ctx, 52);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(bs, bs);
        badgeLp.bottomMargin = JZSUi.dpi(ctx, 10);
        content.addView(badge, badgeLp);

        final TextView title = new TextView(ctx);
        title.setTypeface(JZSUi.titleFont(ctx));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTextColor(Color.WHITE);
        title.setShadowLayer(0.01f, 0, JZSUi.dp(ctx, 2), Color.BLACK);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        final TextView body = new TextView(ctx);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        body.setTextColor(JZSUi.TEXT_DIM);
        body.setGravity(Gravity.CENTER);
        body.setLineSpacing(JZSUi.dp(ctx, 3), 1f);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bodyLp.topMargin = JZSUi.dpi(ctx, 8);
        bodyLp.bottomMargin = JZSUi.dpi(ctx, 16);
        content.addView(body, bodyLp);

        // Page dots
        final Dots dots = new Dots(ctx, PAGES.length);
        LinearLayout.LayoutParams dotsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, JZSUi.dpi(ctx, 14));
        dotsLp.bottomMargin = JZSUi.dpi(ctx, 14);
        card.addView(dots, dotsLp);

        // Buttons
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final TextView back = button(ctx, fr ? "Passer" : "Skip", JZSUi.CARD_BORDER, Color.WHITE);
        final TextView next = button(ctx, fr ? "Suivant" : "Next", JZSUi.BLUE, Color.BLACK);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, JZSUi.dpi(ctx, 46), 1f);
        bLp.rightMargin = JZSUi.dpi(ctx, 6);
        row.addView(back, bLp);
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(0, JZSUi.dpi(ctx, 46), 1f);
        nLp.leftMargin = JZSUi.dpi(ctx, 6);
        row.addView(next, nLp);

        final int[] page = {0};
        final Runnable render = new Runnable() {
            @Override
            public void run() {
                int p = page[0];
                badge.setText(String.valueOf(p + 1));
                title.setText(PAGES[p][fr ? 1 : 0]);
                body.setText(PAGES[p][fr ? 3 : 2]);
                dots.setPage(p);
                boolean last = p == PAGES.length - 1;
                back.setText(p == 0 ? (fr ? "Passer" : "Skip") : (fr ? "Retour" : "Back"));
                next.setText(last ? (fr ? "C'est parti !" : "Let's go!") : (fr ? "Suivant" : "Next"));
                setButtonColor(next, last ? JZSUi.YELLOW : JZSUi.BLUE);
            }
        };
        render.run();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page[0] == PAGES.length - 1) {
                    close(activity);
                } else {
                    turn(content, page, +1, render);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page[0] == 0) {
                    close(activity);
                } else {
                    turn(content, page, -1, render);
                }
            }
        });

        activity.getWindow().addContentView(root, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.bringToFront();
        sRoot = root;

        // Entrance: backdrop fades, card springs up
        root.animate().alpha(1f).setDuration(220).start();
        card.setTranslationY(JZSUi.dp(ctx, 80));
        card.setScaleX(0.9f);
        card.setScaleY(0.9f);
        card.animate().translationY(0f).scaleX(1f).scaleY(1f)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .setDuration(420).start();
    }

    private static void turn(final View content, final int[] page, final int dir, final Runnable render) {
        final float shift = JZSUi.dp(content.getContext(), 40) * dir;
        content.animate().cancel();
        content.animate().alpha(0f).translationX(-shift).setDuration(120)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        page[0] += dir;
                        render.run();
                        content.setTranslationX(shift);
                        content.animate().alpha(1f).translationX(0f).setDuration(200)
                                .setInterpolator(new DecelerateInterpolator()).start();
                    }
                }).start();
    }

    private static void close(Activity activity) {
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_SEEN, NOTICE_VERSION).apply();
        final FrameLayout root = sRoot;
        if (root == null) return;
        root.setClickable(false);
        root.animate().alpha(0f).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                if (root.getParent() instanceof ViewGroup) {
                    ((ViewGroup) root.getParent()).removeView(root);
                }
                if (sRoot == root) sRoot = null;
            }
        }).start();
    }

    private static TextView button(Context ctx, String text, int fill, int textColor) {
        TextView b = new TextView(ctx);
        b.setText(text);
        b.setTypeface(JZSUi.titleFont(ctx));
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        b.setTextColor(textColor);
        b.setGravity(Gravity.CENTER);
        b.setClickable(true);
        setButtonColor(b, fill);
        return b;
    }

    private static void setButtonColor(TextView b, int fill) {
        Context ctx = b.getContext();
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(JZSUi.dp(ctx, 12));
        d.setStroke(JZSUi.dpi(ctx, 2), Color.BLACK);
        b.setBackground(d);
    }

    /** Row of page dots; the current one is wider and colored. */
    private static final class Dots extends View {
        private final int mCount;
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int mPage;

        Dots(Context ctx, int count) {
            super(ctx);
            mCount = count;
        }

        void setPage(int page) {
            mPage = page;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas c) {
            float r = getHeight() / 2f * 0.6f;
            float gap = r * 1.6f;
            float active = r * 5f;
            float total = (mCount - 1) * (r * 2f + gap) + active;
            float x = (getWidth() - total) / 2f;
            float cy = getHeight() / 2f;
            for (int i = 0; i < mCount; i++) {
                boolean on = i == mPage;
                float w = on ? active : r * 2f;
                mPaint.setColor(on ? JZSUi.YELLOW : JZSUi.CARD_BORDER);
                c.drawRoundRect(x, cy - r, x + w, cy + r, r, r, mPaint);
                x += w + gap;
            }
        }
    }
}
