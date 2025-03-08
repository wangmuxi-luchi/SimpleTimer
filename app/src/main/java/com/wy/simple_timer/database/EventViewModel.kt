package com.wy.simple_timer.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

class EventViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private var events : Flow<List<Event>>? = null
    private val eventDao = MyDatabase.getDatabase(application).eventDao()

    fun setEvents(events: Flow<List<Event>>){
        this.events = events
    }
    fun getEventDao() = eventDao
    fun getEvents() = events

    fun insertEvent(event: Event) = viewModelScope.launch {
        eventDao.insertEvent(event)
    }

    fun deleteEvent(eventId: Long) = viewModelScope.launch {
        eventDao.deleteEvent(eventId)
    }

    fun updateEvent(
        eventId: Long,
        startTime: Date,
        endTime: Date,
        categoryId: Long,
        notes: String
    ) = viewModelScope.launch {
        eventDao.updateEvent(
            eventId,
            startTime,
            endTime,
            categoryId,
            notes
        )
    }

//    suspend fun getAllEvents(): List<Event>?{
//        // 启动一个异步任务，获取所有事件
//        return eventDao.getAllEvents().firstOrNull()
//    }
//    suspend fun getAllevents() : Flow<List<Event>> {
//        // 启动一个异步任务，获取所有事件Flow
//        return eventDao.getAllEvents()
//    }
//
//    suspend fun getEventsByYear(date: Date): List<Event>? {
//        // 启动一个异步任务，获取所有事件
//        return eventDao.getEventsByYear(date).firstOrNull()
//    }
//
//    suspend fun getEventsByMonth(date: Date): List<Event>?{
//        // 启动一个异步任务，获取所有事件
//        return eventDao.getEventsByMonth(date).firstOrNull()
//    }
//
//    suspend fun getEventsByDay(date: Date) : List<Event>? {
//        // 启动一个异步任务，获取所有事件
//        return eventDao.getEventsByDay(date).firstOrNull()
//    }
//
//    suspend fun getEventsByCategory(categoryId: Long) : List<Event>? {
//        // 启动一个异步任务，获取所有事件
//        return eventDao.getEventsByCategory(categoryId).firstOrNull()
//    }
}
//    fun getAllEvents() = viewModelScope.async {
//        return@async eventDao.getAllEvents()
//    }
//    fun getEventsByYear(date: Date) = viewModelScope.async {
//        return@async eventDao.getEventsByYear(date)
//    }
//    fun getEventsByMonth(date: Date) = viewModelScope.async {
//        return@async eventDao.getEventsByMonth(date)
//    }
//    fun getEventsByDay(date: Date) = viewModelScope.async {
//        return@async eventDao.getEventsByDay(date)
//    }
//    fun getEventsByCategory(categoryId: Long) = viewModelScope.async {
//        return@async eventDao.getEventsByCategory(categoryId)
//    }

//}
// 自定义 ViewModel 工厂类
//class EventViewModelFactory(
//    private val application: Application,
//    private val type: Int = EventViewModel.allEvent,
//    private val date: Date = Date(),
//    private val categoryId: Long = 0L
//) : ViewModelProvider.Factory {
//    // 重写 create 方法，用于创建 ViewModel 实例
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return EventViewModel(application, type, date, categoryId) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//// 定义单例类，简化 ViewModel 的创建过程
//object EventViewModelProvider {
//    // 定义一个静态方法，用于创建 ViewModel 实例
//    fun getInstance(
//        context: ViewModelStore,
//        application: Application,
//        type: Int = EventViewModel.allEvent,
//        date: Date = Date(),
//        categoryId: Long = 0L
//    ): EventViewModel {
//        // 创建 ViewModelProvider 对象，并传入 ViewModel 工厂类
//        val viewModelProvider = ViewModelProvider(
//            context,
//            EventViewModelFactory(
//                application,
//                type,
//                date,
//                categoryId
//            )
//        )[EventViewModel::class.java]
//        // 返回 ViewModel 实例
//        return viewModelProvider
//    }
//    fun getInstance(
//        context: ViewModelStoreOwner,
//        application: Application,
//        type: Int = EventViewModel.allEvent,
//        date: Date = Date(),
//        categoryId: Long = 0L
//        ): EventViewModel {
//        // 创建 ViewModelProvider 对象，并传入 ViewModel 工厂类
//        val viewModelProvider = ViewModelProvider(
//            context,
//            EventViewModelFactory(
//                application,
//                type,
//                date,
//                categoryId
//            )
//        )[EventViewModel::class.java]
//        // 返回 ViewModel 实例
//        return viewModelProvider
//    }
//}