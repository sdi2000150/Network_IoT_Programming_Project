package com.di_team.android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.di_team.android.activities.HazardAlertActivity;
import com.di_team.android.R;

public class HazardAlertService extends Service {
    private static final String TAG = "HazardAlertService";
    private static final String CHANNEL_ID = "HazardAlertServiceChannel";
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String hazardMessage = intent.getStringExtra("hazard_message");
        String hazardLevel = intent.getStringExtra("hazard_level");

        Log.d(TAG, "🚨 Foreground Service Started: " + hazardLevel);
        showNotification(hazardLevel);
        launchHazardAlert(hazardMessage, hazardLevel);

        return START_NOT_STICKY;
    }

    private void showNotification(String hazardLevel) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Hazard Alert")
                .setContentText("A " + hazardLevel + " hazard has been detected!")
                .setSmallIcon(R.drawable.ic_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.RED)
                .build();

        startForeground(1, notification);
    }

    private void launchHazardAlert(String hazardMessage, String hazardLevel) {
        handler.post(() -> {
            Intent alertIntent = new Intent(this, HazardAlertActivity.class);
            alertIntent.putExtra("hazard_message", hazardMessage);
            alertIntent.putExtra("hazard_level", hazardLevel);
            alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(alertIntent);

            // Auto-stop service after alert timeout
            handler.postDelayed(this::stopSelf, 8000);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🛑 Hazard Alert Service Stopped.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Hazard Alert Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
