package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/** Manages configuration properties loaded from the application.properties file. */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();

    /** Static initializer to load configuration at class load time. */
    static {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties file not found in resources.");
            }
            properties.load(input);
            logger.info("[ConfigManager] Configuration file loaded successfully.");
        } catch (Exception e) {
            logger.error("Failed to load configuration file.", e);
        }
    }

    /**
     * Retrieves a configuration property as a string.
     *
     * @param key The key of the property.
     * @return The value of the property.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Retrieves a configuration property as a double.
     *
     * @param key The key of the property.
     * @return The value of the property as a double.
     */
    public static double getDoubleProperty(String key) {
        return Double.parseDouble(properties.getProperty(key));
    }
}
