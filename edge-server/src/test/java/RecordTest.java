//import data_util.Record;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class RecordTest {
//    private static final Logger logger = LoggerFactory.getLogger(RecordTest.class);
//
//    @Test
//    public void testValidRecordParsing() {
//        String validMessage = "smoke 50.0 gas 25.0 temperature 5.0 uv 0.2 latitude 40.7128 longitude -74.0060 battery 80.0 timestamp 2025-01-04 22:00:00";
//        logger.debug("[Test] Valid record message: {}", validMessage);
//
//        Record record = Record.parseMessage(validMessage);
//        assertNotNull(record);
//
//        logger.debug("[Test] Valid record parsed successfully.");
//        assertEquals(50.0, record.getSmokeValue());
//        assertEquals(25.0, record.getGasValue());
//        assertEquals(5.0, record.getTemperatureValue());
//        assertEquals(0.2, record.getUvValue());
//        assertEquals("2025-01-04 22:00:00", record.getFormattedTimestamp());
//    }
//
//    @Test
//    public void testAllSensorsDeactivated() {
//        String deactivatedMessage = "smoke -1.0 gas -1.0 temperature -1.0 uv -1 latitude 40.7128 longitude -74.0060 battery -1.0 timestamp 2025-01-04 22:00:00";
//        logger.debug("[Test] All sensors deactivated message: {}", deactivatedMessage);
//
//        Record record = Record.parseMessage(deactivatedMessage);
//        assertNotNull(record);
//
//        logger.debug("[Test] Deactivated record parsed successfully.");
//        assertTrue(record.isDeactivated());
//    }
//
//
//    @Test
//    public void testExtremeValues() {
//        String extremeValuesMessage = "smoke 1.0 gas 1.7E308 Temperature -1.7E308 uv 0.0 latitude 40.7128 longitude -74.0060 battery 3.4E38 timestamp 2025-01-04 22:00:00";
//        logger.debug("[Test] Extreme values message: {}", extremeValuesMessage);
//
//        Record record = Record.parseMessage(extremeValuesMessage);
//        assertNotNull(record);
//
//        logger.debug("[Test] Extreme values record parsed successfully.");
//        assertEquals(1.0, record.getSmokeValue());
//        assertEquals(1.7E308, record.getGasValue());
//        assertEquals(-1.7E308, record.getTemperatureValue());
//        assertEquals(0.0, record.getUvValue());
//        assertEquals(3.4E38, record.getBattery());
//        assertEquals("2025-01-04 22:00:00", record.getFormattedTimestamp());
//    }
//
//
//    @Test
//    public void testInvalidMessageFormat() {
//        String invalidMessage = "gas 50.0 temperature 25.0 uv"; // Missing fields
//        logger.debug("[Test] Invalid message format: {}", invalidMessage);
//
//        assertThrows(IllegalArgumentException.class, () -> Record.parseMessage(invalidMessage));
//    }
//}
//
//
