package com.wy.simple_timer.repository

import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryDao
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    // 插入分类
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    // 删除分类
    suspend fun deleteCategory(categoryId: Long) {
        categoryDao.deleteCategory(categoryId)
    }

    // 更新分类
    suspend fun updateCategory(
        categoryId: Long,
        name: String,
        color: String,
        position: Int,
        archived: Boolean,
        parentId: Long
    ): Int? {
        return categoryDao.updateCategory(categoryId, name, color, position, archived, parentId)
    }

    // 插入默认分类
    fun insertDefaultCategory(name: String, color: String, position: Int, archived: Boolean, parentId: Long): Long {
        return categoryDao.insertDefaultCategory(name, color, position, archived, parentId)
    }

    // 按ID查询分类列表
    fun getCategoriesById(categoryId: Long): Flow<List<Category>> {
        return categoryDao.getCategoriesById(categoryId)
    }

    // 按ID查询分类
    fun getCategoryById(categoryId: Long): Flow<Category> {
        return categoryDao.getCategoryById(categoryId)
    }

    // 获取所有未归档的一级分类
    fun getUnarchivedRootCategoriesOrderedByPosition(): Flow<List<Category>> {
        return categoryDao.getUnarchivedRootCategoriesOrderedByPosition()
    }

    // 获取所有归档的一级分类
    fun getArchivedRootCategoriesOrderedByPosition(): Flow<List<Category>> {
        return categoryDao.getArchivedRootCategoriesOrderedByPosition()
    }

    // 获取所有未归档的分类
    fun getUnarchivedCategoriesOrderedByPosition(): Flow<List<Category>> {
        return categoryDao.getUnarchivedCategoriesOrderedByPosition()
    }

    // 获取所有归档的分类
    fun getArchivedCategoriesOrderedByPosition(): Flow<List<Category>> {
        return categoryDao.getArchivedCategoriesOrderedByPosition()
    }

    // 获取指定 parentId 的子分类
    fun getCategoriesByParentId(parentId: Long): Flow<List<Category>> {
        return categoryDao.getCategoriesByParentId(parentId)
    }

    // 获取指定 parentId 的子分类（按position排序）
    fun getCategoriesByParentIdOrderedByPosition(parentId: Long): Flow<List<Category>> {
        return categoryDao.getCategoriesByParentIdOrderedByPosition(parentId)
    }

    // 获取所有分类
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }

    // 级联删除分类及其子分类
    suspend fun deleteCategoryAndSubcategories(parentId: Long) {
        categoryDao.deleteCategoryAndSubcategories(parentId)
    }
}