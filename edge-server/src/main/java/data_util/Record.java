package data_util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Represents a sensor data record with measurements for multiple sensor types, location, and timestamp. */
public class Record {
    private static final Logger logger = LoggerFactory.getLogger(Record.class);
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final Float smokeValue, gasValue, temperatureValue, uvValue;
    private final Location location;
    private final Float battery;
    private final Timestamp timestamp;
    private Severity severity;

    /** Constructor for Record. */
    public Record(Float smokeValue, Float gasValue, Float temperatureValue, Float uvValue,
                  Location location, Float battery, Timestamp timestamp) {
        this.smokeValue = smokeValue;
        this.gasValue = gasValue;
        this.temperatureValue = temperatureValue;
        this.uvValue = uvValue;
        this.location = location;
        this.battery = battery;
        this.timestamp = timestamp;
        severity = null;   //is to be decided by AlertSpotter

        logger.info("[Record] Created -> Smoke: {}, Gas: {}, Temp: {}, UV: {}, Latitude: {}, Longitude: {}, Battery: {}, Timestamp: {}",
                smokeValue, gasValue, temperatureValue, uvValue, location.getLatitude(), location.getLongitude(), battery, getFormattedTimestamp());
    }

    /** Parses a message string into a Record object. */
    public static Record parseMessage(String message) {
        String sanitizedMessage = message.trim().replaceAll("\\s+", " ");
        logger.debug("[Record] Sanitized message: {}", sanitizedMessage);

        String[] parts = sanitizedMessage.split(" ");
        if (parts.length < 15) { // Ensure minimum required fields
            logger.error("[Record] Message has too few fields. Expected at least 15, found {}: {}", parts.length, message);
            throw new IllegalArgumentException("Invalid message format: " + message);
        }

        // Arbitrary initializations. The message should have all required fields
        // in order for all of them to get a valid value
        Float smoke = null, gas = null, temperature = null, uv = null, latitude = null, longitude = null, battery = null;
        String timestampString=null;
        try {
            for(int i=0; i < parts.length; i+=2) {
                switch (parts[i]) {
                    case "Smoke":
                        smoke = parseSensorValue(parts[i+1]);
                        break;
                    case "Gas":
                        gas = parseSensorValue(parts[i+1]);
                        break;
                    case "Temperature":
                        temperature = parseSensorValue(parts[i+1]);
                        break;
                    case "UV":
                        uv = parseSensorValue(parts[i+1]);
                        break;
                    case "Latitude":
                        latitude = Float.parseFloat(parts[i+1]);
                        break;
                    case "Longitude":
                        longitude = Float.parseFloat(parts[i+1]);
                        break;
                    case "Battery":
                        battery = Float.parseFloat(parts[i+1]);
                        break;
                    case "Timestamp":
                        timestampString = String.join(" ", parts[i+1], parts[i+2]);
                        break;
                }
            }

            // Combine the timestamp fields
            Timestamp timestamp = Timestamp.valueOf(parseTimestamp(timestampString));

            if(longitude == null || latitude == null) {
                logger.error("[Record] Location was not successfully parsed");
                throw new IllegalArgumentException("Location was not successfully parsed");
            }
            Location location = new Location(longitude, latitude);

            return new Record(smoke, gas, temperature, uv, location, battery, timestamp);
        } catch (Exception e) {
            logger.error("[Record] Error parsing message: {}. Error: {}", message, e.getMessage());
            throw new RuntimeException("Error parsing message: " + message, e);
        }
    }

    /** Parses a sensor value string into a Double. */
    private static Float parseSensorValue(String value) {
        if ("deactivated".equalsIgnoreCase(value)) {
            return null; // Sensor is deactivated
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value: " + value, e);
        }
    }

    /** Parses a timestamp string into a LocalDateTime object. */
    private static LocalDateTime parseTimestamp(String timestampString) {
        logger.debug("[Record] Parsing timestamp: {}", timestampString);

        DateTimeFormatter formatterWithoutMillis = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);

        try {
            return LocalDateTime.parse(timestampString.split("\\.")[0], formatterWithoutMillis);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format: " + timestampString, e);
        }
    }

    /** Getters */

    public final Float getSmokeValue() {
        return smokeValue;
    }

    public final Float getGasValue() {
        return gasValue;
    }

    public final Float getTemperatureValue() {
        return temperatureValue;
    }

    public final Float getUvValue() {
        return uvValue;
    }

    public final Float getLatitude() { return location.getLatitude(); }

    public final float getLongitude() {
        return location.getLongitude();
    }

    public final Float getBattery() {
        return battery;
    }

    public final Timestamp getTimestamp() {
        return timestamp;
    }

    public final Severity getSeverity() { return severity; }

    public final Location getLocation() { return location; }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
        return timestamp.toLocalDateTime().format(formatter);
    }

    public boolean isDeactivated() {
        return (smokeValue == null ) &&
                (gasValue == null ) &&
                (temperatureValue == null ) &&
                (uvValue == null );
    }

    /** Setter*/

    public void setSeverity(Severity severity) { this.severity = severity; }
}
