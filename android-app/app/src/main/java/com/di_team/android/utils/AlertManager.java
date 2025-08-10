package com.di_team.android.utils;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.di_team.android.R;

public class AlertManager {
    private static final String INTERNET_ALERT_CHANNEL_ID = "internet_alert_channel";

    private static final int INTERNET_NOTIFICATION_ID = 2;

    /**
     * Shows a notification when the internet connection is lost.
     *
     * @param context The application context.
     */
    public static void showInternetDisconnectedNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    INTERNET_ALERT_CHANNEL_ID,
                    "Internet Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(context, INTERNET_ALERT_CHANNEL_ID)
                .setContentTitle("No Internet Connection")
                .setContentText("Your device has lost internet connectivity. MQTT transmission paused.")
                .setSmallIcon(R.drawable.ic_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if  (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return; // Exit if permission is not granted
            }
        }
        manager.notify(INTERNET_NOTIFICATION_ID, notification);
    }

    /**
     * Cancels the internet disconnection notification when connectivity is restored.
     *
     * @param context The application context.
     */
    public static void cancelInternetDisconnectedNotification(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.cancel(INTERNET_NOTIFICATION_ID);
    }
}
