package com.wy.simple_timer.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.wy.simple_timer.DatabaseManagementService
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.EventDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

interface EventViewModelDaoHelper<T> {
    val eventDao: EventDao
    val _viewModelScope: CoroutineScope
    fun <T : Application> getApplication(): T


    fun insertEvent(event: Event) {
        // 替换为服务调用
        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
            action = "INSERT_EVENT"
            putExtra("object", event)
        }
        getApplication<Application>().startService(intent)
    }
    fun insertEvent(startTime: Calendar, endTime: Calendar, categoryId: Long, notes: String) {
        // 替换为服务调用
        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
            action = "INSERT_EVENT"
            putExtra("object", Event(
                startTime = startTime,
                endTime = endTime,
                categoryId = categoryId,
                notes = notes
            ))
        }
        getApplication<Application>().startService(intent)
    }

    fun deleteEvent(eventId: Long) = _viewModelScope.launch {
        eventDao.deleteEvent(eventId)
    }

    fun updateEvent(
        eventId: Long,
        startTime: Calendar,
        endTime: Calendar,
        categoryId: Long,
        notes: String
    ) = _viewModelScope.launch {
        eventDao.updateEvent(
            eventId,
            startTime,
            endTime,
            categoryId,
            notes
        )
    }
    fun updateEvent(
        eventId: Long,
        startTime: Date,
        endTime: Date,
        categoryId: Long,
        notes: String
    ){
        _viewModelScope.launch {
            eventDao.updateEvent(
                eventId,
                Calendar.getInstance().apply { time = startTime },
                Calendar.getInstance().apply { time = endTime },
                categoryId,
                notes
            )
        }
    }


}