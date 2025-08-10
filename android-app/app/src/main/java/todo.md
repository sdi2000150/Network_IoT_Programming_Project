## To-Dos for: 
1. ~~HazardHandler: ensure proper parsing of the message. Messages are of type "Distance <..> Severity <...>". The word that follows Severity is either "Moderate" or "High"~~
2. ~~Ensure background functionality: integrate logic indicated in classes IoTApp, AppLifecycleObserver from IoT.~~
3. ~~Find a way to stop the location updates once service stops.~~
4. ~~Define conditions under which TransmissionService stops: this can be either~~
    - ~~when location mode switch changes state, or~~
    - ~~when user presses start service, onStart properly handles the case that the service was already running (stop ongoing publishes/threads, nullify fields etc.)~~
5. ~~Battery optimization exclusion. Take it straight from IoT.~~
### Extras: 
1. ~~Service runs persistently in the background, can be stopped by a button in its push notification.~~