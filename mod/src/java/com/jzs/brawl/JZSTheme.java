package com.jzs.brawl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public final class JZSTheme {

    public static final int THEME_DARK  = 0;
    public static final int THEME_GLASS = 1;

    private static final String PREFS = "jzs_settings";
    private static final String KEY_THEME = "selected_theme";

    private static int sCurrentTheme = THEME_DARK;
    private static Listener sListener;

    private JZSTheme() {}

    public interface Listener {
        void onThemeChanged(int themeId);
    }

    public static void setListener(Listener l) { sListener = l; }

    public static void load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sCurrentTheme = sp.getInt(KEY_THEME, THEME_DARK);
    }

    public static void apply(Context ctx, int themeId) {
        sCurrentTheme = themeId;
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
           .edit().putInt(KEY_THEME, themeId).apply();
        if (sListener != null) sListener.onThemeChanged(themeId);
    }

    public static int current() { return sCurrentTheme; }

    public static String name(int id) {
        switch (id) {
            case THEME_GLASS: return "Glass";
            default:          return "Dark";
        }
    }

    // ── Main panel background ──
    // Dark:  solid #1e1e1e at ~230 alpha
    // Glass: white at ~40 alpha (translucent frost)
    public static int panelBg(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(40, 255, 255, 255);
            default:          return Color.argb(230, 30, 30, 30);
        }
    }

    // ── Header bar background ──
    // Dark:  slightly lighter #2a2a2a
    // Glass: white at ~50 alpha
    public static int headerBg(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(50, 255, 255, 255);
            default:          return Color.argb(240, 42, 42, 42);
        }
    }

    // ── Card / tile background (Trophies, Highest, etc.) ──
    // Dark:  #333333
    // Glass: white at ~30 alpha
    public static int cardBg(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(30, 255, 255, 255);
            default:          return Color.argb(255, 51, 51, 51);
        }
    }

    // ── Sidebar button background ──
    // Dark:  #383838
    // Glass: white at ~45 alpha
    public static int sidebarBtnBg(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(45, 255, 255, 255);
            default:          return Color.argb(255, 56, 56, 56);
        }
    }

    // ── Sidebar active button background ──
    // Dark:  #444444
    // Glass: white at ~70 alpha
    public static int sidebarBtnActiveBg(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(70, 255, 255, 255);
            default:          return Color.argb(255, 68, 68, 68);
        }
    }

    // ── Primary text (labels, titles) ──
    // Dark:  white
    // Glass: white
    public static int textPrimary(int id) {
        return Color.WHITE;
    }

    // ── Secondary text (values, subtitles) ──
    // Dark:  #AAAAAA
    // Glass: white at ~80%
    public static int textSecondary(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(200, 255, 255, 255);
            default:          return Color.parseColor("#AAAAAA");
        }
    }

    // ── Border / stroke color ──
    // Dark:  white at ~8% (very subtle)
    // Glass: white at ~30% (visible frost edge)
    public static int borderColor(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(76, 255, 255, 255);
            default:          return Color.argb(20, 255, 255, 255);
        }
    }

    // ── Border stroke width in dp ──
    public static int borderWidthDp(int id) {
        switch (id) {
            case THEME_GLASS: return 1;
            default:          return 0;
        }
    }

    // ── Corner radius in dp ──
    // Dark:  8dp
    // Glass: 14dp (softer, rounder)
    public static int cornerRadiusDp(int id) {
        switch (id) {
            case THEME_GLASS: return 14;
            default:          return 8;
        }
    }

    // ── Panel corner radius in dp ──
    // Dark:  12dp
    // Glass: 20dp
    public static int panelCornerDp(int id) {
        switch (id) {
            case THEME_GLASS: return 20;
            default:          return 12;
        }
    }

    // ── Shadow / glow behind text ──
    // Dark:  none (radius 0)
    // Glass: subtle white glow
    public static float textShadowRadius(int id) {
        switch (id) {
            case THEME_GLASS: return 4f;
            default:          return 0f;
        }
    }

    public static int textShadowColor(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(60, 255, 255, 255);
            default:          return Color.TRANSPARENT;
        }
    }

    // ── "Friends" button color ──
    // Both themes keep the same accent colors
    public static int btnFriendsColor() { return Color.parseColor("#4CAF50"); }

    // ── "Settings" button color ──
    public static int btnSettingsColor() { return Color.parseColor("#9C27B0"); }

    // ── "Edit GUI" button color ──
    public static int btnEditGuiColor() { return Color.parseColor("#FF9800"); }

    // ── Close (X) button color ──
    public static int btnCloseColor() { return Color.parseColor("#F44336"); }

    // ── "Config 1" dropdown background ──
    // Dark:  #3a3a3a
    // Glass: white at ~40 alpha
    public static int dropdownBg(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(40, 255, 255, 255);
            default:          return Color.argb(255, 58, 58, 58);
        }
    }

    // ── Divider line (ACCOUNT separator) ──
    // Dark:  #444444
    // Glass: white at ~20%
    public static int dividerColor(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(50, 255, 255, 255);
            default:          return Color.argb(255, 68, 68, 68);
        }
    }

    // ── Whether to apply blur effect (Glass only) ──
    public static boolean useBlur(int id) {
        return id == THEME_GLASS;
    }

    // ── Badge background (top-left "JZS" box) ──
    // Dark:  #333333
    // Glass: white at ~50 alpha
    public static int badgeBg(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(50, 255, 255, 255);
            default:          return Color.argb(255, 51, 51, 51);
        }
    }

    // ── MOD MENU button background ──
    // Dark:  #2a2a2a
    // Glass: white at ~35 alpha
    public static int modMenuBtnBg(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(35, 255, 255, 255);
            default:          return Color.argb(230, 42, 42, 42);
        }
    }

    // ── Icon tint color ──
    // Both: white icons
    public static int iconTint(int id) {
        return Color.WHITE;
    }

    // ── Chevron / arrow color (">") ──
    // Dark:  #888888
    // Glass: white at ~60%
    public static int chevronColor(int id) {
        switch (id) {
            case THEME_GLASS: return Color.argb(153, 255, 255, 255);
            default:          return Color.parseColor("#888888");
        }
    }
}
