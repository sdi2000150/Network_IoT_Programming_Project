package main;

// Our imports
import data_util.Location;
import data_util.Record;
import data_util.SensorType;
import data_util.Severity;
import misc.*;
import database.model.Event;
import database.model.IotDevice;
import database.DatabaseManager;

// Library imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/** Thread that functions in 2 modes: 1. decides risk level of new IoT measurement record and alerts users if new record denotes danger <br>
 * 2. Simply searches if existing alerts denote danger for new user*/
public class AlertSpotter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AlertSpotter.class);
    private final int mode;
    private final MqttListener mqttListener;
    private final Map<SensorType, Double> thresholds;
    private final long timeThresholdMillis;
    private final ConcurrentHashMap<String, Record> iotMap;
    private final ConcurrentHashMap<String, Location> userMap;
    private final DatabaseManager databaseManager;
    private final String deviceId;
    private final Record newRecord;
    private final String userID;
    private final Location userLocation;

    /** Constructor for AlertSpotter, when AlertSpotter aims to add a new record and possibly notify all users (mode 1).
     * @param deviceId the id of the iot that issued the most recent newRecord
     * @param record the new record issued (the risk level is undecided).
     * @param iotMap the map of current IoT records with decided risk level
     * @param userMap the map of current user locations
     * @param mqttListener the listener used for alert issues
     * @param thresholds a map linking each sensor type to alert thresholds
     * @param timeThresholdMinutes minutes threshold above which records are not considered for alert issues*/
    public AlertSpotter(String deviceId, Record record, ConcurrentHashMap<String, Record> iotMap, ConcurrentHashMap<String, Location> userMap,
                        Map<SensorType, Double> thresholds, long timeThresholdMinutes, MqttListener mqttListener) {
        this.userID = null;             // Fields needed for mode 2 are nullified.
        this.userLocation = null;
        this.deviceId = deviceId;
        this.newRecord = record;
        this.iotMap = iotMap;
        this.userMap = userMap;
        this.thresholds = thresholds;
        this.timeThresholdMillis = timeThresholdMinutes * 60 * 1000;  // Convert minutes to ms
        this.mqttListener = mqttListener;
        this.databaseManager = DatabaseManager.getInstance();
        this.mode = 1;
    }

    /**Constructor used for when AlertSpotter aims to search for alerts for only 1 new user. (mode 2)
     * @param userID the user's unique identifier
     * @param userLocation their most recent device location
     * @param iotMap the map of current IoT records with decided risk level
     * @param timeThresholdMinutes minutes threshold above which records are not considered for alert issues*/
    public AlertSpotter(String userID, Location userLocation, ConcurrentHashMap<String, Record> iotMap, long timeThresholdMinutes, MqttListener mqttListener) {
        this.deviceId = null;       //Fields needed for mode 1 are nullified.
        this.newRecord = null;
        this.userMap = null;
        this.databaseManager = null;
        this.thresholds = null;
        this.userID = userID;                                        //Information for user to warn
        this.userLocation = userLocation;
        this.iotMap = iotMap;
        this.timeThresholdMillis = timeThresholdMinutes * 60 * 1000; // Convert minutes to ms
        this.mqttListener = mqttListener;
        this.mode = 2;
    }

    /** Determines risk level of newRecord issued and adds it to iotMap. Issues alerts to users in case of existing risk.*/
    @Override
    public void run() {
        if(mode == 1) {
            onNewRecord();
        } else {
            onNewUser();
        }
    }

    /**Decides Severity for newRecord, adds it to iotMap, and handles alert issuing and database storage if record denotes danger*/
    private void onNewRecord() {
        logger.info("[AlertSpotter] Checking severity for record issued by {}", deviceId);
        Float smokeValue = newRecord.getSmokeValue();
        Float gasValue = newRecord.getGasValue();
        Float temperatureValue = newRecord.getTemperatureValue();
        Float uvValue = newRecord.getUvValue();

        // Skip newRecord if all sensors are deactivated
        if (smokeValue == null && gasValue == null && temperatureValue == null && uvValue == null) {
            logger.info("[AlertSpotter] Skipping newRecord with all sensors deactivated: {}", newRecord);
            return;
        }

        boolean smokeExceeded = smokeValue != null && smokeValue > thresholds.getOrDefault(SensorType.SMOKE, Double.MAX_VALUE);
        boolean gasExceeded = gasValue != null && gasValue > thresholds.getOrDefault(SensorType.GAS, Double.MAX_VALUE);
        boolean temperatureExceeded = temperatureValue != null && temperatureValue > thresholds.getOrDefault(SensorType.TEMPERATURE, Double.MAX_VALUE);
        boolean uvExceeded = uvValue != null && uvValue > thresholds.getOrDefault(SensorType.UV, Double.MAX_VALUE);

        Severity severity = determineSeverity(smokeExceeded, gasExceeded, temperatureExceeded, uvExceeded);
        newRecord.setSeverity(severity);
        Record prevRecord = iotMap.put(deviceId, newRecord);

        if (severity != Severity.None) {
            storeAlert();
            // issue alerts to users if: IoT's current state denotes greater danger than the previous or if this is the first record for given IoT that denotes danger
            if(prevRecord == null || severity.isMoreSevereThan(prevRecord.getSeverity()))
                issueAlerts();
        }
        // if IoT transitioned from an alert status to neutral, overwrite all retained alerts
        else if(prevRecord != null && prevRecord.getSeverity().isMoreSevereThan(newRecord.getSeverity())) {
            removeAlerts();
        }
    }

    /**Searches and issues alert specifically for new user. Used for newcomers who might've missed recent alerts
     * User will be alerted only if there is at least 1 IoT record that is recent and denotes danger.*/
    private void onNewUser() {
        logger.info("[AlertSpotter] Checking alerts for user {} in location {}", userID, userLocation.toString());
        Vector<Location> alertLocations = new Vector<>();
        Severity maxSeverity = getAlertLocations(alertLocations);

        if(alertLocations.isEmpty()) {
            logger.info("[AlertSpotter] No alerts found for user {}", userID);
        } else {
            Location pointOfDanger = GeoCalculator.computeCenter(alertLocations);
            logger.debug("[AlertSpotter] Point of danger calculated: {}", pointOfDanger.toString());

            float distance = GeoCalculator.computeDistanceKm(pointOfDanger, userLocation);
            logger.info("[AlertSpotter] Distance calculated: {}", distance);

            String message = "Distance " + distance + " Severity " + maxSeverity;
            logger.info("[AlertSpotter] Sending the following alert to user {} : {}", userID, message );
            mqttListener.publishMessage("Alerts/" +  userID, message, true); //Alert issues are important so they are retained by broker

        }
    }


    /** Determines the severity level based on limits exceeded. */
    private Severity determineSeverity(boolean smoke, boolean gas, boolean temperature, boolean uv) {
        if (gas || (gas && smoke) || (gas && smoke && uv && temperature)) {
            return Severity.High;
        } else if (temperature && uv) {
            return Severity.Moderate;
        }
        return Severity.None;
    }

    /** Updates the database tables, based on newRecord */
    private void storeAlert() {

        logger.warn("[AlertSpotter] Alert generated -> Severity: {}", newRecord.getSeverity());
        int iotId = -1;

        IotDevice iotDevice = new IotDevice(
                deviceId,
                newRecord.getLatitude(),
                newRecord.getLongitude(),
                newRecord.getBattery()
        );
        logger.info("[AlertSpotter] IotDevice created: {}", iotDevice);
        try {
            if ((iotId = databaseManager.saveIotDevice(iotDevice)) == -1)   {
                logger.error("[AlertSpotter] Error retrieving the key (iotId) of the iot_device while saving in database.");
            }
            logger.info("[AlertSpotter] IoT device saved successfully.");
        } catch (Exception e) {
            logger.error("[AlertSpotter] Error saving iot_device to database: ", e);
        }

        Event event = new Event(
                iotId,                                 // IoT device ID
                newRecord.getTimestamp(),
                newRecord.getLatitude(),
                newRecord.getLongitude(),
                newRecord.getSmokeValue() != null       ? newRecord.getSmokeValue()       : -1,        // -1 denotes sensor inactivity (FIXME: replace with null?)
                newRecord.getGasValue()   != null       ? newRecord.getGasValue()         : -1,
                newRecord.getTemperatureValue() != null ? newRecord.getTemperatureValue() : -1,
                newRecord.getUvValue()    != null       ? newRecord.getUvValue()          : -1,
                newRecord.getSeverity().toString()
        );
        logger.info("[AlertSpotter] Event created: {}", event);
        try {
            databaseManager.saveEvent(event);
            logger.info("[AlertSpotter] Event saved successfully.");
        } catch (Exception e) {
            logger.error("[AlertSpotter] Error saving event to database: ", e);
        }

    }

    /** Publishes alerts to all users for the IoT Record newRecord, and any other ones in proximity.*/
    private void issueAlerts() {

        // Find all recent IoT records that denote danger - it's at least 1, when this function is called
        Vector<Location> alertLocations = new Vector<>();
        getAlertLocations(alertLocations);      //Ignore the function's return statement

        // All alerts issued have the risk level of the most recent,
        // ignoring if nearby alerts are less severe
        Severity level = newRecord.getSeverity();
        Location pointOfDanger = GeoCalculator.computeCenter(alertLocations);
        logger.debug("[AlertSpotter] point of danger calculated: {}", pointOfDanger.toString());
        for ( Map.Entry<String, Location> entry : userMap.entrySet() ) {
            String userId = entry.getKey();
            Location userLocation = entry.getValue();
            float distance = GeoCalculator.computeDistanceKm(pointOfDanger, userLocation);

            //alert issuing logic: keep steady distance threshold, under which user should be notified
            String message = "Distance " + distance + " Severity " + level.toString();
            mqttListener.publishMessage("Alerts/" + userId, message, true); //alert issues are important and should be retained by broker

        }
        logger.info("[AlertSpotter] issued alerts to {} users", userMap.size());
    }

    /**Overwrites all alerts currently retained by broker.*/
    private void removeAlerts() {
        for (String userId:userMap.keySet()) {
            mqttListener.publishMessage("Alerts/"+userId, "", true);
        }
    }

    /** Fills vector passed as argument with current alerts and returns their max severity*/
    private Severity getAlertLocations(Vector<Location> alertLocations) {
        Severity maxSeverity = Severity.None;
        logger.debug("[AlertSpotter] searching for alerts among {} records", iotMap.size());
        for ( Map.Entry<String, Record> entry : iotMap.entrySet() ) {
            Record record = entry.getValue();
            Severity severity = record.getSeverity();
            long recordAgeMillis = System.currentTimeMillis() - record.getTimestamp().getTime();
            logger.debug("[AlertSpotter] Entry has severity {} and age {}", severity, recordAgeMillis);
            if (severity != Severity.None && recordAgeMillis < timeThresholdMillis) {
                logger.debug("Entry Added");
                alertLocations.add(record.getLocation());
                if (record.getSeverity().isMoreSevereThan(maxSeverity)) {
                    maxSeverity = record.getSeverity();
                }
            }
        }
        logger.debug("[AlertSpotter] {} alert locations found, max severity {}", alertLocations.size(), maxSeverity);
        return maxSeverity;
    }

}

