package com.di_team.android.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.di_team.android.R;
import com.di_team.android.core.BaseActivity;
import com.di_team.android.services.TransmissionService;
import com.di_team.android.utils.GPSManager;
import com.di_team.android.utils.HazardHandler;

public class MenuActivity extends BaseActivity {
    private static final String TAG = "MenuActivity";
    private static final String PREFS_NAME = "AppPrefs";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private EditText serverIP, serverPort, deviceID, durationEditText;
    private SwitchCompat gpsSwitch;
    private Button startMqttButton, stopMqttButton;
    private TextView gpsModeLabel;

    private int timesVisited;
    private boolean isTransmitting = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        initializeViews();
        setupGpsSwitch();
        loadSavedSettings();

        startMqttButton.setOnClickListener(this::startMQTTConnection);
        stopMqttButton.setOnClickListener(this::stopMQTTTransmission);
    }

    private void initializeViews() {
        serverIP = findViewById(R.id.server_ip_edit_text);
        serverPort = findViewById(R.id.server_port_edit_text);
        deviceID = findViewById(R.id.deviceID_edit_text);
        gpsSwitch = findViewById(R.id.gps_switch);
        startMqttButton = findViewById(R.id.start_mqtt_button);
        stopMqttButton = findViewById(R.id.stop_mqtt_button);
        durationEditText = findViewById(R.id.duration_edit_text);
        gpsModeLabel = findViewById(R.id.gps_mode_label);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Use shared preferences to save current menu configurations
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("server_ip", serverIP.getText().toString());
        editor.putString("server_port", serverPort.getText().toString());
        editor.putString("device_id", deviceID.getText().toString());
        editor.putBoolean("gps_enabled", gpsSwitch.isChecked());
        editor.putBoolean("transmission_state", isTransmitting);
        editor.apply(); // Save changes asynchronously
    }

    private void loadSavedSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        serverIP.setText(prefs.getString("server_ip", "10.0.2.2"));
        serverPort.setText(prefs.getString("server_port", "1883"));
        deviceID.setText(prefs.getString("device_id", "User"));
        gpsSwitch.setChecked(prefs.getBoolean("gps_enabled", false));
        isTransmitting = prefs.getBoolean("transmission_state", false);
        durationEditText.setVisibility(gpsSwitch.isChecked() ? View.GONE : View.VISIBLE);
        durationEditText.setText(prefs.getString("transmission_duration", "360"));
        timesVisited = prefs.getInt("times_visited", 0);
        timesVisited++;

        // Every few visits, remind user to exclude app from battery optimization
        if (timesVisited == 3) {
            timesVisited = 0;
            requestIgnoreBatteryOptimizations();
        }
        prefs.edit().putInt("times_visited", timesVisited).apply();
    }

    private void setupGpsSwitch() {
        gpsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Switch state changed");
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("gps_enabled", isChecked);
            editor.apply();
            if(isChecked &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                requestLocationPermission();

            // ✅ Update label without shifting position

            gpsModeLabel.setText(isChecked ? "Automated" : "Manual");
            durationEditText.setVisibility(isChecked ? View.GONE : View.VISIBLE);

            gpsSwitch.setThumbTintList(ContextCompat.getColorStateList(
                    getApplicationContext(),
                    isChecked ? R.color.switch_thumb : R.color.switch_thumb_disabled
            ));

            gpsSwitch.setTrackTintList(ContextCompat.getColorStateList(
                    getApplicationContext(),
                    isChecked ? R.color.switch_track : R.color.switch_track_disabled
            ));

            // If switching modes while transmitting, stop transmission
            if (isTransmitting) {
                stopMQTTTransmission(null);
                Log.w(TAG, "Stopped transmission due to mode switch!");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Location Permission Granted!");
            } else {
                Log.e(TAG, "❌ Location Permission Denied!");

                // Check if Fine Location was denied
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Request Coarse Location if Fine was denied
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // If Coarse is also denied, check if "Don't Ask Again" was selected
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (!showRationale) {
                        // 🚨 User selected "Don't Ask Again" - Guide to Settings
                        showSettingsDialog();
                    } else {
                        // User just denied once, disable GPS switch
                        gpsSwitch.setChecked(false);
                    }
                }
            }
        }
    }

    public void requestLocationPermission() {
        boolean fineLocationDenied = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean coarseLocationDenied = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;

        if (fineLocationDenied) {
            // First attempt: Request Fine Location
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (coarseLocationDenied) {
            // If Fine Location was denied, fall back to Coarse Location
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "✅ Location Permission Already Granted");
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Location permission is required to track your position. Please enable it in settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    gpsSwitch.setChecked(false);  // Disable switch if permission is denied
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    public void startMQTTConnection(View view) {
        String brokerIp = serverIP.getText().toString().trim();
        String port = serverPort.getText().toString().trim();
        String clientId = deviceID.getText().toString().trim();
        String durationInput = durationEditText.getText().toString().trim();
        boolean isManualMode = !gpsSwitch.isChecked();

        if (brokerIp.isEmpty() || port.isEmpty() || clientId.isEmpty()) {
            Log.e(TAG, "❌ Server IP, Port, or Device ID is empty!");
            return;
        }
        isTransmitting = true;

        int transmissionDuration = -1;
        if (!durationInput.isEmpty()) {
            try {
                transmissionDuration = Integer.parseInt(durationInput);
            } catch (NumberFormatException e) {
                Log.e(TAG, "⚠️ Invalid duration input. Using default (-1)");
            }
        }

        String brokerUrl = "tcp://" + brokerIp + ":" + port;

        Intent serviceIntent = new Intent(this.getApplicationContext(), TransmissionService.class);
        serviceIntent.putExtra("BROKER_URI", brokerUrl);
        serviceIntent.putExtra("CLIENT_ID", clientId);
        serviceIntent.putExtra("TRANSMISSION_DURATION", transmissionDuration);
        serviceIntent.putExtra("IS_MANUAL_MODE", isManualMode);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Log.d(TAG, "🚀 Starting TransmissionService with broker: " + brokerUrl +
                " | Mode: " + (isManualMode ? "Manual (CSV)" : "Auto (GPS)"));
    }

    public void stopMQTTTransmission(View view) {
        Intent stopServiceIntent = new Intent(this, TransmissionService.class);
        stopService(stopServiceIntent);
        isTransmitting = false;
        Log.d(TAG, "🛑 Stopping MQTT TransmissionService");
    }

    public void showConfirmationDialogue(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**Request the user to ignore battery optimizations for the app*/
    public void requestIgnoreBatteryOptimizations() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
            showBatteryOptimizationDialog();
        }
    }

    /**Shows a dialog to the user and prompt them into settings to exclude the app from battery optimization*/
    private void showBatteryOptimizationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Battery Optimization")
                .setMessage("To ensure the app runs smoothly, please exclude it from battery optimization.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(MenuActivity.this, "Battery optimization not ignored", Toast.LENGTH_SHORT).show())
                .show();
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    hideKeyboard(view);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void goBack(View view) {
        finish();
    }
}