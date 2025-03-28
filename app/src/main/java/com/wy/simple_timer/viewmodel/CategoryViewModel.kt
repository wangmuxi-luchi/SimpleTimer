package com.wy.simple_timer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryDao
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class CategoryViewModel(application: Application) : AndroidViewModel(application), CategoryViewModelDaoHelper<Application> {
    protected var categories : Flow<List<Category>> = emptyFlow()

    // 实现 CategoryViewModelDaoHelper 接口
    override val categoryDao = MyDatabase.getDatabase(application).categoryDao()
    override val _viewModelScope: CoroutineScope
        get() = viewModelScope

    fun refreshCategories(func:(CategoryDao) -> Flow<List<Category>> ): Flow<List<Category>>{
        categories = func(categoryDao)
        return categories
    }


//    fun getCategories() = categories
//
//    fun insertCategory(category: Category) {
//        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
//            action = "INSERT_CATEGORY"
//            putExtra("object", category)
//        }
//        getApplication<Application>().startService(intent)
//    }
//    fun updateCategory(
//        categoryId: Long,
//        name: String,
//        color: String,
//        position: Int,
//        archived: Boolean,
//        parentId: Long
//    ) {
//        val category = Category(
//            id = categoryId,
//            categoryName = name,
//            categoryColor = color,
//            position = position,
//            archived = archived,
//            parentId = parentId
//        )
//
//        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
//            action = "UPDATE_CATEGORY"
//            putExtra("object", category)
//        }
//        getApplication<Application>().startService(intent)
//    }
//    fun updateCategory(category: Category) {
//        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
//            action = "UPDATE_CATEGORY"
//            putExtra("object", category)
//        }
//        getApplication<Application>().startService(intent)
//    }
//    fun deleteCategory(categoryId: Long) {
//        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
//            action = "DELETE_CATEGORY"
//            putExtra("categoryId", categoryId)
//        }
//        getApplication<Application>().startService(intent)
//    }
//    fun getCategoriesByParentId(parentId: Long) = viewModelScope.launch {
//        categoryDao.getCategoriesByParentId(parentId)
//    }
//    fun getCategoryById(categoryId: Long) = viewModelScope.launch {
//        categoryDao.getCategoriesById(categoryId)
//    }

}
