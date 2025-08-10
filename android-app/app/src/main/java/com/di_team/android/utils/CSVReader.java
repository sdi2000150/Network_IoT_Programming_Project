package com.di_team.android.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.di_team.android.services.LocationUpdateCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**Loads and parses CSV files with raw location data. Handles each location update through passed callback.*/
public class CSVReader {
    private static final String TAG = "CSVReader";
    private final Context context;
    private final List<String[]> locationQueue;
    private int transmissionIndex = 0;
    private boolean isTransmitting = false;
    private final Handler handler = new Handler();

    public int getTransmissionIndex() { return transmissionIndex; }

    public void setTransmissionIndex(int transmissionIndex) { this.transmissionIndex = transmissionIndex;}

    private final LocationUpdateCallback csvCallback;

    public CSVReader(Context context, LocationUpdateCallback csvCallback) {
        this.context = context;
        this.csvCallback = csvCallback;
        this.locationQueue = new ArrayList<>();

    }

    public void loadCSVFromResources(int[] csvFiles) {
        locationQueue.clear();
        int selectedFile = csvFiles[new Random().nextInt(csvFiles.length)];
        Log.w(TAG, "Selected CSV file: " + context.getResources().getResourceEntryName(selectedFile));

        try (InputStream is = context.getResources().openRawResource(selectedFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean skipHeader = true;

            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                String[] columns = line.split(",");
                if (columns.length >= 2) {
//                    Log.w(TAG, "✅ Loaded row: " + columns[0] + ", " + columns[1]); // Debug log
                    locationQueue.add(columns);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file: " + e.getMessage());
        }
    }

    public void startManualTransmission(int times, int intervalMs) {
        if (locationQueue.isEmpty()) {
            Log.e(TAG, "❌ No location data available in CSV queue.");
            return;
        }

        if (times <= 0) {
            times = locationQueue.size();  // ✅ Ensure times is valid
        }

        isTransmitting = true;
        int rowCount = Math.min(times, locationQueue.size());
        Log.w(TAG, "📡 Starting Manual Transmission - Rows to Transmit: " + rowCount);

        for (int i = 0; i < rowCount; i++) {
            final int currentIndex = transmissionIndex;
            transmissionIndex = (transmissionIndex + 1) % locationQueue.size();
            final String[] row = locationQueue.get(currentIndex);
            int finalI = i;

            handler.postDelayed(() -> {
                if (!isTransmitting) {
                    Log.w(TAG, "✅ Transmission already stopped, skipping.");
                    return;
                }

                double latitude = Double.parseDouble(row[1]);
                double longitude = Double.parseDouble(row[0]);

                Log.w(TAG, "📡 Sending row [" + currentIndex + "]: X=" + longitude + " Y=" + latitude);

                // Prompt TransmissionService to publish location
                if(csvCallback != null)
                    csvCallback.onLocationUpdate(latitude, longitude, true);

                if (finalI + 1 == rowCount) {
                    Log.w(TAG, "🛑 Reached 'times' limit. Stopping transmission.");
                    stopTransmission();
                }
            }, (long) i * intervalMs);
        }
    }

    public void stopTransmission() {
        isTransmitting = false;
        handler.removeCallbacksAndMessages(null); // ✅ Cancel all delayed tasks
        Log.w(TAG, "🛑 Manual CSV Transmission fully stopped.");
    }
}