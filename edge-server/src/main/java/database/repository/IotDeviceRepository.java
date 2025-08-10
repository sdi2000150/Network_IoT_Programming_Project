package database.repository;

// Our imports
import database.DatabaseManager;
import database.model.IotDevice;

// Library imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Handles database operations for the iot_devices table. */
public class IotDeviceRepository {
    private static final Logger logger = LoggerFactory.getLogger(IotDeviceRepository.class);

    // SQL query statement to insert an IoT device into the iot_devices table
    private static final String INSERT_IOT_DEVICE_SQL =
            "INSERT INTO iot_devices (device_name, latitude, longitude, battery_level) " +
                    "VALUES (?, ?, ?, ?)";

    // SQL query statement to update an existing IoT device in the iot_devices table
    private static final String UPDATE_IOT_DEVICE_SQL =
            "UPDATE iot_devices SET device_name = ?, latitude = ?, longitude = ?, battery_level = ? WHERE iot_id = ?";

    // SQL query statement to select an IoT device by its ID
    private static final String SELECT_IOT_DEVICE_SQL =
            "SELECT * FROM iot_devices WHERE iot_id = ?";

    // SQL query statement to select an IoT device by its name
    private static final String SELECT_BY_DEVICE_NAME_SQL =
            "SELECT * FROM iot_devices WHERE device_name = ?";

    /** Saves an IoT device to the iot_devices table in the database. */
    public int saveIotDevice(IotDevice iotDevice) {
        try (Connection connection = DatabaseManager.getConnection()) {
            // Check if the IoT device already exists
            IotDevice existingDevice = getIotDeviceByName(iotDevice.getDeviceName());

            if (existingDevice != null) {
                // Check if any field has changed
                if (!existingDevice.equals(iotDevice)) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_IOT_DEVICE_SQL)) {
                        preparedStatement.setString(1, iotDevice.getDeviceName());
                        preparedStatement.setDouble(2, iotDevice.getLatitude());
                        preparedStatement.setDouble(3, iotDevice.getLongitude());
                        preparedStatement.setDouble(4, iotDevice.getBatteryLevel());
                        preparedStatement.setInt(5, getIotIdByName(iotDevice.getDeviceName())); // 5th parameter is the iot_id (auto-generated)

                        preparedStatement.executeUpdate();
                        logger.info("[IotDeviceRepository] IoT device updated successfully -> {}", iotDevice);

                        return getIotIdByName(iotDevice.getDeviceName());
                    }
                } else {
                    logger.info("[IotDeviceRepository] No changes detected for IoT device -> {}", iotDevice);
                    return getIotIdByName(iotDevice.getDeviceName());
                }
            } else {
                // Insert the new IoT device
                try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_IOT_DEVICE_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, iotDevice.getDeviceName());
                    preparedStatement.setDouble(2, iotDevice.getLatitude());
                    preparedStatement.setDouble(3, iotDevice.getLongitude());
                    preparedStatement.setDouble(4, iotDevice.getBatteryLevel());

                    preparedStatement.executeUpdate();
                    logger.info("[IotDeviceRepository] IoT device inserted successfully -> {}", iotDevice);

                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Inserting IoT device failed, no ID obtained.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("[IotDeviceRepository] Error saving or updating IoT device -> {}. SQL State: {}, Error Code: {}",
                    iotDevice, e.getSQLState(), e.getErrorCode(), e);
            throw new RuntimeException("Error saving or updating IoT device", e);
        }
    }

    /** Retrieves an IoT ID from the iot_devices table by its Name. */
    private int getIotIdByName(String deviceName) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_DEVICE_NAME_SQL)) {

            preparedStatement.setString(1, deviceName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("iot_id");
            } else {
                throw new SQLException("IoT device with name " + deviceName + " not found.");
            }
        }
    }

    /** Retrieves a whole IoT device from the iot_devices table by its Name. */
    public IotDevice getIotDeviceByName(String deviceName) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_DEVICE_NAME_SQL)) {

            preparedStatement.setString(1, deviceName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new IotDevice(
                        resultSet.getString("device_name"),
                        resultSet.getDouble("latitude"),
                        resultSet.getDouble("longitude"),
                        resultSet.getDouble("battery_level")
                );
            }
        } catch (SQLException e) {
            logger.error("[IotDeviceRepository] Error retrieving IoT device with name '{}'. SQL State: {}, Error Code: {}",
                    deviceName, e.getSQLState(), e.getErrorCode(), e);
        }
        return null;
    }
}

