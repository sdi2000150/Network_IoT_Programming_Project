package com.di_team.android.utils;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.di_team.android.R;
import com.di_team.android.services.StopServiceReceiver;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String ALERTS_CHANNEL_ID = "Alerts";
    private static final String SYSTEM_CHANNEL_ID = "SystemEvents";

    /**
     * Creates a notification to be used for foreground services.
     * This ensures the service is not killed by the system.
     *
     * @param context  The application context.
     * @param title    The title of the notification.
     * @param message  The message body.
     * @param iconRes  The small icon for the notification.
     * @return         A Notification object ready for foreground service usage.
     */
    public static Notification createForegroundServiceNotification(Context context, String title, String message, int iconRes) {
        String channelId = SYSTEM_CHANNEL_ID; // Use the system events channel

        // Intent to stop the service
        Intent stopIntent = new Intent(context, StopServiceReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        channelId,
                        "Foreground Service",
                        NotificationManager.IMPORTANCE_LOW
                );
                serviceChannel.setDescription("Service notifications (MQTT, Internet status, etc.)");
                manager.createNotificationChannel(serviceChannel);
            }
        }

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // Keep it persistent
                .addAction(0, "Stop", stopPendingIntent) // Stop button
                .build();
    }
    /**
     * Ensures notification channels are created only once when the app starts.
     * This method should be called in `Application` class or `MainActivity`.
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // 🔔 Alerts Notification Channel
            NotificationChannel alertChannel = new NotificationChannel(
                    ALERTS_CHANNEL_ID,
                    "Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("Notifications for important alerts.");
            alertChannel.enableVibration(true);
            alertChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(alertChannel);

            // ⚙️ System Events Channel (e.g., Internet status, MQTT status)
            NotificationChannel systemChannel = new NotificationChannel(
                    SYSTEM_CHANNEL_ID,
                    "System Events",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            systemChannel.setDescription("General system notifications.");
            systemChannel.enableLights(true);
            manager.createNotificationChannel(systemChannel);

            Log.i(TAG, "✅ Notification channels initialized.");
        }
    }

    /**
     * Shows an **Alert Notification** for critical events.
     * Example: Environmental hazard warnings or critical alerts.
     */
    public static void showAlertNotification(Context context, String title, String message) {
        showNotification(context, ALERTS_CHANNEL_ID, title, message, NotificationManager.IMPORTANCE_HIGH);
    }

    /**
     * Shows a **System Notification** for status updates (e.g., MQTT connection, Internet loss).
     */
    public static void showSystemNotification(Context context, String title, String message) {
        showNotification(context, SYSTEM_CHANNEL_ID, title, message, NotificationManager.IMPORTANCE_DEFAULT);
    }

    /**
     * Displays a notification with the specified parameters.
     *
     * @param context    The application context.
     * @param channelId  The notification channel ID.
     * @param title      The title of the notification.
     * @param message    The message body.
     * @param importance The priority level of the notification.
     */
    private static void showNotification(Context context, String channelId, String title, String message, int importance) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(importance == NotificationManager.IMPORTANCE_HIGH ?
                        NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        // Generate a unique ID to prevent overwriting previous notifications
        int notificationId = (int) System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return; // Exit if permission is not granted
            }
        }
        manager.notify(notificationId, builder.build());


        Log.i(TAG, "🔔 Notification Sent: [" + channelId + "] " + title + " - " + message);
    }
}
