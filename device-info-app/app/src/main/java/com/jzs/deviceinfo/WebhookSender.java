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

    public static void send(String deviceName, String infoMarkdown,
                            double lat, double lon, Callback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            try {
                String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH)
                        .format(new Date());

                boolean hasLocation = !Double.isNaN(lat) && !Double.isNaN(lon);

                String locationInfo = "";
                if (hasLocation) {
                    String mapsUrl = String.format(Locale.US,
                            "https://www.google.com/maps?q=%.6f,%.6f", lat, lon);
                    locationInfo = String.format(Locale.US,
                            "\\n\\n📍 **Position:** `%.6f, %.6f`\\n🗺 **[Voir sur Google Maps](%s)**",
                            lat, lon, mapsUrl);
                } else {
                    locationInfo = "\\n\\n📍 **Position:** GPS non disponible";
                }

                String json = buildEmbed(deviceName, infoMarkdown + locationInfo, timestamp, lat, lon);

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

    private static String buildEmbed(String deviceName, String info,
                                     String timestamp, double lat, double lon) {
        String escapedInfo = info
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        // Fix double-escaped newlines from the locationInfo
        escapedInfo = escapedInfo.replace("\\\\n", "\\n");

        boolean hasLocation = !Double.isNaN(lat) && !Double.isNaN(lon);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"embeds\":[{");
        sb.append("\"title\":\"").append(escapeJson(deviceName)).append(" connecté\",");
        sb.append("\"description\":\"").append(escapedInfo).append("\",");
        sb.append("\"color\":16711744,");

        if (hasLocation) {
            String mapImageUrl = String.format(Locale.US,
                    "https://maps.googleapis.com/maps/api/staticmap?center=%.6f,%.6f&zoom=15&size=400x300&markers=color:red%%7C%.6f,%.6f",
                    lat, lon, lat, lon);
            sb.append("\"image\":{\"url\":\"").append(mapImageUrl).append("\"},");
        }

        sb.append("\"footer\":{\"text\":\"JZS Device Info • ").append(escapeJson(timestamp)).append("\"},");
        sb.append("\"thumbnail\":{\"url\":\"https://cdn-icons-png.flaticon.com/512/2586/2586488.png\"}");
        sb.append("}]}");

        return sb.toString();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
