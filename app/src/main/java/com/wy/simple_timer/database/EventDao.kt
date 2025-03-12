package com.wy.simple_timer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
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

    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE strftime('%Y', startTime) = strftime('%Y', :date)")
    fun getEventsByYear(date: Date): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE strftime('%Y-%m', startTime) = strftime('%Y-%m', :date)")
    fun getEventsByMonth(date: Date): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE strftime('%Y-%m-%d', endTime) = strftime('%Y-%m-%d', :date)")
    fun getEventsByDay(date: Date): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE categoryId = :categoryId")
    fun getEventsByCategory(categoryId: Long): Flow<List<Event>>
}
