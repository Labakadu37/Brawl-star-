package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.InputStream;

public final class JZSOverlay {

    public static final String MOD_NAME = "JZS Brawl";
    public static final String MOD_VERSION = "JZS V1 (lastest)";

    private static ImageView sBadge;

    private JZSOverlay() {}

    public static void attach(final Activity activity) {
        if (activity == null || sBadge != null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    installBadge(activity);
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "overlay attach failed", t);
                }
            }
        });
    }

    private static void installBadge(Activity activity) {
        Context ctx = activity;

        Bitmap banner = null;
        try {
            InputStream is = ctx.getAssets().open("jzs_banner.png");
            banner = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            android.util.Log.e("JZS", "failed to load jzs_banner.png from assets", e);
            return;
        }

        ImageView iv = new ImageView(ctx);
        iv.setImageBitmap(banner);
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_START);

        int bannerWidth = dp(ctx, 180);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                bannerWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.topMargin = dp(ctx, 16);
        lp.leftMargin = dp(ctx, 10);
        iv.setLayoutParams(lp);

        FrameLayout host = new FrameLayout(ctx);
        host.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        host.addView(iv);

        activity.getWindow().addContentView(host, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.bringToFront();

        sBadge = iv;
        android.util.Log.i("JZS", MOD_NAME + " banner overlay attached");
    }

    private static int dp(Context ctx, int value) {
        float d = ctx.getResources().getDisplayMetrics().density;
        return (int) (value * d + 0.5f);
    }
}
