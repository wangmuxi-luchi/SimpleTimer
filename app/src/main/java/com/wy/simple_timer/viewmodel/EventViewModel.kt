package com.wy.simple_timer.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wy.simple_timer.DatabaseManagementService
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryDao
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.EventDao
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.util.Date

class EventViewModel(
    application: Application,
) : AndroidViewModel(application), EventViewModelDaoHelper<Application> {
    protected var events: Flow<List<Event>> = emptyFlow()

    // 实现 EventViewModelDaoHelper 接口
    override val eventDao = MyDatabase.getDatabase(application).eventDao()
    override val _viewModelScope: CoroutineScope
        get() = viewModelScope

//    fun setEvents(events: Flow<List<Event>>) {
//        this.events = events
//    }

    fun refreshEvents(func:(EventDao) -> Flow<List<Event>> ): Flow<List<Event>>{
        events = func(eventDao)
        return events
    }
//    fun getEvents() = events

//    fun insertEvent(event: Event) {
//        // 替换为服务调用
//        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
//            action = "INSERT_EVENT"
//            putExtra("object", event)
//        }
//        getApplication<Application>().startService(intent)
//    }
//
//    fun deleteEvent(eventId: Long) = viewModelScope.launch {
//        eventDao.deleteEvent(eventId)
//    }
//
//    fun updateEvent(
//        eventId: Long,
//        startTime: Date,
//        endTime: Date,
//        categoryId: Long,
//        notes: String
//    ) = viewModelScope.launch {
//        eventDao.updateEvent(
//            eventId,
//            startTime,
//            endTime,
//            categoryId,
//            notes
//        )
//    }
}