package com.di_team.iot.sensor;

import static com.di_team.iot.sensor.SensorType.Gas;
import static com.di_team.iot.sensor.SensorType.Smoke;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
/**Singleton class which maintains and updates a list of the so far configured sensors.
Every activity always has access to the same instance of SensorStateManager*/
public class SensorStateManager {
    private static SensorStateManager instance;
    List<SensorConfig> sensorConfigs;
    private static final String TAG = "SensorStateManager";

    /**Returns instance of SensorStateManager. if it's null, it's initialized.
    * synchronized: only one thread (i.e.) activity can access it at a time*/
    public static synchronized SensorStateManager getInstance(){
        if(instance == null)
            instance = new SensorStateManager();
        return instance;
    }
    private SensorStateManager(){
        //initialize list with the configurations of default sensors: gas and smoke
        sensorConfigs = new ArrayList<>();
        SensorConfig gas_config = new SensorConfig(Gas,
                Gas.getSensorLimits().getMin(),
                Gas.getSensorLimits().getMax());
        SensorConfig smoke_config = new SensorConfig(Smoke,
                Smoke.getSensorLimits().getMin(),
                Smoke.getSensorLimits().getMax());
        sensorConfigs.add(gas_config);
        sensorConfigs.add(smoke_config);
        Log.d(TAG, "Instance created with list reference:" + sensorConfigs);
    }

    /**Returns unmodifiable copy of current sensor configurations*/
    public List<SensorConfig> getSensorConfigs() {
            return Collections.unmodifiableList(sensorConfigs);
    }

    /**Returns a sensor configuration object of the given type.*/
    public final SensorConfig getConfig(SensorType type) {
        for(SensorConfig config: sensorConfigs)
            if(config.getType() == type)
                return config;
        return null;
    }

    /** Adds a new sensor configuration to the list.*/
    public void addSensorConfig(SensorConfig config) {
        sensorConfigs.add(config);
        Log.d(TAG, "New sensor was added.");
    }

    /**Checks if a sensor of the given type exists in the list.*/
    public boolean sensorTypeExists(SensorType type) {
        for (SensorConfig config : sensorConfigs) {
            if (config.getType() == type) {
                return true;
            }
        }
        return false;
    }

    /**Returns a list of uninstalled sensors.*/
    public List<SensorType> getUninstalledSensors() {
        // Get all possible sensor types, store in EnumSet
        EnumSet<SensorType> allSensorTypes = EnumSet.allOf(SensorType.class);

        // Derive a list of installed sensor types
        List<SensorType> installedSensorTypes = new ArrayList<>();
        for (SensorConfig config : sensorConfigs) {
            installedSensorTypes.add(config.getType());
        }

        // Uninstalled = (All) - (Installed)
        installedSensorTypes.forEach(allSensorTypes::remove);

        // Convert to a list and return
        return new ArrayList<>(allSensorTypes);
    }
}
