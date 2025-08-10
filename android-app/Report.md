# Λειτουργικότητα της android Εφαρμογής User
*Η User-App είναι μια Android εφαρμογή υλοποιημένη σε Java μέσω Gradle και Android Studio*

## Η User Android εφαρμογή αποτελεί βασικό στοιχείο του συστήματος επικοινωνίας μέσω MQTT. Οι κύριες λειτουργίες της περιλαμβάνουν:
Συνεχή μετάδοση τοποθεσίας μέσω GPS ή προσομοιωμένων δεδομένων (CSV).\
Λήψη ειδοποιήσεων κινδύνου από τον Edge Server.\
Εναλλαγή μεταξύ αυτόματης και χειροκίνητης αποστολής δεδομένων.\
Ενημέρωση χρήστη μέσω ειδοποιήσεων.

# Αρχιτεκτονική της Εφαρμογής:

Η εφαρμογή είναι δομημένη σε επιμέρους πακέτα, καθένα από τα οποία εξυπηρετεί διαφορετικό σκοπό:

# Διεπαφή Χρήστη (UI - Activities)

- [HazardAlertActivity](#ανάλυση-hazardalertactivity): Προβάλλει ειδοποιήσεις κινδύνου στον χρήστη.
- [MainActivity](#ανάλυση-mainactivity): Κεντρική δραστηριότητα της εφαρμογής, παρέχει πρόσβαση στο κύριο μενού.
- [MenuActivity](#ανάλυση-menuactivity): Διεπαφή χρήστη για τη διαμόρφωση των ρυθμίσεων MQTT και αποστολής τοποθεσίας.

# Κεντρικός Πυρήνας της Εφαρμογής (Core)

- [UserApp](#ανάλυση-userapp): Η κύρια κλάση της εφαρμογής που αρχικοποιεί σημαντικά στοιχεία.
- [BaseActivity](#ανάλυση-baseactivity): Παρέχει κοινές λειτουργίες για όλες τις δραστηριότητες της εφαρμογής.

# Διαχείριση Κύκλου Ζωής (Lifecycle)

- [AppLifecycleObserver](#ανάλυση-applifecycleobserver): Παρακολουθεί τον κύκλο ζωής της εφαρμογής.

# Υπηρεσίες (Services)

- [HazardAlertService](#ανάλυση-hazardalertservice): Διαχειρίζεται ειδοποιήσεις κινδύνου και την εμφάνισή τους.
- [LocationUpdtateCallback (Interface)](#ανάλυση-locationupdatecallback): Ορίζει callback για την ενημέρωση τοποθεσίας.
- [MQTTManager](#ανάλυση-mqttmanager): Διαχειρίζεται την επικοινωνία μέσω MQTT.
- [StopServiceReceiver](#ανάλυση-stopservicereceiver): Υποστηρίζει την απενεργοποίηση των υπηρεσιών μετάδοσης.
- [TransmissionService](#ανάλυση-transmissionservice): Χειρίζεται τη συνεχή αποστολή τοποθεσίας μέσω MQTT.

# Βοηθητικές Κλάσεις (Utilities)

- [AlertManager](#ανάλυση-alertmanager): Διαχειρίζεται ειδοποιήσεις αποσύνδεσης δικτύου.
- [CsvReader](#ανάλυση-csvreader): Αναλύει αρχεία CSV για χειροκίνητη μετάδοση δεδομένων τοποθεσίας.
- [GPSManager](#ανάλυση-gpsmanager): Χρησιμοποιεί τον GPS αισθητήρα για λήψη τοποθεσίας.
- [HazardHandler](#ανάλυση-hazardhandler): Διαχειρίζεται την ανάλυση και επεξεργασία δεδομένων κινδύνου.
- [NetworkUtils](#ανάλυση-networkutils): Παρέχει μεθόδους ελέγχου συνδεσιμότητας στο διαδίκτυο.
- [NotificationHelper](#ανάλυση-notificationhelper): Δημιουργεί και διαχειρίζεται ειδοποιήσεις χρήστη.

# Ανάλυση HazardAlertActivity

Η κλάση HazardAlertActivity είναι υπεύθυνη για την εμφάνιση ειδοποιήσεων κινδύνου στον χρήστη.\
Όταν λαμβάνεται ένα μήνυμα κινδύνου, εμφανίζεται μια πλήρης οθόνη ειδοποίησης που ενημερώνει τον χρήστη για το επίπεδο επικινδυνότητας (Υψηλό ή Μέτριο) και του παρέχει την επιλογή να απορρίψει την ειδοποίηση.\
Η ειδοποίηση κλείνει είτε χειροκίνητα είτε αυτόματα μετά από 7 δευτερόλεπτα.

## Ανάλυση κύριων λειτουργιών

Λαμβάνει τα δεδομένα του κινδύνου μέσω Intent.\
Προσαρμόζει χρώματα και μέγεθος κειμένου:\
        - Υψηλός Κίνδυνος → Κόκκινο φόντο, λευκό κείμενο.\
        - Μέτριος Κίνδυνος → Κίτρινο φόντο, μαύρο κείμενο.

### Η ειδοποίηση εμφανίζεται ακόμα και αν η συσκευή είναι κλειδωμένη:

```
getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                     WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
```

### Χειροκίνητο & αυτόματο κλείσιμο

Ο χρήστης μπορεί να πατήσει το κουμπί "Dismiss".\
Ένας Broadcast Receiver κλείνει την ειδοποίηση αυτόματα μετά από 7 δευτερόλεπτα (private final BroadcastReceiver closeAlertReceiver).

# Ανάλυση MainActivity

Η κλάση MainActivity αποτελεί την κεντρική οθόνη της εφαρμογής.\
Εμφανίζει πληροφορίες για την τελευταία ειδοποίηση κινδύνου, επιτρέπει τη μετάβαση στο μενού
ρυθμίσεων, και διαχειρίζεται τις ειδοποιήσεις συστήματος.

## Βασικές λειτουργίες

### Aρχικοποίηση και διαχείριση UI

Δημιουργία καναλιών ειδοποιήσεων μέσω NotificationHelper.\
Ρύθμιση κουμπιού για μετάβαση στο MenuActivity.\
Προβολή των στοιχείων της τελευταίας ειδοποίησης (χρόνος, σοβαρότητα, απόσταση)

### Ανανέωση δεδομένων ειδοποίησης (updateWindow())

Διαβάζει τις τελευταίες αποθηκευμένες ειδοποιήσεις από SharedPreferences (SharedPreferences alertStatus).\
Αν δεν υπάρχει ειδοποίηση, εμφανίζει μήνυμα "Καμία πρόσφατη ειδοποίηση".\
Αν υπάρχει ειδοποίηση, εμφανίζει:\
        - Επίπεδο κινδύνου (π.χ., Υψηλό, Μέτριο).\
        - Απόσταση από το σημείο κινδύνου.\
        - Χρόνος που πέρασε από τη λήψη της ειδοποίησης σε αναγνώσιμη μορφή.

### Μετατροπή του χρόνου λήψης σε ανθρώπινη μορφή

Υπολογίζει πόσα λεπτά ή ώρες έχουν περάσει από τη λήψη της ειδοποίησης. (ConvertTime(long timeMillis))

### Μετάβαση στο MenuActivity

Ο χρήστης μπορεί να μεταβεί στις ρυθμίσεις της εφαρμογής με το κουμπί μενού (public void startMenuActivity(View view)).

# Ανάλυση MenuActivity

## Βασικές λειτουργείες.

### Αρχικοποίηση UI και ανάκτηση αποθηκευμένων ρυθμίσεων

Ορίζει τα πεδία εισαγωγής για MQTT Broker (IP, Port), Device ID και διάρκεια μετάδοσης.\
Ανακτά αποθηκευμένες ρυθμίσεις από SharedPreferences (SharedPreferences prefs).\
Υπενθυμίζει στον χρήστη να εξαιρέσει την εφαρμογή από την εξοικονόμηση μπαταρίας κάθε 3 επισκέψεις στο μενού.

### Διαχείριση GPS λειτουργίας μέσω Switch

Δίνεται στον χρήστη η επιλογή του τρόπου λήψης των συντεταγμένων της συσκευής του (Auto/Manual).\
By default η εφαρμογή ξεκινάει σε manual mode, κατά την οποία οι συντεταγμένες λαμβάνονται μέσω CSV αρχείων. Στο manual mode υπάρχει και η δυνατότητα επιλογής του χρονικού ορίου κατά το οποίο η  εφαρμογή θα μεταδίδει τα δεδομένα της τοποθεσίας (στο Auto το πεδίο αυτό δεν υπάρχει).\
Όταν ο χρήστης αλλάζει το mode, ενημερώνονται οι ρυθμίσεις και σταματά αυτόματα η μετάδοση δεδομένων αν αυτή είναι ενεργή.\
Αν απαιτούνται δικαιώματα τοποθεσίας, ζητούνται δυναμικά από τον χρήστη.

### Διαχείριση Δικαιωμάτων Τοποθεσίας

Ζητά πρόσβαση στο Fine Location (Ακριβής τοποθεσία) και, αν απορριφθεί, προσπαθεί να λάβει Coarse Location (Λιγότερο ακριβής).\
Αν ο χρήστης επιλέξει "Don't Ask Again", προτείνει άνοιγμα των ρυθμίσεων συστήματος.

### Έναρξη και διακοπή μετάδοσης MQTT

Εκκινεί την υπηρεσία TransmissionService και μεταδίδει δεδομένα μέσω MQTT, είτε από GPS (Auto Mode) είτε από CSV (Manual Mode).\
Στην περίπτωση που έχει επιλεγεί Manual Mode, αντλεί και το χρονικό διάστημα για το οποίο θα μεταδίδει δεδομένα.\
Αποθηκεύει τις παραμέτρους MQTT και μεταφέρει τις απαραίτητες πληροφορίες στην υπηρεσία.

### Η μετάδοση σταματά με το πάτημα του κουμπιού ή κατά την αλλαγή λειτουργίας.

```
public void stopMQTTTransmission(View view) {
    stopService(new Intent(this, TransmissionService.class));
    isTransmitting = false;
    Log.d(TAG, "🛑 Stopping MQTT TransmissionService");
}
```

### Διαχείριση εξοικονόμησης μπαταρίας

Υπενθυμίζει στον χρήστη να εξαιρέσει την εφαρμογή από battery optimization, ώστε να συνεχίζει να λειτουργεί στο παρασκήνιο (PowerManager pm).\
Αν η εξαίρεση δεν έχει δοθεί, εμφανίζεται διάλογος καθοδήγησης στις ρυθμίσεις.

### Βελτιωμένη εμπειρία χρήστη

Απόκρυψη πληκτρολογίου όταν ο χρήστης πατά έξω από το πεδίο εισαγωγής (public boolean dispatchTouchEvent(MotionEvent event)).\
Απόκρυψη περιττών πεδίων ανάλογα με το GPS mode.\
Επιλογή χρωμάτων στις δραστηριότητες που να ακολουθούν τις αρχές ομοιότητας και συνήθειας.\
Επιβεβαίωση εξόδου μέσω διαλόγου.

# Ανάλυση UserApp

Η κλάση UserApp αποτελεί την κύρια κλάση εφαρμογής (Application) και είναι υπεύθυνη για:\
        - Αρχικοποίηση της εφαρμογής κατά την εκκίνηση.\
        - Διαχείριση του κύκλου ζωής της εφαρμογής μέσω του AppLifecycleObserver.\
        - Καθαρισμό των αποθηκευμένων δεδομένων για διατήρηση της συνοχής του συστήματος.

## Βασικές λειτουργίες

### Εκκαθάριση αποθηκευμένων ρυθμίσεων (SharedPreferences)

Διαγράφει τις παλιές ρυθμίσεις εφαρμογής (AppPrefs) κατά την εκκίνηση.\
Καθαρίζει τα δεδομένα ειδοποιήσεων (AlertStatus) αν είναι παλαιότερα από 24 ώρες.

### Καταχώριση παρατηρητή κύκλου ζωής (AppLifecycleObserver)

Παρακολουθεί αν η εφαρμογή εκτελείται στο παρασκήνιο και διαχειρίζεται κατάλληλα τη σύνδεση MQTT.\

```
ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver(this));
```

### Διαχείριση κατάστασης εφαρμογής (setRunsInBackground)

```
public void setRunsInBackground(boolean runsInBackground) {
    this.runsInBackground = runsInBackground;
}
```

# Ανάλυση BaseActivity

Η κλάση BaseActivity είναι η βάση όλων των δραστηριοτήτων (Activities) στην εφαρμογή. Παρέχει κοινές λειτουργίες, όπως:\
        - Διαχείριση του πλήκτρου επιστροφής (Back Button) με έλεγχο διπλού πατήματος.\
        - Έλεγχος αν η εφαρμογή τρέχει στο παρασκήνιο και απαιτεί άδεια τοποθεσίας.\
        - Έλεγχος αν η υπηρεσία TransmissionService είναι ενεργή.\
        - Εμφάνιση διαλόγου για άδεια τοποθεσίας στο παρασκήνιο.

## Βασικές λειτουργίες

### Διαχείριση του πλήκτρου επιστροφής (backPressedCallback()):
Αν η δραστηριότητα δεν είναι η κύρια (root), κλείνει άμεσα.\
Αν η δραστηριότητα είναι η κύρια, ο χρήστης πρέπει να πατήσει δύο φορές το back button για έξοδο.\
Αν τρέχει η υπηρεσία TransmissionService, ζητά άδεια τοποθεσίας στο παρασκήνιο πριν τερματιστεί η εφαρμογή.

### Έλεγχος αν τρέχει η υπηρεσία TransmissionService

Βρίσκει τις τρέχουσες υπηρεσίες (RunningServices) και ελέγχει αν η TransmissionService είναι ενεργή.

### Αίτημα για άδεια τοποθεσίας στο παρασκήνιο

Αν η εφαρμογή χρειάζεται πρόσβαση στην τοποθεσία στο background, εμφανίζει διάλογο καθοδήγησης για να δώσει ο χρήστης την άδεια μέσω των ρυθμίσεων (protected void requestBackgroundLocationPermission()).

# Ανάλυση AppLifecycleObserver

Η κλάση AppLifecycleObserver είναι παρατηρητής κύκλου ζωής της εφαρμογής (Lifecycle Observer) και χρησιμοποιείται για να εντοπίζει πότε η εφαρμογή μετακινείται στο προσκήνιο ή στο παρασκήνιο.

## Βασικές λειτουργίες

### Ανίχνευση της μετάβασης στο προσκήνιο (onStart)

Όταν η εφαρμογή μετακινείται στο προσκήνιο (foreground), ενημερώνει την UserApp ότι δεν τρέχει στο παρασκήνιο (setRunsInBackground(false)).\
Χρήσιμο για επαναφορά σύνδεσης MQTT ή συνέχιση της μετάδοσης δεδομένων.

### Ανίχνευση της μετάβασης στο παρασκήνιο (onStop)

Όταν η εφαρμογή μεταβαίνει στο παρασκήνιο (background), ενημερώνει την UserApp ότι τρέχει στο παρασκήνιο (setRunsInBackground(true)).\
Μπορεί να χρησιμοποιηθεί για διαχείριση πόρων, όπως παύση της μετάδοσης δεδομένων.

### Σύνδεση με το UserdApp

Η AppLifecycleObserver εγγράφεται στο Lifecycle κατά την εκκίνηση της εφαρμογής (UserdApp.java).\
Εξασφαλίζει ότι η εφαρμογή παρακολουθεί την κατάστασή της χωρίς να χρειάζεται επιπλέον κώδικα στις δραστηριότητες (Activities).

```
ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver(this));
```

# Ανάλυση HazardAlertService

Η κλάση HazardAlertService είναι μια Foreground Service που ενεργοποιείται όταν ανιχνεύεται ένας κίνδυνος.\
Εμφανίζει ειδοποίηση συστήματος για τον κίνδυνο.\
Εκκινεί το HazardAlertActivity για να προειδοποιήσει τον χρήστη.\
Τερματίζεται αυτόματα μετά από 8 δευτερόλεπτα.

## Βασικές λειτουργίες

### Έναρξη και ειδοποίηση στο σύστημα (onStartCommand)

Λαμβάνει μήνυμα κινδύνου (hazard_message) και επίπεδο κινδύνου (hazard_level).\
Εκκινεί το service στο προσκήνιο και εμφανίζει ειδοποίηση συστήματος (launchHazardAlert(hazardMessage, hazardLevel);).

### Eμφάνιση ειδοποίησης συστήματος (showNotification)

Δημιουργεί μια υψηλής προτεραιότητας ειδοποίηση (PRIORITY_HIGH) με το επίπεδο του κινδύνου.\
Ορίζει κόκκινο χρώμα για να τραβήξει την προσοχή του χρήστη.

### Εκκίνηση της δραστηριότητας ειδοποίησης (launchHazardAlert)

Ανοίγει το HazardAlertActivity, ακόμα και όταν η εφαρμογή είναι στο παρασκήνιο.\
Μεταφέρει το μήνυμα και το επίπεδο του κινδύνου στην οθόνη ειδοποίησης.\
Τερματίζει αυτόματα την υπηρεσία μετά από 8 δευτερόλεπτα για εξοικονόμηση πόρων.\

```
private void launchHazardAlert(String hazardMessage, String hazardLevel)
```

### Δημιουργία καναλιού ειδοποιήσεων (createNotificationChannel)

Για συσκευές Android 8.0+, δημιουργεί ένα Notification Channel για να μπορεί η ειδοποίηση να εμφανίζεται σωστά (private void createNotificationChannel()).

### Καθαρισμός και τερματισμός υπηρεσίας (onDestroy)

Εξασφαλίζει ότι γίνεται cleanup στο parent Service και κάνει log το κατάλληλο μήνυμα καταστροφής του current Service.

# Ανάλυση LocationUpdateCallback

Η διεπαφή (interface) LocationUpdateCallback καθορίζει τη συμπεριφορά της εφαρμογής όταν ενημερώνεται η τοποθεσία.\
Υποστηρίζει ενημερώσεις τοποθεσίας από GPS ή χειροκίνητα δεδομένα (CSV).\
Επιτρέπει την ευέλικτη διαχείριση της μετάδοσης δεδομένων από διαφορετικές πηγές.

## Μέθοδος onLocationUpdate(double latitude, double longitude, boolean originCSV)

Καλείται όταν ενημερώνεται η τοποθεσία της συσκευής.\
Περιέχει τρεις παραμέτρους:\
    latitude → Το γεωγραφικό πλάτος της νέας τοποθεσίας.\
    longitude → Το γεωγραφικό μήκος της νέας τοποθεσίας.\
    originCSV → Boolean που δείχνει αν τα δεδομένα προέρχονται από CSV (true) ή GPS (false).\

```
void onLocationUpdate(double latitude, double longitude, boolean originCSV);
```

## Χρήση στη TransmissionService

Η TransmissionService χρησιμοποιεί το LocationUpdateCallback για να λαμβάνει ενημερώσεις τοποθεσίας και να αποφασίζει αν θα στείλει τα δεδομένα μέσω MQTT.

Παράδειγμα εφαρμογής:\

```
public class TransmissionService extends Service implements LocationUpdateCallback {
    @Override
    public void onLocationUpdate(double latitude, double longitude, boolean originCSV) {
        mqttManager.publishLocation(latitude, longitude);
    }
}
```

# Ανάλυση MQTTManager

Η κλάση MQTTManager είναι υπεύθυνη για τη διαχείριση της επικοινωνίας μέσω MQTT, επιτρέποντας τη
συσκευή να λαμβάνει ειδοποιήσεις κινδύνου και να δημοσιεύει δεδομένα τοποθεσίας στον MQTT Broker.

Συνδέεται στον MQTT Broker και κάνει subscribe στις ειδοποιήσεις κινδύνου.\
Εξασφαλίζει αυτόματη επανασύνδεση σε περίπτωση αποσύνδεσης.\
Στέλνει δεδομένα τοποθεσίας.\
Επεξεργάζεται ειδοποιήσεις και εμφανίζει προειδοποιήσεις στον χρήστη.

## Βασικές λειτουργίες

### Σύνδεση και εγγραφή στο MQTT Broker (initializeMQTT)

Συνδέεται με τον MQTT Broker και κάνει subscribe στο Alerts/DeviceID για τη λήψη ειδοποιήσεων κινδύνου.\
Ρυθμίζει τον μηχανισμό αυτόματης επανασύνδεσης.

### Λήψη ειδοποιήσεων κινδύνου (messageArrived)

Όταν λαμβάνεται μήνυμα σε Alerts/DeviceID, εξάγει τη σοβαρότητα και την απόσταση του κινδύνου (public void messageArrived(String topic, MqttMessage message)).

### Αυτόματη επανασύνδεση (reconnectToMQTT)

Εάν η σύνδεση χαθεί, προσπαθεί να επανασυνδεθεί με εκθετική καθυστέρηση retry (2s έως 30s).\
Ελέγχει αν υπάρχει internet πριν επιχειρήσει σύνδεση.\

```
while (!mqttClient.isConnected() && attempts < maxRetries) {
    if (!NetworkUtils.isInternetAvailable(context)) {
        Log.e(TAG, "❌ No internet available. Retrying later.");
        return;
    }
    mqttClient.reconnect();
    attempts++;
}
```

### Δημοσίευση τοποθεσίας (publishLocation)

Στέλνει δεδομένα τοποθεσίας (longitude, latitude, device ID) στο topic Locations.\
Ελέγχει αν ο πελάτης MQTT είναι συνδεδεμένος και αν υπάρχει internet πριν δημοσιεύσει.

### Αποσύνδεση από τον MQTT Broker (disconnect)

Σταματά τη μετάδοση και κλείνει τη σύνδεση με τον Broker.\

```
if (mqttClient != null && mqttClient.isConnected()) {
    mqttClient.disconnect();
    mqttClient.close();
}
```

# Ανάλυση StopServiceReceiver

Η κλάση StopServiceReceiver είναι ένας Broadcast Receiver που χρησιμοποιείται για τον τερματισμό της υπηρεσίας μετάδοσης (TransmissionService) όταν λαμβάνεται αντίστοιχη εντολή.\
Λαμβάνει broadcasts για τη διακοπή της μετάδοσης δεδομένων.\
Σταματά την TransmissionService όταν ο χρήστης πατά το κουμπί διακοπής.\
Χρησιμοποιείται για ελαφριά και γρήγορη διαχείριση της υπηρεσίας.

## Βασικές λειτουργίες

### Λήψη broadcast και διακοπή της υπηρεσίας μετάδοσης (onReceive)

Όταν ο χρήστης πατήσει το κουμπί διακοπής, ο StopServiceReceiver ενεργοποιείται και διακόπτει το TransmissionService.\
(public void onReceive(Context context, Intent intent))

### Χρήση στη διακοπή υπηρεσιών

Ο StopServiceReceiver μπορεί να χρησιμοποιηθεί σε ειδοποιήσεις ή UI κουμπιά για να δώσει στον χρήστη έναν γρήγορο τρόπο να σταματήσει τη μετάδοση δεδομένων.\

Παράδειγμα ενσωμάτωσης σε ειδοποίηση:\

```
Intent stopIntent = new Intent(context, StopServiceReceiver.class);
PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
```

# Ανάλυση TransmissionService

Η κλάση TransmissionService είναι Foreground Service που χειρίζεται τη μετάδοση δεδομένων τοποθεσίας μέσω MQTT.\
Υποστηρίζει δύο λειτουργίες μετάδοσης:\
        - Αυτόματη (GPS) → Ζωντανή ενημέρωση τοποθεσίας από το GPS.\
        - Χειροκίνητη (CSV) → Αποστολή προκαθορισμένων τοποθεσιών από αρχείο CSV.\
Παρακολουθεί τη σύνδεση στο διαδίκτυο και κάνει επανασύνδεση όταν αποσυνδεθεί.\
Σταματά αυτόματα αν έχει οριστεί χρονικό όριο μετάδοσης.

## Βασικές λειτουργίες

### Έναρξη και αρχικοποίηση υπηρεσίας (onCreate)

Ξεκινά το MQTTManager και το GPSManager.\
Ενεργοποιεί το WakeLock για να αποτρέψει τον τερματισμό της υπηρεσίας από το σύστημα.\

```
mqttManager = new MQTTManager(this);
gpsManager = new GPSManager(this, this);
csvReader = new CSVReader(this, this);`

PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
if (powerManager != null) {
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UserApp::MQTTWakeLock");
    wakeLock.acquire(10 * 60 * 1000L);
}
```

### Έναρξη μετάδοσης (onStartCommand)

Διαβάζει τις παραμέτρους MQTT (Broker URL, Client ID, διάρκεια μετάδοσης).\
Επιλέγει μεταξύ CSV και GPS λειτουργίας ανάλογα με τη ρύθμιση isManualMode.\

```
isManualMode = intent.getBooleanExtra("IS_MANUAL_MODE", false);
if (isManualMode) {
    csvReader.startManualTransmission(transmissionDuration, 1000);
} else {
    gpsManager.startLocationUpdates();
}
```

Ρυθμίζει χρονοδιακόπτη για να σταματήσει την υπηρεσία μετά από ορισμένη χρονική διάρκεια.\

```
if (transmissionDuration > 0) {
    halterThread = new Thread(() -> {
        try {
            Thread.sleep(transmissionDuration * 1000L);
            stopSelf();
        } catch (InterruptedException e) {
            Log.d(TAG, "Halter thread interrupted.");
        }
    });
    halterThread.start();
}
```

### Παρακολούθηση σύνδεσης στο διαδίκτυο (startInternetMonitoring)

Ελέγχει αν υπάρχει σύνδεση στο διαδίκτυο κάθε 5 δευτερόλεπτα.\
Διακόπτει τη μετάδοση αν η συσκευή αποσυνδεθεί.\
Επανασυνδέει το MQTT όταν αποκατασταθεί η σύνδεση.\

```
boolean isConnected = NetworkUtils.isInternetAvailable(this);
if (!isConnected && wasConnected) {
    AlertManager.showInternetDisconnectedNotification(this);
} else if (isConnected && !wasConnected) {
    AlertManager.cancelInternetDisconnectedNotification(this);
    mqttManager.reconnectToMQTT();
}
```

### Ενημέρωση τοποθεσίας (onLocationUpdate)

Στέλνει την τοποθεσία μόνο αν ταιριάζει με τον τρέχοντα τρόπο λειτουργίας (GPS ή CSV).\

```
if (isManualMode && originCSV) {
    mqttManager.publishLocation(latitude, longitude);
} else if (!isManualMode && !originCSV) {
    mqttManager.publishLocation(latitude, longitude);
}
```

### Τερματισμός υπηρεσίας (onDestroy)

Σταματά τη μετάδοση δεδομένων και κλείνει τη σύνδεση MQTT.\
Αποδεσμεύει το WakeLock για εξοικονόμηση μπαταρίας.

# Ανάλυση AlertManager

Η κλάση AlertManager είναι υπεύθυνη για τη διαχείριση ειδοποιήσεων σχετικά με την κατάσταση του δικτύου.\
Εμφανίζει ειδοποίηση όταν η συσκευή αποσυνδέεται από το διαδίκτυο.\
Ακυρώνει την ειδοποίηση όταν αποκαθίσταται η σύνδεση.\
Χρησιμοποιεί NotificationChannel για σωστή λειτουργία σε Android 8.0+.

## Βασικές λειτουργίες

### Εμφάνιση ειδοποίησης αποσύνδεσης (showInternetDisconnectedNotification)

Δημιουργεί ένα NotificationChannel για Android 8.0+.\
Εμφανίζει ειδοποίηση υψηλής προτεραιότητας (PRIORITY_HIGH) με μήνυμα αποσύνδεσης.

### Ακύρωση ειδοποίησης όταν αποκαθίσταται η σύνδεση (cancelInternetDisconnectedNotification)

Αν η σύνδεση αποκατασταθεί, η ειδοποίηση απομακρύνεται (public static void cancelInternetDisconnectedNotification(Context context)).

# Ανάλυση CSVReader

Η κλάση CSVReader είναι υπεύθυνη για τη φόρτωση και μετάδοση προκαθορισμένων τοποθεσιών από αρχεία CSV.\
Διαβάζει και αποθηκεύει δεδομένα τοποθεσίας από όσα CSV αρχεία τοποθετηθούν από τους devs.\
Μεταδίδει τις x τοποθεσίες που έχει επιλέξει ο χρήστης σταδιακά (ανά 1 δευτερόλεπτο) μέχρις ότου είτε να 
ολοκληρωθεί η μετάδοση των x τοποθεσιών είτε να τερματιστεί manually από τον user (by default στέλνεται όλο το αρχείο).

## Βασικές λειτουργίες

### Φόρτωση δεδομένων τοποθεσίας από CSV (loadCSVFromResources)

Επιλέγει τυχαία ένα αρχείο CSV από τη λίστα που παρέχεται (η λίστα μπορεί να περιέχει όσα αρχεία βάλουμε).\
Κάνει skip την πρώτη γραμμή που περιέχει τα header x & y.\
Διαβάζει κάθε γραμμή και αποθηκεύει τις συντεταγμένες (latitude, longitude) σε λίστα (locationQueue).

### Έναρξη χειροκίνητης μετάδοσης (startManualTransmission)

Ξεκινά τη μετάδοση των τοποθεσιών από το CSV.\
Αποστέλλει κάθε τοποθεσία με συγκεκριμένο χρονικό διάστημα (intervalMs).\
Καλεί το LocationUpdateCallback για να δημοσιεύσει τα δεδομένα μέσω MQTT.\
Σταματά αυτόματα όταν ολοκληρωθεί η αποστολή.

### Διακοπή της μετάδοσης (stopTransmission)

Ακυρώνει όλες τις προγραμματισμένες αποστολές δεδομένων.\
Αποτρέπει περαιτέρω δημοσιεύσεις αν η μετάδοση διακοπεί από τον χρήστη.

# Ανάλυση GPSManager

Η κλάση GPSManager διαχειρίζεται τη λήψη δεδομένων τοποθεσίας μέσω GPS και ενημερώνει την TransmissionService ή άλλη υπηρεσία που χρησιμοποιεί το LocationUpdateCallback.\
Υποστηρίζει συνεχείς ενημερώσεις τοποθεσίας κάθε 5 δευτερόλεπτα.\
Σταματά τη λήψη δεδομένων αν είναι ενεργοποιημένη η χειροκίνητη λειτουργία (CSV Mode).\
Ελέγχει αν η εφαρμογή έχει τα απαραίτητα δικαιώματα GPS.

## Βασικές λειτουργίες

### Έναρξη ενημερώσεων τοποθεσίας (startLocationUpdates)

Ελέγχει αν η συσκευή έχει τα απαραίτητα δικαιώματα τοποθεσίας (ACCESS_FINE_LOCATION ή ACCESS_COARSE_LOCATION).\
Αν η εφαρμογή είναι σε χειροκίνητη λειτουργία (CSV Mode), αποτρέπει τις ενημερώσεις GPS.\
Ελέγχει εάν το service τρέχει ήδη, ώστε να μην ξεκινήσει δεύτερο instance του.\
Ενεργοποιεί τη συλλογή δεδομένων τοποθεσίας μέσω ενός Runnable task που γίνεται scheduled να εκτελείται κάθε 1 δευτερόλεπτο.\

```
Runnable locationUpdateTask = new Runnable() {
    @SuppressLint("MissingPermission")
    public void run() {
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                Log.d(TAG, "GPS Update: " + location.getLatitude() + ", " + location.getLongitude());
                gpsCallback.onLocationUpdate(location.getLatitude(), location.getLongitude(), false);
            } else {
                Log.w(TAG, "Failed to retrieve last known location.");
            }
        });
    }
};
scheduledExecutorService.scheduleWithFixedDelay(locationUpdateTask, 0, 1, TimeUnit.SECONDS);
```

### Διακοπή ενημερώσεων τοποθεσίας (stopLocationUpdates)

Ακυρώνει τη συλλογή δεδομένων GPS όταν η υπηρεσία τερματίζεται ή η λειτουργία αλλάζει.

# Ανάλυση HazardHandler

Η κλάση HazardHandler είναι υπεύθυνη για τη διαχείριση ειδοποιήσεων κινδύνου.\
Αποθηκεύει και διαχειρίζεται τις ειδοποιήσεις σε ουρά για αποφυγή επικαλύψεων.\
Εμφανίζει ειδοποίηση στον χρήστη και ενεργοποιεί τον HazardAlertService.\
Αναπαράγει προειδοποιητικό ηχητικό σήμα ανάλογα με το επίπεδο κινδύνου.

## Βασικές λειτουργίες

### Αποθήκευση και διαχείριση ειδοποιήσεων (showHazardAlert)

Καταγράφει την ειδοποίηση στη μνήμη (SharedPreferences) ώστε να είναι διαθέσιμη ακόμα και μετά από επανεκκίνηση της εφαρμογής.\
Προσθέτει την ειδοποίηση σε ουρά (Queue) για αποφυγή επικαλύψεων.\
Ξεκινά την επεξεργασία της ουράς μέσω processNextAlert().

### Επεξεργασία ειδοποίησης (processNextAlert)

Ελέγχει αν υπάρχει ήδη ενεργή ειδοποίηση.\
Λαμβάνει την επόμενη ειδοποίηση από την ουρά και την εκκινεί.\
Ενεργοποιεί τον HazardAlertService και εμφανίζει ειδοποίηση.

### Εκκίνηση του HazardAlertService (startForegroundService)

Στέλνει τα δεδομένα της ειδοποίησης στην HazardAlertService για να εμφανίσει το μήνυμα στον χρήστη.\
Ρυθμίζει χρονόμετρο ώστε να προχωρήσει στην επόμενη ειδοποίηση μετά από 7-10 δευτερόλεπτα.

### Αναπαραγωγή ηχητικού σήματος (playAlertSound)

Αποτρέπει επαναλαμβανόμενη αναπαραγωγή αν ο ήχος παίζει ήδη.\
Επιλέγει διαφορετικό ήχο για High και Moderate κίνδυνο.

### Διακοπή ήχου και απελευθέρωση πόρων (stopAlertSound)

Διακόπτει την αναπαραγωγή και αποδεσμεύει τον MediaPlayer όταν δεν χρειάζεται πλέον.

# Ανάλυση NetworkUtils

Η κλάση NetworkUtils παρέχει μια βοηθητική μέθοδο για τον έλεγχο της σύνδεσης στο διαδίκτυο.\
Χρησιμοποιεί το ConnectivityManager για να ελέγξει αν η συσκευή είναι συνδεδεμένη στο διαδίκτυο.\
Υποστηρίζει έλεγχο για WiFi και δεδομένα κινητής τηλεφωνίας (CELLULAR).\
Συμβατή με τις πιο πρόσφατες εκδόσεις Android (NetworkCapabilities).

## Βασικές λειτουργίες

### Έλεγχος ενεργής σύνδεσης (isInternetAvailable)

Χρησιμοποιεί το ConnectivityManager για να ανακτήσει το ενεργό δίκτυο.\
Ελέγχει αν η σύνδεση είναι μέσω WiFi ή δεδομένων κινητής τηλεφωνίας.\
Επιστρέφει true αν υπάρχει διαθέσιμο διαδίκτυο, αλλιώς false.

## Χρήση στην εφαρμογή
Η NetworkUtils χρησιμοποιείται σε άλλες κλάσεις όπως TransmissionService και MQTTManager για να ελέγχει αν υπάρχει διαθέσιμο διαδίκτυο πριν στείλει δεδομένα.

## Παράδειγμα ελέγχου πριν από την αποστολή δεδομένων:

```
if (!NetworkUtils.isInternetAvailable(context)) {
    Log.e(TAG, "❌ No internet connection. Skipping publish.");
    return;
}
```

# Ανάλυση NotificationHelper

Η κλάση NotificationHelper διαχειρίζεται τις ειδοποιήσεις της εφαρμογής, διασφαλίζοντας ότι ο χρήστης λαμβάνει σημαντικές ενημερώσεις και προειδοποιήσεις.\
Δημιουργεί και διαχειρίζεται Notification Channels για Android 8.0+.\
Υποστηρίζει ειδοποιήσεις για κρίσιμες ειδοποιήσεις (Alerts) και συστημικά γεγονότα (System Events).\
Προσθέτει κουμπί διακοπής (Stop) σε ειδοποιήσεις υπηρεσιών (Foreground Services).

## Βασικές λειτουργίες

### Δημιουργία ειδοποιήσεων για foreground υπηρεσίες (createForegroundServiceNotification)

Εμφανίζει επίμονη ειδοποίηση για την υπηρεσία μετάδοσης MQTT.\
Επιτρέπει στον χρήστη να τη σταματήσει μέσω κουμπιού (Stop).\
Χρησιμοποιεί PendingIntent για αποστολή Broadcast στον StopServiceReceiver.\

```
Intent stopIntent = new Intent(context, StopServiceReceiver.class);
PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
        context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

return new NotificationCompat.Builder(context, SYSTEM_CHANNEL_ID)
        .setSmallIcon(iconRes)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .addAction(0, "Stop", stopPendingIntent) // Stop button
        .build();
```

### Δημιουργία Notification Channels (createNotificationChannels)

Εξασφαλίζει ότι τα κανάλια ειδοποιήσεων δημιουργούνται μόνο μία φορά.\
Υποστηρίζει δύο τύπους ειδοποιήσεων:\
        - 🔔 ALERTS_CHANNEL_ID → Για κρίσιμες προειδοποιήσεις (High Priority).\
        - ⚙️ SYSTEM_CHANNEL_ID → Για γενικά συστημικά γεγονότα (Default Priority).

### Εμφάνιση κρίσιμων ειδοποιήσεων (showAlertNotification)

Χρησιμοποιείται για την αποστολή προειδοποιήσεων κινδύνου μέσω MQTT.

### Εμφάνιση συστημικών ειδοποιήσεων (showSystemNotification)

Χρησιμοποιείται για ειδοποιήσεις σχετικά με MQTT, σύνδεση στο διαδίκτυο κ.λπ.

### Αποστολή ειδοποίησης (showNotification)

Χρησιμοποιεί το NotificationManagerCompat για την εμφάνιση της ειδοποίησης.\
Ελέγχει αν η εφαρμογή έχει άδεια POST_NOTIFICATIONS σε Android 13+.