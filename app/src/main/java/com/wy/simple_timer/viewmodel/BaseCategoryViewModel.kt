package com.wy.simple_timer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Deprecated(
    message = "BaseCategoryViewModel 已弃用，请使用 CategoryViewModel 替代。",
    replaceWith = ReplaceWith("BaseCategoryAdapterRV"),
    level = DeprecationLevel.ERROR
)
abstract class BaseCategoryViewModel(
    application: Application,
    ) : AndroidViewModel(application) {
    private val categoryDao = MyDatabase.getDatabase(application).categoryDao()
    abstract val categories : Flow<List<Category>>// 返回 Flow<List<User>>
    fun insertCategory(category: Category) = viewModelScope.launch {
        categoryDao.insertCategory(category)
    }
    fun updateCategory(
        categoryId: Long,
        name: String,
        color: String,
        position: Int,
        archived: Boolean,
        parentId: Long
    ) = viewModelScope.launch {
        categoryDao.updateCategory(
            categoryId,
            name,
            color,
            position,
            archived,
            parentId
        )
    }
    fun deleteCategory(categoryId: Long) = viewModelScope.launch {
        categoryDao.deleteCategory(categoryId)
    }
    fun getCategoriesByParentId(parentId: Long) = viewModelScope.launch {
        categoryDao.getCategoriesByParentId(parentId)
    }
    fun getCategoriesByParentIdOrderedByPosition(parentId: Long) = viewModelScope.launch {
        categoryDao.getCategoriesByParentIdOrderedByPosition(parentId)
    }
    fun getUnarchivedRootCategoriesOrderedByPosition() = viewModelScope.launch {
        categoryDao.getUnarchivedRootCategoriesOrderedByPosition()
    }
    fun getArchivedRootCategoriesOrderedByPosition() = viewModelScope.launch {
        categoryDao.getArchivedRootCategoriesOrderedByPosition()
    }

}

