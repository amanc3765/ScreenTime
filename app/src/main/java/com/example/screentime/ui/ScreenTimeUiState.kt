package com.example.screentime.ui

import com.example.screentime.data.model.Session
import com.example.screentime.repository.DaySummaryData
import com.example.screentime.repository.SessionUiItem
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
