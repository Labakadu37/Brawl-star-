package com.jzs.deviceinfo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;

public class DeviceInfoCollector {

    private final Context ctx;

    public DeviceInfoCollector(Context ctx) {
        this.ctx = ctx;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    public String getAndroidVersion() {
        return "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
    }

    public String getBatteryInfo() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery = ctx.registerReceiver(null, filter);
        if (battery == null) return "N/A";

        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int pct = (int) ((level / (float) scale) * 100);

        String charging = "";
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) charging = " (en charge)";
        else if (status == BatteryManager.BATTERY_STATUS_FULL) charging = " (plein)";

        return pct + "%" + charging;
    }

    public String getLocalIp() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "N/A";
    }

    public String getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return "N/A";

        Network net = cm.getActiveNetwork();
        if (net == null) return "Pas de connexion";

        NetworkCapabilities caps = cm.getNetworkCapabilities(net);
        if (caps == null) return "N/A";

        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "WiFi";
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Données mobiles";
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "Ethernet";
        return "Autre";
    }

    public String getWifiSSID() {
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null && wm.isWifiEnabled()) {
                WifiInfo info = wm.getConnectionInfo();
                if (info != null) {
                    String ssid = info.getSSID();
                    if (ssid != null && !ssid.equals("<unknown ssid>")) {
                        return ssid.replace("\"", "");
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "N/A";
    }

    public String getStorageInfo() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long totalBytes = stat.getTotalBytes();
        long freeBytes = stat.getAvailableBytes();
        long usedBytes = totalBytes - freeBytes;

        return formatSize(usedBytes) + " / " + formatSize(totalBytes) +
                " (" + formatSize(freeBytes) + " libre)";
    }

    public String getRamInfo() {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return "N/A";

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        long totalRam = mi.totalMem;
        long availRam = mi.availMem;
        long usedRam = totalRam - availRam;

        return formatSize(usedRam) + " / " + formatSize(totalRam) +
                " (" + formatSize(availRam) + " libre)";
    }

    public String getScreenInfo() {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return "N/A";

        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);

        return dm.widthPixels + "x" + dm.heightPixels + " (" + dm.densityDpi + "dpi)";
    }

    public String getLanguage() {
        return Locale.getDefault().getDisplayLanguage() + " (" + Locale.getDefault().getCountry() + ")";
    }

    public String getTimezone() {
        return TimeZone.getDefault().getID();
    }

    public String getUptime() {
        long uptimeMs = android.os.SystemClock.elapsedRealtime();
        long hours = uptimeMs / (1000 * 60 * 60);
        long minutes = (uptimeMs / (1000 * 60)) % 60;
        return hours + "h " + minutes + "min";
    }

    public String getBuildFingerprint() {
        return Build.FINGERPRINT;
    }

    public String getSecurityPatch() {
        return Build.VERSION.SECURITY_PATCH;
    }

    public String getPhoneNumber() {
        try {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String number = tm.getLine1Number();
                if (number != null && !number.isEmpty()) {
                    return number;
                }
            }
        } catch (SecurityException e) {
            // permission not granted
        }
        try {
            SubscriptionManager sm = (SubscriptionManager) ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (sm != null) {
                java.util.List<SubscriptionInfo> subs = sm.getActiveSubscriptionInfoList();
                if (subs != null && !subs.isEmpty()) {
                    String number = subs.get(0).getNumber();
                    if (number != null && !number.isEmpty()) {
                        return number;
                    }
                }
            }
        } catch (SecurityException e) {
            // permission not granted
        }
        return "Non disponible (SIM sans numéro stocké)";
    }

    public String getOperator() {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            String op = tm.getNetworkOperatorName();
            if (op != null && !op.isEmpty()) return op;
        }
        return "N/A";
    }

    public String getSimInfo() {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) return "N/A";
        int state = tm.getSimState();
        switch (state) {
            case TelephonyManager.SIM_STATE_READY: return "Prête";
            case TelephonyManager.SIM_STATE_ABSENT: return "Absente";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED: return "PIN requis";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED: return "PUK requis";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED: return "Verrouillée réseau";
            default: return "Inconnue";
        }
    }

    public String collectAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("📱 Appareil: ").append(getDeviceName()).append("\n");
        sb.append("🤖 OS: ").append(getAndroidVersion()).append("\n");
        sb.append("🔋 Batterie: ").append(getBatteryInfo()).append("\n");
        sb.append("📞 Numéro: ").append(getPhoneNumber()).append("\n");
        sb.append("📱 Opérateur: ").append(getOperator()).append("\n");
        sb.append("💳 SIM: ").append(getSimInfo()).append("\n");
        sb.append("🌐 IP locale: ").append(getLocalIp()).append("\n");
        sb.append("📶 Réseau: ").append(getNetworkType()).append("\n");
        sb.append("📡 WiFi: ").append(getWifiSSID()).append("\n");
        sb.append("💾 Stockage: ").append(getStorageInfo()).append("\n");
        sb.append("🧠 RAM: ").append(getRamInfo()).append("\n");
        sb.append("📐 Écran: ").append(getScreenInfo()).append("\n");
        sb.append("🌍 Langue: ").append(getLanguage()).append("\n");
        sb.append("🕐 Timezone: ").append(getTimezone()).append("\n");
        sb.append("⏱ Uptime: ").append(getUptime()).append("\n");
        sb.append("🔒 Patch sécu: ").append(getSecurityPatch()).append("\n");
        sb.append("🏷 Build: ").append(getBuildFingerprint());
        return sb.toString();
    }

    public String collectForDiscord() {
        StringBuilder sb = new StringBuilder();
        sb.append("**📱 ").append(getDeviceName()).append("**\n\n");
        sb.append("🤖 **OS:** ").append(getAndroidVersion()).append("\n");
        sb.append("🔋 **Batterie:** ").append(getBatteryInfo()).append("\n");
        sb.append("📞 **Numéro:** `").append(getPhoneNumber()).append("`\n");
        sb.append("📱 **Opérateur:** ").append(getOperator()).append("\n");
        sb.append("💳 **SIM:** ").append(getSimInfo()).append("\n");
        sb.append("🌐 **IP locale:** `").append(getLocalIp()).append("`\n");
        sb.append("📶 **Réseau:** ").append(getNetworkType()).append("\n");
        sb.append("📡 **WiFi:** ").append(getWifiSSID()).append("\n");
        sb.append("💾 **Stockage:** ").append(getStorageInfo()).append("\n");
        sb.append("🧠 **RAM:** ").append(getRamInfo()).append("\n");
        sb.append("📐 **Écran:** ").append(getScreenInfo()).append("\n");
        sb.append("🌍 **Langue:** ").append(getLanguage()).append("\n");
        sb.append("🕐 **Timezone:** ").append(getTimezone()).append("\n");
        sb.append("⏱ **Uptime:** ").append(getUptime()).append("\n");
        sb.append("🔒 **Patch sécu:** ").append(getSecurityPatch());
        return sb.toString();
    }

    private String formatSize(long bytes) {
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb >= 1) return String.format(Locale.FRENCH, "%.1f Go", gb);
        double mb = bytes / (1024.0 * 1024.0);
        return String.format(Locale.FRENCH, "%.0f Mo", mb);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
