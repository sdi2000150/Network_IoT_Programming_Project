package com.di_team.android.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.di_team.android.services.HazardAlertService;
import com.di_team.android.R;

import java.util.LinkedList;
import java.util.Queue;

public class HazardHandler {
    private final Context context;
    private boolean isAlertActive = false; // ✅ Prevents duplicate alerts
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "HazardAlertServiceChannel";
    private final Queue<AlertData> alertQueue = new LinkedList<>(); // ✅ Queue for pending alerts

    public HazardHandler(Context context) {
        this.context = context;
        createNotificationChannel(); // Ensure notification channel exists
    }

    public void showHazardAlert(String riskLevel, String distance) {
        Log.d("HazardHandler", "🚨 New Alert Requested -> Risk: " + riskLevel + " Distance: " + distance);
        alertQueue.add(new AlertData(riskLevel, distance)); // ✅ Add to queue

        // ✅ Save alert details to shared preferences
        SharedPreferences alertStatus = getContext().getSharedPreferences("AlertStatus", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = alertStatus.edit();
        editor.putLong("time_received", System.currentTimeMillis()) //
                .putString("severity", riskLevel)
                .putFloat("distance", Float.parseFloat(distance))
                .apply();

        processNextAlert(); // ✅ Try to display an alert
    }

    private void processNextAlert() {
        if (isAlertActive || alertQueue.isEmpty()) {
            return; // ✅ If an alert is already showing, wait before processing the next one
        }

        AlertData nextAlert = alertQueue.poll(); // Get the next alert
        if (nextAlert == null) return;

        isAlertActive = true;
        startForegroundService(nextAlert.riskLevel, nextAlert.distance);
        NotificationHelper.showAlertNotification(context, nextAlert.riskLevel, "Hazard detected at " + nextAlert.distance + "Km");
        playAlertSound(nextAlert.riskLevel);
    }
    private void startForegroundService(String riskLevel, String distance) {
        Intent serviceIntent = new Intent(context, HazardAlertService.class);
        serviceIntent.putExtra("hazard_level", riskLevel);
        serviceIntent.putExtra("hazard_message", "Distance: " + distance + " Km\nSeverity: " + riskLevel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        int dismissTime = riskLevel.equalsIgnoreCase("High") ? 10000 : 7000;
        new Handler().postDelayed(() -> {
            isAlertActive = false;
            processNextAlert(); // ✅ Show next alert after dismissing this one
        }, dismissTime);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Hazard Alert Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void playAlertSound(String riskLevel) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.w("HazardHandler", "⚠️ Sound already playing, skipping duplicate.");
            return;
        }

        int soundResId = riskLevel.equalsIgnoreCase("High") ? R.raw.high_alert_sound : R.raw.moderate_alert_sound;
        mediaPlayer = MediaPlayer.create(context, soundResId);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(false); // Ensure sound does not repeat
            mediaPlayer.setOnCompletionListener(mp -> stopAlertSound());
            mediaPlayer.start();
            Log.d("HazardHandler", "🔊 Playing custom alert sound for " + riskLevel);
        }
    }

    public void stopAlertSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("HazardHandler", "🛑 Alert sound stopped.");
        }
        isAlertActive = false;
    }

    public Context getContext() {
        return context;
    }

    private static class AlertData {
        String riskLevel;
        String distance;

        AlertData(String riskLevel, String distance) {
            this.riskLevel = riskLevel;
            this.distance = distance;
        }
    }
}
