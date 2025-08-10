//import data_util.Record;
//import data_util.RecordKey;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.sql.Timestamp;
//import java.util.HashMap;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Unit tests for demonstrating and validating interactions with the EdgeServer's map.
// */
//public class HashMapTest {
//
//    private HashMap<RecordKey, Record> sensorDataMap;
//
//    @BeforeEach
//    public void setUp() {
//        // Initialize the sensor data map
//        sensorDataMap = new HashMap<>();
//
//        // Example topics
//        String topic1 = "IoT1"; // Updated to remove "/all_sensors"
//        String topic2 = "IoT2"; // Updated to remove "/all_sensors"
//
//        Record record1 = new Record(
//                0.0, // Smoke value
//                50.0, // Gas value
//                35.0, // Temperature value
//                1.5, // UV value
//                40.7128, // Latitude
//                -74.0060, // Longitude
//                85.0, // Battery level
//                Timestamp.valueOf("2024-01-01 10:00:00")
//        );
//
//        Record record2 = new Record(
//                null, // Smoke value (deactivated)
//                null, // Gas value (deactivated)
//                null, // Temperature value(deactivated)
//                null, // UV value (deactivated)
//                40.7130, // Latitude
//                -74.0070, // Longitude
//                70.0, // Battery level
//                Timestamp.valueOf("2024-01-01 10:05:00")
//        );
//
//        // Add records to the map
//        sensorDataMap.put(new RecordKey(topic1), record1);
//        sensorDataMap.put(new RecordKey(topic2), record2);
//    }
//
//    @Test
//    public void testActiveSensorData() {
//        RecordKey activeKey = new RecordKey("IoT1");
//        assertTrue(sensorDataMap.containsKey(activeKey), "Active sensor should exist in the map.");
//
//        Record activeRecord = sensorDataMap.get(activeKey);
//        assertNotNull(activeRecord);
//        assertFalse(activeRecord.isDeactivated(), "Active sensor should not be deactivated.");
//    }
//
//    @Test
//    public void testDeactivatedSensorData() {
//        RecordKey deactivatedKey = new RecordKey("IoT2");
//        assertTrue(sensorDataMap.containsKey(deactivatedKey), "Deactivated sensor should exist in the map.");
//
//        Record deactivatedRecord = sensorDataMap.get(deactivatedKey);
//        assertNotNull(deactivatedRecord, "Deactivated record should not be null.");
//        assertTrue(deactivatedRecord.isDeactivated(), "All sensors in this record should be deactivated.");
//    }
//
//}
//
//
