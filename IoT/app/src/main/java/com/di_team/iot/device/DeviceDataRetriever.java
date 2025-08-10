package com.di_team.iot.device;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.CountDownLatch;

/**Class that fetches device data, mediates between the OS and DataTransmitService*/
public class DeviceDataRetriever {
    private boolean manualLocationConfig;
    private final Context context;  // associated context for location updates
    private static final String TAG = "DeviceDataRetriever";
    private String deviceID;
    private Location currentAutoLocation; // GPS location of the device
    private final CountDownLatch locationLatch = new CountDownLatch(1); // Ensures first location retrieval is synchronous.

    public DeviceDataRetriever(Context context, boolean manualLocationConfig) {
        this.context = context;
        this.manualLocationConfig = manualLocationConfig;

        // currentAutoLocation starts as null; initialized asynchronously on first access.
        this.currentAutoLocation = null;

        //deviceID will be set upon an mqtt event in service
        this.deviceID = null;

        // Trigger first location update on separate thread
        if(!manualLocationConfig)
            new Thread(this::updateDeviceLocation).start();

        Log.d(TAG, "DeviceDataRetriever created");
    }

    /**Returns a DeviceData object containing device-specific data.<br>
     * Exception may be thrown in case of missing permissions.*/
    public DeviceData getDeviceData() throws RuntimeException {
        float batteryPct = getBatteryPct();

        Location location;
        if (manualLocationConfig) { // MANUAL LOCATION
            location = getManualLocation();
        } else {    // AUTOMATED LOCATION EXTRACTION
            location = getAutoLocation();
        }
        // Return device data (battery + location)
        return new DeviceData(batteryPct, location);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters:

    // Get battery percentage
    private float getBatteryPct() {
        //receive sticky intent from BatteryManager (needs no broadcast receiver)
        IntentFilter i_filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, i_filter);

        //retrieve battery level and scale from intent
        int level, scale;
        if (batteryStatus != null) {
            level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        } else {
            Log.d(TAG, "Battery status is null, sending -1.0");
            return -1.f;
        }

        return level * 100 / (float) scale;
    }

    /** Updates device location <b>asynchronously</b> using FusedLocationProviderClient, caches it to currentAutoLocation. <br>
     * Throws exception if location permission is not granted.*/
    private void updateDeviceLocation() {
        try {
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

            // Check if location permissions are granted
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                throw new RuntimeException("Location permissions not granted.");
            }

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    currentAutoLocation = task.getResult();
                    Log.d(TAG, "Updated location to: " + currentAutoLocation.getLatitude() + ", " + currentAutoLocation.getLongitude());
                } else {
                    Log.w(TAG, "Failed to retrieve last known location.");
                }
                locationLatch.countDown(); // Signal that the location has been updated
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception during location update: ", e);
            locationLatch.countDown(); // Ensure latch is always decremented
        }
    }

    /**Fetches the current location of the device. <br>
     * First call blocks calling thread until location is fetched. <br>*/
    private Location getAutoLocation() {
        // Ensure the first initialization blocks until location is available
        try {
            locationLatch.await(); // Wait for the first location to be fetched
        } catch (InterruptedException e) {
            Log.e(TAG, "Location initialization interrupted", e);
            Thread.currentThread().interrupt();
        }

        // Trigger an asynchronous update for subsequent calls
        updateDeviceLocation();

        if (currentAutoLocation == null) {
            throw new RuntimeException("Auto location could not be initialized.");
        }

        return currentAutoLocation;
    }


    private Location getManualLocation() throws RuntimeException {
        if(deviceID == null){
            throw new RuntimeException("Device ID not set.");
        }
        Location location = new Location("manual");
        if ("IoT1".equals(deviceID)) {
            location.setLatitude(37.96809452684323);
            location.setLongitude(23.76630586399502);
        } else if ("IoT2".equals(deviceID)) {
            location.setLatitude(37.96799937191987);
            location.setLongitude(23.766603589104385);
        } else if ("IoT3".equals(deviceID)) {
            location.setLatitude(37.967779456380754);
            location.setLongitude(23.767174897611685);
        } else { // "IoT4"
            location.setLatitude(37.96790421900921);
            location.setLongitude(23.76626294807113);
        }
        return location;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Setters:

    // Set manual or automatic location config
    public void setManualLocationConfig(boolean manualLocationConfig) {
        boolean modeChanged = this.manualLocationConfig != manualLocationConfig;
        this.manualLocationConfig = manualLocationConfig;
        Log.d(TAG, "Manual location config set to " + manualLocationConfig);

        // if location mode switched to auto, initiate a location update
        if(modeChanged && !this.manualLocationConfig) {
            Log.d(TAG, "Location mode switched to auto, initiating location update..");
            new Thread(this::updateDeviceLocation).start();
        }
    }

    // Set DeviceID (TOPIC)
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
        Log.d(TAG, "Device ID set to " + deviceID);
    }
}
