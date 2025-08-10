package com.di_team.iot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.content.Intent;
import android.Manifest;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.di_team.iot.net.IPValidator;
import com.di_team.iot.net.TransmissionService;
import com.di_team.iot.sensor.SensorAdder;

public class MenuActivity extends BaseActivity {
    ///////////// CLASS VARIABLES-SUBMODULES /////////////////////////////////
    private static final String GPS_CONFIG_STATE = "gps_config_state";
    private static final String TAG = "MenuActivity";
    private static final String SERVER_IP = "Server_IP";
    private static final String SERVER_PORT = "Server_Port";
    private static final String DEVICE_ID = "Device_ID";
    private static final String TIMES_VISITED = "Times_Visited";

    private SensorAdder sensorAdder;
    ///////////// PROGRAMMATICALLY CONTROLLED UI COMPONENTS /////////////////////////////////
    private SwitchCompat AutoGPSSwitch;
    private EditText deviceID, serverIP, serverPort, sensorMinValue, sensorMaxValue;
    private Spinner sensorTypeSpinner;

    //////////////// ACTIVITY LIFECYCLE CALLBACKS ////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        // Initialize UI components
        serverIP = findViewById(R.id.server_ip_edit_text);
        serverPort = findViewById(R.id.server_port_edit_text);
        deviceID = findViewById(R.id.deviceID_edit_text);
        AutoGPSSwitch = findViewById(R.id.gps_switch);
        sensorMinValue = findViewById(R.id.min_value_edit_text);
        sensorMaxValue = findViewById(R.id.max_value_edit_text);
        sensorTypeSpinner = findViewById(R.id.sensor_type_spinner);

        // Load saved values, fall back to default values if none are saved yet
        SharedPreferences prefs = getSharedPreferences("MenuPrefs", MODE_PRIVATE);
        serverIP.setText(prefs.getString(SERVER_IP, "10.0.2.2"));
        serverPort.setText(prefs.getString(SERVER_PORT, "1883"));
        deviceID.setText(prefs.getString(DEVICE_ID, "IoT1"));
        int timesVisited = prefs.getInt(TIMES_VISITED, 0); // Used for periodic reminders
        Log.d(TAG, "Loaded times visited: " + timesVisited);
        timesVisited++;


        boolean gpsState = prefs.getBoolean(GPS_CONFIG_STATE, false);
        String mode = (gpsState) ? "Auto" : "Manual";
        AutoGPSSwitch.setChecked(gpsState);
        AutoGPSSwitch.setText(mode);

        // Set up sensor spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sensor_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sensorTypeSpinner.setAdapter(adapter);
        AutoGPSSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onGPSConfigChanged(isChecked));

        sensorAdder = new SensorAdder(this);

        // Every few visits, request the user to ignore battery optimizations for the app
        if(timesVisited == 3) {
            timesVisited = 0;
            requestIgnoreBatteryOptimizations();
        }
        Log.d(TAG, "Saving times visited: " + timesVisited);
        prefs.edit().putInt(TIMES_VISITED, timesVisited).apply();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(GPS_CONFIG_STATE, AutoGPSSwitch.isChecked());
        outState.putString(SERVER_IP, serverIP.getText().toString());
        outState.putString(DEVICE_ID, deviceID.getText().toString());
        outState.putString(SERVER_PORT, serverPort.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        AutoGPSSwitch.setChecked(savedInstanceState.getBoolean(GPS_CONFIG_STATE, false));
        serverIP.setText(savedInstanceState.getString(SERVER_IP, "10.0.2.2"));
        serverPort.setText(savedInstanceState.getString(SERVER_PORT, "1883"));
        deviceID.setText(savedInstanceState.getString(DEVICE_ID, "IoT1"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Use shared preferences to save current menu configurations
        SharedPreferences prefs = getSharedPreferences("MenuPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(SERVER_IP, serverIP.getText().toString());
        editor.putString(SERVER_PORT, serverPort.getText().toString());
        editor.putString(DEVICE_ID, deviceID.getText().toString());
        editor.putBoolean(GPS_CONFIG_STATE, AutoGPSSwitch.isChecked());

        editor.apply(); // Save changes asynchronously
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

    public void serviceTryStop(View view) {
        stopService(new Intent(this, TransmissionService.class));
    }

    /**Handles the result of location permission request*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            // Handle fine location permission response
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Request coarse-grained location permission as a fallback
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        } else if (requestCode == COARSE_LOCATION_PERMISSION_REQUEST_CODE) {
            // Handle coarse location permission response
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Both permissions denied; inform the user
                showErrorDialog("GPS Feature Disabled","Location permissions are required to use this feature.");
                AutoGPSSwitch.setChecked(false); // Revert to manual mode
            }
        }
    }

    ///////////////// UI COMPONENT CALLBACKS ///////////////////////////////////////////

    /**onClick callback for exit button.*/
    public void displayConfirmationDialogue(View view) {
        if(isServiceRunning(TransmissionService.class)){
            requestBackgroundLocationPermission();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit App");
        builder.setMessage("Are you sure you want to exit the app?");
        builder.setPositiveButton("Yes", (dialog, which) -> finishAffinity());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**OnClick callback of new sensor button. <br>
     * Assigns the addition of a new sensor to the sensorAdder.*/
    public void sensorTryAdd(View view) {
        Log.d("MenuActivity", "Sensor try add called");
        // Retrieve filled out info about new sensor
        String sensorType = sensorTypeSpinner.getSelectedItem().toString();
        String min = sensorMinValue.getText().toString();
        String max = sensorMaxValue.getText().toString();

        // Ensure validity of the data, then add it
        sensorAdder.addNewSensor(sensorType, min, max);
    }

    /**Checks the validity of the user input and then initiates data transmission.*/
    public void serviceTryStart(View view) {
        // Get the IP, Port, and Device ID from user input
        String ip = serverIP.getText().toString().trim();
        String port = serverPort.getText().toString().trim();
        String deviceID = this.deviceID.getText().toString().trim();

        // Validate that all fields are filled
        if (!IPValidator.isValidIP(ip)) {
            showErrorDialog("Invalid IP","Please enter a valid MQTT Broker IP.");
            return;
        }

        if (port.isEmpty()) {
            showErrorDialog("Required Fields Missing","Please enter the MQTT Broker Port.");
            return;
        }

        if (deviceID.isEmpty()) {
            showErrorDialog("Required Fields Missing","Please enter the IoT's Device ID.");
            return;
        }

        // Permission already granted and all fields are valid; start MQTT service
        startMqttService(ip, port, deviceID);

    }

    private void startMqttService(String ip, String port, String deviceID) {
        Intent intent = new Intent(this, TransmissionService.class); // Use TransmissionService for simpler debugging, change if needed
        String brokerUri = "tcp://" + ip + ":" + port;

//        String brokerUri = "tcp://10.0.2.2:1883"; // For Emulator (localhost)
// OR
//        String brokerUri = "tcp://192.168.1.100:1883"; // For Physical Device (replace with your IP)

        String locationMode = (AutoGPSSwitch.isChecked()) ? "auto" : "manual";

        // Pass the broker URI and device ID (as TOPIC) to the service as intent extras
        intent.putExtra("BROKER_URI", brokerUri);
        intent.putExtra("LOCATION_MODE", locationMode);
        intent.putExtra("DEVICE_ID", deviceID);

        Log.d(TAG, "Starting MQTT service with location mode: " + locationMode);
        startService(intent);

        Toast.makeText(this, "Sensor Data Transmission started", Toast.LENGTH_SHORT).show();
    }

    /**Handles changes in user's choice of gps configuration*/
    public void onGPSConfigChanged(boolean isChecked) {
        if (isChecked) {
            // Request location permission if not granted
            if (ActivityCompat.checkSelfPermission(MenuActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting location permissions");
                ActivityCompat.requestPermissions(MenuActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        String mode = (isChecked) ? "Auto" : "Manual";
        AutoGPSSwitch.setText(mode);
    }

    /**onClick callback of Back button*/
    public void goBack(View view){ finish(); }

}

