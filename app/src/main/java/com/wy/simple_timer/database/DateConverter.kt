package com.wy.simple_timer.database

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    /**
     * 将 `Long` 类型的时间戳转换为 `Date` 类型的日期对象。
     *
     * @param value 可空的 `Long` 类型的时间戳，代表自 1970 年 1 月 1 日 00:00:00 UTC 以来的毫秒数。
     * @return 可空的 `Date` 类型的日期对象，如果 `value` 为 `null`，则返回 `null`。
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
