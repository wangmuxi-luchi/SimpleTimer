package com.wy.simple_timer.repository

import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.EventDao
import com.wy.simple_timer.utils.resetToStartOfPeriod
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

class EventRepository(private val eventDao: EventDao) {

    // 插入事件
    suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    // 删除事件
    suspend fun deleteEvent(eventId: Long) {
        eventDao.deleteEvent(eventId)
    }

    // 按分类ID删除事件
    suspend fun deleteEventsByCategory(categoryId: Long) {
        eventDao.deleteEventsByCategory(categoryId)
    }

    // 更新事件
    suspend fun updateEvent(
        eventId: Long,
        startTime: Date,
        endTime: Date,
        categoryId: Long,
        notes: String
    ) {
        val startCalendar = Calendar.getInstance().apply { time = startTime }
        val endCalendar = Calendar.getInstance().apply { time = endTime }
        eventDao.updateEvent(eventId, startCalendar, endCalendar, categoryId, notes)
    }

    // 按分类ID查询事件
    fun getEventsByCategory(categoryId: Long): Flow<List<Event>> {
        return eventDao.getEventsByCategory(categoryId)
    }

    // 按ID查询事件
    fun getEventById(eventId: Long): Flow<Event> {
        return eventDao.getEventById(eventId)
    }

    // 查询所有事件
    fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }

    // 按日期范围查询事件（Long参数）
    fun getEventsInRange(start: Long, end: Long): Flow<List<Event>> {
        return eventDao.getEventsInRange(start, end)
    }

    // 按日期范围查询事件（Calendar参数）
    fun getEventsInRange(start: Calendar, end: Calendar): Flow<List<Event>> {
        return eventDao.getEventsInRange(start.timeInMillis, end.timeInMillis)
    }

    // 按日期查询事件
    fun getEventsByDate(date: Date, timeUnit: Int): Flow<List<Event>> {
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

    // 按年查询
    fun getEventsByYear(date: Date): Flow<List<Event>> {
        return getEventsByDate(date, Calendar.YEAR)
    }

    // 按月查询
    fun getEventsByMonth(date: Date): Flow<List<Event>> {
        return getEventsByDate(date, Calendar.MONTH)
    }

    // 按周查询
    fun getEventsByWeek(date: Date): Flow<List<Event>> {
        return getEventsByDate(date, Calendar.WEEK_OF_MONTH)
    }

    // 按日查询
    fun getEventsByDay(date: Date): Flow<List<Event>> {
        return getEventsByDate(date, Calendar.DAY_OF_MONTH)
    }
}