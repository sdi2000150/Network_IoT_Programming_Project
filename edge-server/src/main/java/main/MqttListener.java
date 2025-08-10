package main;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class MqttListener implements MqttCallback {
    private static final Logger logger = LoggerFactory.getLogger(MqttListener.class);
    private ConcurrentHashMap<String, String> clientTopics; //Map linking registered clients (IoT's) to their associated topics
    private final MqttClient client;
    private final MessageProcessor messageProcessor;

    public MqttListener(String brokerURL, String clientId) throws MqttException {
        client = new MqttClient(brokerURL, clientId, new MemoryPersistence());
        client.setCallback(this);
        client.connect();
        clientTopics = new ConcurrentHashMap<>();
        messageProcessor = new MessageProcessor(this);
    }

    /** Subscribes to registrations topic */
    public void subscribeToTopics() throws MqttException {
        client.subscribe("register");
        client.subscribe("Locations");
        logger.info("[EdgeServer] waiting for register requests..");
    }

    /** Handles incoming MQTT messages (overridden method of the MqttCallback interface). */
    @Override
    public void messageArrived(String topic, MqttMessage message) {

        // Server received a register request from a new IoT or user
        if (topic.equals("register")) {
            String request = new String(message.getPayload());
            String requestingClient = request.split(" ")[0];
            String requestedTopic = request.split(" ")[1];

            logger.info("[MqttListener] Received register request -> client '{}' requests to use topic: '{}'", requestingClient, requestedTopic);
            String response;

            // If client hasn't connected before and asks for an already reserved topic
            if (!clientTopics.containsKey(requestingClient) && clientTopics.contains(requestedTopic)) {

                response = "no";    //use of requested topic is disapproved
            } else {

                response = "yes";   //use of requested topic is approved. If the topic is new, server may subscribe to it
                if (!clientTopics.contains(requestedTopic)) {
                    String previousTopic = clientTopics.put(requestingClient, requestedTopic);

                    try {
                        client.subscribe(requestedTopic, 2);
                        if(previousTopic != null)
                            client.unsubscribe(previousTopic);  //if client used a different topic previously, unsubscribe from it
                    } catch (MqttException e) {
                        logger.error("[MqttListener] Subscribing to topic '{}' failed. Cause: '{}'", requestedTopic, e.getMessage());
                    }
                }
            }

            // publish response to client specific sub-topic
            try {
                client.publish(topic + "/" + requestingClient, response.getBytes(), 2, false);
                logger.info("[MqttListener] Providing client with response: '{}'", response);
            } catch (MqttException e) {
                logger.error("[MqttListener] Publishing to topic '{}' failed. Cause:'{}'", topic, e.getMessage());
            }
        } else if (topic.startsWith("IoT")) {
            messageProcessor.processIoTMessage(topic, message);
        } else if (topic.equals("Locations")){
            messageProcessor.processUserMessage(message);
        } else {
            logger.error("[MqttListener] Unknown topic '{}'", topic);
        }
    }

    public void publishMessage(String topic, String message, boolean retained) {
        try {
            client.publish(topic, message.getBytes(), 2, retained);
        } catch (MqttException e) {
            logger.warn("[MqttListener] Skipping publish of message {}", message, e.getCause());
        }
    }

    /** Clean resources on shutdown */
    public void performCleanup() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                logger.info("[MqttListener] MQTT client disconnected successfully.");
            }
            if (client != null) {
                client.close();
                logger.info("[MqttListener] MQTT client resources released.");
            }
        } catch (MqttException e) {
            logger.error("[MqttListener] Error during client cleanup", e);
        }

        clientTopics.clear();
        clientTopics = null;
    }

    /** Handles connection loss (overridden method of the MqttCallback interface). */
    @Override
    public void connectionLost(Throwable cause) {
        logger.error("Connection lost", cause);
    }

    /** Handles message delivery completion (overridden method of the MqttCallback interface). */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("[MqttListener] Message delivery complete: {}", token);
    }
}
