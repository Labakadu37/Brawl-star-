package com.jzs.brawl;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Reads/writes the flag file consumed by libjzs_brawlv2.so.
 * Each enabled toggle is one line in /data/data/com.supercell.brawlstars/files/revenge/flags.txt
 */
public final class JzsConfig {

    public static final String DIR  = "/data/data/com.supercell.brawlstars/files/revenge";
    public static final String PATH = DIR + "/flags.txt";

    private static final Set<String> ENABLED = new HashSet<>();
    private static boolean LOADED;

    private JzsConfig() {}

    public static synchronized void load() {
        if (LOADED) return;
        LOADED = true;
        try {
            File f = new File(PATH);
            if (!f.exists()) return;
            FileInputStream in = new FileInputStream(f);
            byte[] buf = new byte[(int) f.length()];
            int n = in.read(buf);
            in.close();
            if (n <= 0) return;
            for (String line : new String(buf, 0, n, "UTF-8").split("\n")) {
                String s = line.trim();
                if (!s.isEmpty()) ENABLED.add(s);
            }
        } catch (Throwable ignored) {}
    }

    public static synchronized boolean get(String flag) {
        if (!LOADED) load();
        return ENABLED.contains(flag);
    }

    public static synchronized void set(String flag, boolean on) {
        if (!LOADED) load();
        if (on) ENABLED.add(flag); else ENABLED.remove(flag);
        save();
    }

    public static synchronized void toggle(String flag) {
        set(flag, !get(flag));
    }

    private static void save() {
        try {
            new File(DIR).mkdirs();
            StringBuilder sb = new StringBuilder();
            for (String s : ENABLED) sb.append(s).append('\n');
            FileOutputStream out = new FileOutputStream(PATH);
            out.write(sb.toString().getBytes("UTF-8"));
            out.close();
        } catch (Throwable ignored) {}
    }

    public static void writeString(Context ctx, String name, String value) {
        try {
            new File(DIR).mkdirs();
            FileOutputStream out = new FileOutputStream(DIR + "/" + name);
            out.write(value.getBytes("UTF-8"));
            out.close();
        } catch (Throwable ignored) {}
    }

    public static String readString(String name, String defVal) {
        try {
            File f = new File(DIR + "/" + name);
            if (!f.exists()) return defVal;
            FileInputStream in = new FileInputStream(f);
            byte[] buf = new byte[(int) f.length()];
            int n = in.read(buf);
            in.close();
            return n > 0 ? new String(buf, 0, n, "UTF-8").trim() : defVal;
        } catch (Throwable e) { return defVal; }
    }
}
