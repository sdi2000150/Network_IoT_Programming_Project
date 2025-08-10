package com.di_team.android.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import java.util.concurrent.TimeUnit;

import com.di_team.android.services.LocationUpdateCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.concurrent.Executors;

/**Class responsible for monitoring device location. Handles location updates through passed callback.*/
public class GPSManager {
    private static final String TAG = "GPSManager";
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationUpdateCallback gpsCallback;
    private boolean isTracking = false; // ✅ Track if GPS should update
    private final java.util.concurrent.ScheduledExecutorService scheduledExecutorService; //scheduler for location update tasks

    public GPSManager(Context context, LocationUpdateCallback callback) {
        this.context = context;
        this.gpsCallback = callback;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.e(TAG, "❌ GPS permission not granted.");
            return;
        }

        // ✅ Check if GPS Mode is enabled (Auto Mode)
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean isAutoMode = prefs.getBoolean("gps_enabled", false); // Auto = true, Manual = false

        if (!isAutoMode) {
            Log.w(TAG, "🚫 GPS updates prevented - Manual Mode is ON.");
            stopLocationUpdates(); // ✅ Ensure GPS is stopped in Manual Mode
            return;
        }

        if (isTracking) {
            Log.d(TAG, "⚠️ GPS Updates already running.");
            return; // ✅ Prevent multiple starts
        }

        isTracking = true;  // ✅ Enable GPS tracking
        Log.d(TAG, "📡 GPS Updates Started");


        Runnable locationUpdateTask = new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        Log.d(TAG, "GPS Update: " + location.getLatitude() + ", " + location.getLongitude());
                        gpsCallback.onLocationUpdate(location.getLatitude(), location.getLongitude(), false);
                    } else {
                        Log.w(TAG, "Failed to retrieve last known location.");
                    }
                });
            }
        };
        scheduledExecutorService.scheduleWithFixedDelay(locationUpdateTask, 0, 1, TimeUnit.SECONDS);
    }

    public void stopLocationUpdates() {
        if (!isTracking) {
            Log.d(TAG, "⚠️ GPS Updates were not running.");
            return; // ✅ Prevent redundant stop calls
        }

        isTracking = false;  // ✅ Stop tracking
        scheduledExecutorService.shutdownNow();
        Log.d(TAG, "❌ GPS Updates Fully Stopped");
    }
}