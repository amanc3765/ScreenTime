package com.example.screentime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.screentime.data.preferences.ScreenTimePreferences
import com.example.screentime.service.ScreenMonitoringService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = ScreenTimePreferences.getInstance(context)
            if (prefs.getTrackingEnabledSync()) {
                Log.d(TAG, "Tracking was enabled before reboot. Restarting ScreenMonitoringService.")
                ScreenMonitoringService.startService(context, isReboot = true)
            } else {
                Log.d(TAG, "Tracking was not enabled. Skipping service startup.")
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
