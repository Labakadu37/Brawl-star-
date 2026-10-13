package com.jzs.brawl;

import android.content.Context;
import android.graphics.Typeface;
import java.util.Locale;

/** Shared look for everything JZS draws outside the mod menu (splash, notice). */
final class JZSUi {

    // Same palette as the mod menu so the splash and notice feel like one piece.
    static final int BG_DARK = 0xFF141318;
    static final int BG_DEEP = 0xFF08080A;
    static final int CARD = 0xFF1C1B22;
    static final int CARD_BORDER = 0xFF46454A;
    static final int BLUE = 0xFF85CFFC;
    static final int YELLOW = 0xFFFFC93C;
    static final int ORANGE = 0xFFFDC087;
    static final int TEXT_DIM = 0xFFA9A8AE;

    static final int[] RAINBOW = {
            0xFFFF4D6D, 0xFFFFC93C, 0xFF5CFF8A, 0xFF4DD2FF, 0xFFB86BFF, 0xFFFF4D6D
    };

    private static Typeface sTitleFont;

    private JZSUi() {}

    /** LilitaOne (the Brawl Stars font) when the game ships it, bold system font otherwise. */
    static Typeface titleFont(Context ctx) {
        if (sTitleFont != null) return sTitleFont;
        String[] paths = {"font/LilitaOne-Regular.ttf", "fonts/LilitaOne-Regular.ttf"};
        for (String p : paths) {
            try {
                sTitleFont = Typeface.createFromAsset(ctx.getAssets(), p);
                return sTitleFont;
            } catch (Throwable ignored) {
                // try the next path
            }
        }
        sTitleFont = Typeface.DEFAULT_BOLD;
        return sTitleFont;
    }

    static float dp(Context ctx, float value) {
        return value * ctx.getResources().getDisplayMetrics().density;
    }

    static int dpi(Context ctx, float value) {
        return (int) (dp(ctx, value) + 0.5f);
    }

    static boolean french() {
        return "fr".equals(Locale.getDefault().getLanguage());
    }

    static String tr(String en, String fr) {
        return french() ? fr : en;
    }
}
