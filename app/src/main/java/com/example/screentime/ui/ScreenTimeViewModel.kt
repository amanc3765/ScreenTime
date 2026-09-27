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
import com.example.screentime.repository.SessionUiItem
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

enum class SessionSortColumn {
    START_TIME,
    END_TIME,
    DURATION
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

data class ScreenTimeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val isNextDayEnabled: Boolean = false,
    val isToday: Boolean = true,
    val daySummary: DaySummaryData? = null,
    val sortedSessions: List<SessionUiItem> = emptyList(),
    val sortColumn: SessionSortColumn = SessionSortColumn.START_TIME,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
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
    private val sortColumnFlow = MutableStateFlow(SessionSortColumn.START_TIME)
    private val sortOrderFlow = MutableStateFlow(SortOrder.ASCENDING)

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

    private val sortedSummaryFlow = combine(
        daySummaryFlow,
        sortColumnFlow,
        sortOrderFlow
    ) { summary, column, order ->
        val rawSessions = summary.sessions
        val sorted = when (column) {
            SessionSortColumn.START_TIME -> {
                if (order == SortOrder.ASCENDING) {
                    rawSessions.sortedBy { it.startTimestamp }
                } else {
                    rawSessions.sortedByDescending { it.startTimestamp }
                }
            }
            SessionSortColumn.END_TIME -> {
                if (order == SortOrder.ASCENDING) {
                    rawSessions.sortedWith(
                        compareBy<SessionUiItem> { it.isActive }
                            .thenBy { it.endTimestamp ?: Long.MAX_VALUE }
                    )
                } else {
                    rawSessions.sortedWith(
                        compareByDescending<SessionUiItem> { it.isActive }
                            .thenByDescending { it.endTimestamp ?: Long.MIN_VALUE }
                    )
                }
            }
            SessionSortColumn.DURATION -> {
                if (order == SortOrder.ASCENDING) {
                    rawSessions.sortedBy { it.rawDurationMillis }
                } else {
                    rawSessions.sortedByDescending { it.rawDurationMillis }
                }
            }
        }
        Triple(summary, sorted, Pair(column, order))
    }

    private val baseStateFlow = combine(
        selectedDateFlow,
        sortedSummaryFlow,
        preferences.isTrackingEnabled
    ) { date, (summary, sorted, sortInfo), isTracking ->
        StateBundle(date, summary, sorted, sortInfo.first, sortInfo.second, isTracking)
    }

    private data class StateBundle(
        val date: LocalDate,
        val summary: DaySummaryData,
        val sortedSessions: List<SessionUiItem>,
        val sortColumn: SessionSortColumn,
        val sortOrder: SortOrder,
        val isTracking: Boolean
    )

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
    ) { bundle, (activeSession, isInterrupted, interruptionMsg) ->
        val today = LocalDate.now()
        ScreenTimeUiState(
            selectedDate = bundle.date,
            isNextDayEnabled = bundle.date.isBefore(today),
            isToday = bundle.date.isEqual(today),
            daySummary = bundle.summary,
            sortedSessions = bundle.sortedSessions,
            sortColumn = bundle.sortColumn,
            sortOrder = bundle.sortOrder,
            isTrackingEnabled = bundle.isTracking,
            activeSession = activeSession,
            isInterrupted = isInterrupted,
            interruptionMessage = interruptionMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScreenTimeUiState()
    )

    fun onSortColumnClick(column: SessionSortColumn) {
        if (sortColumnFlow.value == column) {
            // Toggle order
            sortOrderFlow.value = if (sortOrderFlow.value == SortOrder.ASCENDING) {
                SortOrder.DESCENDING
            } else {
                SortOrder.ASCENDING
            }
        } else {
            sortColumnFlow.value = column
            sortOrderFlow.value = SortOrder.ASCENDING
        }
    }

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
