package com.jzs.brawl;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Cosmetic, client-side settings for the overlay, read from a JSON file so the
 * user can change them without rebuilding the APK. Nothing here touches the
 * game, the network, or gameplay.
 *
 * Location (no runtime permission needed, visible in any file manager):
 *   Android/data/com.supercell.brawlstars/files/jzs/config.json
 *
 * Example:
 *   {
 *     "text": "JZS V1",
 *     "corner": "top-left",     // top-left | top-right | bottom-left | bottom-right
 *     "textColor": "#FFFFFF",
 *     "backgroundColor": "#A0000000",
 *     "textSizeSp": 14,
 *     "showBadge": true,
 *     "showFps": false
 *   }
 */
public final class JZSConfig {

    public String text = JZSOverlay.MOD_VERSION;
    public int gravity = Gravity.TOP | Gravity.START;
    public int textColor = Color.WHITE;
    public int backgroundColor = Color.argb(160, 0, 0, 0);
    public float textSizeSp = 14f;
    public boolean showBadge = true;
    public boolean showFps = false;

    private JZSConfig() {}

    public static JZSConfig load(Context ctx) {
        JZSConfig cfg = new JZSConfig();
        try {
            File dir = ctx.getExternalFilesDir(null);
            if (dir == null) return cfg;
            File file = new File(new File(dir, "jzs"), "config.json");
            if (!file.isFile()) return cfg;

            JSONObject json = new JSONObject(readFile(file));
            cfg.text = json.optString("text", cfg.text);
            cfg.textColor = parseColor(json.optString("textColor", null), cfg.textColor);
            cfg.backgroundColor = parseColor(json.optString("backgroundColor", null), cfg.backgroundColor);
            cfg.textSizeSp = (float) json.optDouble("textSizeSp", cfg.textSizeSp);
            cfg.showBadge = json.optBoolean("showBadge", cfg.showBadge);
            cfg.showFps = json.optBoolean("showFps", cfg.showFps);
            cfg.gravity = parseCorner(json.optString("corner", null), cfg.gravity);
        } catch (Throwable t) {
            android.util.Log.w("JZS", "config load failed, using defaults: " + t.getMessage());
        }
        return cfg;
    }

    private static int parseCorner(String corner, int fallback) {
        if (corner == null) return fallback;
        switch (corner.trim().toLowerCase()) {
            case "top-left":
                return Gravity.TOP | Gravity.START;
            case "top-right":
                return Gravity.TOP | Gravity.END;
            case "bottom-left":
                return Gravity.BOTTOM | Gravity.START;
            case "bottom-right":
                return Gravity.BOTTOM | Gravity.END;
            default:
                return fallback;
        }
    }

    private static int parseColor(String value, int fallback) {
        if (value == null || value.length() == 0) return fallback;
        try {
            return Color.parseColor(value.trim());
        } catch (Throwable t) {
            return fallback;
        }
    }

    private static String readFile(File file) throws Exception {
        FileInputStream in = new FileInputStream(file);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            return out.toString("UTF-8");
        } finally {
            in.close();
        }
    }
}
