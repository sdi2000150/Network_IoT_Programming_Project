package com.di_team.android.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.di_team.android.R;
import androidx.annotation.RequiresApi;

public class HazardAlertActivity extends Activity {
    private static final String TAG = "HazardAlertActivity";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hazard_alert_activity);

        Log.d(TAG, "🚨 Hazard Alert Activity Opened!");

        // ✅ Get hazard details
        String hazardMessage = getIntent().getStringExtra("hazard_message");
        String hazardLevel = getIntent().getStringExtra("hazard_level");

        // ✅ Set message text
        TextView alertText = findViewById(R.id.alert_text);
        alertText.setText(hazardMessage);

        // ✅ Adjust UI based on severity
        RelativeLayout alertLayout = findViewById(R.id.alert_layout);
        if ("High".equalsIgnoreCase(hazardLevel)) {
            alertLayout.setBackgroundColor(Color.RED); // 🔴 High Risk (Red Background)
            alertText.setTextSize(22); // Larger text for urgency
            alertText.setTextColor(Color.WHITE);
        } else {
            alertLayout.setBackgroundColor(Color.YELLOW); // 🟡 Moderate Risk (Yellow Background)
            alertText.setTextSize(18);
            alertText.setTextColor(Color.BLACK);
        }

        // ✅ Dismiss button closes alert manually
        Button dismissButton = findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener(v -> closeAlert("🛑 User dismissed alert."));

        // ✅ Ensure alert is shown over any screen (even when locked)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // ✅ Adjust the window size
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = 800;  // Set desired width in pixels
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; // Set desired height in pixels
        getWindow().setAttributes(params);

        // ✅ Register receiver to listen for auto-close
        IntentFilter filter = new IntentFilter("CLOSE_HAZARD_ALERT");
        registerReceiver(closeAlertReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        // ⏳ **Auto-close after 7 seconds**
        new Handler().postDelayed(() -> sendBroadcast(new Intent("CLOSE_HAZARD_ALERT")), 7000);
    }

    // ✅ Closes the alert safely
    private void closeAlert(String reason) {
        Log.d(TAG, reason);
        finish(); // Closes the alert
    }

    // ✅ Broadcast receiver for auto-close
    private final BroadcastReceiver closeAlertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "🛑 Received Close Alert Broadcast.");
            closeAlert("🛑 Closing Hazard Alert via Broadcast.");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(closeAlertReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "⚠️ Broadcast receiver was already unregistered.");
        }
    }
}
