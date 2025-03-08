package com.wy.simple_timer.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private var categories : Flow<List<Category>>? = null
    private val categoryDao = MyDatabase.getDatabase(application).categoryDao()

    fun setCategories(categories: Flow<List<Category>>){
        this.categories = categories
    }
    fun getCategories() = categories
    fun getCategoryDao() = categoryDao
//    abstract val categories : Flow<List<Category>>// 返回 Flow<List<User>>
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
//    fun getCategoriesByParentIdOrderedByPosition(parentId: Long) = viewModelScope.launch {
//        categoryDao.getCategoriesByParentIdOrderedByPosition(parentId)
//    }
//    fun getUnarchivedRootCategoriesOrderedByPosition() = viewModelScope.launch {
//        categoryDao.getUnarchivedRootCategoriesOrderedByPosition()
//    }
//    fun getArchivedRootCategoriesOrderedByPosition() = viewModelScope.launch {
//        categoryDao.getArchivedRootCategoriesOrderedByPosition()
//    }

}

// 自定义的 ViewModel 工厂类
//class CategoryViewModelFactory(
//    private val application: Application,
//    private val type: Int = CategoryViewModel.allCategory,
//    private val id: Long = 0L
//): ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
//            return CategoryViewModel(application, type, id) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//
//}