//import data_util.SensorType;
//import data_util.ThresholdManager;
//import org.junit.jupiter.api.Test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class ThresholdManagerTest {
//
//    @Test
//    void testLoadValidThresholds() {
//        // Simulate valid thresholds
//        System.setProperty("threshold.smoke", "0.14");
//        System.setProperty("threshold.gas", "9.15");
//        System.setProperty("threshold.temperature", "50.0");
//        System.setProperty("threshold.uv", "11.0");
//
//        Map<SensorType, Double> thresholds = ThresholdManager.getDefaultThresholds();
//
//        assertEquals(0.14, thresholds.get(SensorType.SMOKE));
//        assertEquals(9.15, thresholds.get(SensorType.GAS));
//        assertEquals(50.0, thresholds.get(SensorType.TEMPERATURE));
//        assertEquals(11.0, thresholds.get(SensorType.UV));
//    }
//
//
//    @Test
//    void testHandleMissingThresholds() {
//        // Simulate missing thresholds
//        System.clearProperty("threshold.smoke");
//
//        Map<SensorType, Double> thresholds = ThresholdManager.getDefaultThresholds();
//
//        assertEquals(0.14, thresholds.get(SensorType.SMOKE)); // Custom default
//    }
//
//    @Test
//    void testHandleInvalidThresholds() {
//        // Simulate invalid thresholds
//        System.setProperty("threshold.gas", "invalid_value");
//
//        Map<SensorType, Double> thresholds = ThresholdManager.getDefaultThresholds();
//
//        assertEquals(9.15, thresholds.get(SensorType.GAS)); // Custom default
//    }
//
//    @Test
//    void testValidateThresholds() {
//        // Simulate thresholds with an out-of-range value using a mutable map
//        Map<SensorType, Double> thresholds = new HashMap<>();
//        thresholds.put(SensorType.SMOKE, -1.0); // Invalid
//        thresholds.put(SensorType.GAS, 9.15);   // Valid
//        thresholds.put(SensorType.TEMPERATURE, Double.MAX_VALUE + 1); // Invalid
//
//        // Validate thresholds
//        ThresholdManager.validateThresholds(thresholds);
//
//        // Assertions
//        assertEquals(ThresholdManager.getCustomDefaultThreshold(SensorType.SMOKE), thresholds.get(SensorType.SMOKE), "SMOKE threshold should be adjusted to custom default");
//        assertEquals(9.15, thresholds.get(SensorType.GAS), "GAS threshold should remain valid");
//        assertEquals(Double.MAX_VALUE, thresholds.get(SensorType.TEMPERATURE), "TEMPERATURE threshold should be adjusted to max value");
//    }
//
//}
//
