package com.di_team.android.services;

/**Defines behavior upon location update. Update can be triggered either by manual data (CSV) or GPS.*/
public interface LocationUpdateCallback {
    void onLocationUpdate(double latitude, double longitude, boolean originCSV);
}
