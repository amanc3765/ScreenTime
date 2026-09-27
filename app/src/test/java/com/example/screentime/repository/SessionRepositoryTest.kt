package com.example.screentime.repository

import com.example.screentime.data.db.SessionDao
import com.example.screentime.data.model.Session
import com.example.screentime.data.model.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class SessionRepositoryTest {

    private val fakeDao = object : SessionDao {
        override fun insert(session: Session): Long = 1L
        override fun update(session: Session): Int = 1
        override fun getActiveSession(): Session? = null
        override fun getActiveSessionFlow(): Flow<Session?> = emptyFlow()
        override fun getSessionById(id: Long): Session? = null
        override fun getSessionsForStartDate(date: String): Flow<List<Session>> = emptyFlow()
        override fun getSessionsIntersectingRange(rangeStart: Long, rangeEnd: Long): Flow<List<Session>> = emptyFlow()
        override fun getSessionsIntersectingRangeSync(rangeStart: Long, rangeEnd: Long): List<Session> = emptyList()
        override fun getAllSessionsFlow(): Flow<List<Session>> = emptyFlow()
        override fun getAllActiveSessions(): List<Session> = emptyList()
    }

    private val repository = SessionRepository(fakeDao)
    private val zoneId = ZoneId.of("UTC")

    @Test
    fun testFormatDuration() {
        assertEquals("0m 0s", SessionRepository.formatDuration(0L))
        assertEquals("0m 30s", SessionRepository.formatDuration(30_000L))
        assertEquals("25m 0s", SessionRepository.formatDuration(25 * 60 * 1000L))
        assertEquals("155m 0s", SessionRepository.formatDuration((2 * 3600 + 35 * 60) * 1000L))
        assertEquals("60m 0s", SessionRepository.formatDuration(3600 * 1000L))
        assertEquals("2m 15s", SessionRepository.formatDuration((2 * 60 + 15) * 1000L))
    }

    @Test
    fun testMidnightCrossingSessionAllocation() {
        // Day 1: 2026-09-27
        // Day 2: 2026-09-28
        val day1 = LocalDate.of(2026, 9, 27)
        val day2 = LocalDate.of(2026, 9, 28)

        val day1Start = day1.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val day1End = day2.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val day2End = day2.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        // Session starts at 23:55 on Day 1 (5 mins before midnight)
        // Session ends at 00:10 on Day 2 (10 mins after midnight)
        val sessionStart = day1End - (5 * 60 * 1000L)
        val sessionEnd = day1End + (10 * 60 * 1000L)
        val totalDuration = 15 * 60 * 1000L

        val crossMidnightSession = Session(
            id = 1L,
            startTimestamp = sessionStart,
            endTimestamp = sessionEnd,
            durationMillis = totalDuration,
            startDate = "2026-09-27",
            endDate = "2026-09-28",
            status = SessionStatus.COMPLETED
        )

        // Day 1 calculation
        val day1Summary = repository.calculateDaySummary(
            date = day1,
            startOfDay = day1Start,
            endOfDay = day1End,
            intersectingSessions = listOf(crossMidnightSession),
            startOnDateSessions = listOf(crossMidnightSession),
            zoneId = zoneId,
            currentTimeMillis = sessionEnd
        )

        // Day 1 should have exactly 5 minutes allocated
        assertEquals(5 * 60 * 1000L, day1Summary.totalDurationMillis)
        assertEquals("5m 0s", day1Summary.formattedTotalDuration)
        // Counted once on its start date
        assertEquals(1, day1Summary.totalSessionsCount)
        assertEquals(1, day1Summary.sessions.size)
        assertEquals(1, day1Summary.sessions[0].sequenceNumber)

        // Day 2 calculation
        val day2Summary = repository.calculateDaySummary(
            date = day2,
            startOfDay = day1End,
            endOfDay = day2End,
            intersectingSessions = listOf(crossMidnightSession),
            startOnDateSessions = emptyList(), // did not start on day 2
            zoneId = zoneId,
            currentTimeMillis = sessionEnd
        )

        // Day 2 should have exactly 10 minutes allocated
        assertEquals(10 * 60 * 1000L, day2Summary.totalDurationMillis)
        assertEquals("10m 0s", day2Summary.formattedTotalDuration)
        // Should not be counted in Day 2 session count
        assertEquals(0, day2Summary.totalSessionsCount)
        assertEquals(0, day2Summary.sessions.size)
    }

    @Test
    fun testActiveSessionCalculation() {
        val today = LocalDate.of(2026, 9, 27)
        val dayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val sessionStart = dayStart + (3600 * 1000L) // 1 hour into day
        val currentTime = sessionStart + (12 * 60 * 1000L) // 12 mins later

        val activeSession = Session(
            id = 42L,
            startTimestamp = sessionStart,
            endTimestamp = null,
            durationMillis = 0L,
            startDate = "2026-09-27",
            endDate = null,
            status = SessionStatus.ACTIVE
        )

        val summary = repository.calculateDaySummary(
            date = today,
            startOfDay = dayStart,
            endOfDay = dayEnd,
            intersectingSessions = listOf(activeSession),
            startOnDateSessions = listOf(activeSession),
            zoneId = zoneId,
            currentTimeMillis = currentTime
        )

        assertEquals(1, summary.totalSessionsCount)
        assertEquals(12 * 60 * 1000L, summary.totalDurationMillis)
        assertEquals("12m 0s", summary.formattedTotalDuration)
        assertEquals(1, summary.sessions.size)
        assertTrue(summary.sessions[0].isActive)
        assertEquals("In progress", summary.sessions[0].endTimeFormatted)
    }
}
