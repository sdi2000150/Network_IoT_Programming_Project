package database.repository;

// Our imports
import database.DatabaseManager;
import database.model.Event;

// Library imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Handles database operations for the events table. */
public class EventRepository {
    private static final Logger logger = LoggerFactory.getLogger(EventRepository.class);

    // SQL query statement to insert an event into the events table
    private static final String INSERT_EVENT_SQL =
            "INSERT INTO events (iot_id, timestamp, latitude, longitude, smoke_value, gas_value, temperature_value, uv_value, severity_level) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** Saves an event to the events table in the database. */
    public void saveEvent(Event event) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_EVENT_SQL)) {

            preparedStatement.setInt(1, event.getIotId());
            preparedStatement.setTimestamp(2, event.getTimestamp());
            preparedStatement.setDouble(3, event.getLatitude());
            preparedStatement.setDouble(4, event.getLongitude());
            preparedStatement.setDouble(5, event.getSmokeValue());
            preparedStatement.setDouble(6, event.getGasValue());
            preparedStatement.setDouble(7, event.getTemperatureValue());
            preparedStatement.setDouble(8, event.getUvValue());
            preparedStatement.setString(9, event.getSeverityLevel());

            preparedStatement.executeUpdate();
            logger.info("[EventRepository] Event saved successfully -> {}", event);

        } catch (SQLException e) {
            logger.error("[EventRepository] Error saving event -> {}. SQL State: {}, Error Code: {}",
                    event, e.getSQLState(), e.getErrorCode(), e);
        }
    }
}
