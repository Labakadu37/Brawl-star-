package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public final class JZSInit {

    private static final String PREFS = "jzs_brawl_state";
    private static final String KEY_LAUNCH_COUNT = "launch_count";
    private static final String DISCORD_URL = "https://discord.gg/jzbrawl";

    private static boolean sLoaded;
    private static boolean sDiscordHandledThisProcess;

    private JZSInit() {}

    public static synchronized void install(Activity activity) {
        if (activity == null) return;
        if (!sLoaded) {
            try {
                System.loadLibrary("jz");
            } catch (Throwable t) {
                android.util.Log.w("JZS", "libjz.so not loaded (non-fatal): " + t.getMessage());
            }
            sLoaded = true;
            android.util.Log.i("JZS", "JZS Brawl init - " + JZSOverlay.MOD_VERSION);
        }
        try {
            maybeOpenDiscord(activity);
        } catch (Throwable t) {
            android.util.Log.w("JZS", "discord step failed (non-fatal): " + t.getMessage());
        }
        try {
            JZSOverlay.attach(activity);
        } catch (Throwable t) {
            android.util.Log.e("JZS", "overlay attach threw (non-fatal)", t);
        }
    }

    private static void maybeOpenDiscord(Activity activity) {
        if (sDiscordHandledThisProcess) return;
        sDiscordHandledThisProcess = true;

        Context ctx = activity.getApplicationContext();
        SharedPreferences sp;
        try {
            sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        } catch (Throwable t) {
            android.util.Log.w("JZS", "prefs unavailable: " + t.getMessage());
            return;
        }

        long count = 0L;
        try {
            count = sp.getLong(KEY_LAUNCH_COUNT, 0L);
        } catch (Throwable ignored) {
            try { count = sp.getInt(KEY_LAUNCH_COUNT, 0); } catch (Throwable ignored2) {}
        }
        long next = count + 1L;
        try {
            sp.edit().putLong(KEY_LAUNCH_COUNT, next).apply();
        } catch (Throwable t) {
            android.util.Log.w("JZS", "prefs write failed: " + t.getMessage());
        }

        // 1 launch out of 2 (odd launches: 1, 3, 5, ...): open Discord
        if ((next % 2L) == 1L) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(DISCORD_URL));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(i);
            } catch (Throwable t) {
                android.util.Log.w("JZS", "discord intent failed: " + t.getMessage());
            }
        }
    }

    public static native String nativeVersion();
}
