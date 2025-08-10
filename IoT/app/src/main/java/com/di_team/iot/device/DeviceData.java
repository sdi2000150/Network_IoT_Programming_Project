package com.di_team.iot.device;

import android.location.Location;

/**Class containing device-specific data to be sent through MQTT*/
public class DeviceData {
    private final float batteryPct;
    private final Location location;

    public DeviceData(float batteryPct, Location location) {
        this.batteryPct = batteryPct;
        this.location = location;
    }

    public float getBatteryPct() {
        return batteryPct;
    }

    /**Returns a label-data string representation of the location*/
    public String getLocationToString() {
        return "Latitude " + location.getLatitude() + " Longitude " + location.getLongitude();
    }
}
