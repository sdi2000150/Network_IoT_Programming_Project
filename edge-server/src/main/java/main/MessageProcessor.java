package main;

import data_util.Location;
import data_util.Record;
import data_util.ThresholdManager;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;

import webserver.SseBroadcaster;


public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private final MqttListener listener;
    private final ConcurrentHashMap<String, Record> iotMap;     //links iotID's (topics) to their current status
    private final ConcurrentHashMap<String, Location> userMap;  //links userID's to their device locations


    MessageProcessor(MqttListener mqttListener) {
        this.listener = mqttListener;
        iotMap = new ConcurrentHashMap<>();
        userMap = new ConcurrentHashMap<>();
    }

    void processIoTMessage(String iotID, MqttMessage message) {
        try {
            Record record = Record.parseMessage(message.toString());
            // Immediately broadcast the raw IoT message (before alert processing).
            String jsonData = "{\"type\":\"iot\", " +
                    "\"id\":\"" + iotID + "\", " +
                    "\"lat\":" + record.getLatitude() + ", " +
                    "\"lng\":" + record.getLongitude() + ", " +
                    "\"battery\":" + record.getBattery() + ", " +
                    "\"smoke\":" + record.getSmokeValue() + ", " +
                    "\"gas\":" + record.getGasValue() + ", " +
                    "\"temperature\":" + record.getTemperatureValue() + ", " +
                    "\"uv\":" + record.getUvValue() +
                    "}";

            // Broadcast the JSON data via SSE.
            SseBroadcaster.broadcast(jsonData);

            // decide on new record's severity, and issue alerts if it denotes danger
            AlertSpotter alertSpotter = new AlertSpotter(iotID, record, iotMap, userMap, ThresholdManager.getDefaultThresholds(), 5, this.listener);
            new Thread(alertSpotter).start();

        } catch (Exception e) {
            logger.error("[MessageProcessor] Error starting alert spotter in the background: ", e);
        }
    }

    void processUserMessage(MqttMessage message) {
        String sMessage = new String(message.getPayload());
        String[] tokens = sMessage.trim().split(" ");
        Float latitude = null, longitude = null; String userID = null;
        for (int i = 0; i < tokens.length; i+=2) {
            switch (tokens[i]) {
                case "Longitude":
                    longitude = Float.parseFloat(tokens[i + 1]);
                    break;
                case "Latitude":
                    latitude = Float.parseFloat(tokens[i + 1]);
                    break;
                case "ID":
                    userID = tokens[i + 1];
                    break;
                default:
                    logger.warn("[MessageProcessor] Unknown token: {}", tokens[i]);
            }

        }
        if (longitude == null || latitude == null || userID == null) {
            logger.error("[MessageProcessor] message left data uninitialized: Longitude: {}, Latitude: {}, ID: {}", longitude, latitude, userID);
            return;
        }
        Location userLocation = new Location(longitude, latitude);
        logger.info("[MessageProcessor] user with id {} has location: {}", userID, userLocation);

        // If user sent location for the first time, check for recent alerts they might have missed.
        if(!userMap.containsKey(userID)) {
            AlertSpotter alertSpotter = new AlertSpotter(userID, userLocation, iotMap, 5, this.listener);
            new Thread(alertSpotter).start();
        }
        //Update user location
        userMap.put(userID, userLocation);
        //broadcast(message)
        // Broadcast a JSON update for the user's location
        String jsonData = "{\"type\":\"user\", \"id\":\"" + userID +
                "\", \"lat\":" + userLocation.getLatitude() +
                ", \"lng\":" + userLocation.getLongitude() + "}";
        SseBroadcaster.broadcast(jsonData);
    }

    void broadcast(Record record) {
        // Optionally, could add a broadcast method here
        // TODO maybe : 1. Turn message to JSON
        //              2. call SseBroadcaster.broadcast(jsonData); in order to broadcast changes to the front end
    }
}
