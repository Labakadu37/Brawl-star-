package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public final class JZSOverlay {

    public static final String MOD_NAME = "JZS Brawl";
    public static final String MOD_VERSION = "JZS V5.5";

    private static final int GOLD = 0xFFFFD700;
    private static final int GREEN = 0xFF00E676;
    private static final long REFRESH_MS = 2000;

    private static JZSOverlay sInstance;

    private Activity mActivity;
    private LinearLayout mContainer;
    private TextView mBadge;
    private TextView mServerView;
    private Handler mHandler;
    private String mLastServer = "";

    private JZSOverlay() {}

    public static JZSOverlay getInstance() {
        return sInstance;
    }

    public static void attach(final Activity activity) {
        if (activity == null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sInstance == null) {
                        sInstance = new JZSOverlay();
                    }
                    sInstance.install(activity);
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "overlay attach failed", t);
                }
            }
        });
    }

    private void install(Activity activity) {
        mActivity = activity;
        mHandler = new Handler(Looper.getMainLooper());

        mContainer = new LinearLayout(activity);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.setPadding(dp(12), dp(24), dp(12), 0);

        FrameLayout.LayoutParams containerLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        containerLp.gravity = Gravity.TOP | Gravity.START;
        mContainer.setLayoutParams(containerLp);

        mBadge = new TextView(activity);
        mBadge.setText(MOD_VERSION);
        mBadge.setTextColor(GREEN);
        mBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        mBadge.setTypeface(Typeface.DEFAULT_BOLD);
        mBadge.setShadowLayer(4f, 0f, 0f, Color.BLACK);
        mBadge.setPadding(dp(10), dp(6), dp(10), dp(4));
        mBadge.setBackgroundColor(Color.argb(160, 0, 0, 0));
        mContainer.addView(mBadge);

        mServerView = new TextView(activity);
        mServerView.setText("Serveur: ...");
        mServerView.setTextColor(GOLD);
        mServerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        mServerView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        mServerView.setShadowLayer(4f, 0f, 0f, Color.BLACK);
        mServerView.setPadding(dp(10), dp(2), dp(10), dp(6));
        mServerView.setBackgroundColor(Color.argb(160, 0, 0, 0));
        mContainer.addView(mServerView);

        FrameLayout host = new FrameLayout(activity);
        host.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        host.addView(mContainer);

        activity.getWindow().addContentView(host,
                new WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        android.util.Log.i("JZS", MOD_NAME + " overlay attached: " + MOD_VERSION);

        startServerPoller();
    }

    private void startServerPoller() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String server = findGameServer();
                if (!server.equals(mLastServer)) {
                    mLastServer = server;
                    if (mServerView != null) {
                        mServerView.setText(server);
                    }
                }
                mHandler.postDelayed(this, REFRESH_MS);
            }
        });
    }

    private String findGameServer() {
        List<String[]> conns = new ArrayList<>();
        parseProcNet("/proc/net/tcp", conns, false);
        parseProcNet("/proc/net/tcp6", conns, true);

        for (String[] c : conns) {
            String ip = c[0];
            String port = c[1];
            if (isGameServer(ip, port)) {
                return ip + ":" + port;
            }
        }
        return "Aucun serveur";
    }

    private void parseProcNet(String path, List<String[]> out, boolean ipv6) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; }
                line = line.trim();
                String[] parts = line.split("\\s+");
                if (parts.length < 4) continue;

                String state = parts[3];
                if (!"01".equals(state)) continue;

                String remAddr = parts[2];
                String[] ipPort = remAddr.split(":");
                if (ipPort.length != 2) continue;

                String hexIp = ipPort[0];
                int portNum;
                try {
                    portNum = Integer.parseInt(ipPort[1], 16);
                } catch (NumberFormatException e) {
                    continue;
                }

                String ip;
                if (ipv6) {
                    ip = parseIpv6(hexIp);
                } else {
                    ip = parseIpv4(hexIp);
                }

                if (ip == null) continue;
                if (isLocalAddress(ip)) continue;

                out.add(new String[]{ip, String.valueOf(portNum)});
            }
        } catch (Throwable ignored) {
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Throwable ignored) {}
            }
        }
    }

    private static String parseIpv4(String hex) {
        if (hex.length() != 8) return null;
        try {
            long addr = Long.parseLong(hex, 16);
            return (addr & 0xFF) + "."
                    + ((addr >> 8) & 0xFF) + "."
                    + ((addr >> 16) & 0xFF) + "."
                    + ((addr >> 24) & 0xFF);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String parseIpv6(String hex) {
        if (hex.length() < 24) return null;
        String v4part = hex.substring(24);
        if (hex.startsWith("0000000000000000FFFF0000")
                || hex.startsWith("00000000000000000000FFFF")) {
            return parseIpv4(v4part);
        }
        if (v4part.length() == 8) {
            String maybe = parseIpv4(v4part);
            if (maybe != null && !isLocalAddress(maybe)) {
                return maybe;
            }
        }
        return formatFullIpv6(hex);
    }

    private static String formatFullIpv6(String hex) {
        if (hex.length() != 32) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i += 8) {
            if (sb.length() > 0) sb.append(":");
            String word = hex.substring(i, i + 8);
            String b3 = word.substring(6, 8);
            String b2 = word.substring(4, 6);
            String b1 = word.substring(2, 4);
            String b0 = word.substring(0, 2);
            sb.append(b3).append(b2).append(":").append(b1).append(b0);
        }
        return sb.toString();
    }

    private static boolean isLocalAddress(String ip) {
        return "0.0.0.0".equals(ip)
                || "127.0.0.1".equals(ip)
                || ip.startsWith("127.")
                || "::1".equals(ip)
                || "::".equals(ip);
    }

    private static boolean isGameServer(String ip, String port) {
        int p;
        try {
            p = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return false;
        }
        if (p == 443 || p == 80) return false;
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) return false;
        return p >= 1024;
    }

    public void refreshOverlays() {
    }

    public void updateFps(int fps) {
    }

    private int dp(int value) {
        float d = mActivity.getResources().getDisplayMetrics().density;
        return (int) (value * d + 0.5f);
    }
}
