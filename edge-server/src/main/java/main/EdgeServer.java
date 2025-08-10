package main;

// Our imports
import config.ConfigManager;
import database.DatabaseManager;
import webserver.SimpleHttpServer;

// Library imports
import org.eclipse.paho.client.mqttv3.*;    // Eclipse Paho MQTT v3 client library
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** EdgeServer is the main starting point of the backend application,
 which handles MQTT communication with IoT and User Android devices. */
public class EdgeServer {   // Implementing MqttCallback interface for MQTT Protocol message handling
    private static final Logger logger = LoggerFactory.getLogger(EdgeServer.class);

    private final DatabaseManager databaseManager;

    /** Main method to start the EdgeServer. */
    public static void main(String[] args) {
        try {
            String URL = ConfigManager.getProperty("broker.url");
            String clientID = ConfigManager.getProperty("broker.clientId");

            // Start our HTTP server (serves static files and SSE live updates endpoint)
            SimpleHttpServer httpServer = new SimpleHttpServer(8080);
            new Thread(() -> {
                httpServer.start();
            }).start();

            EdgeServer edgeServer = new EdgeServer(URL, clientID);
            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("[EdgeServer] creation error", e);
        }
    }

    /** Constructor for the EdgeServer. */
    public EdgeServer(String brokerURL, String clientID) throws MqttException {
        this.databaseManager = DatabaseManager.getInstance();
        MqttListener mqttListener = new MqttListener(brokerURL, clientID);
        mqttListener.subscribeToTopics();
    }

}


