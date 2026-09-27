package com.example.screentime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.screentime.manager.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScreenStateReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val timestamp = System.currentTimeMillis()
        val sessionManager = SessionManager.getInstance(context)

        Log.d(TAG, "Received screen broadcast action: $action at $timestamp")

        when (action) {
            Intent.ACTION_SCREEN_ON,
            Intent.ACTION_USER_PRESENT -> {
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        sessionManager.onScreenOn(timestamp)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        sessionManager.onScreenOff(timestamp)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "ScreenStateReceiver"
    }
}
