//import database.DatabaseManager;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Comprehensive integration test for DatabaseManager.
// */
//public class DatabaseManagerIntegrationTest {
//
//    @Test
//    public void testDatabaseManagerIntegration() throws Exception {
//        // Initialize Database
//        DatabaseManager.initializeDatabase();
//
//        // Verify Singleton Instance
//        DatabaseManager instance1 = DatabaseManager.getInstance();
//        DatabaseManager instance2 = DatabaseManager.getInstance();
//        assertSame(instance1, instance2, "DatabaseManager should return the same instance");
//
//        // Verify Connection
//        Connection connection = instance1.getConnection();
//        assertNotNull(connection, "Database connection should not be null");
//        assertFalse(connection.isClosed(), "Database connection should be open");
//
//        // Verify Schema Initialization
//        try (Statement statement = connection.createStatement()) {
//            ResultSet resultSet = statement.executeQuery(
//                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'events'");
//            assertTrue(resultSet.next(), "Schema initialization should have created the 'events' table");
//            assertEquals(2, resultSet.getInt(1), "The 'events' table should exist");
//        }
//
//        // Test Table Truncation
//        try (Statement statement = connection.createStatement()) {
//            // Insert dummy data
//            statement.executeUpdate("INSERT INTO events (timestamp, latitude, longitude, smoke_value, gas_value, temperature_value, uv_value, severity_level) VALUES (NOW(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 'LOW')");
//
//            // Truncate Table
//            instance1.truncateTable("events");
//
//            // Verify Table Is Empty
//            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM events");
//            resultSet.next();
//            assertEquals(0, resultSet.getInt(1), "The 'events' table should be empty after truncation");
//        }
//
//        // Close Connection
//        instance1.closeConnection();
//        assertTrue(connection.isClosed(), "Database connection should be closed");
//    }
//
//}
//
//
