package com.jzs.deviceinfo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

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

        TextView infoText = findViewById(R.id.infoText);
        TextView statusText = findViewById(R.id.statusText);

        DeviceInfoCollector collector = new DeviceInfoCollector(this);

        // Show info on screen
        infoText.setText(collector.collectAll());

        // Send to Discord webhook
        statusText.setText("Envoi en cours...");
        statusText.setTextColor(0xFF999999);

        WebhookSender.send(
                collector.getDeviceName(),
                collector.collectForDiscord(),
                (success, message) -> {
                    statusText.setText(message);
                    statusText.setTextColor(success ? 0xFF00FF00 : (int) 0xFFFF0040L);
                });
    }
}
