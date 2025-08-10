package data_util;

/** Enum representing sensor types in the system. */
public enum SensorType {
    SMOKE, GAS, TEMPERATURE, UV;

    public static SensorType fromString(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor type string cannot be null or empty");
        }

        switch (s.trim().toLowerCase()) {
            case "smoke":
                return SMOKE;
            case "gas":
                return GAS;
            case "temperature":
                return TEMPERATURE;
            case "uv":
                return UV;
            default:
                throw new IllegalArgumentException("Unknown sensor type: " + s);
        }
    }
}
