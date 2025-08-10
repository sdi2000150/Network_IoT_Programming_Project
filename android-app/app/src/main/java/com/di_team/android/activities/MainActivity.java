package com.di_team.android.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;

import com.di_team.android.R;
import com.di_team.android.utils.NotificationHelper;
import com.di_team.android.core.BaseActivity;

import java.text.MessageFormat;


public class MainActivity extends BaseActivity {
    private Button menuButton;
    private TextView timeReceivedTextView, severityTextView, distanceTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        NotificationHelper.createNotificationChannels(this);
        menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(this::startMenuActivity); // Set click listener

        timeReceivedTextView = findViewById(R.id.timeReceived);
        severityTextView = findViewById(R.id.severity);
        distanceTextView = findViewById(R.id.distance);

       updateWindow();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWindow();
        menuButton.setOnClickListener(this::startMenuActivity); // Set click listener
    }

    public void startMenuActivity(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    //Updates the window with the latest alert information
    private void updateWindow() {
        SharedPreferences alertStatus = getSharedPreferences("AlertStatus", MODE_PRIVATE);
        long timeReceived = alertStatus.getLong("time_received", -1);
        if(timeReceived == -1) {
            timeReceivedTextView.setText(R.string.no_recent_alerts);
            severityTextView.setText("");
            distanceTextView.setText("");
        } else {
            String severity = alertStatus.getString("severity", "Moderate");
            float distance = alertStatus.getFloat("distance", -1);
            severityTextView.setText(MessageFormat.format("{0} {1}", getString(R.string.severity), severity));
            distanceTextView.setText(MessageFormat.format("{0} {1}km", getString(R.string.distance), distance));

            int millisReceived = (int) (System.currentTimeMillis() - timeReceived);
            String timeReceivedString = convertTime(millisReceived);

            timeReceivedTextView.setText(MessageFormat.format("{0} {1}", getString(R.string.received), timeReceivedString));
        }
    }

    // Express time received in a human-readable format
    private String convertTime(long timeMillis) {
        long minutes, hours;
        minutes = (timeMillis / 1000) / 60;
        if (minutes > 60){
            hours = minutes / 60;
            return hours + " hours ago";
        } else {
            return minutes + " minutes ago";
        }

    }
}