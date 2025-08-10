package com.di_team.iot.sensor;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class SensorAdder {
    private final Context linkedContext;
    SensorStateManager sensorStateManager;

    /** Creates SensorAdder object, links it to calling activity's context in order to display Toasts*/
    public SensorAdder(Context context) {
        this.linkedContext = context;
        sensorStateManager = SensorStateManager.getInstance();
    }

    /**Attempts to add a new sensor, displays toasts in case of error */
    public void addNewSensor(String type, String from, String to) {
        if (!fieldAreValid(type, from, to)) return;

        float min = Float.parseFloat(from.trim());
        float max = Float.parseFloat(to.trim());
        SensorType s_type = SensorType.fromString(type);
        SensorType.SensorLimits limits = s_type.getSensorLimits();

        if (!isRangeValid(min, max)) return;

        if (!areLimitsValid(type, min, max, limits)) return;

        if(sensorExists(s_type)) return;

        onSensorCreationSuccess(s_type, min, max);
    }

    /** Checks if all fields are filled, displays error toast otherwise */
    private boolean fieldAreValid(@Nullable String type, @Nullable String from, @Nullable String to) {
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) {
            showToast("All fields should be filled out for a new sensor.");
            return false;
        }
        try {
            Double.parseDouble(from);
            Double.parseDouble(to);
        } catch (NumberFormatException e) {
            showToast("Invalid number format for min or max value.");
            return false;
        }
        return true;
    }

    /** Ensures minimum value is not greater than maximum value, displays error toast otherwise */
    private boolean isRangeValid(double min, double max) {
        if (min > max) {
            showToast("Minimum value must be lower than maximum value.");
            return false;
        }
        return true;
    }

    /** Ensures sensor doesn't already exist, displays error toast otherwise */
    private boolean sensorExists(SensorType type){
        if(sensorStateManager.sensorTypeExists(type)) {
            showToast("Requested sensor type already exists.");
            return true;
        }
        return false;
    }

    /** Validates against sensor-specific limits, displays error toast in case of violation */
    private boolean areLimitsValid(String type, double min, double max, SensorType.SensorLimits limits) {
        if (min < limits.getMin()) {
            showToast("Error: Minimum for " + type + " sensor should be ≥ " + limits.getMin());
            return false;
        }
        if (max < limits.getThreshold()) {
            showToast("Error: Maximum for " + type + " sensor should be ≥ " + limits.getThreshold());
            return false;
        }
        if (max > limits.getMax()) {
            // escape code used for less or equal sign
            showToast("Error: Maximum for " + type + " sensor should be ≤ " + limits.getMax());
            return false;
        }
        return true;
    }

    /** Handles successful sensor creation */
    private void onSensorCreationSuccess(SensorType type, float min, float max) {
        SensorConfig config = new SensorConfig(type, min, max);

        //add new configuration to sensorStateManager
        sensorStateManager.addSensorConfig(config);
        showToast("Sensor created successfully.");
    }

    /** Utility to display toast messages */
    private void showToast(String message) {
        Toast.makeText(linkedContext, message, Toast.LENGTH_SHORT).show();
    }

}
