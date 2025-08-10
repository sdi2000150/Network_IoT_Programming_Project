package com.di_team.android.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**Receives broadcasts regarding stopping TransmissionService*/
public class StopServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ServiceStopReceiver", "🛑 Stop button clicked, stopping service...");
        context.stopService(new Intent(context, TransmissionService.class));
    }
}