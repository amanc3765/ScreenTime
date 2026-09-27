package com.example.screentime

import android.app.Application
import com.example.screentime.data.db.AppDatabase
import com.example.screentime.data.preferences.ScreenTimePreferences
import com.example.screentime.manager.SessionManager
import com.example.screentime.repository.SessionRepository
import com.example.screentime.service.ScreenMonitoringService

class ScreenTimeApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var repository: SessionRepository
        private set
    lateinit var sessionManager: SessionManager
        private set
    lateinit var preferences: ScreenTimePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        repository = SessionRepository(database.sessionDao())
        preferences = ScreenTimePreferences.getInstance(this)
        sessionManager = SessionManager.getInstance(this)

        // If tracking was previously enabled, ensure service is started
        if (preferences.getTrackingEnabledSync()) {
            ScreenMonitoringService.startService(this)
        }
    }

    companion object {
        lateinit var instance: ScreenTimeApp
            private set
    }
}
