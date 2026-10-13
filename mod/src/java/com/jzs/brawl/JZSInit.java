package com.jzs.brawl;

import android.app.Activity;

public final class JZSInit {

    private static boolean sLoaded;

    private JZSInit() {}

    public static synchronized void install(final Activity activity) {
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
        JZSNetInfo.attach(activity);
        // Intro animation first, then the welcome guide on first launch.
        JZSSplash.show(activity, new Runnable() {
            @Override
            public void run() {
                JZSNotice.showIfFirstTime(activity);
            }
        });
    }

    public static native String nativeVersion();
}
