package com.di_team.iot.net;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executors;

import com.di_team.iot.IoTApp;
import com.di_team.iot.device.DeviceData;
import com.di_team.iot.device.DeviceDataRetriever;
import com.di_team.iot.sensor.SensorConfig;
import com.di_team.iot.sensor.SensorStateManager;
import com.di_team.iot.sensor.SensorType;

public class TransmissionService extends Service {
    private static final String TAG = "TransmissionService";
    private String topic, locationMode;
    private AtomicBoolean isConnecting;
    private final int publishInterval = 1000; // 1 second (in ms)
    private MqttAndroidClient mqttClient;
    private DeviceDataRetriever dataRetriever;
    private SensorStateManager sensorStateManager;
    private java.util.concurrent.ScheduledExecutorService scheduler;

    @Override
    public void onCreate() {
        super.onCreate();

        // On creation, the location mode is set to manual (it will be set to user requested config as soon as onStartCommand is run)
        dataRetriever = new DeviceDataRetriever(this, true);
        sensorStateManager = SensorStateManager.getInstance();
        isConnecting = new AtomicBoolean(false);
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent == null) {
            Log.e(TAG, "Intent is null, stopping service");
            broadcastFatalError("Something went wrong.",
                    null,
                    "Try restarting transmission.");
            stopSelf();
            return START_NOT_STICKY;
        }

        //remove all pending tasks from previous instance of service
        if(scheduler != null)
            scheduler.shutdownNow();

        // Extract intention extras: where to connect, gps configuration, what topic to publish to
        String brokerUri = intent.getStringExtra("BROKER_URI");
        String locationMode = intent.getStringExtra("LOCATION_MODE");
        String topic = intent.getStringExtra("DEVICE_ID");


        if (brokerUri == null || locationMode == null || topic == null) {
            Log.e(TAG, "Extras missing in intent");
            broadcastFatalError("Something went wrong.",
                    null,
                    "Ensure all blanks for MQTT broker are filled out, and restart transmission.");
            stopSelf();
            return START_NOT_STICKY;
        }
        this.topic = topic;
        this.locationMode = locationMode;

        if(locationMode.equals("auto") && !permissionsGranted())
            broadcastFatalError("Foreground location permissions missing.",
                    null,
                    "Reconsider permissions and restart transmission.");

        dataRetriever.setManualLocationConfig(locationMode.equals("manual"));


        if(mqttClient != null && clientConnectionIsStable(brokerUri)) {

            // when broker uri hasn't changed, simply re-register and begin publishing
            requestToRegister();
            Log.d(TAG, "Existing connection found. Re-registering..");
        } else if(mqttClient != null && !clientConnectionIsStable(brokerUri)){
            // if broker uri has changed, disconnect and reconnect
            Log.d(TAG, "Creating new connection with broker uri " + brokerUri);
            disconnectFromBroker(true, brokerUri);
        } else {
            Log.d(TAG, "Initializing client from scratch");
            initClient(brokerUri);
        }

        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();

        if(mqttClient != null && mqttClient.isConnected()) {
            Log.d("TransmissionService", "Disconnecting from broker");
            disconnectFromBroker();
        } else if(mqttClient != null) {
            mqttClient.close();
            mqttClient = null;
        }

        // Stop all pending publishing tasks
        scheduler.shutdownNow();
        scheduler = null;

        Log.d(TAG, "TransmissionService Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This is a started service, not a bound service, so we return null.
        return null;
    }

    private void initClient(final String brokerUri) {
        if(isConnecting.get()) {
            Log.d(TAG, "Client already connecting, skipping init");
            return;
        }
        isConnecting.set(true);

        //reuse old client name if it exists
        String clientId = (mqttClient != null)? mqttClient.getClientId() :  "__AndroidClient_"+System.currentTimeMillis();
        mqttClient = new MqttAndroidClient(this.getApplicationContext(), brokerUri, clientId);
        Log.d(TAG, "New mqtt client created with id: "+ clientId);

        try {
            IMqttToken token = mqttClient.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connected to MQTT broker");
                    isConnecting.set(false);
                    requestToRegister();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to connect to MQTT broker", exception);
                    isConnecting.set(false);
                    broadcastFatalError("Failed to connect to MQTT broker. ",
                            exception,
                            null);
                    stopSelf();
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Error connecting to MQTT broker", e);
            isConnecting.set(false);
            broadcastFatalError("Error connecting to MQTT broker. ",
                    e,
                    null);
            stopSelf();
        }
    }

    /**Disconnects client from broker
     * @param  reconnect: whether to reconnect after disconnection
     * @param brokerUri: where to connect if reconnect is true*/
    private void disconnectFromBroker(boolean reconnect, String brokerUri) {
        mqttClient.setCallback(null);
        try {
            mqttClient.disconnect(this.getApplicationContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Successfully disconnected MQTT client");
                    mqttClient.close();
                    if (reconnect) {
                        Log.d(TAG, "initClient..");
                        initClient(brokerUri);
                    }
                    else{
                        mqttClient = null; // Nullify to avoid reusing the closed client
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Disconnection failed", exception);
                    mqttClient.close(); // Ensure close is only called once
                    if (reconnect) {
                        initClient(brokerUri);
                    }
                    else{
                        mqttClient = null; // Nullify to avoid reusing the closed client
                    }
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Error disconnecting from MQTT broker", e);
        }
    }

    /**Disconnects client from broker without reconnecting*/
    private void disconnectFromBroker() {
        disconnectFromBroker(false, null);
    }

    /**Determines whether the MQTT client is already connected to the given broker URI*/
    private boolean clientConnectionIsStable(String brokerUri){
        return mqttClient.isConnected()
                && mqttClient.getServerURI().equals(brokerUri);
    }

    private void startPeriodicPublishing() {
        Log.d(TAG, "Starting Periodic Publishing..");

        //if scheduler already exists, it has been shutdown within onStart(), create a new one
        scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable publishTask = () -> {
            if(locationMode.equals("auto") && !canUseLocation()){
                Log.w(TAG, "Background location permission missing. Skipping publish.");
                return;
            }
            if (mqttClient != null && mqttClient.isConnected()) {
                // Get data and publish
                MqttMessage message = new MqttMessage(createMessage().getBytes());
                try {
                    mqttClient.publish(topic, message);
                    Log.d(TAG, "Data published: " + message);
                } catch (MqttException e) {
                    Log.e(TAG, "Failed to publish data", e);
                }
            }
        };

        // Schedule publishing tasks with fixed delay execution
        scheduler.scheduleWithFixedDelay(publishTask, 0, publishInterval, TimeUnit.MILLISECONDS);
    }

    /**Creates a label-data string representation of current sensor states and device data. <br>
     * -<b>Sensor states</b> are of format "&lt;sensorType&gt; &lt;value/deactivated&gt;", according to activity status. <br>
     * -<b>Device data</b> is of format "Battery &lt;batteryPct&gt; Longitude &lt;longitude&gt; Latitude &lt;latitude&gt;"*/
    private String createMessage() {
        List<SensorConfig> sensorConfigs = sensorStateManager.getSensorConfigs();
        List<SensorType> uninstalledSensors = sensorStateManager.getUninstalledSensors();

        StringBuilder builder = new StringBuilder();
        // For every installed sensor, append their state
        for(SensorConfig sensor : sensorConfigs)
        {
            SensorType type = sensor.getType();
            Float progress = sensor.getProgress();

            //Append tokens of type "<sensorType> <value/deactivated> "
            if(!sensor.isActive()) {
                builder.append(type.toString()).append(" deactivated ");
            }
            else{
                builder.append(type.toString()).append(" ").append(progress).append(" ");
            }
        }
        // For uniformity and simplicity, every uninstalled sensor is considered deactivated
        for(SensorType type : uninstalledSensors)
            builder.append(type.toString()).append(" deactivated ");

        //Append device specific data and timestamp
        DeviceData deviceData = dataRetriever.getDeviceData();
        builder.append("Battery ").append(deviceData.getBatteryPct()).append(" ")
                .append(deviceData.getLocationToString());

        Timestamp now = new Timestamp(System.currentTimeMillis());
        builder.append(" Timestamp ").append(now);

        return builder.toString();
    }

    /**Requests to use this.DeviceID as a unique publishing topic, and handles the server response*/
    private void requestToRegister() {
        //Attach callback for request response from server
        mqttClient.setCallback(new TransmissionService.RegisterCallback(this));
        try{
            // Request logic: "<client id> requests to publish to topic <deviceID>. Is it taken?"
            String request = mqttClient.getClientId() + " " + topic;
            mqttClient.publish("register", request.getBytes(), 2, false);
            Log.d(TAG, "Request sent to server: " + request);
            //wait for response in client specific subtopic
            // Response is: "yes" or "no"
            mqttClient.subscribe("register/"+mqttClient.getClientId(), 2);
        } catch (MqttException e) {
            Log.e(TAG, "Error on register request process", e);
            broadcastFatalError("Error on register request process. ",
                    e,
                    ". Try restarting transmission.");
            stopSelf();
        }
    }

    private boolean permissionsGranted() {
        IoTApp app = (IoTApp) this.getApplication();
        //Service can't run if foreground permissions weren't given
        return ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean canUseLocation() {
        IoTApp app = (IoTApp) this.getApplication();
        if(!app.getRunsInBackground()){
            return true;
        } else {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }



    /**Broadcasts fatal error to all listeners*/
    private void broadcastFatalError(String message, Throwable exception, String advice) {
        Intent intent = new Intent("com.di_team.iot.ERROR_BROADCAST");
        String errorReport = message;
        if(exception != null)
            errorReport += "\n"+ exception.getCause();
        if(advice != null)
            errorReport += "\n"+ advice;
        intent.putExtra("error_message", errorReport);
        sendBroadcast(intent);
    }


    /**implements MqttCallback for the purpose of configuring the IoT's unique publishing topic.*/
    private static class RegisterCallback implements MqttCallback {
        final private TransmissionService service;

        RegisterCallback(TransmissionService service){
            this.service = service;
        }

        public void connectionLost(Throwable cause) {
            // If connection is lost, stop the service ???
            Log.e(TAG, "Connection lost", cause);
            service.broadcastFatalError("Connection lost. ",
                    cause,
                    "Check internet connection and restart transmission.");
            service.stopSelf();
        }


        public void messageArrived(String topic, MqttMessage message) {
            // If received response concerning topic name given by server, publishing process may begin
            if(topic.equals("register/"+service.mqttClient.getClientId())){
                String response = new String(message.getPayload());
                Log.d(TAG, "Response received: " + response);
                if(response.equals("yes")){
                    //requested topic can now be safely used for manual location, hand over to data retriever
                    service.dataRetriever.setDeviceID(service.topic);
                    service.startPeriodicPublishing();
                }
                else {
                    Log.e(TAG, "Topic name already taken, killing service");
                    service.broadcastFatalError("Chosen device ID is already taken.",
                            null,
                            "Change it and restart transmission.");
                    service.stopSelf();
                }
            }
        }

        // no response needed upon message delivery (for now)
        public void deliveryComplete(IMqttDeliveryToken token) { }
    }
}
