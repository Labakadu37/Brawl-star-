package com.jzs.brawl;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;

import java.util.ArrayList;
import java.util.List;

/**
 * The mod's settings, persisted in the game's own {@code SharedPreferences}
 * file (a private file named {@code jzs_brawl}, not the game's).
 *
 * Every setting here is cosmetic: where the badge sits, how loud it is, what
 * the accent colour is, whether the HUD extras show. Nothing in this class
 * reaches gameplay, network or game state - that is deliberate and the menu
 * has nothing else to offer.
 */
public final class JZSConfig {

    private static final String FILE = "jzs_brawl";

    private static final String K_OVERLAY = "overlay_visible";
    private static final String K_CORNER = "badge_corner";
    private static final String K_OPACITY = "badge_opacity";
    private static final String K_SCALE = "badge_scale";
    private static final String K_ACCENT = "accent_index";
    private static final String K_FPS = "show_fps";
    private static final String K_CLOCK = "show_clock";
    private static final String K_COMPACT = "compact_badge";
    private static final String K_HAPTICS = "haptics";
    private static final String K_REDUCE = "reduce_motion";

    /** Badge corner, in the order the menu's segmented control shows them. */
    public static final int CORNER_TOP_LEFT = 0;
    public static final int CORNER_TOP_RIGHT = 1;
    public static final int CORNER_BOTTOM_LEFT = 2;
    public static final int CORNER_BOTTOM_RIGHT = 3;

    public static final String[] CORNER_NAMES = {"Top L", "Top R", "Bot L", "Bot R"};

    private static JZSConfig sInstance;

    private final SharedPreferences prefs;
    private final List<Listener> listeners = new ArrayList<Listener>(4);

    private JZSConfig(Context ctx) {
        this.prefs = ctx.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static synchronized JZSConfig get(Context ctx) {
        if (sInstance == null) sInstance = new JZSConfig(ctx);
        return sInstance;
    }

    /* ------------------------------------------------------------- getters */

    public boolean overlayVisible() { return prefs.getBoolean(K_OVERLAY, true); }

    public int badgeCorner() { return clampCorner(prefs.getInt(K_CORNER, CORNER_TOP_LEFT)); }

    /** 0.25 .. 1.0 */
    public float badgeOpacity() { return clamp(prefs.getFloat(K_OPACITY, 0.92f), 0.25f, 1f); }

    /** 0.75 .. 1.5 */
    public float badgeScale() { return clamp(prefs.getFloat(K_SCALE, 1f), 0.75f, 1.5f); }

    public int accentIndex() {
        int i = prefs.getInt(K_ACCENT, 0);
        return (i < 0 || i >= JZSTheme.ACCENTS.length) ? 0 : i;
    }

    public int accent() { return JZSTheme.accent(accentIndex()); }

    public boolean showFps() { return prefs.getBoolean(K_FPS, false); }

    public boolean showClock() { return prefs.getBoolean(K_CLOCK, false); }

    public boolean compactBadge() { return prefs.getBoolean(K_COMPACT, false); }

    public boolean haptics() { return prefs.getBoolean(K_HAPTICS, true); }

    public boolean reduceMotion() { return prefs.getBoolean(K_REDUCE, false); }

    /* ------------------------------------------------------------- setters */

    public void setOverlayVisible(boolean v) { putBool(K_OVERLAY, v); }

    public void setBadgeCorner(int corner) { putInt(K_CORNER, clampCorner(corner)); }

    public void setBadgeOpacity(float v) { putFloat(K_OPACITY, clamp(v, 0.25f, 1f)); }

    public void setBadgeScale(float v) { putFloat(K_SCALE, clamp(v, 0.75f, 1.5f)); }

    public void setAccentIndex(int index) {
        putInt(K_ACCENT, (index < 0 || index >= JZSTheme.ACCENTS.length) ? 0 : index);
    }

    public void setShowFps(boolean v) { putBool(K_FPS, v); }

    public void setShowClock(boolean v) { putBool(K_CLOCK, v); }

    public void setCompactBadge(boolean v) { putBool(K_COMPACT, v); }

    public void setHaptics(boolean v) { putBool(K_HAPTICS, v); }

    public void setReduceMotion(boolean v) {
        putBool(K_REDUCE, v);
        JZSAnim.setReducedMotion(v);
    }

    /** Wipes every stored setting and tells listeners to rebuild. */
    public void resetToDefaults() {
        prefs.edit().clear().apply();
        JZSAnim.setReducedMotion(false);
        notifyChanged();
    }

    /* ------------------------------------------------------------ derived */

    /** The badge corner as a {@link Gravity} pair. */
    public int badgeGravity() {
        switch (badgeCorner()) {
            case CORNER_TOP_RIGHT:    return Gravity.TOP | Gravity.END;
            case CORNER_BOTTOM_LEFT:  return Gravity.BOTTOM | Gravity.START;
            case CORNER_BOTTOM_RIGHT: return Gravity.BOTTOM | Gravity.END;
            case CORNER_TOP_LEFT:
            default:                  return Gravity.TOP | Gravity.START;
        }
    }

    /* ---------------------------------------------------------- listeners */

    public void addListener(Listener l) {
        if (l != null && !listeners.contains(l)) listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    /**
     * Fans a change out to every listener. One listener throwing must not stop
     * the others from being told, and must never reach the game's own frame.
     */
    public void notifyChanged() {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onConfigChanged(this);
            } catch (Throwable t) {
                android.util.Log.w("JZS", "config listener failed", t);
            }
        }
    }

    public interface Listener {
        void onConfigChanged(JZSConfig config);
    }

    /* ------------------------------------------------------------ plumbing */

    private void putBool(String key, boolean v) {
        prefs.edit().putBoolean(key, v).apply();
        notifyChanged();
    }

    private void putInt(String key, int v) {
        prefs.edit().putInt(key, v).apply();
        notifyChanged();
    }

    private void putFloat(String key, float v) {
        prefs.edit().putFloat(key, v).apply();
        notifyChanged();
    }

    private static int clampCorner(int corner) {
        return (corner < 0 || corner > CORNER_BOTTOM_RIGHT) ? CORNER_TOP_LEFT : corner;
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }
}
