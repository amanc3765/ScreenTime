package com.example.screentime.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.screentime.MainActivity
import com.example.screentime.manager.SessionManager
import com.example.screentime.receiver.ScreenStateReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ScreenMonitoringService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var screenReceiver: ScreenStateReceiver? = null
    private var isReceiverRegistered = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ScreenMonitoringService onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val isReboot = intent?.getBooleanExtra(EXTRA_IS_REBOOT, false) ?: false
        Log.d(TAG, "ScreenMonitoringService onStartCommand action=$action, isReboot=$isReboot")

        if (action == ACTION_STOP) {
            serviceScope.launch {
                SessionManager.getInstance(applicationContext).pauseTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            return START_NOT_STICKY
        }

        // Start Foreground with persistent notification
        startForegroundServiceWithNotification()

        // Register runtime screen state receiver if not registered
        registerScreenReceiver()

        val isRecovery = (intent == null) || isReboot
        if (isRecovery) {
            serviceScope.launch {
                SessionManager.getInstance(applicationContext).reconcileState(isReboot = isReboot)
            }
        }

        return START_STICKY
    }

    private fun registerScreenReceiver() {
        if (!isReceiverRegistered) {
            screenReceiver = ScreenStateReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            registerReceiver(screenReceiver, filter)
            isReceiverRegistered = true
            Log.d(TAG, "ScreenStateReceiver registered dynamically")
        }
    }

    private fun unregisterScreenReceiver() {
        if (isReceiverRegistered && screenReceiver != null) {
            try {
                unregisterReceiver(screenReceiver)
                Log.d(TAG, "ScreenStateReceiver unregistered")
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
            isReceiverRegistered = false
            screenReceiver = null
        }
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildPersistentNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, notification, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, ScreenMonitoringService::class.java).apply {
            action = ACTION_STOP
        }
        val pausePendingIntent = PendingIntent.getService(
            this,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Time Tracking Active")
            .setContentText("Monitoring screen-state sessions continuously.")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Time Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing persistent notification for screen session tracking."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ScreenMonitoringService onDestroy")
        unregisterScreenReceiver()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "ScreenMonitoringService"
        const val CHANNEL_ID = "screen_time_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.screentime.ACTION_START"
        const val ACTION_STOP = "com.example.screentime.ACTION_STOP"
        const val EXTRA_IS_REBOOT = "extra_is_reboot"

        fun startService(context: Context, isReboot: Boolean = false) {
            val intent = Intent(context, ScreenMonitoringService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_IS_REBOOT, isReboot)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ScreenMonitoringService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
