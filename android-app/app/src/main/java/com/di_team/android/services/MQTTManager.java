package com.di_team.android.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import com.di_team.android.utils.HazardHandler;
import com.di_team.android.utils.NetworkUtils;
import com.di_team.android.utils.NotificationHelper;

/**Manages an MQTTClient, manually handles its reconnects*/
public class MQTTManager {
    private static final String TAG = "MQTTManager";
    private final String locationTopic = "Locations";
    private volatile boolean isReconnecting = false;
    private MqttClient mqttClient;
    private String clientId = "AndroidClient"; // Default value, override as needed
    private boolean canPublish = false;
    private final Context context;

    private final HazardHandler hazardHandler;

    public MQTTManager(Context context) {
        this.context = context;
        this.hazardHandler = new HazardHandler(context);
    }

    public void subscribeToAlerts() {
        String alertTopic = "Alerts/" + clientId;
        Log.i(TAG, "📡 Subscribing to topic: " + alertTopic); // ✅ Log subscription topic

        try {
            mqttClient.subscribe(alertTopic, 1);
            Log.i(TAG, "🔔 Successfully subscribed to: " + alertTopic);
        } catch (MqttException e) {
            Log.e(TAG, "❌ MQTT Exception during subscription", e);
        }
    }

    public void initializeMQTT(String brokerUrl, String clientId) {
        if (mqttClient != null && mqttClient.isConnected() &&
                brokerUrl.equals(mqttClient.getServerURI()) ) {
            Log.d(TAG, "✅ Client already connected, skipping initialization.");
            return;
        }
        this.clientId = clientId;

        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(false); // ✅ We handle reconnection manually


            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "❌ MQTT Connection lost", cause);
                    NotificationHelper.showAlertNotification(context, "MQTT Disconnected", "Attempting to reconnect...");
                    reconnectToMQTT(); // Reconnect logic
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = message.toString();
                    Log.i(TAG, "📩 Received MQTT message on topic: " + topic);
                    Log.i(TAG, "📜 Message content: " + payload);

                    if (topic.contains("Alerts") && !message.toString().isEmpty()) {
                        Log.d(TAG, "🚨 Alert message received! Processing...");

                        try {
                            // ✅ Extract hazard details
                            String severity = extractSeverity(payload);
                            String distance = extractDistance(payload);

                            if (severity != null && distance != null) {
                                Log.i(TAG, "✅ Parsed hazard: " + severity + " at " + distance + "m");

                                // ✅ Run on the Main (UI) Thread to prevent crashes
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    hazardHandler.showHazardAlert(severity, distance);
                                });

                            } else {
                                throw new IllegalArgumentException("Invalid hazard message format.");
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing hazard message: " + payload, e);
                        }
                    } else {
                        Log.w(TAG, "⚠️ Received message on unexpected topic: " + topic);
                    }
                }


                /**
                 * ✅ Extracts the severity (High/Moderate) from the MQTT message payload.
                 */
                private String extractSeverity(String payload) {
                    if (payload.contains("High")) return "High";
                    if (payload.contains("Moderate")) return "Moderate";
                    return null; // ❌ Invalid format if no severity found
                }

                /**
                 * ✅ Extracts the numerical distance from the MQTT message payload.
                 */
                private String extractDistance(String payload) {
                    try {
                        String[] parts = payload.split(" ");
                        for (int i = 0; i < parts.length; i++) {
                            if (parts[i].equalsIgnoreCase("Distance") && i + 1 < parts.length) {
                                return parts[i + 1]; // ✅ Extracts the number after "Distance"
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error extracting distance from payload: " + payload, e);
                    }
                    return null; // ❌ Invalid format if distance not found
                }


                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i(TAG, "📤 Message delivered.");
                }
            });

            mqttClient.connect(options);
            Log.i(TAG, "✅ Connected to MQTT Broker!");

            // ✅ Ensure MQTT is connected before starting location updates & subscriptions
            if (mqttClient != null && mqttClient.isConnected()) {
                subscribeToAlerts(); // ✅ Ensure MQTT connection before subscribing
                canPublish = true;
            } else {
                Log.e(TAG, "❌ MQTT client is not connected. Retrying...");
            }

        } catch (MqttException e) {
            Log.e(TAG, "❌ MQTT Exception during connection", e);
            reconnectToMQTT();
        }
    }

    public void reconnectToMQTT() {
        if (isReconnecting) {
            Log.d(TAG, "Already reconnecting..");
            return;
        }

        isReconnecting = true;
        new Thread(() -> {
            int retryInterval = 2000;
            int maxRetries = 5;
            int attempts = 0;

            while (!mqttClient.isConnected() && attempts < maxRetries) {
                if (!NetworkUtils.isInternetAvailable(context)) {
                    Log.e(TAG, "❌ No internet available. Will retry when restored.");
                    isReconnecting = false;
                    return;
                }

                try {
                    Log.d(TAG, "🔄 Attempting MQTT reconnect...");
                    mqttClient.reconnect();

                    while (!mqttClient.isConnected()) {
                        Thread.sleep(500);
                    }

                    Log.i(TAG, "✅ Reconnected to MQTT Broker!");
                    NotificationHelper.showAlertNotification(context, "MQTT Reconnected", "Successfully connected!");
                    isReconnecting = false;
                    return;
                } catch (MqttException | InterruptedException e) {
                    attempts++;
                    Log.e(TAG, "❌ Reconnection failed (Attempt " + attempts + "/" + maxRetries + "). Retrying in " + (retryInterval / 1000) + "s...");
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ex) {
                        isReconnecting = false;
                        return;
                    }
                    retryInterval = Math.min(retryInterval * 2, 30000);
                }
            }

            if (attempts >= maxRetries) {
                Log.e(TAG, "❌ Max retry limit reached. Could not reconnect to MQTT.");
            }
            isReconnecting = false;
        }).start();
    }

    public void publishLocation(double latitude, double longitude) {
        if (context == null) {
            Log.e(TAG, "❌ Context is null. Cannot check internet availability.");
            return;
        }

        if (!NetworkUtils.isInternetAvailable(context)) {
            Log.e(TAG, "❌ No internet connection. Skipping publish.");
            return;
        }

        if (mqttClient == null || !mqttClient.isConnected()) {
            Log.e(TAG, "❌ MQTT Client is not connected or is disconnecting, skipping publish.");
            return;
        }

        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            Log.e(TAG, "❌ Invalid latitude or longitude values.");
            return;
        }

        if(!canPublish) {
            Log.e(TAG, "Client has not yet connected. Skipping publish..");
        }

        if (clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "❌ Client ID is null or empty. Skipping publish.");
            return;
        }

        String locationData = "Longitude " + longitude + " Latitude " + latitude + " ID " + clientId;

        try {
            MqttMessage message = new MqttMessage(locationData.getBytes());
            message.setQos(1); // ✅ Ensure at-least-once delivery

            mqttClient.publish(locationTopic, message);
            Log.i(TAG, "📍 Published location: " + locationData);
        } catch (MqttException e) {
            Log.e(TAG, "❌ Error publishing location", e);
        }
    }

    public void disconnect() {
        Log.i(TAG, "🛑 Stopping MQTT Transmission...");
        canPublish = false;

        // ✅ Properly Disconnect MQTT Client
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                Log.i(TAG, "✅ MQTT Client Disconnected");
            }
        } catch (MqttException e) {
            Log.e(TAG, "❌ Error closing MQTT Client", e);
        }
    }
}