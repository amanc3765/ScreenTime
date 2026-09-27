package com.example.screentime.repository

import com.example.screentime.data.db.SessionDao
import com.example.screentime.data.model.Session
import com.example.screentime.data.model.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SessionUiItem(
    val id: Long,
    val sequenceNumber: Int,
    val startTimeFormatted: String,
    val endTimeFormatted: String,
    val durationFormatted: String,
    val rawDurationMillis: Long,
    val isActive: Boolean,
    val isInterrupted: Boolean
)

data class DaySummaryData(
    val date: LocalDate,
    val totalSessionsCount: Int,
    val totalDurationMillis: Long,
    val formattedTotalDuration: String,
    val sessions: List<SessionUiItem>
)

class SessionRepository(private val sessionDao: SessionDao) {

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getActiveSessionFlow(): Flow<Session?> = sessionDao.getActiveSessionFlow()

    fun getAllSessionsFlow(): Flow<List<Session>> = sessionDao.getAllSessionsFlow()

    /**
     * Observe sessions and aggregate summary for a specific calendar date in local timezone.
     */
    fun observeDaySummary(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Flow<DaySummaryData> {
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        return combine(
            sessionDao.getSessionsIntersectingRange(startOfDay, endOfDay),
            sessionDao.getSessionsForStartDate(date.format(dateFormatter))
        ) { intersectingSessions, startOnDateSessions ->
            calculateDaySummary(date, startOfDay, endOfDay, intersectingSessions, startOnDateSessions, zoneId, currentTimeMillis)
        }
    }

    fun calculateDaySummary(
        date: LocalDate,
        startOfDay: Long,
        endOfDay: Long,
        intersectingSessions: List<Session>,
        startOnDateSessions: List<Session>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): DaySummaryData {
        // 1. Calculate cumulative duration allocated to this calendar day
        var totalDayDurationMillis = 0L
        for (session in intersectingSessions) {
            val sessionEnd = session.endTimestamp ?: currentTimeMillis
            val overlapStart = maxOf(session.startTimestamp, startOfDay)
            val overlapEnd = minOf(sessionEnd, endOfDay)
            if (overlapEnd > overlapStart) {
                totalDayDurationMillis += (overlapEnd - overlapStart)
            }
        }

        // 2. Count sessions starting on this day (including active session if started today)
        val totalSessionsCount = startOnDateSessions.size

        // 3. Prepare table items for sessions that started on this day, sorted earliest first
        val sortedSessions = startOnDateSessions.sortedBy { it.startTimestamp }
        val uiItems = sortedSessions.mapIndexed { index, session ->
            val isActive = session.status == SessionStatus.ACTIVE
            val isInterrupted = session.status == SessionStatus.INTERRUPTED

            val startTimeFormatted = Instant.ofEpochMilli(session.startTimestamp)
                .atZone(zoneId)
                .format(timeFormatter)

            val endTimeFormatted = if (isActive) {
                "In progress"
            } else if (session.endTimestamp != null) {
                Instant.ofEpochMilli(session.endTimestamp)
                    .atZone(zoneId)
                    .format(timeFormatter)
            } else {
                "Incomplete"
            }

            val sessionDuration = if (isActive) {
                maxOf(0L, currentTimeMillis - session.startTimestamp)
            } else {
                session.durationMillis
            }

            SessionUiItem(
                id = session.id,
                sequenceNumber = index + 1,
                startTimeFormatted = startTimeFormatted,
                endTimeFormatted = endTimeFormatted,
                durationFormatted = formatDuration(sessionDuration, showSecondsForShort = isActive),
                rawDurationMillis = sessionDuration,
                isActive = isActive,
                isInterrupted = isInterrupted
            )
        }

        return DaySummaryData(
            date = date,
            totalSessionsCount = totalSessionsCount,
            totalDurationMillis = totalDayDurationMillis,
            formattedTotalDuration = formatDuration(totalDayDurationMillis, showSecondsForShort = false),
            sessions = uiItems
        )
    }

    companion object {
        fun formatDuration(durationMillis: Long, showSecondsForShort: Boolean = false): String {
            if (durationMillis <= 0L) return "0m"

            val totalSeconds = durationMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return when {
                hours > 0 -> {
                    if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
                }
                minutes > 0 -> {
                    if (showSecondsForShort && seconds > 0) "${minutes}m ${seconds}s" else "${minutes}m"
                }
                showSecondsForShort -> {
                    "${seconds}s"
                }
                else -> {
                    // Under 1 minute formatted as "0m" unless showSecondsForShort
                    if (totalSeconds > 0) "<1m" else "0m"
                }
            }
        }
    }
}
