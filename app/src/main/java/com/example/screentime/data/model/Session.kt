package com.example.screentime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimestamp: Long,
    val endTimestamp: Long? = null,
    val durationMillis: Long = 0L,
    val startDate: String, // Format: YYYY-MM-DD
    val endDate: String? = null, // Format: YYYY-MM-DD
    val status: SessionStatus = SessionStatus.ACTIVE
)
