//import data_util.TopicManager;
//import main.MockPublisher;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.sql.Timestamp;
//import java.util.Random;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class MockPublisherTest {
//
//    @Mock
//    private MqttClient mqttClientMock;
//
//    @Test
//    void testTopicGeneration() {
//        String deviceId = "1";
//        String topic = TopicManager.getTopic(deviceId);
//        assertEquals("IoT1", topic, "The topic should be in the format 'IoT<ID>'.");
//    }
//
//    @Test
//    void testFullMessageContent() {
//        Random random = new Random();
//
//        String temperature = MockPublisher.generateSensorValue(random, 20.0, 50.0);
//        String smoke = MockPublisher.generateSensorValue(random, 0.1, 1.0);
//        String gas = MockPublisher.generateSensorValue(random, 10.0, 100.0);
//        String uv = MockPublisher.generateSensorValue(random, 1.0, 10.0);
//
//        double latitude = 40.7000 + (0.0200 * random.nextDouble());
//        double longitude = -74.0100 - (0.0200 * random.nextDouble());
//        double battery = random.nextDouble() * 100;
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//
//        String message = String.format(
//                "temperature %s smoke %s gas %s uv %s latitude %.6f longitude %.6f battery %.2f timestamp %s",
//                temperature, smoke, gas, uv, latitude, longitude, battery, timestamp
//        );
//
//        assertTrue(message.contains("temperature"), "Message should contain 'temperature'.");
//        assertTrue(message.contains("smoke"), "Message should contain 'smoke'.");
//        assertTrue(message.contains("gas"), "Message should contain 'gas'.");
//        assertTrue(message.contains("uv"), "Message should contain 'uv'.");
//        assertTrue(message.contains("latitude"), "Message should contain 'latitude'.");
//        assertTrue(message.contains("longitude"), "Message should contain 'longitude'.");
//        assertTrue(message.contains("battery"), "Message should contain 'battery'.");
//        assertTrue(message.contains("timestamp"), "Message should contain 'timestamp'.");
//    }
//
//
//    @Test
//    void testPublishingLogic() throws Exception {
//        MockitoAnnotations.openMocks(this);
//        MockPublisher publisher = new MockPublisher(mqttClientMock); // Pass the mock
//
//        String topic = "IoT1";
//        String messageContent = "temperature 25.0 smoke 0.5 gas 50.0 uv 1.0 latitude 40.7128 longitude -74.0060 battery 85.0 timestamp 2024-01-01 10:00:00";
//
//        doNothing().when(mqttClientMock).publish(eq(topic), any(MqttMessage.class));
//
//        publisher.publishMessage(topic, messageContent); // Adjust method if needed
//        verify(mqttClientMock, times(1)).publish(eq(topic), any(MqttMessage.class));
//    }
//    @Test
//    void testGenerateSensorValue() {
//        Random random = mock(Random.class);
//
//        // Simulate the 20% chance for "deactivated"
//        when(random.nextDouble()).thenReturn(0.15);
//        String value = MockPublisher.generateSensorValue(random, 10.0, 20.0);
//        assertEquals("deactivated", value, "Value should be 'deactivated' when probability < 0.2");
//
//        // Simulate normal value generation
//        when(random.nextDouble()).thenReturn(0.5).thenReturn(0.3); // First for activation, second for range
//        value = MockPublisher.generateSensorValue(random, 10.0, 20.0);
//        assertTrue(value.matches("\\d+\\.\\d{2}"), "Value should be a valid decimal number.");
//    }
//
//}
//
//
