package database.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Timestamp;

/** Represents a detected danger event, including sensor metadata and severity level. */
public class Event {
    private static final Logger logger = LoggerFactory.getLogger(Event.class);

    private int iotId;
    private final Timestamp timestamp;
    private final double latitude;
    private final double longitude;
    private final double smokeValue;
    private final double gasValue;
    private final double temperatureValue;
    private final double uvValue;
    private final String severityLevel;

    /** Constructor for Event. */
    public Event(int iotId, Timestamp timestamp, double latitude, double longitude,
                 double smokeValue, double gasValue, double temperatureValue,
                 double uvValue, String severityLevel) {
        this.iotId = iotId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.smokeValue = smokeValue;
        this.gasValue = gasValue;
        this.temperatureValue = temperatureValue;
        this.uvValue = uvValue;
        this.severityLevel = severityLevel;

        logger.info("[Event] Created Event -> IoT_ID: {} Timestamp: {}, Latitude: {}, Longitude: {}, Smoke: {}, Gas: {}, Temp: {}, UV: {}, Severity: {}",
                iotId, timestamp, latitude, longitude, smokeValue, gasValue, temperatureValue, uvValue, severityLevel);
    }

    /** Getters */

    public int getIotId() {
        return iotId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getSmokeValue() {
        return smokeValue;
    }

    public double getGasValue() {
        return gasValue;
    }

    public double getTemperatureValue() {
        return temperatureValue;
    }

    public double getUvValue() {
        return uvValue;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }
}

