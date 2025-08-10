package com.di_team.iot;


import android.app.Application;
import android.content.SharedPreferences;
import androidx.lifecycle.ProcessLifecycleOwner;

public class IoTApp extends Application {
    private boolean runsInBackground;

    @Override
    public void onCreate() {
        super.onCreate();

        // Clear saved preferences
        SharedPreferences prefs = getSharedPreferences("MenuPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Register lifecycle observer
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver(this));
    }

    public void setRunsInBackground(boolean runsInBackground) {
        this.runsInBackground = runsInBackground;
    }

    public boolean getRunsInBackground() {
        return runsInBackground;
    }
}
