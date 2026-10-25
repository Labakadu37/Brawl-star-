package com.phonk.blaster;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

public class PhonkService extends Service {

    private static final String CHANNEL_ID = "phonk_channel";
    private static final int NOTIFICATION_ID = 1;
    private MediaPlayer player;
    private ContentObserver volumeObserver;
    private AudioManager audioManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);

        if (player == null) {
            player = MediaPlayer.create(this, R.raw.phonk);
            if (player != null) {
                player.setLooping(true);
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
                player.setVolume(1.0f, 1.0f);
                player.start();
            }
        }

        // Monitor volume changes and reset to max
        startVolumeMonitor();

        return START_STICKY;
    }

    private void startVolumeMonitor() {
        if (volumeObserver != null) return;

        Handler handler = new Handler(Looper.getMainLooper());
        volumeObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (audioManager != null) {
                    int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (currentVol < maxVol) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
                    }
                }
            }
        };

        getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, volumeObserver);

        // Set to max right now
        if (audioManager != null) {
            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
        }
    }

    @Override
    public void onDestroy() {
        if (volumeObserver != null) {
            getContentResolver().unregisterContentObserver(volumeObserver);
            volumeObserver = null;
        }
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Phonk Player",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Phonk music playback");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        Intent stopIntent = new Intent(this, PhonkService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
                .setContentTitle("PHONK")
                .setContentText("Playing...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
    }
}
