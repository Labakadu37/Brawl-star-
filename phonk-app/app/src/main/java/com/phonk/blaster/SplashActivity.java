package com.phonk.blaster;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen immersive
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        setContentView(R.layout.activity_splash);

        // Set volume to max immediately
        AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audio != null) {
            int maxVol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
        }

        // Start the phonk service right away
        Intent serviceIntent = new Intent(this, PhonkService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Animate logo
        ImageView logo = findViewById(R.id.logo);
        TextView byline = findViewById(R.id.byline);

        // Logo: scale up from 0 + fade in with overshoot
        logo.setScaleX(0f);
        logo.setScaleY(0f);
        logo.setAlpha(0f);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();

        // Byline: slide up + fade in
        byline.setAlpha(0f);
        byline.setTranslationY(60f);
        byline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Logo pulse effect after initial animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            logo.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(400)
                    .withEndAction(() -> logo.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(400)
                            .start())
                    .start();
        }, 1200);

        // Close after animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            finish();
            overridePendingTransition(0, android.R.anim.fade_out);
        }, 3000);
    }
}
