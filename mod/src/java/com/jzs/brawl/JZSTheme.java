package com.jzs.brawl;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

public final class JZSTheme {

    public static final int GOLD        = 0xFFFFD700;
    public static final int GOLD_DARK   = 0xFFDAA520;
    public static final int BLUE        = 0xFF1E90FF;
    public static final int BLUE_DARK   = 0xFF0A1929;
    public static final int NAVY        = 0xFF1A1A2E;
    public static final int SURFACE     = 0xFF16213E;
    public static final int SURFACE_ALT = 0xFF0F3460;
    public static final int RED         = 0xFFE94560;
    public static final int GREEN       = 0xFF00E676;
    public static final int WHITE       = 0xFFFFFFFF;
    public static final int TEXT_DIM    = 0xAAFFFFFF;
    public static final int DIVIDER     = 0x33FFFFFF;
    public static final int SHADOW      = 0x99000000;

    public static final int TOGGLE_ON   = GREEN;
    public static final int TOGGLE_OFF  = 0xFF555555;
    public static final int TOGGLE_KNOB = WHITE;

    public static final float CORNER_RADIUS   = 16f;
    public static final float CORNER_SM       = 10f;
    public static final float CORNER_XS       = 6f;
    public static final float BUTTON_RADIUS   = 28f;

    private JZSTheme() {}

    public static GradientDrawable pill(int color, float radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(radius);
        gd.setColor(color);
        return gd;
    }

    public static GradientDrawable circle(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        return gd;
    }

    public static GradientDrawable panel() {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(CORNER_RADIUS);
        gd.setColor(NAVY);
        gd.setStroke(2, SURFACE_ALT);
        return gd;
    }

    public static GradientDrawable headerGradient() {
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{SURFACE_ALT, NAVY});
        gd.setCornerRadii(new float[]{
                CORNER_RADIUS, CORNER_RADIUS,
                CORNER_RADIUS, CORNER_RADIUS,
                0, 0, 0, 0});
        return gd;
    }

    public static GradientDrawable tabActive() {
        return pill(SURFACE_ALT, CORNER_SM);
    }

    public static GradientDrawable tabInactive() {
        return pill(Color.TRANSPARENT, CORNER_SM);
    }

    public static GradientDrawable slider(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(4f);
        gd.setColor(color);
        return gd;
    }
}
