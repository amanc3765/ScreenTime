package com.example.screentime.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenTimePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("screen_time_prefs", Context.MODE_PRIVATE)

    private val _isTrackingEnabled = MutableStateFlow(prefs.getBoolean(KEY_TRACKING_ENABLED, false))
    val isTrackingEnabled: StateFlow<Boolean> = _isTrackingEnabled.asStateFlow()

    private val _isInterrupted = MutableStateFlow(prefs.getBoolean(KEY_INTERRUPTED, false))
    val isInterrupted: StateFlow<Boolean> = _isInterrupted.asStateFlow()

    private val _interruptionMessage = MutableStateFlow(prefs.getString(KEY_INTERRUPTION_MSG, null))
    val interruptionMessage: StateFlow<String?> = _interruptionMessage.asStateFlow()

    fun setTrackingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TRACKING_ENABLED, enabled).apply()
        _isTrackingEnabled.value = enabled
    }

    fun getTrackingEnabledSync(): Boolean {
        return prefs.getBoolean(KEY_TRACKING_ENABLED, false)
    }

    fun setInterrupted(interrupted: Boolean, message: String? = null) {
        prefs.edit()
            .putBoolean(KEY_INTERRUPTED, interrupted)
            .putString(KEY_INTERRUPTION_MSG, message)
            .apply()
        _isInterrupted.value = interrupted
        _interruptionMessage.value = message
    }

    fun clearInterruption() {
        setInterrupted(false, null)
    }

    companion object {
        private const val KEY_TRACKING_ENABLED = "key_tracking_enabled"
        private const val KEY_INTERRUPTED = "key_interrupted"
        private const val KEY_INTERRUPTION_MSG = "key_interruption_msg"

        @Volatile
        private var INSTANCE: ScreenTimePreferences? = null

        fun getInstance(context: Context): ScreenTimePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScreenTimePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
