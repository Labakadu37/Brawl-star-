package com.jzs.brawl;

import android.app.Activity;

public final class JZSInit {

    private static boolean sLoaded;

    private JZSInit() {}

    public static synchronized void install(Activity activity) {
        if (!sLoaded) {
            try {
                System.loadLibrary("jz");
            } catch (Throwable t) {
                android.util.Log.w("JZS", "libjz.so not loaded (non-fatal): " + t.getMessage());
            }
            sLoaded = true;
            android.util.Log.i("JZS", "JZS Brawl init - " + JZSOverlay.MOD_VERSION);
        }
        JZSOverlay.attach(activity);
    }

    public static native String nativeVersion();
}
