package com.jzs.deviceinfo;

import android.os.Handler;
import android.os.Looper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class WebhookSender {

    public interface Callback {
        void onResult(boolean success, String message);
    }

    private static final String WEBHOOK_URL = BuildConfig.WEBHOOK_URL;

    public static void send(String deviceName, String infoMarkdown, Callback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            try {
                String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH)
                        .format(new Date());

                String json = buildEmbed(deviceName, infoMarkdown, timestamp);

                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                byte[] body = json.getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(body.length);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                }

                int code = conn.getResponseCode();
                conn.disconnect();

                boolean ok = code >= 200 && code < 300;
                mainHandler.post(() -> callback.onResult(ok,
                        ok ? "Envoyé !" : "Erreur HTTP " + code));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onResult(false, "Erreur: " + e.getMessage()));
            }
        });
    }

    private static String buildEmbed(String deviceName, String info, String timestamp) {
        String escapedInfo = info
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        return "{\"embeds\":[{"
                + "\"title\":\"" + escapeJson(deviceName) + " connecté\","
                + "\"description\":\"" + escapedInfo + "\","
                + "\"color\":16711744,"
                + "\"footer\":{\"text\":\"JZS Device Info • " + escapeJson(timestamp) + "\"},"
                + "\"thumbnail\":{\"url\":\"https://cdn-icons-png.flaticon.com/512/2586/2586488.png\"}"
                + "}]}";
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
