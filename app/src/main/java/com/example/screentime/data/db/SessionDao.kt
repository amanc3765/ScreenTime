package com.example.screentime.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.screentime.data.model.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: Session): Long

    @Update
    fun update(session: Session): Int

    @Query("SELECT * FROM sessions WHERE status = 'ACTIVE' ORDER BY startTimestamp DESC LIMIT 1")
    fun getActiveSession(): Session?

    @Query("SELECT * FROM sessions WHERE status = 'ACTIVE' ORDER BY startTimestamp DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<Session?>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSessionById(id: Long): Session?

    @Query("SELECT * FROM sessions WHERE startDate = :date ORDER BY startTimestamp ASC")
    fun getSessionsForStartDate(date: String): Flow<List<Session>>

    @Query("""
        SELECT * FROM sessions 
        WHERE startTimestamp < :rangeEnd 
          AND (endTimestamp IS NULL OR endTimestamp > :rangeStart)
        ORDER BY startTimestamp ASC
    """)
    fun getSessionsIntersectingRange(rangeStart: Long, rangeEnd: Long): Flow<List<Session>>

    @Query("""
        SELECT * FROM sessions 
        WHERE startTimestamp < :rangeEnd 
          AND (endTimestamp IS NULL OR endTimestamp > :rangeStart)
        ORDER BY startTimestamp ASC
    """)
    fun getSessionsIntersectingRangeSync(rangeStart: Long, rangeEnd: Long): List<Session>

    @Query("SELECT * FROM sessions ORDER BY startTimestamp ASC")
    fun getAllSessionsFlow(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE status = 'ACTIVE'")
    fun getAllActiveSessions(): List<Session>
}
