package com.di_team.android.lifecycle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.di_team.android.core.UserApp;

/**Manages application's lifecycle*/
public class AppLifecycleObserver implements DefaultLifecycleObserver {
    private final UserApp application;

    public AppLifecycleObserver(UserApp application) {
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