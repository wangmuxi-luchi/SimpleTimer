package com.wy.simple_timer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.repository.EventRepository
import com.wy.simple_timer.repository.RepositoryProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class EventViewModel(
    application: Application,
) : AndroidViewModel(application) {
    protected var events: Flow<List<Event>> = emptyFlow()

    // 使用 Repository 替代直接 DAO 调用
    private val eventRepository = RepositoryProvider.provideEventRepository(application)

    fun refreshEvents(func: (EventRepository) -> Flow<List<Event>>): Flow<List<Event>> {
        events = func(eventRepository)
        return events
    }

    fun insertEvent(event: Event) = viewModelScope.launch {
        eventRepository.insertEvent(event)
    }

    fun deleteEvent(eventId: Long) = viewModelScope.launch {
        eventRepository.deleteEvent(eventId)
    }

    fun updateEvent(
        eventId: Long,
        startTime: Date,
        endTime: Date,
        categoryId: Long,
        notes: String
    ) = viewModelScope.launch {
        eventRepository.updateEvent(eventId, startTime, endTime, categoryId, notes)
    }
}