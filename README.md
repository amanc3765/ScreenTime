# ScreenTime - Android Screen Time Session Tracker

A lightweight, privacy-focused Android application built with **Kotlin, Jetpack Compose, and Room** that automatically tracks device screen time through individual sessions (screen on/off transitions).

---

## Features

- **Continuous Background Monitoring**: Uses an Android Foreground Service with a persistent ongoing notification to monitor `ACTION_SCREEN_ON` and `ACTION_SCREEN_OFF` events across the entire device.
- **Session Lifecycle & Deduplication**:
  - Automatic session creation when the screen turns on and becomes interactive.
  - Automatic session completion when the screen turns off.
  - Deduplication prevents duplicate sessions from repeated broadcasts.
  - Active sessions display live elapsed duration in real time.
- **Midnight Session Splitting**: Sessions spanning midnight (e.g. 11:55 PM to 12:10 AM) are stored as continuous sessions and allocated proportionally to each calendar day's total screen time.
- **Recovery & Edge Cases**:
  - Auto-restart after device reboot (`RECEIVE_BOOT_COMPLETED`) if tracking was enabled.
  - Detects monitoring interruptions (e.g., system process termination) and flags incomplete sessions.
  - Safe pause and resume controls without losing previous records.
- **Privacy Guarantee**: Exclusively tracks hardware screen state transitions. Does not track individual apps, keystrokes, personal data, or network activity. All data is stored locally in Room.

---

## Architecture Overview

```
com.example.screentime/
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt          // Room Database
│   │   ├── Converters.kt           // TypeConverters for SessionStatus
│   │   └── SessionDao.kt            // Data access object for sessions
│   ├── model/
│   │   ├── Session.kt               // Room Entity
│   │   └── SessionStatus.kt         // ACTIVE, COMPLETED, INTERRUPTED
│   └── preferences/
│       └── ScreenTimePreferences.kt // SharedPreferences wrapper for tracking state
├── manager/
│   └── SessionManager.kt            // Core lifecycle, deduplication & reconciliation
├── receiver/
│   ├── BootReceiver.kt              // Restores monitoring after reboot
│   └── ScreenStateReceiver.kt       // Dynamic receiver for screen on/off
├── repository/
│   └── SessionRepository.kt         // Aggregations, daily totals, midnight splitting
├── service/
│   └── ScreenMonitoringService.kt   // Foreground service with ongoing notification
├── ui/
│   ├── DashboardScreen.kt           // Jetpack Compose Material 3 UI
│   └── ScreenTimeViewModel.kt       // UI State & 1-second active session ticker
├── MainActivity.kt                  // Single Activity entry point
└── ScreenTimeApp.kt                 // Application class & dependency initialization
```

---

## Building and Testing

### 1. Build the APK
To assemble the debug APK from the terminal:
```bash
./gradlew assembleDebug
```
The resulting APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Unit Tests
To run unit tests (verifying midnight splitting, duration formatting, and active session calculations):
```bash
./gradlew test
```

### 3. Install on a Physical Android Device
1. Enable **Developer Options** and **USB Debugging** on your Android phone.
2. Connect your device via USB (or wireless ADB).
3. Verify connection:
   ```bash
   adb devices
   ```
4. Install and launch the application:
   ```bash
   ./gradlew installDebug
   ```

### 4. Testing Background Tracking on Physical Device
1. Open **Screen Time** and tap **Resume** / **Start Tracking**.
2. When prompted on Android 13+, allow notification permission to display the persistent notification.
3. Turn off your phone screen (press Power button).
4. Wait 15–30 seconds.
5. Turn the screen back on and unlock the device.
6. Open the app: you will see the completed session with start time, end time, and duration recorded in the session table and added to the daily summary.
7. While using other apps or remaining on the home screen, notice the ongoing notification indicating tracking is active.
