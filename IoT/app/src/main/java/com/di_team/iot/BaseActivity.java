package com.di_team.iot;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.di_team.iot.net.TransmissionService;

/**Base class that all activities extend. Contains useful utilities like: <br>
 * 1. Using broadcast receiver to scan for service errors <br>
 * 2. Using dialogue windows to notify user for them. */
public class BaseActivity extends AppCompatActivity {
    protected static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 1001;
    protected static final int COARSE_LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private long backPressedTime = 0;

    final private BroadcastReceiver errorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String error = intent.getStringExtra("error_message");
            showErrorDialog("Transmission Service Error", error);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressedCallback();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.di_team.iot.ERROR_BROADCAST");
        registerReceiver(errorReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(errorReceiver);
    }

    protected void showErrorDialog(String cause, String message) {
        new AlertDialog.Builder(this)
                .setTitle(cause)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**Behavior of app's back button.*/
    private void backPressedCallback() {
        // non-root activities finish smoothly
        if(!isTaskRoot()){
            finish();
            return;
        }

        //root activity exit happens on a double back tap, and background location permission is requested if service is running
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (isServiceRunning(TransmissionService.class) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestBackgroundLocationPermission();
            } else {
                finish(); // Exit app
            }
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    protected boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Any of the app's activities can prompt for background location permission before exit (if needed).*/
    protected void requestBackgroundLocationPermission() {
        new AlertDialog.Builder(this)
                .setTitle("Allow Background Location")
                .setMessage("To continue using location features, please grant background location access.\nIoT will continue sending messages in the background after providing permissions.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Reset the back press timer to allow exit on next back press
                    backPressedTime = 0;
                    stopService(new Intent(this, TransmissionService.class));
                    dialog.dismiss();
                })
                .show();
    }
}

