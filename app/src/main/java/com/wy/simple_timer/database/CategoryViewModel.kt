package com.wy.simple_timer.database

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wy.simple_timer.DatabaseManagementService
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
    fun insertCategory(category: Category) {
        // 替换前：
        // categoryDao.insertCategory(category)
        
        // 替换后：
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
    fun deleteCategory(categoryId: Long) {
        // 替换前：
        // categoryDao.deleteCategory(categoryId)
        
        // 替换后：
        val intent = Intent(getApplication(), DatabaseManagementService::class.java).apply {
            action = "DELETE_CATEGORY"
            putExtra("categoryId", categoryId)
        }
        getApplication<Application>().startService(intent)
    }
    fun getCategoriesByParentId(parentId: Long) = viewModelScope.launch {
        categoryDao.getCategoriesByParentId(parentId)
    }
//    getCategoryById
    fun getCategoryById(categoryId: Long) = viewModelScope.launch {
        categoryDao.getCategoriesById(categoryId)
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