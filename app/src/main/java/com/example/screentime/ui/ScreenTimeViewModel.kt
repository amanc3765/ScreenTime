package com.example.screentime.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.screentime.ScreenTimeApp
import com.example.screentime.data.model.Session
import com.example.screentime.data.preferences.ScreenTimePreferences
import com.example.screentime.manager.SessionManager
import com.example.screentime.repository.DaySummaryData
import com.example.screentime.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScreenTimeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val isNextDayEnabled: Boolean = false,
    val isToday: Boolean = true,
    val daySummary: DaySummaryData? = null,
    val isTrackingEnabled: Boolean = false,
    val activeSession: Session? = null,
    val isInterrupted: Boolean = false,
    val interruptionMessage: String? = null
)

class ScreenTimeViewModel(
    private val repository: SessionRepository,
    private val sessionManager: SessionManager,
    private val preferences: ScreenTimePreferences
) : ViewModel() {

    private val selectedDateFlow = MutableStateFlow(LocalDate.now())

    // 1-second ticker for live active session update
    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }

    private val daySummaryFlow = combine(
        selectedDateFlow,
        tickerFlow
    ) { date, currentTime ->
        Pair(date, currentTime)
    }.flatMapLatest { (date, currentTime) ->
        repository.observeDaySummary(date = date, currentTimeMillis = currentTime)
    }

    private val baseStateFlow = combine(
        selectedDateFlow,
        daySummaryFlow,
        preferences.isTrackingEnabled
    ) { date, summary, isTracking ->
        Triple(date, summary, isTracking)
    }

    private val monitorStateFlow = combine(
        sessionManager.activeSessionFlow,
        preferences.isInterrupted,
        preferences.interruptionMessage
    ) { activeSession, isInterrupted, interruptionMsg ->
        Triple(activeSession, isInterrupted, interruptionMsg)
    }

    val uiState: StateFlow<ScreenTimeUiState> = combine(
        baseStateFlow,
        monitorStateFlow
    ) { (date, summary, isTracking), (activeSession, isInterrupted, interruptionMsg) ->
        val today = LocalDate.now()
        ScreenTimeUiState(
            selectedDate = date,
            isNextDayEnabled = date.isBefore(today),
            isToday = date.isEqual(today),
            daySummary = summary,
            isTrackingEnabled = isTracking,
            activeSession = activeSession,
            isInterrupted = isInterrupted,
            interruptionMessage = interruptionMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScreenTimeUiState()
    )

    fun onPreviousDay() {
        selectedDateFlow.value = selectedDateFlow.value.minusDays(1)
    }

    fun onNextDay() {
        val current = selectedDateFlow.value
        val today = LocalDate.now()
        if (current.isBefore(today)) {
            selectedDateFlow.value = current.plusDays(1)
        }
    }

    fun onToday() {
        selectedDateFlow.value = LocalDate.now()
    }

    fun toggleTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            if (preferences.getTrackingEnabledSync()) {
                sessionManager.pauseTracking()
            } else {
                sessionManager.startTracking()
            }
        }
    }

    fun dismissInterruption() {
        preferences.clearInterruption()
    }

    fun resetSelectedDay() {
        viewModelScope.launch(Dispatchers.IO) {
            val date = selectedDateFlow.value
            sessionManager.resetDay(date)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = ScreenTimeApp.instance
                return ScreenTimeViewModel(
                    repository = app.repository,
                    sessionManager = app.sessionManager,
                    preferences = app.preferences
                ) as T
            }
        }
    }
}
