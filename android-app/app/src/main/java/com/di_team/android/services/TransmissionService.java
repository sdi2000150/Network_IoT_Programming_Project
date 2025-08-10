package com.di_team.android.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.di_team.android.utils.CSVReader;
import com.di_team.android.R;
import com.di_team.android.utils.AlertManager;
import com.di_team.android.utils.GPSManager;
import com.di_team.android.utils.HazardHandler;
import com.di_team.android.utils.NetworkUtils;
import com.di_team.android.utils.NotificationHelper;

/**Handles periodic transmission of device location through MQTT. Stops after a given transmission time*/
public class TransmissionService extends Service implements LocationUpdateCallback {
    private static final String TAG = "TransmissionService";
    private PowerManager.WakeLock wakeLock;
    private boolean isRunning;
    private Thread monitoringThread, halterThread;
    private MQTTManager mqttManager;
    private GPSManager gpsManager;
    private CSVReader csvReader;
    private boolean isManualMode = false;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 Transmission Service Created");

        // Initialize MQTT Manager
        mqttManager = new MQTTManager(this);

        // Initialize GPS Manager
        gpsManager = new GPSManager(this, this);

        csvReader = new CSVReader(this, this);
        Notification notification = NotificationHelper.createForegroundServiceNotification(
                this,
                "⚠️ Transmission Running",
                "MQTT Transmission is active.",
                R.drawable.ic_transmission// Replace with your actual icon
        );

        // Prevent CPU from sleeping while running the service
        int[] csvFiles = {R.raw.android_1, R.raw.android_2};
        csvReader.loadCSVFromResources(csvFiles);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UserApp::MQTTWakeLock");
            wakeLock.acquire(10 * 60 * 1000L);
        }
    }

    private void startInternetMonitoring() {
        isRunning = false;
        monitoringThread = new Thread(() -> {
            boolean wasConnected = true;
            while (isRunning) {
                boolean isConnected = NetworkUtils.isInternetAvailable(this);

                if (!isConnected && wasConnected) {
                    Log.e(TAG, "❌ Internet Disconnected!");
                    AlertManager.showInternetDisconnectedNotification(this);
                    wasConnected = false;
                } else if (isConnected && !wasConnected) {
                    Log.i(TAG, "✅ Internet Reconnected!");
                    AlertManager.cancelInternetDisconnectedNotification(this); // NEW: Cancels notification when internet is restored
                    mqttManager.reconnectToMQTT();
                    wasConnected = true;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    if (!isRunning) {
                        Log.i(TAG, "✅ Internet monitoring stopped as expected.");
                    } else {
                        Log.e(TAG, "❌ Internet monitoring interrupted unexpectedly", e);
                    }
                }

            }
        });
        monitoringThread.start();
        isRunning = true;
        Toast.makeText(getApplicationContext(), "Transmission started successfully.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🚀 TransmissionService Started");

        if (intent == null) {
            Log.e(TAG, "❌ Intent is null, stopping service.");
            stopSelf();
            return START_NOT_STICKY;
        }

        startForegroundServiceProperly();

        String brokerUrl = intent.getStringExtra("BROKER_URI");
        String clientId = intent.getStringExtra("CLIENT_ID");

        isManualMode = intent.getBooleanExtra("IS_MANUAL_MODE", false);
        Log.d(TAG, "📌 Transmission Mode: " + (isManualMode ? "Manual (CSV)" : "Auto (GPS)"));

        Log.d(TAG, "📡 MQTT Broker URL: " + brokerUrl);
        Log.d(TAG, "📡 MQTT Client ID: " + clientId);
        int transmissionDuration = intent.getIntExtra("TRANSMISSION_DURATION", -1);

        mqttManager.initializeMQTT(brokerUrl, clientId);

        //Handle fields of previous instance of TransmissionService
        if(monitoringThread != null) {
            monitoringThread.interrupt();
            halterThread = null;
        }
        if(halterThread != null){
            halterThread.interrupt();
            halterThread = null;
        }
        stopTransmissions();


        startInternetMonitoring();

        // ✅ Start either Manual Mode (CSV) or Auto Mode (GPS), but not both

        // ✅ Start Transmission Mode (CSV or GPS)
        if (isManualMode) {
            Log.d(TAG, "📡 Manual Mode Enabled - Using CSV Data");
            csvReader.loadCSVFromResources(new int[]{R.raw.android_1, R.raw.android_2});
            csvReader.startManualTransmission(transmissionDuration, 1000);
        } else {
            Log.d(TAG, "📍 Auto Mode Enabled - Using Real GPS");
            gpsManager.startLocationUpdates();
        }
        // ✅ If a duration is set, stop the service after the given time
        if (transmissionDuration > 0) {
            halterThread = new Thread(() -> {
                try {
                    Thread.sleep(transmissionDuration * 1000L); // Convert to milliseconds
                    Log.i(TAG, "⏳ Transmission duration expired. Stopping service.");
                    stopSelf();
                } catch (InterruptedException e) {
                    Log.d(TAG, "Halter thread interrupted.");
                }
            });
            halterThread.start();
        }

        return START_STICKY; // ✅ Ensures service restarts with stored values if killed
    }

    private void startForegroundServiceProperly() {
        Notification notification = NotificationHelper.createForegroundServiceNotification(
                this,
                "MQTT Service",
                "Running MQTT Transmission",
                R.drawable.ic_transmission
        );
        startForeground(1, notification);
    }

    @Override
    public void onLocationUpdate(double latitude, double longitude, boolean originCSV) {

        // if origin of data aligns with service's current mode, publish location
        if (isManualMode && originCSV)
            mqttManager.publishLocation(latitude, longitude);
        else if (!isManualMode && !originCSV)
            mqttManager.publishLocation(latitude, longitude);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "🛑 Stopping TransmissionService...");
        isRunning = false;

        // Stop Network Monitoring Thread
        if (monitoringThread != null) {
            monitoringThread.interrupt();
            monitoringThread = null;
        }
        if (halterThread != null) {
            halterThread.interrupt();
            halterThread = null;
        }

        stopTransmissions();
        // ✅ Stop MQTT Transmission Properly
        if (mqttManager != null)
            mqttManager.disconnect();

        //  Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopForeground(true);
        Toast.makeText(getApplicationContext(), "Transmission stopped successfully.", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private void stopTransmissions() {
        // ✅ Stop GPS Updates
        if (gpsManager != null)
            gpsManager.stopLocationUpdates();

        // ✅ Stop CSV Transmission
        if (csvReader != null)
            csvReader.stopTransmission();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}