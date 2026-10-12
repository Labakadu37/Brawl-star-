package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * JZS Brawl mod menu.
 *
 * Every dimension below is expressed in "design pixels" taken from a 1920x1200
 * reference screenshot and multiplied by {@link #sS} at runtime, so the menu
 * keeps the exact same proportions on every screen.
 *
 * Feature state still lives in ht64 (getInt / setInt), same keys as before.
 */
public final class JZSModMenu {

    // ------------------------------------------------------------------ branding
    static final String BRAND_LOGO = "JZS";
    static final String BRAND_TITLE = "JZS Brawl";
    static final String BRAND_SUB = "V5.2";

    // ------------------------------------------------------------------ features
    // { display name, ht64 key, group, sub group }
    private static final String[][] TOGGLES = {
            {"Aimbot", "hernaim", "Battle", "Combat"},
            {"Kill Aura", "hernaura", "Battle", "Combat"},
            {"Wall Check", "aurawall", "Battle", "Combat"},
            {"Gadget Auto", "herngadget", "Battle", "Combat"},
            {"Stealth", "hernstealth", "Battle", "Combat"},
            {"Auto Dodge", "herndodge", "Battle", "Utility"},
            {"Spinner", "hernspinner", "Battle", "Utility"},
            {"Hazards", "hernhaz", "Battle", "Utility"},
            {"Shelly Atk", "dblk0", "Visuals", "Dodge BL"},
            {"Shelly Super", "dblk1", "Visuals", "Dodge BL"},
            {"Primo", "dblk2", "Visuals", "Dodge BL"},
            {"Frank Atk", "dblk3", "Visuals", "Dodge BL"},
            {"Rosa", "dblk4", "Visuals", "Dodge BL"},
            {"Frank Super", "dblk5", "Visuals", "Dodge BL"},
            {"Grom", "dblk6", "Visuals", "Dodge BL"},
            {"Bull", "dblk7", "Visuals", "Dodge BL"},
            {"FPS Unlock", "fps", "Misc", "General"},
            {"Anti-AFK", "hernantiafk", "Misc", "General"},
            {"Nick Changer", "namechanger", "Misc", "General"},
    };

    // { label, ht64 key, owner toggle key, min, max, step, default }
    private static final String[][] SLIDERS = {
            {"Reaction", "dreaction", "herndodge", "50", "180", "10", "100"},
            {"Safety", "dsafety", "herndodge", "50", "180", "10", "100"},
            {"Power", "dpower", "herndodge", "60", "140", "10", "100"},
            {"Distance", "ddistance", "herndodge", "70", "140", "10", "100"},
            {"Commit", "dcommit", "herndodge", "50", "150", "10", "100"},
            {"Burst", "dburst", "herndodge", "300", "1600", "100", "1060"},
            {"Filter", "dfilter", "herndodge", "0", "100", "10", "0"},
            {"React", "aimreact", "hernaim", "0", "300", "10", "100"},
            {"Smooth", "aimslew", "hernaim", "5", "100", "10", "100"},
    };

    private static final String[] GROUPS = {"Battle", "Visuals", "Misc"};

    // ------------------------------------------------------------------ geometry (design px, panel-local)
    static final float PANEL_W = 1726f, PANEL_H = 984f;
    static final float REF_W = 1920f, REF_H = 1200f;
    static final float VIEW_X = 200f, VIEW_Y = 244f, VIEW_W = 1504f, VIEW_H = 736f;
    static final float SEC_X = 20f, SEC_W = 1465f, SEC_TOP = 13f, SEC_GAP = 24f;
    static final float CARD_W = 463f, CARD_H = 143f, CARD_COL = 480.5f, CARD_ROW = 150f;
    static final float TAB_Y = 158f, TAB_H = 80f, TAB_X0 = 217f, TAB_GAP = 17f;
    static final float SIDE_X = 23f, SIDE_W = 135f, SIDE_H = 136f;
    static final float[] SIDE_Y = {144f, 286f, 428f};
    static final float SEARCH_Y = 878f, SEARCH_H = 93f;
    static final float BTN_Y = 22f, BTN_H = 88f;

    // ------------------------------------------------------------------ state
    private static Activity sAct;
    private static boolean sOn;
    private static float sS = 1f;
    private static float sUserScale = 1f;
    private static float sOpacity = 1f;
    private static FrameLayout sRoot;
    private static FrameLayout sPanel;
    private static FrameLayout sTabsRow;
    private static FrameLayout sContent;
    private static ScrollView sScroll;
    private static FrameLayout sModal;
    private static Box sFab;
    private static Box sEditBtn;
    private static View sEditOverlay;
    private static final Box[] sSide = new Box[3];
    private static Box sSearchTile;
    private static Box sConfigBtn;
    private static final ArrayList<Box> sTabs = new ArrayList<Box>();
    private static final ArrayList<Float> sSecTops = new ArrayList<Float>();
    private static final ArrayList<Box> sCards = new ArrayList<Box>();
    private static int sGroup = 0;
    private static int sTabSel = 0;
    private static boolean sOpen;
    private static boolean sSearch;
    private static String sQuery = "";
    private static boolean sEdit;
    private static int sConfig = 0;
    private static float sPanelX = Float.NaN, sPanelY = Float.NaN;
    private static float sFabX = 40f, sFabY = 230f;
    private static SharedPreferences sPrefs;
    private static java.lang.reflect.Method sGetInt, sSetInt;
    private static final Handler sUi = new Handler(Looper.getMainLooper());

    private JZSModMenu() {}

    // ================================================================== entry points

    public static void attach(final Activity act) {
        if (act == null) return;
        if (sOn && sAct == act) return;
        sOn = true;
        sAct = act;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    build(act);
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "mod menu build failed", t);
                }
            }
        });
    }

    public static void show(Context ctx) {
        if (ctx instanceof Activity) attach((Activity) ctx);
    }

    // ================================================================== ht64 bridge

    private static int htGet(String key, int def) {
        try {
            if (sGetInt == null) {
                sGetInt = Class.forName("ht64").getMethod("getInt", String.class, Integer.TYPE);
            }
            return (Integer) sGetInt.invoke(null, key, def);
        } catch (Exception e) {
            return def;
        }
    }

    private static void htSet(String key, int val, int min, int max) {
        try {
            if (sSetInt == null) {
                sSetInt = Class.forName("ht64").getMethod("setInt",
                        String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            }
            sSetInt.invoke(null, key, val, min, max);
        } catch (Exception ignored) {
        }
    }

    private static boolean isOn(String key) {
        return htGet(key, 0) != 0;
    }

    private static int countOn(String group, String sub) {
        int n = 0;
        for (String[] t : TOGGLES) {
            if (t[2].equals(group) && (sub == null || t[3].equals(sub)) && isOn(t[1])) n++;
        }
        return n;
    }

    private static ArrayList<String> subsFor(String group) {
        ArrayList<String> out = new ArrayList<String>();
        for (String[] t : TOGGLES) {
            if (t[2].equals(group) && !out.contains(t[3])) out.add(t[3]);
        }
        return out;
    }

    private static ArrayList<String[]> slidersFor(String key) {
        ArrayList<String[]> out = new ArrayList<String[]>();
        for (String[] s : SLIDERS) if (s[2].equals(key)) out.add(s);
        return out;
    }

    // ================================================================== build

    private static void build(Activity act) {
        Gfx.init(act);
        sPrefs = act.getSharedPreferences("jzs_menu", Context.MODE_PRIVATE);
        sUserScale = sPrefs.getInt("scale", 100) / 100f;
        sOpacity = sPrefs.getInt("opacity", 100) / 100f;
        sConfig = sPrefs.getInt("config", 0);
        sPanelX = sPrefs.getFloat("px", Float.NaN);
        sPanelY = sPrefs.getFloat("py", Float.NaN);
        sFabX = sPrefs.getFloat("fx", 40f);
        sFabY = sPrefs.getFloat("fy", 230f);

        if (sRoot != null && sRoot.getParent() instanceof ViewGroup) {
            ((ViewGroup) sRoot.getParent()).removeView(sRoot);
        }
        sRoot = new FrameLayout(act);
        ViewGroup content = (ViewGroup) act.findViewById(0x1020002);
        if (content == null) content = (ViewGroup) act.getWindow().getDecorView();
        content.addView(sRoot, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sRoot.bringToFront();

        int w = act.getResources().getDisplayMetrics().widthPixels;
        int h = act.getResources().getDisplayMetrics().heightPixels;
        sS = Math.min(Math.max(w, h) / REF_W, Math.min(w, h) / REF_H) * sUserScale;

        buildPanel(act, Math.max(w, h), Math.min(w, h));
        buildFab(act);
        if (sOpen) {
            sPanel.setVisibility(View.VISIBLE);
            sFab.setVisibility(View.GONE);
        } else {
            sPanel.setVisibility(View.GONE);
            sFab.setVisibility(View.VISIBLE);
        }
    }

    private static int px(float d) {
        return Math.round(d * sS);
    }

    private static void place(FrameLayout parent, View v, float x, float y, float w, float h) {
        if (v instanceof Box) {
            float m = Box.M;
            x -= m;
            y -= m;
            w += 2 * m;
            h += 2 * m;
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(px(x + w) - px(x), px(y + h) - px(y));
        lp.leftMargin = px(x);
        lp.topMargin = px(y);
        lp.gravity = Gravity.TOP | Gravity.LEFT;
        parent.addView(v, lp);
    }

    private static void rebuild() {
        if (sAct == null) return;
        sTabs.clear();
        sCards.clear();
        sSecTops.clear();
        sModal = null;
        sEdit = false;
        build(sAct);
    }

    // ------------------------------------------------------------------ panel

    private static void buildPanel(final Activity act, int sw, int sh) {
        sPanel = new FrameLayout(act) {
            @Override
            protected void dispatchDraw(Canvas c) {
                c.save();
                c.scale(sS, sS);
                Gfx.panel(c, PANEL_W, PANEL_H);
                c.restore();
                super.dispatchDraw(c);
            }
        };
        sPanel.setWillNotDraw(false);
        sPanel.setClickable(true);
        sPanel.setAlpha(sOpacity);
        float pw = PANEL_W * sS, ph = PANEL_H * sS;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Math.round(pw), Math.round(ph));
        lp.gravity = Gravity.TOP | Gravity.LEFT;
        sRoot.addView(sPanel, lp);
        float defX = (sRoot.getResources().getDisplayMetrics().widthPixels - pw) / 2f;
        float defY = (sRoot.getResources().getDisplayMetrics().heightPixels - ph) / 2f;
        sPanel.setX(Float.isNaN(sPanelX) ? defX : sPanelX);
        sPanel.setY(Float.isNaN(sPanelY) ? defY : sPanelY);

        buildHeader(act);
        buildSidebar(act);

        sTabsRow = new FrameLayout(act);
        place(sPanel, sTabsRow, 0, TAB_Y - 6, PANEL_W, TAB_H + 12);

        sScroll = new ScrollView(act) {
            @Override
            protected void onScrollChanged(int l, int t, int ol, int ot) {
                super.onScrollChanged(l, t, ol, ot);
                syncTabToScroll();
            }
        };
        sScroll.setVerticalScrollBarEnabled(false);
        sScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        sScroll.setFillViewport(true);
        place(sPanel, sScroll, VIEW_X, VIEW_Y, VIEW_W, VIEW_H);
        sContent = new FrameLayout(act);
        sScroll.addView(sContent, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        refreshGroup(false);
    }

    private static void buildHeader(final Activity act) {
        // logo + title are part of a static view
        View title = new View(act) {
            @Override
            protected void onDraw(Canvas c) {
                c.save();
                c.scale(sS, sS);
                Gfx.logo(c, 28, 27, BRAND_LOGO);
                Gfx.text(c, BRAND_TITLE, 116, 61, 33, 0xFFFFFFFF, Gfx.LILITA, 0, true);
                Gfx.text(c, BRAND_SUB, 115, 84, 18, 0xFFC4C3C8, Gfx.SANS, 0, false);
                c.restore();
            }
        };
        place(sPanel, title, 0, 0, 600, 118);

        final Box lib = new Box(act, Gfx.LIBRARY, new Painter() {
            public void paint(Canvas c, Box b) {
                Gfx.text(c, "Library", 26, 48, 23, 0xFFFFFFFF, Gfx.LILITA, 0, true);
                Gfx.chevron(c, 125, 41.5f, 0xFFFFFFFF);
            }
        });
        lib.click = new Click() {
            public void click(Box b, float x, float y) {
                showLibrary(act, b);
            }
        };
        place(sPanel, lib, 280, BTN_Y, 167, BTN_H);

        Box friends = headerButton(act, Gfx.FRIENDS, 0, "Friends", 43, 80);
        friends.click = new Click() {
            public void click(Box b, float x, float y) {
                showFriends(act);
            }
        };
        place(sPanel, friends, 1002, BTN_Y, 179, BTN_H);

        Box settings = headerButton(act, Gfx.SETTINGS, 1, "Settings", 41, 77);
        settings.click = new Click() {
            public void click(Box b, float x, float y) {
                showSettings(act);
            }
        };
        place(sPanel, settings, 1195, BTN_Y, 184, BTN_H);

        sEditBtn = headerButton(act, Gfx.EDIT, 2, "Edit GUI", 41, 77);
        sEditBtn.click = new Click() {
            public void click(Box b, float x, float y) {
                setEditMode(act, !sEdit);
            }
        };
        place(sPanel, sEditBtn, 1394, BTN_Y, 185, BTN_H);

        Box close = new Box(act, Gfx.CLOSE, new Painter() {
            public void paint(Canvas c, Box b) {
                Gfx.cross(c, 52, 38, 13.5f, 0xFFFFFFFF);
            }
        });
        close.click = new Click() {
            public void click(Box b, float x, float y) {
                closePanel();
            }
        };
        place(sPanel, close, 1594, BTN_Y, 105, BTN_H);
    }

    private static Box headerButton(Activity act, Gfx.Skin skin, final int emoji, final String label,
                                    final float iconCx, final float textX) {
        return new Box(act, skin, new Painter() {
            public void paint(Canvas c, Box b) {
                if (emoji == 0) Gfx.emoji(c, Gfx.EMO_FRIENDS, iconCx, 40, 50, 0);
                else if (emoji == 1) Gfx.emoji(c, Gfx.EMO_GEAR, iconCx, 38, 0, 47);
                else Gfx.emoji(c, Gfx.EMO_WRENCH, iconCx, 38, 48, 0);
                Gfx.text(c, label, textX, 47, 23, 0xFFFFFFFF, Gfx.LILITA, 0, true);
            }
        });
    }

    // ------------------------------------------------------------------ sidebar

    private static void buildSidebar(final Activity act) {
        for (int i = 0; i < 3; i++) {
            final int gi = i;
            Box tile = new Box(act, Gfx.GREY, new Painter() {
                public void paint(Canvas c, Box b) {
                    int face = b.selected ? Gfx.SEL_FACE : Gfx.GREY.face;
                    if (gi == 0) Gfx.target(c, 67.5f, 47, face);
                    else if (gi == 1) Gfx.eye(c, 67.5f, 46);
                    else Gfx.wrench(c, 67.5f, 46, face);
                    Gfx.textCentered(c, GROUPS[gi], 67.5f, 95, 22, 0xFFFFFFFF, Gfx.LILITA, true);
                    int n = countOn(GROUPS[gi], null);
                    if (n > 0) Gfx.badge(c, 107, 26.5f, 16, String.valueOf(n),
                            b.selected ? 0xFF46454B : 0xFF2C2B30, 17);
                }
            });
            tile.click = new Click() {
                public void click(Box b, float x, float y) {
                    if (sSearch) setSearch(act, false);
                    sGroup = gi;
                    sTabSel = 0;
                    refreshGroup(false);
                }
            };
            sSide[i] = tile;
            place(sPanel, tile, SIDE_X, SIDE_Y[i], SIDE_W, SIDE_H);
        }
        sSearchTile = new Box(act, Gfx.GREY, new Painter() {
            public void paint(Canvas c, Box b) {
                Gfx.search(c, 68, 41);
            }
        });
        sSearchTile.click = new Click() {
            public void click(Box b, float x, float y) {
                setSearch(act, !sSearch);
            }
        };
        place(sPanel, sSearchTile, SIDE_X, SEARCH_Y, SIDE_W, SEARCH_H);
    }

    // ------------------------------------------------------------------ tabs + content

    private static void refreshGroup(boolean keepScroll) {
        Activity act = sAct;
        for (int i = 0; i < 3; i++) {
            sSide[i].selected = !sSearch && i == sGroup;
            sSide[i].invalidate();
        }
        sSearchTile.selected = sSearch;
        sSearchTile.invalidate();

        int scrollY = keepScroll ? sScroll.getScrollY() : 0;
        buildTabs(act);
        buildSections(act);
        if (!keepScroll && sTabSel > 0) {
            scrollToSection(sTabSel, false);
        } else {
            final int y = scrollY;
            sScroll.post(new Runnable() {
                public void run() {
                    sScroll.scrollTo(0, y);
                }
            });
        }
    }

    private static void buildTabs(final Activity act) {
        sTabsRow.removeAllViews();
        sTabs.clear();
        if (sSearch) {
            buildSearchField(act);
        } else {
            final ArrayList<String> subs = subsFor(GROUPS[sGroup]);
            float x = TAB_X0;
            for (int i = 0; i < subs.size(); i++) {
                final int ti = i;
                final String sub = subs.get(i);
                final int n = countOn(GROUPS[sGroup], sub);
                float tw = Gfx.textW(sub, 24, Gfx.LILITA, 0);
                float w = n > 0 ? tw + 108 : tw + 56;
                final float textX = n > 0 ? 31 : 28;
                Box tab = new Box(act, Gfx.TAB, new Painter() {
                    public void paint(Canvas c, Box b) {
                        Gfx.text(c, sub, textX, 42, 24, 0xFFFFFFFF, Gfx.LILITA, 0, true);
                        if (n > 0) {
                            float bx = textX + Gfx.textW(sub, 24, Gfx.LILITA, 0) + 30;
                            Gfx.badge(c, bx, 33, 15.5f, String.valueOf(n),
                                    b.selected ? 0xFF48474C : 0xFF2B2A2F, 18);
                        }
                    }
                });
                tab.selected = i == sTabSel;
                tab.click = new Click() {
                    public void click(Box b, float cx, float cy) {
                        selectTab(ti);
                        scrollToSection(ti, true);
                    }
                };
                sTabs.add(tab);
                place(sTabsRow, tab, x, 6, w, TAB_H);
                x += w + TAB_GAP;
            }
        }
        // config dropdown (always visible on the right)
        sConfigBtn = new Box(act, Gfx.CONFIG, new Painter() {
            public void paint(Canvas c, Box b) {
                Gfx.layoutIcon(c, 40.5f, 33.5f);
                Gfx.text(c, configName(sConfig), 69, 44, 23, 0xFFFFFFFF, Gfx.LILITA, 0, true);
                Gfx.chevron(c, 195, 37.5f, 0xFFFFFFFF);
            }
        });
        sConfigBtn.click = new Click() {
            public void click(Box b, float x, float y) {
                showConfigs(act, b);
            }
        };
        place(sTabsRow, sConfigBtn, 1449, 6, 235, TAB_H);
    }

    private static void buildSearchField(final Activity act) {
        Box field = new Box(act, Gfx.TAB_FLAT, new Painter() {
            public void paint(Canvas c, Box b) {
                Gfx.search(c, 34, 33);
            }
        });
        field.selected = true;
        field.pressable = false;
        place(sTabsRow, field, TAB_X0, 6, 1200, TAB_H);

        final EditText et = new EditText(act);
        et.setBackgroundColor(0);
        et.setPadding(0, 0, 0, 0);
        et.setSingleLine(true);
        et.setTextColor(0xFFFFFFFF);
        et.setHintTextColor(0xFFA9A8AE);
        et.setHint("Search features...");
        et.setTypeface(Gfx.LILITA);
        et.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24 * sS);
        et.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        et.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        et.setText(sQuery);
        et.setSelection(sQuery.length());
        et.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

            public void onTextChanged(CharSequence s, int a, int b, int c) {}

            public void afterTextChanged(Editable e) {
                sQuery = e.toString();
                buildSections(act);
            }
        });
        place(sTabsRow, et, TAB_X0 + 64, 6 + 6, 1200 - 80, TAB_H - 22);
        et.requestFocus();
        et.postDelayed(new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(et, 0);
            }
        }, 120);
    }

    private static void setSearch(Activity act, boolean on) {
        sSearch = on;
        if (!on) {
            sQuery = "";
            View f = act.getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && f != null) imm.hideSoftInputFromWindow(f.getWindowToken(), 0);
        }
        sTabSel = 0;
        refreshGroup(false);
    }

    private static void buildSections(final Activity act) {
        sContent.removeAllViews();
        sCards.clear();
        sSecTops.clear();
        float y = SEC_TOP;
        if (sSearch) {
            ArrayList<String[]> hits = new ArrayList<String[]>();
            String q = sQuery.trim().toLowerCase(Locale.US);
            for (String[] t : TOGGLES) {
                if (q.length() == 0 || t[0].toLowerCase(Locale.US).contains(q)
                        || t[3].toLowerCase(Locale.US).contains(q)) hits.add(t);
            }
            y = addSection(act, hits.isEmpty() ? "No results" : "Results", hits, y);
        } else {
            String group = GROUPS[sGroup];
            for (String sub : subsFor(group)) {
                ArrayList<String[]> feats = new ArrayList<String[]>();
                for (String[] t : TOGGLES) if (t[2].equals(group) && t[3].equals(sub)) feats.add(t);
                sSecTops.add(y);
                y = addSection(act, sub, feats, y) + SEC_GAP;
            }
            y -= SEC_GAP;
        }
        // bottom padding so the last section can be scrolled to the top
        View pad = new View(act);
        place(sContent, pad, 0, y, 10, 20);
    }

    private static float addSection(final Activity act, final String label, ArrayList<String[]> feats, float y) {
        int rows = Math.max(1, (feats.size() + 2) / 3);
        final float h = feats.isEmpty() ? 100 : 81 + rows * CARD_ROW;
        final String up = label.toUpperCase(Locale.US);
        View sec = new View(act) {
            @Override
            protected void onDraw(Canvas c) {
                c.save();
                c.scale(sS, sS);
                Gfx.section(c, SEC_W, h, up);
                c.restore();
            }
        };
        place(sContent, sec, SEC_X, y, SEC_W, h);
        for (int i = 0; i < feats.size(); i++) {
            final String[] f = feats.get(i);
            Box card = new Box(act, Gfx.CARD, new Painter() {
                public void paint(Canvas c, Box b) {
                    Gfx.cardContent(c, b.w(), b.faceH(), f[0], b.selected);
                }
            });
            card.selected = isOn(f[1]);
            card.click = new Click() {
                public void click(Box b, float cx, float cy) {
                    if (cx >= 372) {
                        showFeatureSettings(act, f);
                    } else {
                        boolean on = !isOn(f[1]);
                        htSet(f[1], on ? 1 : 0, 0, 1);
                        b.selected = isOn(f[1]);
                        b.invalidate();
                        refreshCounts();
                    }
                }
            };
            sCards.add(card);
            float cx = SEC_X + 21 + (i % 3) * CARD_COL;
            float cy = y + 78 + (i / 3) * CARD_ROW;
            place(sContent, card, Math.round(cx), cy, CARD_W, CARD_H);
        }
        return y + h;
    }

    private static void refreshCounts() {
        for (Box b : sSide) b.invalidate();
        if (!sSearch) {
            int sx = sScroll.getScrollY();
            buildTabs(sAct);
            sScroll.scrollTo(0, sx);
        }
    }

    private static void selectTab(int i) {
        if (i == sTabSel) return;
        sTabSel = i;
        for (int k = 0; k < sTabs.size(); k++) {
            sTabs.get(k).selected = k == i;
            sTabs.get(k).invalidate();
        }
    }

    private static void scrollToSection(final int i, final boolean smooth) {
        if (i < 0 || i >= sSecTops.size()) return;
        final int y = px(sSecTops.get(i) - SEC_TOP);
        sScroll.post(new Runnable() {
            public void run() {
                if (smooth) sScroll.smoothScrollTo(0, y);
                else sScroll.scrollTo(0, y);
            }
        });
    }

    private static void syncTabToScroll() {
        if (sSearch || sSecTops.isEmpty()) return;
        float top = sScroll.getScrollY() / sS + SEC_TOP + 40;
        int sel = 0;
        for (int i = 0; i < sSecTops.size(); i++) if (sSecTops.get(i) <= top) sel = i;
        View child = sScroll.getChildAt(0);
        if (child != null && sScroll.getScrollY() + sScroll.getHeight() >= child.getHeight() - 2) {
            sel = sSecTops.size() - 1;
        }
        selectTab(sel);
    }

    // ------------------------------------------------------------------ open / close / fab

    private static void buildFab(final Activity act) {
        sFab = new Box(act, Gfx.GREY, new Painter() {
            public void paint(Canvas c, Box b) {
                Gfx.logo(c, 15, 12, BRAND_LOGO);
            }
        });
        sFab.draggable = true;
        sFab.click = new Click() {
            public void click(Box b, float x, float y) {
                openPanel();
            }
        };
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(px(100 + 2 * Box.M), px(110 + 2 * Box.M));
        lp.gravity = Gravity.TOP | Gravity.LEFT;
        sRoot.addView(sFab, lp);
        sFab.setX(sFabX * sS);
        sFab.setY(sFabY * sS);
    }

    private static void openPanel() {
        sOpen = true;
        sFab.setVisibility(View.GONE);
        sPanel.setVisibility(View.VISIBLE);
        sPanel.setScaleX(0.92f);
        sPanel.setScaleY(0.92f);
        sPanel.setAlpha(0f);
        sPanel.animate().scaleX(1f).scaleY(1f).alpha(sOpacity).setDuration(160).start();
        for (Box b : sCards) b.invalidate();
        for (Box b : sSide) b.invalidate();
    }

    private static void closePanel() {
        sOpen = false;
        if (sEdit) setEditMode(sAct, false);
        dismissModal();
        sPanel.animate().scaleX(0.92f).scaleY(0.92f).alpha(0f).setDuration(130).withEndAction(new Runnable() {
            public void run() {
                sPanel.setVisibility(View.GONE);
                sFab.setVisibility(View.VISIBLE);
            }
        }).start();
    }

    // ------------------------------------------------------------------ edit gui (drag panel)

    private static void setEditMode(Activity act, boolean on) {
        sEdit = on;
        sEditBtn.selected = on;
        sEditBtn.invalidate();
        if (on) {
            sEditOverlay = new View(act) {
                float lx, ly, dx, dy;
                boolean moved;

                @Override
                protected void onDraw(Canvas c) {
                    c.save();
                    c.scale(sS, sS);
                    Gfx.editOverlay(c, PANEL_W, PANEL_H);
                    c.restore();
                }

                @Override
                public boolean onTouchEvent(MotionEvent e) {
                    switch (e.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            lx = e.getRawX();
                            ly = e.getRawY();
                            dx = dy = 0;
                            moved = false;
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            float mx = e.getRawX() - lx, my = e.getRawY() - ly;
                            lx = e.getRawX();
                            ly = e.getRawY();
                            dx += mx;
                            dy += my;
                            if (Math.abs(dx) + Math.abs(dy) > 8) moved = true;
                            sPanel.setX(sPanel.getX() + mx);
                            sPanel.setY(sPanel.getY() + my);
                            return true;
                        case MotionEvent.ACTION_UP:
                            float x = e.getX() / sS, y = e.getY() / sS;
                            if (!moved && x >= 1394 && x <= 1579 && y >= BTN_Y && y <= BTN_Y + BTN_H) {
                                setEditMode(sAct, false);
                            }
                            sPrefs.edit().putFloat("px", sPanel.getX()).putFloat("py", sPanel.getY()).apply();
                            sPanelX = sPanel.getX();
                            sPanelY = sPanel.getY();
                            return true;
                    }
                    return true;
                }
            };
            place(sPanel, sEditOverlay, 0, 0, PANEL_W, PANEL_H);
            sEditBtn.bringToFront();
        } else if (sEditOverlay != null) {
            sPanel.removeView(sEditOverlay);
            sEditOverlay = null;
        }
    }

    // ------------------------------------------------------------------ modals / popups

    private static void dismissModal() {
        if (sModal != null && sModal.getParent() != null) {
            ((ViewGroup) sModal.getParent()).removeView(sModal);
        }
        sModal = null;
    }

    /** A full-panel layer; dim=true darkens the menu behind. Tapping outside closes it. */
    private static FrameLayout openLayer(Activity act, final boolean dim) {
        dismissModal();
        FrameLayout layer = new FrameLayout(act) {
            @Override
            protected void dispatchDraw(Canvas c) {
                if (dim) {
                    c.save();
                    c.scale(sS, sS);
                    Gfx.scrim(c, PANEL_W, PANEL_H);
                    c.restore();
                }
                super.dispatchDraw(c);
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                if (e.getActionMasked() == MotionEvent.ACTION_UP) dismissModal();
                return true;
            }
        };
        layer.setWillNotDraw(false);
        place(sPanel, layer, 0, 0, PANEL_W, PANEL_H);
        sModal = layer;
        return layer;
    }

    private static View modalFrame(Activity act, final String title, final float w, final float h) {
        View v = new View(act) {
            @Override
            protected void onDraw(Canvas c) {
                c.save();
                c.scale(sS, sS);
                Gfx.modal(c, w, h, title);
                c.restore();
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return true;
            }
        };
        return v;
    }

    private static void addModalClose(Activity act, FrameLayout layer, float mx, float my, float mw) {
        Box x = new Box(act, Gfx.CLOSE, new Painter() {
            public void paint(Canvas c, Box b) {
                Gfx.cross(c, 34, 27, 9f, 0xFFFFFFFF);
            }
        });
        x.click = new Click() {
            public void click(Box b, float cx, float cy) {
                dismissModal();
            }
        };
        place(layer, x, mx + mw - 92, my + 16, 68, 64);
    }

    private static void showFeatureSettings(final Activity act, final String[] feat) {
        final ArrayList<String[]> sl = slidersFor(feat[1]);
        FrameLayout layer = openLayer(act, true);
        float mw = 900;
        float mh = sl.isEmpty() ? 250 : 112 + sl.size() * 104 + 20;
        mh = Math.min(mh, PANEL_H - 60);
        float mx = (PANEL_W - mw) / 2f, my = (PANEL_H - mh) / 2f;
        place(layer, modalFrame(act, feat[0], mw, mh), mx, my, mw, mh);
        addModalClose(act, layer, mx, my, mw);
        if (sl.isEmpty()) {
            View msg = new View(act) {
                @Override
                protected void onDraw(Canvas c) {
                    c.save();
                    c.scale(sS, sS);
                    Gfx.textCentered(c, "No settings for this feature", 450, 60, 24, 0xFFA9A8AE, Gfx.LILITA, false);
                    c.restore();
                }
            };
            place(layer, msg, mx, my + 120, mw, 100);
            return;
        }
        ScrollView sv = new ScrollView(act);
        sv.setVerticalScrollBarEnabled(false);
        sv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        place(layer, sv, mx + 24, my + 104, mw - 48, mh - 124);
        FrameLayout list = new FrameLayout(act);
        sv.addView(list, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < sl.size(); i++) {
            String[] s = sl.get(i);
            addSliderRow(act, list, 0, i * 104, mw - 48, s[0], s[1],
                    Integer.parseInt(s[3]), Integer.parseInt(s[4]), Integer.parseInt(s[5]),
                    Integer.parseInt(s[6]), null);
        }
    }

    interface ValueSink {
        void set(int v);
    }

    private static void addSliderRow(Activity act, FrameLayout parent, float x, float y, float w,
                                     final String label, final String key, final int min, final int max,
                                     final int step, final int def, final ValueSink sink) {
        final int[] val = {sink == null ? clamp(htGet(key, def), min, max) : def};
        final float trackX = 300, trackW = w - 300 - 170;
        final View row = new View(act) {
            boolean drag;

            @Override
            protected void onDraw(Canvas c) {
                c.save();
                c.scale(sS, sS);
                Gfx.sliderRow(c, w, label, val[0], min, max, trackX, trackW);
                c.restore();
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                float ex = e.getX() / sS;
                int a = e.getActionMasked();
                if (a == MotionEvent.ACTION_DOWN) {
                    drag = ex >= trackX - 20 && ex <= trackX + trackW + 20;
                    if (drag && getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (drag && (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_MOVE || a == MotionEvent.ACTION_UP)) {
                    float t = Math.max(0f, Math.min(1f, (ex - trackX) / trackW));
                    int v = min + Math.round(t * (max - min) / step) * step;
                    v = clamp(v, min, max);
                    if (v != val[0]) {
                        val[0] = v;
                        commit(key, v, min, max, sink);
                        invalidate();
                    }
                }
                return true;
            }
        };
        place(parent, row, x, y, w - 170, 96);
        final Box minus = stepButton(act, false);
        final Box plus = stepButton(act, true);
        minus.click = new Click() {
            public void click(Box b, float cx, float cy) {
                val[0] = clamp(val[0] - step, min, max);
                commit(key, val[0], min, max, sink);
                row.invalidate();
            }
        };
        plus.click = new Click() {
            public void click(Box b, float cx, float cy) {
                val[0] = clamp(val[0] + step, min, max);
                commit(key, val[0], min, max, sink);
                row.invalidate();
            }
        };
        place(parent, minus, x + w - 160, y + 14, 72, 70);
        place(parent, plus, x + w - 78, y + 14, 72, 70);
    }

    private static void commit(String key, int v, int min, int max, ValueSink sink) {
        if (sink != null) sink.set(v);
        else htSet(key, v, min, max);
    }

    private static Box stepButton(Activity act, final boolean plus) {
        return new Box(act, Gfx.GREY, new Painter() {
            public void paint(Canvas c, Box b) {
                Gfx.plusMinus(c, b.w() / 2f, 28, plus);
            }
        });
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static void showSettings(final Activity act) {
        FrameLayout layer = openLayer(act, true);
        float mw = 900, mh = 112 + 2 * 104 + 20;
        float mx = (PANEL_W - mw) / 2f, my = (PANEL_H - mh) / 2f;
        place(layer, modalFrame(act, "Settings", mw, mh), mx, my, mw, mh);
        addModalClose(act, layer, mx, my, mw);
        FrameLayout list = new FrameLayout(act);
        place(layer, list, mx + 24, my + 104, mw - 48, 2 * 104);
        addSliderRow(act, list, 0, 0, mw - 48, "Menu Scale", null, 60, 130, 5,
                Math.round(sUserScale * 100), new ValueSink() {
                    public void set(int v) {
                        sPrefs.edit().putInt("scale", v).apply();
                        sUi.removeCallbacksAndMessages("scale");
                        sUi.postAtTime(new Runnable() {
                            public void run() {
                                sOpen = true;
                                sPanelX = Float.NaN;
                                sPanelY = Float.NaN;
                                sPrefs.edit().remove("px").remove("py").apply();
                                rebuild();
                            }
                        }, "scale", android.os.SystemClock.uptimeMillis() + 700);
                    }
                });
        addSliderRow(act, list, 0, 104, mw - 48, "Opacity", null, 40, 100, 5,
                Math.round(sOpacity * 100), new ValueSink() {
                    public void set(int v) {
                        sOpacity = v / 100f;
                        sPanel.setAlpha(sOpacity);
                        sPrefs.edit().putInt("opacity", v).apply();
                    }
                });
    }

    private static void showFriends(final Activity act) {
        FrameLayout layer = openLayer(act, true);
        final float mw = 760, mh = 330;
        float mx = (PANEL_W - mw) / 2f, my = (PANEL_H - mh) / 2f;
        place(layer, modalFrame(act, "Friends", mw, mh), mx, my, mw, mh);
        addModalClose(act, layer, mx, my, mw);
        View body = new View(act) {
            @Override
            protected void onDraw(Canvas c) {
                c.save();
                c.scale(sS, sS);
                Gfx.emoji(c, Gfx.EMO_FRIENDS, 80, 60, 90, 0);
                Gfx.text(c, "JZ Brawl community", 150, 50, 28, 0xFFFFFFFF, Gfx.LILITA, 0, true);
                Gfx.text(c, "Discord : jzbrawl.fr", 150, 92, 22, 0xFFC4C3C8, Gfx.LILITA, 0, false);
                Gfx.text(c, "Telegram : JZ Brawl | Cheating", 150, 128, 22, 0xFFC4C3C8, Gfx.LILITA, 0, false);
                c.restore();
            }
        };
        place(layer, body, mx, my + 110, mw, 200);
    }

    private static void showLibrary(final Activity act, Box anchor) {
        String[] items = new String[GROUPS.length];
        for (int i = 0; i < GROUPS.length; i++) items[i] = GROUPS[i];
        dropdown(act, 280, BTN_Y + BTN_H + 8, 300, items, sSearch ? -1 : sGroup, new ValueSink() {
            public void set(int v) {
                if (sSearch) sSearch = false;
                sQuery = "";
                sGroup = v;
                sTabSel = 0;
                refreshGroup(false);
            }
        });
    }

    private static String configName(int i) {
        return "Config " + (i + 1);
    }

    private static void showConfigs(final Activity act, Box anchor) {
        FrameLayout layer = openLayer(act, false);
        final float x = 1449, y = TAB_Y + TAB_H + 8, w = 235 + 150;
        final float lx = x + 235 - w;
        final float rowH = 74;
        final float h = 3 * rowH + 24;
        place(layer, dropFrame(act, w, h), lx, y, w, h);
        for (int i = 0; i < 3; i++) {
            final int ci = i;
            Box name = new Box(act, Gfx.TAB_FLAT, new Painter() {
                public void paint(Canvas c, Box b) {
                    Gfx.text(c, configName(ci), 22, 38, 22, 0xFFFFFFFF, Gfx.LILITA, 0, true);
                }
            });
            name.selected = i == sConfig;
            name.click = new Click() {
                public void click(Box b, float cx, float cy) {
                    loadConfig(ci);
                    dismissModal();
                }
            };
            place(layer, name, lx + 12, y + 12 + i * rowH, w - 24 - 110, rowH - 8);
            Box save = new Box(act, Gfx.EDIT, new Painter() {
                public void paint(Canvas c, Box b) {
                    Gfx.textCentered(c, "Save", b.w() / 2f, 38, 21, 0xFFFFFFFF, Gfx.LILITA, true);
                }
            });
            save.click = new Click() {
                public void click(Box b, float cx, float cy) {
                    saveConfig(ci);
                    dismissModal();
                }
            };
            place(layer, save, lx + w - 12 - 102, y + 12 + i * rowH, 102, rowH - 8);
        }
    }

    private static View dropFrame(Activity act, final float w, final float h) {
        return new View(act) {
            @Override
            protected void onDraw(Canvas c) {
                c.save();
                c.scale(sS, sS);
                Gfx.dropdown(c, w, h);
                c.restore();
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return true;
            }
        };
    }

    private static void dropdown(Activity act, float x, float y, float w, final String[] items, int sel,
                                 final ValueSink sink) {
        FrameLayout layer = openLayer(act, false);
        float rowH = 74;
        float h = items.length * rowH + 24;
        place(layer, dropFrame(act, w, h), x, y, w, h);
        for (int i = 0; i < items.length; i++) {
            final int ii = i;
            Box row = new Box(act, Gfx.TAB_FLAT, new Painter() {
                public void paint(Canvas c, Box b) {
                    Gfx.text(c, items[ii], 22, 38, 22, 0xFFFFFFFF, Gfx.LILITA, 0, true);
                }
            });
            row.selected = i == sel;
            row.click = new Click() {
                public void click(Box b, float cx, float cy) {
                    dismissModal();
                    sink.set(ii);
                }
            };
            place(layer, row, x + 12, y + 12 + i * rowH, w - 24, rowH - 8);
        }
    }

    private static void saveConfig(int slot) {
        SharedPreferences.Editor e = sPrefs.edit();
        for (String[] t : TOGGLES) e.putInt("c" + slot + "_" + t[1], htGet(t[1], 0));
        for (String[] s : SLIDERS) e.putInt("c" + slot + "_" + s[1], htGet(s[1], Integer.parseInt(s[6])));
        e.putBoolean("c" + slot + "_saved", true);
        e.putInt("config", slot);
        e.apply();
        sConfig = slot;
        sConfigBtn.invalidate();
    }

    private static void loadConfig(int slot) {
        sConfig = slot;
        sPrefs.edit().putInt("config", slot).apply();
        if (sPrefs.getBoolean("c" + slot + "_saved", false)) {
            for (String[] t : TOGGLES) {
                htSet(t[1], sPrefs.getInt("c" + slot + "_" + t[1], 0), 0, 1);
            }
            for (String[] s : SLIDERS) {
                int def = Integer.parseInt(s[6]);
                htSet(s[1], sPrefs.getInt("c" + slot + "_" + s[1], def),
                        Integer.parseInt(s[3]), Integer.parseInt(s[4]));
            }
        }
        refreshGroup(true);
    }

    // ================================================================== views

    interface Painter {
        void paint(Canvas c, Box b);
    }

    interface Click {
        void click(Box b, float x, float y);
    }

    /** 3D Supercell-style block: dark outline, top highlight, face, dark lip underneath. */
    static final class Box extends View {
        /** extra margin around every box so slanted shapes are not clipped (design px) */
        static final float M = 6f;
        final Gfx.Skin skin;
        final Painter painter;
        Click click;
        boolean selected;
        boolean down;
        boolean pressable = true;
        boolean draggable;
        private float dX, dY, rawX0, rawY0;
        private boolean dragged;

        Box(Context ctx, Gfx.Skin skin, Painter painter) {
            super(ctx);
            this.skin = skin;
            this.painter = painter;
        }

        float w() {
            return getWidth() / sS - 2 * M;
        }

        float h() {
            return getHeight() / sS - 2 * M;
        }

        float faceH() {
            return h() - skin.lip;
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = w(), h = h();
            float dy = down ? skin.lip - 4 : 0;
            c.save();
            c.scale(sS, sS);
            c.translate(M, M);
            Gfx.box(c, w, h, skin, selected, dy);
            if (painter != null) {
                c.translate(0, dy);
                painter.paint(c, this);
            }
            c.restore();
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (!pressable && click == null) return false;
                    down = pressable;
                    dragged = false;
                    rawX0 = e.getRawX();
                    rawY0 = e.getRawY();
                    dX = getX() - rawX0;
                    dY = getY() - rawY0;
                    if (getParent() != null && draggable) getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (draggable) {
                        if (Math.abs(e.getRawX() - rawX0) + Math.abs(e.getRawY() - rawY0) > 12 * sS) dragged = true;
                        if (dragged) {
                            setX(e.getRawX() + dX);
                            setY(e.getRawY() + dY);
                            if (down) {
                                down = false;
                                invalidate();
                            }
                        }
                    } else {
                        boolean in = e.getX() >= 0 && e.getY() >= 0 && e.getX() <= getWidth() && e.getY() <= getHeight();
                        if (!in && down) {
                            down = false;
                            invalidate();
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    boolean wasDown = down || !pressable;
                    down = false;
                    invalidate();
                    if (draggable && dragged) {
                        sFabX = getX() / sS;
                        sFabY = getY() / sS;
                        if (sPrefs != null) sPrefs.edit().putFloat("fx", sFabX).putFloat("fy", sFabY).apply();
                        return true;
                    }
                    boolean inside = e.getX() >= 0 && e.getY() >= 0 && e.getX() <= getWidth() && e.getY() <= getHeight();
                    if (wasDown && inside && click != null) {
                        try {
                            click.click(this, e.getX() / sS - M, e.getY() / sS - M);
                        } catch (Throwable t) {
                            android.util.Log.e("JZS", "menu click failed", t);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    down = false;
                    invalidate();
                    return true;
            }
            return true;
        }
    }

    // ================================================================== drawing (design px)
    // GFX-BEGIN
    static final class Gfx {
        static Typeface LILITA = Typeface.DEFAULT_BOLD;
        static Typeface SANS = Typeface.DEFAULT;
        static Typeface SANS_BOLD = Typeface.DEFAULT_BOLD;
        static Typeface COND = Typeface.DEFAULT_BOLD;
        static Bitmap EMO_FRIENDS, EMO_GEAR, EMO_WRENCH;
        static boolean sInit;

        static final int SEL_FACE = 0xFF616369;
        static final int SEL_BORDER = 0xFFE8E7EC;

        static final class Skin {
            int outline, hl, face, face2, seam, lipTop, lipBot, radius, lip, selFace, selBorder;
            float skew;

            Skin(int outline, int hl, int face, int face2, int seam, int lipTop, int lipBot, int radius, int lip) {
                this.outline = outline;
                this.hl = hl;
                this.face = face;
                this.face2 = face2;
                this.seam = seam;
                this.lipTop = lipTop;
                this.lipBot = lipBot;
                this.radius = radius;
                this.lip = lip;
                this.selFace = SEL_FACE;
                this.selBorder = SEL_BORDER;
            }
        }

        // outline, highlight, face, faceBottom, seam, lipTop, lipBottom, radius, lip
        static final Skin GREY = new Skin(0xFF141318, 0xFF8D8C91, 0xFF46454A, 0, 0,
                0xFF16151A, 0xFF0C0B10, 12, 13);
        static final Skin TAB_FLAT = new Skin(0xFF141318, 0xFF88878C, 0xFF46454A, 0, 0,
                0xFF16151A, 0xFF0C0B10, 11, 11);
        static final Skin TAB = new Skin(0xFF141318, 0xFF88878C, 0xFF46454A, 0, 0,
                0xFF16151A, 0xFF0C0B10, 11, 11);
        static final Skin CARD = new Skin(0xFF19181D, 0xFF7D7C81, 0xFF46454A, 0, 0,
                0xFF16151A, 0xFF0C0B10, 12, 11);
        static final Skin LIBRARY = new Skin(0xFF17161C, 0xFFB9B8C0, 0xFF858390, 0xFF737582, 0xFF141319,
                0xFF45434E, 0xFF2A2931, 11, 10);
        static final Skin CONFIG = new Skin(0xFF141318, 0xFFB7B6BB, 0xFF7B7D8A, 0xFF62606E, 0xFF17161C,
                0xFF48474F, 0xFF28272F, 11, 11);
        static final Skin FRIENDS = new Skin(0xFF290A34, 0xFFE095F4, 0xFFC152DD, 0xFFBF52E0, 0xFF2D013C,
                0xFF61278B, 0xFF5A2776, 11, 10);
        static final Skin SETTINGS = new Skin(0xFF2F1400, 0xFFFDC087, 0xFFF9A052, 0xFFF59E55, 0xFF331000,
                0xFFA25328, 0xFF935733, 11, 10);
        static final Skin EDIT = new Skin(0xFF071D34, 0xFF85CFFC, 0xFF3FADF2, 0xFF42A4E8, 0xFF002441,
                0xFF1F5891, 0xFF20507E, 11, 10);
        static final Skin CLOSE = new Skin(0xFF310806, 0xFFFE877F, 0xFFE93B34, 0xFFE13B36, 0xFF490400,
                0xFF7B1417, 0xFF761C13, 11, 10);

        static {
            // header buttons and tabs are slightly slanted (top edge shifted right)
            for (Skin s : new Skin[]{FRIENDS, SETTINGS, EDIT, CLOSE, LIBRARY, CONFIG, TAB}) s.skew = 7f;
            // colored skins: when "selected" (e.g. Edit GUI active) keep their own colors
            for (Skin s : new Skin[]{FRIENDS, SETTINGS, EDIT, CLOSE, LIBRARY, CONFIG}) {
                s.selFace = s.face;
                s.selBorder = 0xFFFFFFFF;
            }
        }

        private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private static final RectF R = new RectF();
        private static final Path PATH = new Path();

        static void init(Context ctx) {
            if (sInit) return;
            sInit = true;
            String[] fonts = {"font/LilitaOne-Regular.ttf", "fonts/LilitaOne-Regular.ttf"};
            for (String f : fonts) {
                try {
                    LILITA = Typeface.createFromAsset(ctx.getAssets(), f);
                    break;
                } catch (Throwable ignored) {
                }
            }
            SANS = Typeface.create("sans-serif", Typeface.NORMAL);
            SANS_BOLD = Typeface.create("sans-serif", Typeface.BOLD);
            COND = Typeface.create("sans-serif-condensed", Typeface.BOLD);
            EMO_FRIENDS = decode(Emoji.FRIENDS);
            EMO_GEAR = decode(Emoji.GEAR);
            EMO_WRENCH = decode(Emoji.WRENCH);
        }

        static Bitmap decode(String b64) {
            try {
                byte[] d = Base64.decode(b64, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(d, 0, d.length);
            } catch (Throwable t) {
                return null;
            }
        }

        private static void fill(int color) {
            P.reset();
            P.setAntiAlias(true);
            P.setFilterBitmap(true);
            P.setStyle(Paint.Style.FILL);
            P.setColor(color);
        }

        private static void stroke(int color, float w) {
            P.reset();
            P.setAntiAlias(true);
            P.setStyle(Paint.Style.STROKE);
            P.setStrokeWidth(w);
            P.setStrokeCap(Paint.Cap.ROUND);
            P.setStrokeJoin(Paint.Join.ROUND);
            P.setColor(color);
        }

        static void rr(Canvas c, float l, float t, float r, float b, float rad) {
            if (r <= l || b <= t) return;
            rad = Math.max(0, Math.min(rad, Math.min((r - l) / 2f, (b - t) / 2f)));
            R.set(l, t, r, b);
            c.drawRoundRect(R, rad, rad, P);
        }

        static void vgrad(float top, float bottom, int c1, int c2) {
            P.setShader(new LinearGradient(0, top, 0, bottom, c1, c2, Shader.TileMode.CLAMP));
        }

        /** The 3D block. w/h: full size incl. outline and lip. dy: pressed offset. */
        static void box(Canvas c, float w, float h, Skin s, boolean sel, float dy) {
            float o = 3f;
            float r = s.radius;
            float faceBottom = h - o - s.lip;
            c.save();
            if (s.skew != 0) {
                float k = s.skew / Math.max(1f, h - s.lip);
                c.translate(0, h / 2f);
                c.skew(-k, 0);
                c.translate(0, -h / 2f);
            }
            // outline
            fill(s.outline);
            rr(c, 0, 0, w, h, r + 2);
            // lip
            fill(s.lipTop);
            vgrad(faceBottom, h - o, s.lipTop, s.lipBot);
            rr(c, o, o + dy, w - o, h - o, r);
            P.setShader(null);
            // seam (dark line between face and lip on colored buttons)
            if (s.seam != 0) {
                fill(s.seam);
                rr(c, o, o + dy, w - o, faceBottom + 3 + dy, r);
            }
            if (sel) {
                fill(s.selBorder);
                rr(c, o, o + dy, w - o, faceBottom + dy, r);
                int f1 = s.selFace;
                fill(f1);
                if (s.face2 != 0 && s.selFace == s.face) vgrad(o + 3 + dy, faceBottom + dy, s.face, s.face2);
                rr(c, o + 3, o + 3 + dy, w - o - 3, faceBottom - 3 + dy, r - 3);
                P.setShader(null);
            } else {
                fill(s.hl);
                rr(c, o, o + dy, w - o, faceBottom + dy, r);
                fill(s.face);
                if (s.face2 != 0) vgrad(o + 3 + dy, faceBottom + dy, s.face, s.face2);
                rr(c, o, o + 3 + dy, w - o, faceBottom + dy, r - 1);
                P.setShader(null);
            }
            c.restore();
        }

        static float textW(String t, float size, Typeface tf, float spacing) {
            P.reset();
            P.setAntiAlias(true);
            P.setTypeface(tf);
            P.setTextSize(size);
            float w = P.measureText(t);
            if (spacing != 0 && t.length() > 1) w += spacing * (t.length() - 1);
            return w;
        }

        static void text(Canvas c, String t, float x, float base, float size, int color, Typeface tf,
                         float spacing, boolean shadow) {
            for (int pass = shadow ? 0 : 1; pass < 2; pass++) {
                P.reset();
                P.setAntiAlias(true);
                P.setTypeface(tf);
                P.setTextSize(size);
                P.setColor(pass == 0 ? 0x73000000 : color);
                float oy = pass == 0 ? 2f : 0f;
                if (spacing == 0) {
                    c.drawText(t, x, base + oy, P);
                } else {
                    float cx = x;
                    for (int i = 0; i < t.length(); i++) {
                        String ch = t.substring(i, i + 1);
                        c.drawText(ch, cx, base + oy, P);
                        cx += P.measureText(ch) + spacing;
                    }
                }
            }
        }

        static void textCentered(Canvas c, String t, float cx, float base, float size, int color, Typeface tf,
                                 boolean shadow) {
            float w = textW(t, size, tf, 0);
            text(c, t, cx - w / 2f, base, size, color, tf, 0, shadow);
        }

        static void badge(Canvas c, float cx, float cy, float r, String n, int fill, float size) {
            fill(fill);
            c.drawCircle(cx, cy, r, P);
            float w = textW(n, size, LILITA, 0);
            text(c, n, cx - w / 2f, cy + size * 0.35f, size, 0xFFFFFFFF, LILITA, 0, false);
        }

        static void emoji(Canvas c, Bitmap b, float cx, float cy, float w, float h) {
            if (b == null) return;
            if (w == 0) w = h * b.getWidth() / (float) b.getHeight();
            if (h == 0) h = w * b.getHeight() / (float) b.getWidth();
            fill(0xFFFFFFFF);
            R.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
            c.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), R, P);
        }

        // ---------------------------------------------------------- composite pieces

        static void panel(Canvas c, float w, float h) {
            fill(0xFF3A393E);
            rr(c, 0, 0, w, h, 30);
            fill(0xFF222126);
            rr(c, 4, 4, w - 4, h - 4, 26);
            // header
            c.save();
            c.clipRect(4, 4, w - 4, 118);
            fill(0xFF2C2B30);
            rr(c, 4, 4, w - 4, 160, 26);
            fill(0xFF4E4D52);
            c.drawRect(30, 4, w - 30, 5, P);
            c.restore();
            fill(0xFF100F14);
            c.drawRect(4, 118, w - 4, 122, P);
            // sidebar divider
            fill(0xFF0E0D12);
            c.drawRect(177, 122, 181, h - 4, P);
        }

        static void logo(Canvas c, float x, float y, String txt) {
            fill(0xFF0C0B10);
            rr(c, x, y + 3, x + 70, y + 72, 13);
            fill(0xFF3D3C41);
            rr(c, x, y, x + 70, y + 69, 13);
            fill(0xFF000000);
            rr(c, x + 3, y + 3, x + 67, y + 66, 10);
            float size = 29;
            float tw = textW(txt, size, COND, 0);
            if (tw > 56) {
                size = size * 56 / tw;
                tw = 56;
            }
            text(c, txt, x + 35 - tw / 2f, y + 45, size, 0xFFFFFFFF, COND, 0, false);
        }

        static void section(Canvas c, float w, float h, String label) {
            fill(0xFF4B4A4F);
            rr(c, 0, 0, w, h, 16);
            fill(0xFF28272C);
            rr(c, 3, 3, w - 3, h - 3, 13);
            fill(0xFF212025);
            c.drawRect(3, 57, w - 3, 61, P);
            text(c, label, 27, 38, 18, 0xFF8B8A8F, SANS_BOLD, 3.2f, false);
        }

        static void cardContent(Canvas c, float w, float faceH, String name, boolean on) {
            // settings divider + area
            int face = on ? SEL_FACE : CARD.face;
            fill(on ? 0xFF4E5158 : 0xFF3B3A3F);
            c.drawRect(372, 6, 375, faceH - 3 - (on ? 3 : 0), P);
            sliders(c, 417, 66, face);
            text(c, name, 28, 51, 29, 0xFFFFFFFF, LILITA, 0, true);
            pill(c, 31, 75, on);
        }

        static float pill(Canvas c, float x, float y, boolean on) {
            String t = on ? "ON" : "OFF";
            float tw = textW(t, 15, SANS_BOLD, 1.5f);
            float w = tw + 24;
            fill(on ? 0xFF9A9DA4 : 0xFF2C2B30);
            rr(c, x, y, x + w, y + 30, 15);
            text(c, t, x + 12, y + 20.5f, 15, on ? 0xFF3B3E45 : 0xFF8B8A8F, SANS_BOLD, 1.5f, false);
            return w;
        }

        static void scrim(Canvas c, float w, float h) {
            fill(0x99000000);
            rr(c, 0, 0, w, h, 30);
        }

        static void modal(Canvas c, float w, float h, String title) {
            fill(0x66000000);
            rr(c, 0, 8, w, h + 8, 26);
            fill(0xFF3A393E);
            rr(c, 0, 0, w, h, 26);
            fill(0xFF222126);
            rr(c, 4, 4, w - 4, h - 4, 22);
            c.save();
            c.clipRect(4, 4, w - 4, 92);
            fill(0xFF2C2B30);
            rr(c, 4, 4, w - 4, 140, 22);
            c.restore();
            fill(0xFF100F14);
            c.drawRect(4, 92, w - 4, 96, P);
            text(c, title, 32, 61, 33, 0xFFFFFFFF, LILITA, 0, true);
        }

        static void dropdown(Canvas c, float w, float h) {
            fill(0x66000000);
            rr(c, 0, 6, w, h + 6, 18);
            fill(0xFF3A393E);
            rr(c, 0, 0, w, h, 18);
            fill(0xFF28272C);
            rr(c, 3, 3, w - 3, h - 3, 15);
        }

        static void editOverlay(Canvas c, float w, float h) {
            fill(0x33000000);
            rr(c, 0, 0, w, h, 30);
            stroke(0xFF41ACF2, 5);
            P.setPathEffect(new android.graphics.DashPathEffect(new float[]{22, 14}, 0));
            R.set(6, 6, w - 6, h - 6);
            c.drawRoundRect(R, 26, 26, P);
            P.setPathEffect(null);
            textCentered(c, "Drag to move the menu  -  tap Edit GUI to finish", w / 2f, h / 2f, 30,
                    0xFFFFFFFF, LILITA, true);
        }

        static void sliderRow(Canvas c, float w, String label, int v, int min, int max, float tx, float tw) {
            fill(0xFF19181D);
            rr(c, 0, 0, w, 96, 14);
            fill(0xFF2E2D32);
            rr(c, 2, 2, w - 2, 94, 12);
            text(c, label, 22, 57, 26, 0xFFFFFFFF, LILITA, 0, true);
            String vs = String.valueOf(v);
            float vw = textW(vs, 24, LILITA, 0);
            text(c, vs, tx - 26 - vw, 56, 24, 0xFFC4C3C8, LILITA, 0, false);
            float cy = 48;
            float t = max == min ? 0 : (v - min) / (float) (max - min);
            fill(0xFF0C0B10);
            rr(c, tx - 2, cy - 9, tx + tw + 2, cy + 11, 10);
            fill(0xFF16151A);
            rr(c, tx, cy - 7, tx + tw, cy + 9, 8);
            fill(0xFF3FADF2);
            vgrad(cy - 7, cy + 9, 0xFF85CFFC, 0xFF3FADF2);
            rr(c, tx, cy - 7, tx + Math.max(16, tw * t), cy + 9, 8);
            P.setShader(null);
            float kx = tx + tw * t;
            fill(0xFF0C0B10);
            c.drawCircle(kx, cy + 3, 17, P);
            fill(0xFF141318);
            c.drawCircle(kx, cy, 17, P);
            fill(0xFFE8E7EC);
            c.drawCircle(kx, cy, 14, P);
            fill(0xFFFFFFFF);
            c.drawCircle(kx, cy - 2, 11, P);
        }

        // ---------------------------------------------------------- icons

        private static void outlined(Canvas c, Path p, float w) {
            stroke(0xFF0A090E, w + 4);
            c.drawPath(p, P);
            stroke(0xFFFFFFFF, w);
            c.drawPath(p, P);
        }

        static void chevron(Canvas c, float cx, float cy, int color) {
            stroke(color, 3f);
            PATH.reset();
            PATH.moveTo(cx - 7, cy - 3.5f);
            PATH.lineTo(cx, cy + 3.5f);
            PATH.lineTo(cx + 7, cy - 3.5f);
            c.drawPath(PATH, P);
        }

        static void cross(Canvas c, float cx, float cy, float r, int color) {
            float sw = r * 0.62f;
            stroke(0xFF5A1214, sw + 4);
            c.drawLine(cx - r, cy - r + 1.5f, cx + r, cy + r + 1.5f, P);
            c.drawLine(cx + r, cy - r + 1.5f, cx - r, cy + r + 1.5f, P);
            stroke(color, sw);
            c.drawLine(cx - r, cy - r, cx + r, cy + r, P);
            c.drawLine(cx + r, cy - r, cx - r, cy + r, P);
        }

        static void target(Canvas c, float cx, float cy, int bg) {
            // dark outline pass
            stroke(0xFF0A090E, 7.5f);
            c.drawCircle(cx, cy, 9.5f, P);
            c.drawLine(cx, cy - 13.5f, cx, cy - 9, P);
            c.drawLine(cx, cy + 9, cx, cy + 13.5f, P);
            c.drawLine(cx - 13.5f, cy, cx - 9, cy, P);
            c.drawLine(cx + 9, cy, cx + 13.5f, cy, P);
            fill(0xFF0A090E);
            c.drawCircle(cx, cy, 6, P);
            // white pass
            stroke(0xFFFFFFFF, 3.5f);
            c.drawCircle(cx, cy, 9.5f, P);
            c.drawLine(cx, cy - 13.5f, cx, cy - 9, P);
            c.drawLine(cx, cy + 9, cx, cy + 13.5f, P);
            c.drawLine(cx - 13.5f, cy, cx - 9, cy, P);
            c.drawLine(cx + 9, cy, cx + 13.5f, cy, P);
            fill(0xFFFFFFFF);
            c.drawCircle(cx, cy, 3.8f, P);
        }

        static void eye(Canvas c, float cx, float cy) {
            PATH.reset();
            PATH.moveTo(cx - 14.5f, cy);
            PATH.quadTo(cx, cy - 17, cx + 14.5f, cy);
            PATH.quadTo(cx, cy + 17, cx - 14.5f, cy);
            PATH.close();
            stroke(0xFF0A090E, 4);
            c.drawPath(PATH, P);
            fill(0xFFFFFFFF);
            c.drawPath(PATH, P);
            fill(0xFF0A090E);
            c.drawCircle(cx, cy, 5.5f, P);
            fill(0xFF2D2D2D);
            c.drawCircle(cx, cy, 2.5f, P);
        }

        static void wrench(Canvas c, float cx, float cy, int bg) {
            c.save();
            c.rotate(45, cx, cy);
            for (int pass = 0; pass < 2; pass++) {
                int col = pass == 0 ? 0xFF0A090E : 0xFFFFFFFF;
                float g = pass == 0 ? 2f : 0f;
                fill(col);
                rr(c, cx - 3 - g, cy - 3 - g, cx + 3 + g, cy + 13 + g, 3 + g);
                c.drawCircle(cx, cy - 6, 7.5f + g, P);
            }
            fill(bg);
            rr(c, cx - 2.6f, cy - 15, cx + 2.6f, cy - 6, 1.5f);
            c.restore();
        }

        static void search(Canvas c, float cx, float cy) {
            stroke(0xFFFFFFFF, 3.2f);
            c.drawCircle(cx - 2.5f, cy - 2.5f, 10f, P);
            c.drawLine(cx + 5f, cy + 5f, cx + 12.5f, cy + 12.5f, P);
        }

        static void sliders(Canvas c, float cx, float cy, int bg) {
            float[] ys = {-8.5f, 0, 8.5f};
            float[] ks = {4.5f, -4.5f, 1.5f};
            stroke(0xFFFFFFFF, 2.4f);
            for (float y : ys) c.drawLine(cx - 11, cy + y, cx + 11, cy + y, P);
            for (int i = 0; i < 3; i++) {
                fill(bg);
                c.drawCircle(cx + ks[i], cy + ys[i], 3.4f, P);
                stroke(0xFFFFFFFF, 2.4f);
                c.drawCircle(cx + ks[i], cy + ys[i], 3.4f, P);
            }
        }

        static void layoutIcon(Canvas c, float cx, float cy) {
            stroke(0xFFFFFFFF, 2.2f);
            R.set(cx - 10.5f, cy - 9.5f, cx + 10.5f, cy + 9.5f);
            c.drawRoundRect(R, 3, 3, P);
            c.drawLine(cx - 10.5f, cy - 3.5f, cx + 10.5f, cy - 3.5f, P);
            c.drawLine(cx - 3.5f, cy - 3.5f, cx - 3.5f, cy + 9.5f, P);
        }

        static void plusMinus(Canvas c, float cx, float cy, boolean plus) {
            stroke(0xFF0A090E, 10);
            c.drawLine(cx - 11, cy, cx + 11, cy, P);
            if (plus) c.drawLine(cx, cy - 11, cx, cy + 11, P);
            stroke(0xFFFFFFFF, 5.5f);
            c.drawLine(cx - 11, cy, cx + 11, cy, P);
            if (plus) c.drawLine(cx, cy - 11, cx, cy + 11, P);
        }
    }
    // GFX-END

    // ================================================================== embedded emoji (PNG, base64)
    static final class Emoji {
        static final String FRIENDS = "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";
        static final String GEAR = "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";
        static final String WRENCH = "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";
    }
}
