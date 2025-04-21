package com.wy.simple_timer.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryWithEventInf
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import java.util.Calendar

// 分类和事件两个流组合输出一个包含时间信息的流，CategoryWithEventInf，使用方法：先调用setDateMode设置需要的数据范围，再调用get_Categories获取输出Flow
class CategoryWEIViewModel(application: Application): AndroidViewModel(application), CategoryViewModelDaoHelper<Application>, EventViewModelDaoHelper<Application> {
    private var _categories: Flow<List<CategoryWithEventInf>> = emptyFlow()
    private var events: Flow<List<Event>> = emptyFlow()
    private var categories : Flow<List<Category>> = emptyFlow()
//    private val categoryDao = MyDatabase.getDatabase(application).categoryDao()

    private var dateMode: Int = -1
    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()

    // 实现 EventViewModelDaoHelper 接口
    override val eventDao = MyDatabase.getDatabase(application).eventDao()
    // 实现 CategoryViewModelDaoHelper 接口
    override val categoryDao = MyDatabase.getDatabase(application).categoryDao()
    override val _viewModelScope: CoroutineScope
        get() = viewModelScope

//    init {
//        events = eventDao.getAllEvents()
//        refreshCategories()
//    }

//    fun get_Categories() = _categories

    companion object {
        const val DATA_MODE_ALL_UNARCHIVED = 0 // 所有未归档
        const val DATA_MODE_ALL_ARCHIVED = 1 // 所有归档
    }
    fun setDateMode(dateMode: Int, startDate: Calendar, endDate: Calendar) {
        if (this.dateMode != dateMode){
            this.dateMode = dateMode
            when (dateMode) {
                DATA_MODE_ALL_UNARCHIVED -> {
                    categories = categoryDao.getUnarchivedCategoriesOrderedByPosition()
                }
                DATA_MODE_ALL_ARCHIVED -> {
                    categories = categoryDao.getArchivedCategoriesOrderedByPosition()
                }
            }
        }
        if (this.startDate.timeInMillis != startDate.timeInMillis || this.endDate.timeInMillis != endDate.timeInMillis){
            this.startDate.timeInMillis = startDate.timeInMillis
            this.endDate.timeInMillis = endDate.timeInMillis
            events = eventDao.getEventsInRange(startDate.timeInMillis, endDate.timeInMillis)
//            Log.d("CategoryWEIViewModel", "setDateMode: ${this.startDate.get(Calendar.DAY_OF_WEEK)} ${this.endDate.get(Calendar.DAY_OF_WEEK)}")
        }
        refreshCategories()
    }

    fun get_Categories(): Flow<List<CategoryWithEventInf>> {
        return _categories
    }



    private fun refreshCategories(){
        _categories = categories.combine(events) { categoriesOne, eventsOne ->
//            Log.d("CategoryWEIViewModel", "refreshCategories: ${categoriesOne.size} ${eventsOne.size}")
            combineCategoryAndEvent(categoriesOne, eventsOne)
        }
    }


//    private fun refresh_Categories(getCategories: (CategoryDao) -> Flow<List<Category>>,
//                                  getEvents: (EventDao) -> Flow<List<Event>>): Flow<List<CategoryWithEventInf>>{
//        val categories = getCategories(categoryDao)
//        val events = getEvents(eventDao)
//        return categories.combine(events) { categoriesOne, eventsOne ->
//            combineCategoryAndEvent(categoriesOne, eventsOne)
//        }
//    }

    private fun combineCategoryAndEvent(categoryList: List<Category>, eventList: List<Event>) : List<CategoryWithEventInf>{
//        Log.d("CategoryWEIViewModel", "combineCategoryAndEvent: ${categoryList.size} ${eventList.size}")
        if (categoryList.isEmpty()) return emptyList()
//        val categoryDurationList = categoryList.map {
//            val eventsOfCategory = eventList.filter { event -> event.categoryId == it.id }
//            eventsOfCategory.sumOf { event -> event.endTime.timeInMillis - event.startTime.timeInMillis }
//        }
//        val maxDuration = if(categoryDurationList.isEmpty()) 0L else categoryDurationList.maxOf { it }
//        val maxDuration = categoryDurationList.maxOf { it }
        val categoryWithEventInfList = categoryList.map {
            val eventsOfCategory = eventList.filter { event -> event.categoryId == it.id }
            // 遍历 Event，计算总时间，总天数，平均每天时间
            if (eventsOfCategory.isEmpty()) return@map CategoryWithEventInf(it, 0, 0, 0, 0f)

            val eventCount = eventsOfCategory.size
            val categoryDuration = eventsOfCategory.sumOf { event -> event.endTime.timeInMillis - event.startTime.timeInMillis }
//            val timeRatioToMax = if (maxDuration == 0L) 0f else categoryDuration.toFloat() / maxDuration.toFloat()
            var totalDays = 0
            var nowday = Calendar.getInstance().apply {  timeInMillis = 0L }
            for (event in eventsOfCategory) {
                if (nowday.isLaterDay(event.startTime)) {
                    totalDays += 1
                    nowday = event.startTime
                }
                if (nowday.isLaterDay(event.endTime)) {
                    totalDays += 1
                    nowday = event.endTime
                }
            }
//            it.position = 0 // 由于position与子项的View无关，因此设为0，防止不必要的刷新
            // 将计算结果添加到 Category 中
            CategoryWithEventInf(it, eventCount, categoryDuration, totalDays, 0f)
        }
        val maxDuration = categoryWithEventInfList.map { it.totalDuration }.maxOf { it }
        if (maxDuration == 0L) return categoryWithEventInfList
        else return categoryWithEventInfList.apply {
            forEach {
                it.timeRatioToMax = it.totalDuration.toFloat() / maxDuration.toFloat()
                Log.d(
                    "CategoryWEIViewModel",
                    "combineCategoryAndEvent: ${it.category.categoryName} " +
                            "${it.eventCount} ${it.totalDuration} ${it.totalDays} ${it.timeRatioToMax}"
                )
            }
        }
    }
}

fun Calendar.isEarlierDay(calendar: Calendar): Boolean {
    return this.get(Calendar.YEAR) > calendar.get(Calendar.YEAR) ||
                this.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                this.get(Calendar.DAY_OF_YEAR) > calendar.get(Calendar.DAY_OF_YEAR)

}
fun Calendar.isLaterDay(calendar: Calendar): Boolean {
    return this.get(Calendar.YEAR) < calendar.get(Calendar.YEAR) ||
                    this.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    this.get(Calendar.DAY_OF_YEAR) < calendar.get(Calendar.DAY_OF_YEAR)

}

