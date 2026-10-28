package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class ModMenu {

    private static final int BG_DARK      = Color.argb(230, 20, 20, 35);
    private static final int CARD_BG      = Color.argb(255, 45, 52, 70);
    private static final int CARD_BORDER  = Color.argb(255, 70, 200, 130);
    private static final int TAB_BG       = Color.argb(255, 35, 40, 55);
    private static final int TAB_ACTIVE   = Color.argb(255, 70, 200, 130);
    private static final int NAV_BG       = Color.argb(240, 25, 28, 42);
    private static final int TEXT_TITLE   = Color.WHITE;
    private static final int TEXT_DESC    = Color.argb(200, 180, 190, 210);
    private static final int ACCENT       = Color.argb(255, 70, 200, 130);

    private static FrameLayout sRoot;
    private static boolean sVisible = false;
    private static String sCurrentTab = "All Mods";
    private static LinearLayout sGrid;
    private static Activity sActivity;

    private ModMenu() {}

    // --- Module data ---

    static final class Module {
        final String name;
        final String desc;
        final String category;
        boolean enabled;

        Module(String name, String desc, String category) {
            this.name = name;
            this.desc = desc;
            this.category = category;
        }
    }

    private static final List<Module> MODULES = new ArrayList<>();

    static {
        // Combat
        MODULES.add(new Module("Aimbot",         "Auto-aims at enemies with hit prediction",           "Combat"));
        MODULES.add(new Module("Kill Aura",      "Auto-attacks nearby enemies",                        "Combat"));
        MODULES.add(new Module("Auto Dodge",     "Automatically dodges enemy projectiles",             "Combat"));
        MODULES.add(new Module("Auto Attack",    "Attacks targets without leading movement",           "Combat"));
        MODULES.add(new Module("Auto Shoot",     "Keeps shooting while holding attack button",         "Combat"));
        MODULES.add(new Module("Aim Assist",     "Adjusts aim toward the closest enemy",              "Combat"));
        MODULES.add(new Module("Auto Recall",    "Automatically recalls returning projectiles",        "Combat"));
        MODULES.add(new Module("Auto Hyper",     "Automatically activates hypercharge",                "Combat"));
        MODULES.add(new Module("Disable AutoAim","Disables the auto-aim feature",                     "Combat"));
        MODULES.add(new Module("Hold To Shoot",  "Keeps shooting while holding attack button, or you dodge by hand", "Combat"));

        // Battle
        MODULES.add(new Module("Auto Farm",      "Auto-pushes your brawlers while you're AFK",        "Battle"));
        MODULES.add(new Module("Trophy Mode",    "Sets a trophy goal for auto farm sessions",          "Battle"));
        MODULES.add(new Module("Auto Ready",     "Automatically readies up in the lobby",             "Battle"));
        MODULES.add(new Module("Auto Exit",      "Automatically leaves the results screen",            "Battle"));
        MODULES.add(new Module("Auto Switch",    "Auto switches brawler based on config",              "Battle"));
        MODULES.add(new Module("Skip Replay",    "Skips the replay at the end of each match",         "Battle"));
        MODULES.add(new Module("Anti AFK",       "Prevents AFK detection",                             "Battle"));
        MODULES.add(new Module("Chat Spam",      "Spams chat messages in battle",                      "Battle"));
        MODULES.add(new Module("Shade Mod",      "Enables extra gadget activations",                   "Battle"));
        MODULES.add(new Module("Nani Mod",       "Special module for Nani brawler",                    "Battle"));

        // Visual
        MODULES.add(new Module("X-Ray",          "See enemies even through bushes or invisibility",   "Visual"));
        MODULES.add(new Module("Tracers",        "Draws lines to enemy brawlers",                     "Visual"));
        MODULES.add(new Module("Hitbox Shower",  "Shows customizable hitboxes",                       "Visual"));
        MODULES.add(new Module("Ghost Traps",    "Shows spawned traps after they become invisible",   "Visual"));
        MODULES.add(new Module("Ghost Players",  "Shows afterimages of player positions",             "Visual"));
        MODULES.add(new Module("Range Indicator","Shows enemy attack ranges",                         "Visual"));
        MODULES.add(new Module("Skin Changer",   "Lets you change the skin you want",                "Visual"));
        MODULES.add(new Module("Nick Changer",   "Changes the name displayed in battle",              "Visual"));
        MODULES.add(new Module("Outline Color",  "Changes character outline colors",                   "Visual"));
        MODULES.add(new Module("Gray Mod",       "Gray visual mode for battle",                        "Visual"));

        // Lobby
        MODULES.add(new Module("Spectate Tag",   "Spectate a player by their tag",                    "Lobby"));
        MODULES.add(new Module("Server Changer", "Changes the battle server region",                   "Lobby"));
        MODULES.add(new Module("Future Events",  "Shows future events when selecting a mode",          "Lobby"));
        MODULES.add(new Module("Friendly Room",  "Friendly room options",                              "Lobby"));
        MODULES.add(new Module("Duolingo",       "Shows answers in the Duolingo challenge",            "Lobby"));

        // Misc
        MODULES.add(new Module("Hidden Mode",    "Hides mod menu from recordings and screenshots",    "Misc"));
        MODULES.add(new Module("Auto Kick Bots", "Automatically kicks bots from lobby",               "Misc"));
        MODULES.add(new Module("Show Ammo",      "Shows the ammo of enemies",                         "Misc"));
        MODULES.add(new Module("Show Ping",      "Shows your connection ping",                         "Misc"));
        MODULES.add(new Module("Show Trophies",  "Shows player trophy counts",                         "Misc"));
        MODULES.add(new Module("Gadget Timer",   "Shows gadget cooldown timer",                        "Misc"));
        MODULES.add(new Module("Anti Fade",      "Removes fading animation on appear/disappear",       "Misc"));
        MODULES.add(new Module("Hide Mask",      "Removes black bars at screen edges",                 "Misc"));
        MODULES.add(new Module("No Cam Shake",   "Removes camera shake effects",                       "Misc"));
        MODULES.add(new Module("FPS Settings",   "Configure frame rate limit",                         "Misc"));
    }

    // --- Public API ---

    public static void show(Activity activity) {
        sActivity = activity;
        if (sRoot != null) {
            sRoot.setVisibility(View.VISIBLE);
            sVisible = true;
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buildMenu(activity);
            }
        });
    }

    public static void hide() {
        if (sRoot != null) {
            sRoot.setVisibility(View.GONE);
            sVisible = false;
        }
    }

    public static void toggle(Activity activity) {
        if (sVisible) hide();
        else show(activity);
    }

    public static boolean isVisible() {
        return sVisible;
    }

    // --- Build UI ---

    private static void buildMenu(final Activity activity) {
        Context ctx = activity;
        sRoot = new FrameLayout(ctx);
        sRoot.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout main = new LinearLayout(ctx);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(BG_DARK);

        FrameLayout.LayoutParams mainLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = dp(ctx, 20);
        mainLp.setMargins(margin, dp(ctx, 40), margin, dp(ctx, 20));
        main.setLayoutParams(mainLp);

        GradientDrawable mainBg = new GradientDrawable();
        mainBg.setColor(BG_DARK);
        mainBg.setCornerRadius(dp(ctx, 16));
        main.setBackground(mainBg);
        main.setClipToOutline(true);

        // --- Close button ---
        FrameLayout topBar = new FrameLayout(ctx);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 8)));

        TextView closeBtn = new TextView(ctx);
        closeBtn.setText("✕");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        closeBtn.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams closeLp = new FrameLayout.LayoutParams(
                dp(ctx, 36), dp(ctx, 36));
        closeLp.gravity = Gravity.END | Gravity.TOP;
        closeLp.rightMargin = dp(ctx, 8);
        closeBtn.setLayoutParams(closeLp);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        topBar.addView(closeBtn);
        main.addView(topBar);

        // --- Category tabs ---
        main.addView(buildTabs(ctx));

        // --- Module grid (scrollable) ---
        ScrollView scroll = new ScrollView(ctx);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);

        sGrid = new LinearLayout(ctx);
        sGrid.setOrientation(LinearLayout.VERTICAL);
        sGrid.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        scroll.addView(sGrid);
        main.addView(scroll);

        // --- Bottom navigation bar ---
        main.addView(buildBottomNav(ctx));

        sRoot.addView(main);

        activity.getWindow().addContentView(sRoot, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        sVisible = true;
        populateGrid(ctx);
    }

    // --- Tabs row ---

    private static final String[] TABS = {"All Mods", "Combat", "Battle", "Visual", "Lobby", "Misc"};
    private static final String[] TAB_ICONS = {"•••", "⚔", "✦", "◉", "🏠", "⚙"};
    private static List<TextView> sTabViews = new ArrayList<>();

    private static View buildTabs(final Context ctx) {
        HorizontalScrollView hsv = new HorizontalScrollView(ctx);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        hsv.setPadding(dp(ctx, 8), dp(ctx, 6), dp(ctx, 8), dp(ctx, 6));

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        sTabViews.clear();

        for (int i = 0; i < TABS.length; i++) {
            final String tab = TABS[i];
            TextView tv = new TextView(ctx);
            tv.setText(TAB_ICONS[i] + "  " + tab);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp(ctx, 14), dp(ctx, 8), dp(ctx, 14), dp(ctx, 8));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(ctx, 6);
            tv.setLayoutParams(lp);

            updateTabStyle(ctx, tv, tab.equals(sCurrentTab));

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sCurrentTab = tab;
                    for (int j = 0; j < sTabViews.size(); j++) {
                        updateTabStyle(ctx, sTabViews.get(j), TABS[j].equals(sCurrentTab));
                    }
                    populateGrid(ctx);
                }
            });

            sTabViews.add(tv);
            row.addView(tv);
        }

        hsv.addView(row);
        return hsv;
    }

    private static void updateTabStyle(Context ctx, TextView tv, boolean active) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(ctx, 20));
        if (active) {
            bg.setColor(TAB_ACTIVE);
            tv.setTextColor(Color.WHITE);
        } else {
            bg.setColor(TAB_BG);
            tv.setTextColor(Color.argb(200, 180, 190, 210));
        }
        tv.setBackground(bg);
    }

    // --- Grid population ---

    private static void populateGrid(Context ctx) {
        sGrid.removeAllViews();

        List<Module> filtered = new ArrayList<>();
        for (Module m : MODULES) {
            if (sCurrentTab.equals("All Mods") || m.category.equals(sCurrentTab)) {
                filtered.add(m);
            }
        }

        int columns = 5;
        for (int i = 0; i < filtered.size(); i += columns) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < columns; j++) {
                if (i + j < filtered.size()) {
                    row.addView(buildCard(ctx, filtered.get(i + j)));
                } else {
                    View spacer = new View(ctx);
                    LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, 0, 1f);
                    slp.setMargins(dp(ctx, 4), dp(ctx, 4), dp(ctx, 4), dp(ctx, 4));
                    spacer.setLayoutParams(slp);
                    row.addView(spacer);
                }
            }
            sGrid.addView(row);
        }
    }

    // --- Card ---

    private static View buildCard(final Context ctx, final Module module) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(ctx, 10), dp(ctx, 10), dp(ctx, 10), dp(ctx, 10));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(ctx, 90), 1f);
        lp.setMargins(dp(ctx, 4), dp(ctx, 4), dp(ctx, 4), dp(ctx, 4));
        card.setLayoutParams(lp);

        updateCardStyle(ctx, card, module.enabled);

        // Title
        TextView title = new TextView(ctx);
        title.setText(module.name);
        title.setTextColor(TEXT_TITLE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setMaxLines(1);
        card.addView(title);

        // Description
        TextView desc = new TextView(ctx);
        desc.setText(module.desc);
        desc.setTextColor(TEXT_DESC);
        desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
        desc.setMaxLines(3);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.topMargin = dp(ctx, 4);
        desc.setLayoutParams(dlp);
        card.addView(desc);

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                module.enabled = !module.enabled;
                updateCardStyle(ctx, (LinearLayout) v, module.enabled);
            }
        });

        return card;
    }

    private static void updateCardStyle(Context ctx, LinearLayout card, boolean enabled) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(ctx, 10));
        bg.setColor(CARD_BG);
        if (enabled) {
            bg.setStroke(dp(ctx, 2), ACCENT);
        } else {
            bg.setStroke(dp(ctx, 1), Color.argb(60, 100, 110, 130));
        }
        card.setBackground(bg);
    }

    // --- Bottom nav ---

    private static final String[] NAV_ITEMS = {"MAIN PAGE", "CONFIGURATION", "SETTINGS"};
    private static int sNavIndex = 0;
    private static List<TextView> sNavViews = new ArrayList<>();

    private static View buildBottomNav(final Context ctx) {
        LinearLayout nav = new LinearLayout(ctx);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(NAV_BG);
        nav.setPadding(0, dp(ctx, 10), 0, dp(ctx, 10));
        nav.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        sNavViews.clear();
        for (int i = 0; i < NAV_ITEMS.length; i++) {
            final int idx = i;
            TextView tv = new TextView(ctx);
            tv.setText(NAV_ITEMS[i]);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp(ctx, 20), dp(ctx, 8), dp(ctx, 20), dp(ctx, 8));

            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(nlp);

            updateNavStyle(ctx, tv, i == sNavIndex);

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sNavIndex = idx;
                    for (int j = 0; j < sNavViews.size(); j++) {
                        updateNavStyle(ctx, sNavViews.get(j), j == sNavIndex);
                    }
                }
            });

            sNavViews.add(tv);
            nav.addView(tv);
        }

        return nav;
    }

    private static void updateNavStyle(Context ctx, TextView tv, boolean active) {
        if (active) {
            tv.setTextColor(Color.WHITE);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(ctx, 6));
            bg.setColor(Color.argb(40, 70, 200, 130));
            tv.setBackground(bg);
        } else {
            tv.setTextColor(Color.argb(150, 180, 190, 210));
            tv.setBackground(null);
        }
    }

    // --- Floating toggle button ---

    public static void attachFloatingButton(final Activity activity) {
        final Context ctx = activity;
        final TextView fab = new TextView(ctx);
        fab.setText("JZS");
        fab.setTextColor(Color.WHITE);
        fab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        fab.setTypeface(Typeface.DEFAULT_BOLD);
        fab.setGravity(Gravity.CENTER);

        GradientDrawable fabBg = new GradientDrawable();
        fabBg.setShape(GradientDrawable.OVAL);
        fabBg.setColor(ACCENT);
        fabBg.setStroke(dp(ctx, 2), Color.WHITE);
        fab.setBackground(fabBg);

        final int size = dp(ctx, 48);
        final FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(size, size);
        flp.gravity = Gravity.TOP | Gravity.START;
        flp.topMargin = dp(ctx, 120);
        flp.leftMargin = dp(ctx, 12);
        fab.setLayoutParams(flp);

        fab.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            boolean dragging;
            long downTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        dragging = false;
                        downTime = System.currentTimeMillis();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        if (Math.abs(newX - v.getX()) > 5 || Math.abs(newY - v.getY()) > 5) {
                            dragging = true;
                        }
                        v.setX(newX);
                        v.setY(newY);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!dragging && System.currentTimeMillis() - downTime < 300) {
                            toggle(activity);
                        }
                        return true;
                }
                return false;
            }
        });

        activity.getWindow().addContentView(fab, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private static int dp(Context ctx, int value) {
        float d = ctx.getResources().getDisplayMetrics().density;
        return (int) (value * d + 0.5f);
    }
}
