package com.jzs.brawl;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;

/**
 * Single source of truth for the mod's look: palette, metrics and the
 * programmatically built drawables.
 *
 * Everything is code-only on purpose. The build decodes the game APK with
 * {@code apktool d --no-res}, so the mod can never reference an XML resource
 * or an R id - there is no resource table to add them to.
 */
public final class JZSTheme {

    /* ---------------------------------------------------------------- palette */

    /** Panel background, top of the gradient. */
    public static final int BG_TOP = 0xFF171A24;
    /** Panel background, bottom of the gradient. */
    public static final int BG_BOTTOM = 0xFF0E1017;
    /** Raised surface (rows, chips, fields). */
    public static final int SURFACE = 0xFF1E2230;
    /** Raised surface while pressed. */
    public static final int SURFACE_PRESSED = 0xFF272C3C;
    /** Hairline borders. */
    public static final int STROKE = 0x1AFFFFFF;
    /** Dimmer drawn behind the menu. */
    public static final int SCRIM = 0xB305060B;

    public static final int TEXT = 0xFFF2F5FF;
    public static final int TEXT_MUTED = 0xFF8D95AC;
    public static final int TRACK_OFF = 0xFF2C3142;

    /** Selectable accents, in the order the theme tab shows them. */
    public static final int[] ACCENTS = {
            0xFF5B8CFF, // blue    (default)
            0xFF8B5CF6, // violet
            0xFF22D3A5, // mint
            0xFFFF5C8A, // pink
            0xFFFFB020, // amber
            0xFFFF6B4A, // coral
    };

    public static final String[] ACCENT_NAMES = {
            "Blue", "Violet", "Mint", "Pink", "Amber", "Coral",
    };

    /** Accent at {@code index}, wrapped so a stale saved index can never crash. */
    public static int accent(int index) {
        if (index < 0 || index >= ACCENTS.length) index = 0;
        return ACCENTS[index];
    }

    /* ---------------------------------------------------------------- metrics */

    public static int dp(Context ctx, float value) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    public static float dpf(Context ctx, float value) {
        return value * ctx.getResources().getDisplayMetrics().density;
    }

    /** Alpha-blends {@code color} to {@code alpha} (0..1) without touching its RGB. */
    public static int withAlpha(int color, float alpha) {
        if (alpha < 0f) alpha = 0f;
        if (alpha > 1f) alpha = 1f;
        return (color & 0x00FFFFFF) | (((int) (alpha * 255f)) << 24);
    }

    /** Linear ARGB blend, {@code t} 0 = from, 1 = to. */
    public static int blend(int from, int to, float t) {
        if (t <= 0f) return from;
        if (t >= 1f) return to;
        int a = (int) (Color.alpha(from) + t * (Color.alpha(to) - Color.alpha(from)));
        int r = (int) (Color.red(from) + t * (Color.red(to) - Color.red(from)));
        int g = (int) (Color.green(from) + t * (Color.green(to) - Color.green(from)));
        int b = (int) (Color.blue(from) + t * (Color.blue(to) - Color.blue(from)));
        return Color.argb(a, r, g, b);
    }

    /* -------------------------------------------------------------- drawables */

    /** Flat rounded rectangle. */
    public static GradientDrawable round(int color, float radiusPx) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(radiusPx);
        return d;
    }

    /** Rounded rectangle with a hairline border. */
    public static GradientDrawable round(int color, float radiusPx, int strokePx, int strokeColor) {
        GradientDrawable d = round(color, radiusPx);
        if (strokePx > 0) d.setStroke(strokePx, strokeColor);
        return d;
    }

    /** Vertical two-stop gradient, used for the panel body and the header. */
    public static GradientDrawable vertical(int top, int bottom, float radiusPx) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{top, bottom});
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(radiusPx);
        return d;
    }

    /** Horizontal two-stop gradient, used for the badge pill and the accent bar. */
    public static GradientDrawable horizontal(int start, int end, float radiusPx) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end});
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(radiusPx);
        return d;
    }

    /** Perfect circle of {@code color}. */
    public static GradientDrawable circle(int color, int sizePx) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        d.setSize(sizePx, sizePx);
        return d;
    }

    /** A slightly darker shade of {@code color}, for gradient ends. */
    public static int shade(int color, float factor) {
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.argb(Color.alpha(color),
                Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }

    /* ------------------------------------------------------------------ misc */

    public static Typeface bold() {
        return Typeface.create("sans-serif-medium", Typeface.BOLD);
    }

    public static Typeface medium() {
        return Typeface.create("sans-serif-medium", Typeface.NORMAL);
    }

    public static Typeface regular() {
        return Typeface.create("sans-serif", Typeface.NORMAL);
    }

    /** Elevation is a no-op below API 21; guard so older devices stay happy. */
    public static void elevate(View view, float elevationPx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(elevationPx);
        }
    }

    private JZSTheme() {}
}
