package com.wy.simple_timer.viewmodel

import android.app.Application
import android.content.Intent
import com.wy.simple_timer.DatabaseManagementService
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface CategoryViewModelDaoHelper<T> {
    val _viewModelScope: CoroutineScope
    fun <T : Application> getApplication(): T
    val categoryDao: CategoryDao

    fun insertCategory(category: Category) {
        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
            action = "INSERT_CATEGORY"
            putExtra("object", category)
        }
        getApplication<Application>().startService(intent)
    }
    fun updateCategory(
        categoryId: Long,
        name: String,
        color: String,
        position: Int,
        archived: Boolean,
        parentId: Long
    ) {
        val category = Category(
            id = categoryId,
            categoryName = name,
            categoryColor = color,
            position = position,
            archived = archived,
            parentId = parentId
        )

        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
            action = "UPDATE_CATEGORY"
            putExtra("object", category)
        }
        getApplication<Application>().startService(intent)
    }
    fun updateCategory(category: Category) {
        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
            action = "UPDATE_CATEGORY"
            putExtra("object", category)
        }
        getApplication<Application>().startService(intent)
    }
    fun deleteCategory(categoryId: Long) {
        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
            action = "DELETE_CATEGORY"
            putExtra("categoryId", categoryId)
        }
        getApplication<Application>().startService(intent)
    }
    fun getCategoriesByParentId(parentId: Long) = _viewModelScope.launch {
        categoryDao.getCategoriesByParentId(parentId)
    }
    fun getCategoryById(categoryId: Long) = _viewModelScope.launch {
        categoryDao.getCategoriesById(categoryId)
    }


}