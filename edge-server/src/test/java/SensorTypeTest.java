//import data_util.SensorType;
//import data_util.ThresholdManager;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class SensorTypeTest {
//
//    private static final Logger logger = LoggerFactory.getLogger(SensorTypeTest.class);
//
//    /**
//     * Tests the conversion of valid sensor type strings to SensorType enums.
//     */
//    @Test
//    void testValidSensorTypeConversions() {
//        logger.info("[SensorTypeTest] Starting testValidSensorTypeConversions");
//
//        // Test all valid sensor types
//        assertEquals(SensorType.SMOKE, SensorType.fromString("smoke"));
//        logger.info("[SensorTypeTest] Successfully matched 'smoke' to SensorType.SMOKE");
//
//        assertEquals(SensorType.GAS, SensorType.fromString("gas"));
//        logger.info("[SensorTypeTest] Successfully matched 'gas' to SensorType.GAS");
//
//        assertEquals(SensorType.TEMPERATURE, SensorType.fromString("temperature"));
//        logger.info("[SensorTypeTest] Successfully matched 'temperature' to SensorType.TEMPERATURE");
//
//        assertEquals(SensorType.UV, SensorType.fromString("uv"));
//        logger.info("[SensorTypeTest] Successfully matched 'uv' to SensorType.UV");
//
//        // Test case insensitivity
//        assertEquals(SensorType.SMOKE, SensorType.fromString("SMOKE"));
//        logger.info("[SensorTypeTest] Successfully matched 'SMOKE' (case insensitive) to SensorType.SMOKE");
//
//        assertEquals(SensorType.GAS, SensorType.fromString("Gas"));
//        logger.info("[SensorTypeTest] Successfully matched 'Gas' (case insensitive) to SensorType.GAS");
//
//        assertEquals(SensorType.TEMPERATURE, SensorType.fromString("TeMpErAtUrE"));
//        logger.info("[SensorTypeTest] Successfully matched 'TeMpErAtUrE' (case insensitive) to SensorType.TEMPERATURE");
//
//        assertEquals(SensorType.UV, SensorType.fromString("UV"));
//        logger.info("[SensorTypeTest] Successfully matched 'UV' (case insensitive) to SensorType.UV");
//    }
//
//    /**
//     * Tests that an exception is thrown for an unknown sensor type string.
//     */
//    @Test
//    void testInvalidSensorType() {
//        logger.info("[SensorTypeTest] Starting testInvalidSensorType");
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            SensorType.fromString("unknown");
//        });
//
//        logger.error("[SensorTypeTest] Exception caught as expected: {}", exception.getMessage());
//        assertEquals("Unknown sensor type: unknown", exception.getMessage());
//    }
//
//    /**
//     * Tests that an exception is thrown when the input string is null.
//     */
//    @Test
//    void testNullSensorType() {
//        logger.info("[SensorTypeTest] Starting testNullSensorType");
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            SensorType.fromString(null);
//        });
//
//        logger.error("[SensorTypeTest] Exception caught as expected: {}", exception.getMessage());
//        assertTrue(exception.getMessage().contains("Sensor type string cannot be null or empty"));
//    }
//
//    /**
//     * Tests that an exception is thrown when the input string is empty.
//     */
//    @Test
//    void testEmptyStringSensorType() {
//        logger.info("[SensorTypeTest] Starting testEmptyStringSensorType");
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            SensorType.fromString("");
//        });
//
//        logger.error("[SensorTypeTest] Exception caught as expected: {}", exception.getMessage());
//        assertTrue(exception.getMessage().contains("Sensor type string cannot be null or empty"));
//    }
//
//    /**
//     * Tests if thresholds are correctly loaded and mapped to the respective SensorTypes.
//     */
//    @Test
//    void testThresholdLoading() {
//        logger.info("[SensorTypeTest] Starting testThresholdLoading");
//
//        // Load the default thresholds from the ThresholdManager
//        Map<SensorType, Double> thresholds = ThresholdManager.getDefaultThresholds();
//        logger.debug("[SensorTypeTest] Loaded thresholds: {}", thresholds);
//
//        // Assert that thresholds are loaded correctly
//        assertNotNull(thresholds, "Threshold map should not be null");
//        logger.info("[SensorTypeTest] Threshold map is not null");
//
//        assertFalse(thresholds.isEmpty(), "Threshold map should not be empty");
//        logger.info("[SensorTypeTest] Threshold map is not empty");
//
//        // Check specific sensor thresholds
//        assertEquals(50.00, thresholds.get(SensorType.TEMPERATURE), "Temperature threshold mismatch");
//        logger.info("[SensorTypeTest] Verified Temperature threshold: {}", thresholds.get(SensorType.TEMPERATURE));
//
//        assertEquals(0.14, thresholds.get(SensorType.SMOKE), "Smoke threshold mismatch");
//        logger.info("[SensorTypeTest] Verified Smoke threshold: {}", thresholds.get(SensorType.SMOKE));
//
//        assertEquals(9.15, thresholds.get(SensorType.GAS), "Gas threshold mismatch");
//        logger.info("[SensorTypeTest] Verified Gas threshold: {}", thresholds.get(SensorType.GAS));
//
//        assertEquals(11.0, thresholds.get(SensorType.UV), "UV threshold mismatch");
//        logger.info("[SensorTypeTest] Verified UV threshold: {}", thresholds.get(SensorType.UV));
//    }
//}
//
//
