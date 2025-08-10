package com.di_team.android.core;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.di_team.android.lifecycle.AppLifecycleObserver;

public class UserApp extends Application {
    private boolean runsInBackground;

    @Override
    public void onCreate() {
        super.onCreate();

        // Clear saved preferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        SharedPreferences alertStatus = getSharedPreferences("AlertStatus", MODE_PRIVATE);
        // Clear alert status if it's older than 24 hours
        long timeReceived = alertStatus.getLong("time_received", -1);
        if(timeReceived == -1 || System.currentTimeMillis() - timeReceived > 24*60*1000) {
            alertStatus.edit().clear().apply();
        }

        // Register lifecycle observer
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver(this));
    }

    public void setRunsInBackground(boolean runsInBackground) {
        this.runsInBackground = runsInBackground;
    }
}