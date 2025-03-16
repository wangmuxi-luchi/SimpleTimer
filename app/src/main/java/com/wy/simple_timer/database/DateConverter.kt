package com.wy.simple_timer.database

import androidx.room.TypeConverter
import java.util.Calendar
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let { Calendar.getInstance().apply { timeInMillis = it } }
    }

    @TypeConverter
    fun dateToTimestamp(date: Calendar?): Long? {
        return date?.timeInMillis
    }
}
