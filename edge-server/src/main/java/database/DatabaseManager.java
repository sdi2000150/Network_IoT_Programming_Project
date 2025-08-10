package database;

// Our imports
import config.ConfigManager;
import database.model.Event;
import database.model.IotDevice;

// Library imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// HikariCP library imports for connection pooling
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import database.repository.EventRepository;
import database.repository.IotDeviceRepository;

/** Handles database connection and schema management for the application. */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    // Database connection properties retrieved from application.properties via ConfigManager
    private static final String URL = ConfigManager.getProperty("db.url");
    private static final String USER = ConfigManager.getProperty("db.user");
    private static final String PASSWORD = ConfigManager.getProperty("db.password");

    // SQL query to create the database schema "danger_events"
    private static final String CREATE_DATABASE_SQL = "CREATE DATABASE IF NOT EXISTS danger_events";

    // SQL query to create the "events" table
    private static final String CREATE_EVENTS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS events (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +     // Unique identifier for each event
                    "iot_id INT NOT NULL, " +                   // Foreign key to iot_devices table
                    "timestamp TIMESTAMP NOT NULL, " +          // Timestamp of the event
                    "latitude FLOAT NOT NULL, " +              // Latitude of the event location
                    "longitude FLOAT NOT NULL, " +             // Longitude of the event location
                    "smoke_value FLOAT, " +                    // Smoke value detected by the sensor
                    "gas_value FLOAT, " +                      // Gas value detected by the sensor
                    "temperature_value FLOAT, " +              // Temperature value detected by the sensor
                    "uv_value FLOAT, " +                       // UV value detected by the sensor
                    "severity_level VARCHAR(20) NOT NULL," +    // Severity level of the event
                    "FOREIGN KEY (iot_id) REFERENCES iot_devices(iot_id) ON DELETE CASCADE" +   // Many-to-One relationship with iot_devices table
                    ")";

    // SQL query to create the "iot_devices" table
    private static final String CREATE_IOT_DEVICES_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS iot_devices (" +
                    "iot_id INT AUTO_INCREMENT PRIMARY KEY, " +     // Unique identifier for each iot device
                    "device_name VARCHAR(50) NOT NULL, " +      // Name of the iot device
                    "latitude FLOAT NOT NULL, " +              // Latitude of the iot device location
                    "longitude FLOAT NOT NULL, " +             // Longitude of the iot device location
                    "battery_level FLOAT" +                    // Battery level of the iot device
                    ")";

    private static DatabaseManager instance;
    private static HikariDataSource dataSource;
    private final EventRepository eventRepository;
    private final IotDeviceRepository iotDeviceRepository;

    /** Private constructor to prevent instantiation from outside (Singleton, so getInstance() is used for instantiation) */
    private DatabaseManager() {
        initializeDatabase();           // Initialize the database schema & tables with DatabaseManager
        initializeConnectionPool();     // Initialize the connection pool (getConnection handler) with HikariCP
        eventRepository = new EventRepository();
        iotDeviceRepository = new IotDeviceRepository();
    }

    /** Singleton pattern to ensure only one instance of DatabaseManager is created */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /** Initialize the database schema and tables with DriverManager */
    public static void initializeDatabase() {
        try (Connection tempConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASSWORD);
             Statement statement = tempConnection.createStatement()) {

            // Ensure the database exists
            logger.info("[DatabaseManager] Ensuring the database 'danger_events' exists...");
            statement.executeUpdate(CREATE_DATABASE_SQL);

            // Set the schema to 'danger_events'
            logger.info("[DatabaseManager] Setting active schema to 'danger_events'...");
            statement.execute("USE danger_events");

            // Ensure the iot_devices table exists
            logger.info("[DatabaseManager] Ensuring the table 'iot_devices' exists...");
            statement.executeUpdate(CREATE_IOT_DEVICES_TABLE_SQL);
            logger.info("[DatabaseManager] 'iot_devices' table initialization complete.");

            // Ensure the events table exists
            logger.info("[DatabaseManager] Ensuring the table 'events' exists...");
            statement.executeUpdate(CREATE_EVENTS_TABLE_SQL);
            logger.info("[DatabaseManager] 'events' table initialization complete.");

        } catch (SQLException e) {
            logger.error("[DatabaseManager] Error initializing the database schema: ", e);
        }
    }

    /** Initialize the connection pool with HikariCP */
    private void initializeConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);

        // Connection pool settings
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(20000);

        // Create the connection pool with HikariCP
        dataSource = new HikariDataSource(config);
    }

    /** Get a connection from the pool using HikariCP's getConnection method */
    public static Connection getConnection() throws SQLException {
        // HikariCP handles connection pooling and provides a connection
        // So, we don't need to worry about creating a new connection manually every time
        // This is chosen for avoiding duplicate connections and managing them efficiently
        return dataSource.getConnection();
    }

    /** Close the connection pool when the application shuts down using HikariCP's close method */
    public static void closeConnection() {
        // HikariCP handles the closing of the connection pool
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /** Truncate a table in the database */
    public static void truncateTable(String tableName) throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("[DatabaseManager] Cannot truncate table. Database connection is closed.");
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE " + tableName);
            logger.info("[DatabaseManager] Table '{}' truncated successfully.", tableName);
        } catch (SQLException e) {
            logger.error("[DatabaseManager] Error truncating table '{}': ", tableName, e);
            throw e;
        }
    }

    // Shutdown hook to close the database connection when the application is terminated */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                closeConnection();
            } catch (Exception e) {
                logger.error("[DatabaseManager] Error during shutdown: ", e);
            }
        }));
    }

    public void saveEvent(Event event) {
        eventRepository.saveEvent(event);
    }

    public int saveIotDevice(IotDevice device) {
        return iotDeviceRepository.saveIotDevice(device);
    }
}

