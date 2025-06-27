package com.example.androidmobileapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;

/**
 * PUBLIC_INTERFACE
 * Foreground service that plays alarm sound and vibration until dismissed
 */
public class AlarmService extends Service {
    
    private static final String CHANNEL_ID = "AlarmChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private boolean isPlaying = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        acquireWakeLock();
        setupMediaPlayer();
        setupVibrator();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        startAlarm();
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        stopAlarm();
        releaseWakeLock();
        super.onDestroy();
    }
    
    /**
     * Create notification channel for Android O+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getString(R.string.alarm_channel_description));
            channel.setSound(null, null);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Create foreground notification
     */
    private Notification createNotification() {
        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.alarm_notification_title))
            .setContentText(getString(R.string.alarm_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build();
    }
    
    /**
     * Acquire wake lock to keep device awake
     */
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WakeMind:AlarmWakeLock"
        );
        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes max
    }
    
    /**
     * Release wake lock
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
    
    /**
     * Setup media player for alarm sound
     */
    private void setupMediaPlayer() {
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alarmSound);
            
            // Set to play on alarm stream and override silent mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
                mediaPlayer.setAudioAttributes(audioAttributes);
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            }
            
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Setup vibrator
     */
    private void setupVibrator() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    /**
     * Start alarm sound and vibration
     */
    private void startAlarm() {
        if (!isPlaying) {
            isPlaying = true;
            
            // Play alarm sound
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Start vibration pattern
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 1000, 1000}; // Wait 0ms, vibrate 1000ms, wait 1000ms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(pattern, 0, 
                        new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build());
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
        }
    }
    
    /**
     * Stop alarm sound and vibration
     */
    private void stopAlarm() {
        if (isPlaying) {
            isPlaying = false;
            
            // Stop media player
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    mediaPlayer = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Stop vibration
            if (vibrator != null) {
                vibrator.cancel();
            }
        }
    }
    
    /**
     * PUBLIC_INTERFACE
     * Stop the alarm service (called when challenge is solved)
     */
    public static void stopAlarmService(Context context) {
        Intent serviceIntent = new Intent(context, AlarmService.class);
        context.stopService(serviceIntent);
    }
}
