package data_util;

import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** Manages the default thresholds for different sensor types. */
public class ThresholdManager {
    private static final Logger logger = LoggerFactory.getLogger(ThresholdManager.class);

    // Custom default values for each sensor type
    private static final Map<SensorType, Double> CUSTOM_DEFAULTS = new HashMap<>();
    static {
        CUSTOM_DEFAULTS.put(SensorType.SMOKE, 0.14);
        CUSTOM_DEFAULTS.put(SensorType.GAS, 9.15);
        CUSTOM_DEFAULTS.put(SensorType.TEMPERATURE, 50.0);
        CUSTOM_DEFAULTS.put(SensorType.UV, 6.0);
    }


    /**
     * Loads default thresholds for all sensor types from the configuration or custom defaults.
     *
     * @return A map of sensor types to their respective thresholds.
     */
    public static Map<SensorType, Double> getDefaultThresholds() {
        logger.info("[ThresholdManager] Loading default thresholds from configuration...");
        Map<SensorType, Double> thresholds = new HashMap<>();

        for (SensorType sensorType : SensorType.values()) {
            try {
                String propertyKey = "threshold." + sensorType.name().toLowerCase();
                String thresholdValue = ConfigManager.getProperty(propertyKey);

                if (thresholdValue == null || thresholdValue.trim().isEmpty()) {
                    thresholds.put(sensorType, getCustomDefaultThreshold(sensorType));
                } else {
                    thresholds.put(sensorType, Double.parseDouble(thresholdValue));
                }
            } catch (NumberFormatException e) {
                logger.error("[ThresholdManager] Invalid threshold value for '{}'. Using custom default: {}", sensorType, getCustomDefaultThreshold(sensorType), e);
                thresholds.put(sensorType, getCustomDefaultThreshold(sensorType));
            } catch (Exception e) {
                logger.error("[ThresholdManager] Unexpected error while loading threshold for '{}'. Using custom default: {}", sensorType, getCustomDefaultThreshold(sensorType), e);
                thresholds.put(sensorType, getCustomDefaultThreshold(sensorType));
            }
        }

        logger.info("[ThresholdManager] Thresholds loaded: {}", thresholds);
        validateThresholds(thresholds);
        return thresholds;
    }

    /**
     * Validates the loaded thresholds to ensure they are within acceptable ranges.
     *
     * @param thresholds The map of thresholds to validate.
     */
    public static void validateThresholds(Map<SensorType, Double> thresholds) {
        for (Map.Entry<SensorType, Double> entry : thresholds.entrySet()) {
            SensorType sensorType = entry.getKey();
            double threshold = entry.getValue();

            if (threshold < 0 || threshold > Double.MAX_VALUE) {
                logger.warn("[ThresholdManager] Threshold for '{}' is out of acceptable range ({}). Resetting to default maximum value.", sensorType, threshold);
                thresholds.put(sensorType, CUSTOM_DEFAULTS.getOrDefault(sensorType, Double.MAX_VALUE)); // Update with default
            }
        }
        logger.info("[ThresholdManager] Threshold validation complete.");
    }


    /**
     * Retrieves the custom default threshold for a given sensor type.
     *
     * @param sensorType The sensor type for which to retrieve the custom default.
     * @return The custom default threshold value.
     */
    public static double getCustomDefaultThreshold(SensorType sensorType) {
        return CUSTOM_DEFAULTS.getOrDefault(sensorType, Double.MAX_VALUE);
    }
}
