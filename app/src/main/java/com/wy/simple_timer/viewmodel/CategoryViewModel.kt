package com.wy.simple_timer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.repository.CategoryRepository
import com.wy.simple_timer.repository.RepositoryProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    protected var categories: Flow<List<Category>> = emptyFlow()

    // 使用 Repository 替代直接 DAO 调用
    private val categoryRepository = RepositoryProvider.provideCategoryRepository(application)

    fun refreshCategories(func: (CategoryRepository) -> Flow<List<Category>>): Flow<List<Category>> {
        categories = func(categoryRepository)
        return categories
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        categoryRepository.insertCategory(category)
    }

    fun updateCategory(
        categoryId: Long,
        name: String,
        color: String,
        position: Int,
        archived: Boolean,
        parentId: Long
    ) = viewModelScope.launch {
        categoryRepository.updateCategory(categoryId, name, color, position, archived, parentId)
    }

    fun deleteCategory(categoryId: Long) = viewModelScope.launch {
        categoryRepository.deleteCategory(categoryId)
    }
}