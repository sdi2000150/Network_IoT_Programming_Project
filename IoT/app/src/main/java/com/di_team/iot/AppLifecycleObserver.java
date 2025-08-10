package com.di_team.iot;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class AppLifecycleObserver implements DefaultLifecycleObserver {
    private final IoTApp application;

    public AppLifecycleObserver(IoTApp application) {
        this.application = application;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d("AppLifecycleObserver", "App moved to foreground");
        application.setRunsInBackground(false); // App is in the foreground
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d("AppLifecycleObserver", "App moved to background");
        application.setRunsInBackground(true);

    }


}
