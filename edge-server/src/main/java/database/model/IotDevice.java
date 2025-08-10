package database.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Represents an IoT device with its metadata. */
public class IotDevice {
    private static final Logger logger = LoggerFactory.getLogger(IotDevice.class);

    private String deviceName;
    private double latitude;
    private double longitude;
    private double batteryLevel;

    /** Constructor for IotDevice. */
    public IotDevice(String deviceName, double latitude, double longitude, double batteryLevel) {
        this.deviceName = deviceName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.batteryLevel = batteryLevel;

        logger.info("[IotDevice] Created IotDevice -> DeviceName: {}, Latitude: {}, Longitude: {}, BatteryLevel: {}",
                deviceName, latitude, longitude, batteryLevel);
    }

    /** Getters and Setters */

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}

