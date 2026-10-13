package com.jzs.brawl;

import android.content.Context;
import android.content.SharedPreferences;

public final class JZSConfig {

    private static final String PREFS_NAME = "jzs_brawl_config";
    private static JZSConfig sInstance;
    private final SharedPreferences mPrefs;

    public static final String KEY_FPS_COUNTER    = "fps_counter";
    public static final String KEY_ZOOM_ENABLED   = "zoom_enabled";
    public static final String KEY_ZOOM_LEVEL     = "zoom_level";
    public static final String KEY_SHOW_PING      = "show_ping";
    public static final String KEY_SHOW_COORDS    = "show_coords";
    public static final String KEY_SHOW_DEVICE    = "show_device";
    public static final String KEY_SHOW_BADGE     = "show_badge";
    public static final String KEY_MENU_OPACITY   = "menu_opacity";
    public static final String KEY_MENU_BTN_X     = "menu_btn_x";
    public static final String KEY_MENU_BTN_Y     = "menu_btn_y";
    public static final String KEY_DARK_HUD       = "dark_hud";
    public static final String KEY_SHOW_GRID      = "show_grid";
    public static final String KEY_SHOW_MEMORY    = "show_memory";
    public static final String KEY_SHOW_BATTERY   = "show_battery";
    public static final String KEY_ANTI_ALIASING  = "anti_aliasing";
    public static final String KEY_SHOW_HITBOX    = "show_hitbox";

    private JZSConfig(Context ctx) {
        mPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized JZSConfig get(Context ctx) {
        if (sInstance == null) {
            sInstance = new JZSConfig(ctx.getApplicationContext());
        }
        return sInstance;
    }

    public boolean getBool(String key, boolean def) {
        return mPrefs.getBoolean(key, def);
    }

    public void setBool(String key, boolean value) {
        mPrefs.edit().putBoolean(key, value).apply();
    }

    public int getInt(String key, int def) {
        return mPrefs.getInt(key, def);
    }

    public void setInt(String key, int value) {
        mPrefs.edit().putInt(key, value).apply();
    }

    public float getFloat(String key, float def) {
        return mPrefs.getFloat(key, def);
    }

    public void setFloat(String key, float value) {
        mPrefs.edit().putFloat(key, value).apply();
    }

    public void resetAll() {
        mPrefs.edit().clear().apply();
    }
}
