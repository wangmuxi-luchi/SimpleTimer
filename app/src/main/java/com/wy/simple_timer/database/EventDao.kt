package com.wy.simple_timer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)

    // 按分类ID删除事件
    @Query("DELETE FROM events WHERE categoryId = :categoryId")
    suspend fun deleteEventsByCategory(categoryId: Long)

    @Query("UPDATE events SET startTime = :startTime, endTime = :endTime, categoryId = :categoryId, notes = :notes WHERE id = :eventId")
    suspend fun updateEvent(
        eventId: Long,
        startTime: Date,
        endTime: Date,
        categoryId: Long,
        notes: String
    )
    @Query("SELECT * FROM events WHERE categoryId = :categoryId")
    fun getEventsByCategory(categoryId: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Long): Flow<Event>
    
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

//    @Query("SELECT * FROM events WHERE strftime('%Y', startTime) = strftime('%Y', :date)")
//    fun getEventsByYear(date: Date): Flow<List<Event>>
//
//    @Query("SELECT * FROM events WHERE strftime('%Y-%m', startTime) = strftime('%Y-%m', :date)")
//    fun getEventsByMonth(date: Date): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE (startTime BETWEEN :start AND :end) OR (endTime BETWEEN :start AND :end)")
    fun getEventsInRange(start: Long, end: Long): Flow<List<Event>>
}

// 在文件底部添加扩展函数
private fun Calendar.resetToStartOfPeriod(unit: Int) {
    when (unit) {
        Calendar.YEAR -> set(Calendar.MONTH, Calendar.JANUARY)
        Calendar.MONTH -> set(Calendar.DAY_OF_MONTH, 1)
        Calendar.DAY_OF_MONTH -> Unit // 已经由调用方设置
    }
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun EventDao.getEventsByDate(date: Date, timeUnit: Int): Flow<List<Event>> {
    val calendar = Calendar.getInstance().apply { time = date }
    calendar.resetToStartOfPeriod(timeUnit)
    val start = calendar.timeInMillis
    
    calendar.apply {
        add(timeUnit, 1)
        add(Calendar.MILLISECOND, -1)
    }
    val end = calendar.timeInMillis
    
    return getEventsInRange(start, end)
}

// 创建具体查询的扩展函数
fun EventDao.getEventsByYear(date: Date) = getEventsByDate(date, Calendar.YEAR)
fun EventDao.getEventsByMonth(date: Date) = getEventsByDate(date, Calendar.MONTH)
fun EventDao.getEventsByDay(date: Date) = getEventsByDate(date, Calendar.DAY_OF_MONTH)



