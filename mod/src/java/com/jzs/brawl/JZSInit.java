package com.jzs.brawl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

/**
 * JZS Brawl — pure JZS mod boot.
 * Loads libjzs_brawlv2.so, installs the mod menu overlay on every activity.
 */
public final class JZSInit {

    private static boolean sLoaded;

    private JZSInit() {}

    public static synchronized void install(Context ctx) {
        if (!sLoaded) {
            try { System.loadLibrary("jzs_brawlv2"); }
            catch (Throwable t) {
                android.util.Log.w("JZS", "libjzs_brawlv2.so not loaded: " + t.getMessage());
            }
            sLoaded = true;
            JzsConfig.load();
        }
        if (ctx instanceof Application) {
            ((Application) ctx).registerActivityLifecycleCallbacks(new Lc());
        } else if (ctx instanceof Activity) {
            JzsModMenu.attach((Activity) ctx);
        }
    }

    private static final class Lc implements Application.ActivityLifecycleCallbacks {
        public void onActivityCreated(Activity a, Bundle b) {}
        public void onActivityStarted(Activity a) {}
        public void onActivityResumed(Activity a) { JzsModMenu.attach(a); }
        public void onActivityPaused(Activity a) {}
        public void onActivityStopped(Activity a) {}
        public void onActivitySaveInstanceState(Activity a, Bundle b) {}
        public void onActivityDestroyed(Activity a) {}
    }
}
