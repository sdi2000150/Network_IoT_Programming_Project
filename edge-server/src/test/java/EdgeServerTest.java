//import main.EdgeServer;
//import data_util.Record;
//import data_util.RecordKey;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import repository.EventRepository;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//
///**
// * Simplified unit tests for EdgeServer functionality.
// */
//public class EdgeServerTest {
//
//    private EdgeServer edgeServer;
//
//    @Mock
//    private EventRepository mockEventRepository;
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        MockitoAnnotations.openMocks(this);
//        String brokerURL = "tcp://localhost:1883";
//        mockEventRepository = mock(EventRepository.class);
//        edgeServer = new EdgeServer(brokerURL, mockEventRepository);
//    }
//
//    @Test
//    public void testValidMessageProcessing() {
//        String topic = "IoT1";
//        String message = "smoke 0.14 gas 50.0 temperature 35.0 uv 1.5 latitude 40.7128 longitude -74.0060 battery 85.0 timestamp 2024-01-01 10:10:00";
//
//        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
//        edgeServer.messageArrived(topic, mqttMessage);
//
//        RecordKey key = new RecordKey(topic);
//        Record record = edgeServer.getMap().get(key);
//
//        assertNotNull(record, "Record should not be null for valid messages.");
//        assertEquals(0.14, record.getSmokeValue());
//        assertEquals(50.0, record.getGasValue());
//        assertEquals(35.0, record.getTemperatureValue());
//        assertEquals(1.5, record.getUvValue());
//        assertEquals(85.0, record.getBattery());
//        assertEquals("2024-01-01 10:10:00", record.getFormattedTimestamp());
//
//    }
//
//    @Test
//    public void testDeactivatedSensors() {
//        String topic = "IoT2";
//        String message = "smoke deactivated gas deactivated temperature 25 uv deactivated latitude 40.7128 longitude -74.0060 battery 90.0 timestamp 2024-01-01 10:05:00";
//
//        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
//        edgeServer.messageArrived(topic, mqttMessage);
//
//        RecordKey key = new RecordKey(topic);
//        Record record = edgeServer.getMap().get(key);
//
//        assertNotNull(record, "Record should not be null for valid messages.");
//        assertNull(record.getSmokeValue(), "Smoke value should be null for deactivated sensors.");
//        assertNull(record.getGasValue(), "Gas value should be null for deactivated sensors.");
//        assertEquals(25, record.getTemperatureValue(), "Temperature value should match the input message.");
//        assertNull(record.getUvValue(), "UV value should be null for deactivated sensors.");
//        assertEquals(90.0, record.getBattery(), "Battery level should match the input message.");
//    }
//    @Test
//    public void testTimestampParsing() {
//        String topic = "IoT1";
//        String message = "smoke 0.5 gas 50.0 temperature 35.0 uv 1.5 latitude 40.7128 longitude -74.0060 battery 85.0 timestamp 2024-01-01 10:00:00";
//
//        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
//        edgeServer.messageArrived(topic, mqttMessage);
//
//        RecordKey key = new RecordKey(topic);
//        Record record = edgeServer.getMap().get(key);
//
//        assertNotNull(record, "Record should not be null for valid messages.");
//        assertEquals("2024-01-01 10:00:00", record.getFormattedTimestamp(), "Timestamp should match the input message.");
//    }
//}
//
//
