package com.example.screentime.manager

import android.content.Context
import android.os.PowerManager
import android.util.Log
import com.example.screentime.data.db.AppDatabase
import com.example.screentime.data.model.Session
import com.example.screentime.data.model.SessionStatus
import com.example.screentime.data.preferences.ScreenTimePreferences
import com.example.screentime.service.ScreenMonitoringService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SessionManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val sessionDao = db.sessionDao()
    private val preferences = ScreenTimePreferences.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    val activeSessionFlow: Flow<Session?> = sessionDao.getActiveSessionFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private fun getLocalDateString(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(dateFormatter)
    }

    suspend fun onScreenOn(timestamp: Long = System.currentTimeMillis()) {
        mutex.withLock {
            if (!preferences.getTrackingEnabledSync()) {
                Log.d(TAG, "Screen on ignored: tracking disabled")
                return
            }

            val existingActive = sessionDao.getActiveSession()
            if (existingActive != null) {
                Log.d(TAG, "Screen on ignored: session #${existingActive.id} already active")
                return
            }

            val dateStr = getLocalDateString(timestamp)
            val newSession = Session(
                startTimestamp = timestamp,
                endTimestamp = null,
                durationMillis = 0L,
                startDate = dateStr,
                endDate = null,
                status = SessionStatus.ACTIVE
            )

            val id = sessionDao.insert(newSession)
            Log.d(TAG, "Started new screen session #$id at $timestamp ($dateStr)")
        }
    }

    suspend fun onScreenOff(timestamp: Long = System.currentTimeMillis()) {
        mutex.withLock {
            val activeSession = sessionDao.getActiveSession()
            if (activeSession == null) {
                Log.d(TAG, "Screen off ignored: no active session")
                return
            }

            // Ensure timestamp is at least startTimestamp
            val validEndTimestamp = maxOf(timestamp, activeSession.startTimestamp)
            val duration = validEndTimestamp - activeSession.startTimestamp
            val endDateStr = getLocalDateString(validEndTimestamp)

            val updatedSession = activeSession.copy(
                endTimestamp = validEndTimestamp,
                durationMillis = duration,
                endDate = endDateStr,
                status = SessionStatus.COMPLETED
            )

            sessionDao.update(updatedSession)
            Log.d(TAG, "Completed session #${activeSession.id}, duration: ${duration}ms")
        }
    }

    suspend fun startTracking() {
        mutex.withLock {
            preferences.setTrackingEnabled(true)
            preferences.clearInterruption()
            ScreenMonitoringService.startService(context)

            // App opened or tracking started while screen is already on
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isInteractive = powerManager?.isInteractive ?: true

            if (isInteractive) {
                val existingActive = sessionDao.getActiveSession()
                if (existingActive == null) {
                    val now = System.currentTimeMillis()
                    val dateStr = getLocalDateString(now)
                    val newSession = Session(
                        startTimestamp = now,
                        endTimestamp = null,
                        durationMillis = 0L,
                        startDate = dateStr,
                        endDate = null,
                        status = SessionStatus.ACTIVE
                    )
                    sessionDao.insert(newSession)
                    Log.d(TAG, "Tracking enabled: screen currently active, created session at $now")
                }
            }
        }
    }

    suspend fun pauseTracking() {
        mutex.withLock {
            preferences.setTrackingEnabled(false)

            // Close any active session at pause timestamp
            val activeSession = sessionDao.getActiveSession()
            if (activeSession != null) {
                val now = System.currentTimeMillis()
                val duration = maxOf(0L, now - activeSession.startTimestamp)
                val endDateStr = getLocalDateString(now)
                val updatedSession = activeSession.copy(
                    endTimestamp = now,
                    durationMillis = duration,
                    endDate = endDateStr,
                    status = SessionStatus.COMPLETED
                )
                sessionDao.update(updatedSession)
                Log.d(TAG, "Tracking paused: closed active session #${activeSession.id}")
            }

            ScreenMonitoringService.stopService(context)
        }
    }

    suspend fun stopTracking() {
        pauseTracking()
    }

    /**
     * Reconciles database state when service restarts after termination or reboot.
     */
    suspend fun reconcileState(isReboot: Boolean = false) {
        mutex.withLock {
            val trackingEnabled = preferences.getTrackingEnabledSync()
            if (!trackingEnabled) {
                // Tracking is off, ensure no active sessions are left dangling
                val activeSessions = sessionDao.getAllActiveSessions()
                for (active in activeSessions) {
                    val now = System.currentTimeMillis()
                    sessionDao.update(
                        active.copy(
                            endTimestamp = now,
                            durationMillis = maxOf(0L, now - active.startTimestamp),
                            endDate = getLocalDateString(now),
                            status = SessionStatus.INTERRUPTED
                        )
                    )
                }
                return
            }

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isInteractive = powerManager?.isInteractive ?: true
            val now = System.currentTimeMillis()

            val activeSession = sessionDao.getActiveSession()
            if (activeSession != null) {
                val elapsedSinceStart = now - activeSession.startTimestamp
                if (!isReboot && elapsedSinceStart < 5000L) {
                    // Session was just started (e.g. during recent tracking initialization); let it continue
                    Log.d(TAG, "Reconcile: session #${activeSession.id} was recently started ($elapsedSinceStart ms ago), keeping active")
                    return
                }

                // There was an open session before termination / reboot.
                // Mark previous session as INTERRUPTED so we don't fabricate unverified screen time.
                val cappedEnd = now
                val duration = maxOf(0L, cappedEnd - activeSession.startTimestamp)
                val updated = activeSession.copy(
                    endTimestamp = cappedEnd,
                    durationMillis = duration,
                    endDate = getLocalDateString(cappedEnd),
                    status = SessionStatus.INTERRUPTED
                )
                sessionDao.update(updated)

                val message = if (isReboot) {
                    "Monitoring was interrupted by device reboot. Active session marked incomplete."
                } else {
                    "Monitoring was interrupted by system termination. Previous session closed."
                }
                preferences.setInterrupted(true, message)
                Log.w(TAG, "Reconciled interrupted session #${activeSession.id}: $message")
            }

            // If screen is currently interactive, start a fresh new session
            if (isInteractive) {
                val newSession = Session(
                    startTimestamp = now,
                    endTimestamp = null,
                    durationMillis = 0L,
                    startDate = getLocalDateString(now),
                    endDate = null,
                    status = SessionStatus.ACTIVE
                )
                sessionDao.insert(newSession)
                Log.d(TAG, "Reconciled: screen currently interactive, started new session at $now")
            }
        }
    }

    companion object {
        private const val TAG = "SessionManager"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
