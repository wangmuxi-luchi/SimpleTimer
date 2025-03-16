package com.wy.simple_timer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    @Query("UPDATE categories SET categoryName = :name, categoryColor = :color, position = :position, archived = :archived, parentId = :parentId WHERE id = :categoryId")
    suspend fun updateCategory(
        categoryId: Long,
        name: String,
        color: String,
        position: Int,
        archived: Boolean,
        parentId: Long
    ): Int?

    @Query("INSERT OR IGNORE INTO categories (categoryName, categoryColor, position, archived, parentId) VALUES (:name, :color, :position, :archived, :parentId)")
    fun insertDefaultCategory(name: String, color: String, position: Int, archived: Boolean, parentId: Long): Long

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoriesById(categoryId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<Category>

    // 获取所有未归档的一级分类，即 parentId 为 -1 的分类, 按 position 排序, ，即 archived = 0
    @Query("SELECT * FROM categories WHERE parentId = -1 AND archived = 0 ORDER BY position ASC")
    fun getUnarchivedRootCategoriesOrderedByPosition(): Flow<List<Category>>

    // 获取所有归档的一级分类，即 parentId 为 -1 的分类, 按 position 排序, ，即 archived = 1
    @Query("SELECT * FROM categories WHERE parentId = -1 AND archived = 1 ORDER BY position ASC")
    fun getArchivedRootCategoriesOrderedByPosition(): Flow<List<Category>>

    // 获取所有未归档的分类
    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY position ASC")
    fun getUnarchivedCategoriesOrderedByPosition(): Flow<List<Category>>

    // 获取所有归档的分类
    @Query("SELECT * FROM categories WHERE archived = 1 ORDER BY position ASC")
    fun getArchivedCategoriesOrderedByPosition(): Flow<List<Category>>

    // 获取指定 parentId 的子分类
    @Query("SELECT * FROM categories WHERE parentId = :parentId")
    fun getCategoriesByParentId(parentId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY position ASC")
    fun getCategoriesByParentIdOrderedByPosition(parentId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    // 新增级联删除方法
    @Transaction
    @Query("WITH RECURSIVE subcategories(id) AS (" +
            "SELECT id FROM categories WHERE id = :parentId " +
            "UNION ALL " +
            "SELECT c.id FROM categories c " +
            "INNER JOIN subcategories s ON c.parentId = s.id" +
            ") " +
            "DELETE FROM categories WHERE id IN (SELECT id FROM subcategories)")
    suspend fun deleteCategoryAndSubcategories(parentId: Long)
}
