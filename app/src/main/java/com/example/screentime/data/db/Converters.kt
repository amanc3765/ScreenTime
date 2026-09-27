package com.example.screentime.data.db

import androidx.room.TypeConverter
import com.example.screentime.data.model.SessionStatus

class Converters {
    @TypeConverter
    fun fromSessionStatus(status: SessionStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toSessionStatus(value: String?): SessionStatus? {
        return value?.let {
            try {
                SessionStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                SessionStatus.COMPLETED
            }
        }
    }
}
