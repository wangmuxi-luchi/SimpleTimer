package com.wy.simple_timer.viewmodel

import android.app.Application
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryWithEventInf
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow

class CategoryWEIViewModel(application: Application): CategoryViewModel(application) {
    private var _categories: Flow<List<CategoryWithEventInf>> = emptyFlow()
    private var events: Flow<List<Event>> = emptyFlow()
    private val eventDao = MyDatabase.getDatabase(application).eventDao()

    init {
        events = eventDao.getAllEvents()
        refreshCategories()
    }

    fun get_Categories() = _categories



    private fun refreshCategories(){
        _categories = categories.combine(events) { categoriesOne, eventsOne ->
            combineCategoryAndEvent(categoriesOne, eventsOne)
        }
    }

    private fun combineCategoryAndEvent(categoryList: List<Category>, eventList: List<Event>) : List<CategoryWithEventInf>{
        val temCategoryList = mutableListOf<CategoryWithEventInf>()
        categoryList.forEach {
            val eventsOfCategory = eventList.filter { event -> event.categoryId == it.id }
            // 遍历 Event，计算总时间，总天数，平均每天时间
            val eventCount = eventsOfCategory.size
            var totalDuration = 0L
            var totalDays = 0
            var nowday = 0L
            for (event in eventsOfCategory) {
                totalDuration += (event.endTime.time - event.startTime.time)
                val startday = event.startTime.time / 1000 / 60 / 60 / 24
                val endday = event.endTime.time / 1000 / 60 / 60 / 24
                if (startday > nowday) {
                    totalDays += 1
                    nowday = startday
                }
                if (endday > nowday) {
                    totalDays += 1
                    nowday = endday
                }
            }
            // 将计算结果添加到 Category 中
            val category = CategoryWithEventInf(it, eventCount, totalDuration, totalDays)
            temCategoryList.add(category)
        }
        return temCategoryList
    }

}

