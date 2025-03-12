package com.wy.simple_timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryDao
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.EventDao
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DatabaseManagementService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // 移除前台服务相关代码
        // val notification = createNotification()
        // startForeground(1, notification)

        val action = intent?.action
        when (action) {
            "INSERT_CATEGORY" -> {
                val categoryDao = MyDatabase.getDatabase(this).categoryDao()
                val category = IntentCompat.getParcelableExtra(intent, "object", Category::class.java)!!
                performInsertCategoryTask(categoryDao, category)
            }
            "DELETE_CATEGORY" -> {
                val categoryDao = MyDatabase.getDatabase(this).categoryDao()
                val categoryId = intent.getLongExtra("categoryId", -1)
                if (categoryId != -1L) {
                    performDeleteCategoryTask(categoryDao, categoryId)
                }
            }
            "UPDATE_CATEGORY" -> {
                val categoryDao = MyDatabase.getDatabase(this).categoryDao()
                val category = IntentCompat.getParcelableExtra(intent, "object", Category::class.java)!!
                performUpdateCategoryTask(categoryDao, category)
            }
            "INSERT_EVENT" -> {
                val eventDao = MyDatabase.getDatabase(this).eventDao()
                val event = IntentCompat.getParcelableExtra(intent, "object", Event::class.java)!!
                performInsertEventTask(eventDao, event)
            }
            "DELETE_EVENT" -> {
                val eventDao = MyDatabase.getDatabase(this).eventDao()
                val eventId = intent.getLongExtra("eventId", -1)
                if (eventId != -1L) {
                    performDeleteEventTask(eventDao, eventId)
                }
            }
            "UPDATE_EVENT" -> {
                val eventDao = MyDatabase.getDatabase(this).eventDao()
                val event = IntentCompat.getParcelableExtra(intent, "object", Event::class.java)!!
                performUpdateEventTask(eventDao, event)
            }
            "DELETE_CATEGORY_AND_SUBCATEGORIES" -> {
                val categoryDao = MyDatabase.getDatabase(this).categoryDao()
                val parentId = intent.getLongExtra("parentId", -1)
                if (parentId != -1L) {
                    performDeleteCategoryAndSubcategoriesTask(categoryDao, parentId)
                }
            }
        }
        // 修改返回值
        return START_REDELIVER_INTENT
    }


    private fun performInsertCategoryTask(categoryDao: CategoryDao, category: Category) {
        lifecycleScope.launch {
            try {
                categoryDao.insertCategory(category)
                Log.d("DatabaseManagementService", "分类插入成功")
            } catch (e: Exception) {
                Log.e("DatabaseManagementService", "分类插入失败", e)
            }
        }
    }

    private fun performDeleteCategoryTask(categoryDao: CategoryDao, categoryId: Long) {
        lifecycleScope.launch {
            // 添加事务注解
            MyDatabase.getDatabase(this@DatabaseManagementService).withTransaction {
                try {
                    val eventDao = MyDatabase.getDatabase(this@DatabaseManagementService).eventDao()
                    eventDao.deleteEventsByCategory(categoryId)
                    categoryDao.deleteCategory(categoryId)
                    Log.d("DatabaseManagementService", "分类删除成功")
                } catch (e: Exception) {
                    Log.e("DatabaseManagementService", "分类删除失败", e)
                }
            }
        }
    }

    private fun performDeleteCategoryAndSubcategoriesTask(categoryDao: CategoryDao, parentId: Long) {
        lifecycleScope.launch {
            // 添加事务注解
            MyDatabase.getDatabase(this@DatabaseManagementService).withTransaction {
                try {
                    val subCategories = categoryDao.getCategoriesByParentId(parentId).first()
                    val eventDao = MyDatabase.getDatabase(this@DatabaseManagementService).eventDao()
                    
                    subCategories.forEach { category ->
                        // 递归调用自身时自动继承事务
                        performDeleteCategoryAndSubcategoriesTask(categoryDao, category.id)
                    }
                    
                    eventDao.deleteEventsByCategory(parentId)
                    categoryDao.deleteCategory(parentId)
                    Log.d("DatabaseManagementService", "分类及子分类删除成功")
                } catch (e: Exception) {
                    Log.e("DatabaseManagementService", "分类及子分类删除失败", e)
                }
            }
        }
    }
    private fun performUpdateCategoryTask(categoryDao: CategoryDao, category: Category) {
        lifecycleScope.launch {
            try {
                categoryDao.updateCategory(
                    category.id,
                    category.categoryName,
                    category.categoryColor,
                    category.position,
                    category.archived,
                    category.parentId
                )
                Log.d("DatabaseManagementService", "分类更新成功")
            } catch (e: Exception) {
                Log.e("DatabaseManagementService", "分类更新失败", e)
            }
        }
    }

    private fun performInsertEventTask(eventDao: EventDao, event: Event) {
        lifecycleScope.launch {
            try {
                eventDao.insertEvent(event)
                Log.d("DatabaseManagementService", "事件插入成功")
            } catch (e: Exception) {
                Log.e("DatabaseManagementService", "事件插入失败", e)
            }
        }
    }

    private fun performDeleteEventTask(eventDao: EventDao, eventId: Long) {
        lifecycleScope.launch {
            try {
                eventDao.deleteEvent(eventId)
                Log.d("DatabaseManagementService", "事件删除成功")
            } catch (e: Exception) {
                Log.e("DatabaseManagementService", "事件删除失败", e)
            }
        }
    }

    private fun performUpdateEventTask(eventDao: EventDao, event: Event) {
        lifecycleScope.launch {
            try {
                eventDao.updateEvent(
                    event.id,
                    event.startTime,
                    event.endTime,
                    event.categoryId,
                    event.notes
                )
                Log.d("DatabaseManagementService", "事件更新成功")
            } catch (e: Exception) {
                Log.e("DatabaseManagementService", "事件更新失败", e)
            }
        }
    }


}


/*
// Service 使用示例：
// 插入分类
val insertCategoryIntent = Intent(context, DatabaseManagementService::class.java).apply {
    action = "INSERT_CATEGORY"
    putExtra("object", category) // Category 对象
}
context.startService(insertCategoryIntent)

// 删除分类
val deleteCategoryIntent = Intent(context, DatabaseManagementService::class.java).apply {
    action = "DELETE_CATEGORY"
    putExtra("categoryId", categoryId) // Long 类型 ID
}
context.startService(deleteCategoryIntent)

// 更新分类
val updateCategoryIntent = Intent(context, DatabaseManagementService::class.java).apply {
    action = "UPDATE_CATEGORY"
    putExtra("object", category) // Category 对象
}
context.startService(updateCategoryIntent)

// 插入事件
val insertEventIntent = Intent(context, DatabaseManagementService::class.java).apply {
    action = "INSERT_EVENT"
    putExtra("object", event) // Event 对象
}
context.startService(insertEventIntent)

// 删除事件
val deleteEventIntent = Intent(context, DatabaseManagementService::class.java).apply {
    action = "DELETE_EVENT"
    putExtra("eventId", eventId) // Long 类型 ID
}
context.startService(deleteEventIntent)

// 更新事件
val updateEventIntent = Intent(context, DatabaseManagementService::class.java).apply {
    action = "UPDATE_EVENT"
    putExtra("object", event) // Event 对象
}
context.startService(updateEventIntent)


// 级联删除分类示例
val deleteCascadeIntent = Intent(context, DatabaseManagementService::class.java).apply {
    action = "DELETE_CATEGORY_AND_SUBCATEGORIES"
    putExtra("parentId", parentId) // 要删除的父分类ID
}
context.startService(deleteCascadeIntent)


*/