//import data_util.Record;
//import data_util.RecordKey;
//import data_util.SensorType;
//import data_util.ThresholdManager;
//import main.AlertSpotter;
//import model.Event;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import repository.EventRepository;
//import repository.IotDeviceRepository;
//
//import java.sql.Timestamp;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class AlertSpotterTest {
//
//    private ConcurrentHashMap<RecordKey, Record> map;
//    private EventRepository eventrepositoryMock;
//    private IotDeviceRepository iotrepositoryMock;
//    private AlertSpotter alertSpotter;
//
//    @BeforeEach
//    void setUp() {
//        map = new ConcurrentHashMap<>();
//        Map<SensorType, Double> thresholds = Map.of(
//                SensorType.SMOKE, 0.14,
//                SensorType.GAS, 9.15,
//                SensorType.TEMPERATURE, 50.0,
//                SensorType.UV, 11.0
//        );
//        eventrepositoryMock = mock(EventRepository.class);
//        alertSpotter = new AlertSpotter(map, thresholds, 5, eventrepositoryMock, iotrepositoryMock, 1);
//    }
//
//    @Test
//    void testAlertForThresholdViolations() {
//        Record record = new Record(0.2, null, 60.0, null, 40.7128, -74.0060, 90.0, new Timestamp(System.currentTimeMillis()));
//        RecordKey key = new RecordKey("IoT1");
//
//        map.put(key, record);
//
//        alertSpotter.run();
//
//        verify(eventrepositoryMock, times(1)).saveEvent(any(Event.class));
//    }
//
//    @Test
//    void testSkipDeactivatedSensors() {
//        Record record = new Record(null, null, null, null, 40.7128, -74.0060, 90.0, new Timestamp(System.currentTimeMillis()));
//        RecordKey key = new RecordKey("IoT2");
//
//        map.put(key, record);
//
//        alertSpotter.run();
//
//        verify(eventrepositoryMock, never()).saveEvent(any(Event.class));
//    }
//
//    @Test
//    void testSeverityDetermination() {
//        // Example for determining severity
//        assertEquals("High Risk", alertSpotter.determineSeverity(true, true, false, false));
//        assertEquals("Moderate Risk", alertSpotter.determineSeverity(false, false, true, true));
//        assertEquals("No Risk", alertSpotter.determineSeverity(false, false, false, false));
//    }
//}
//
