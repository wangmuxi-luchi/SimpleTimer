package com.wy.simple_timer.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class EventDao(context: Context) {
    private val dbHelper = EventDatabaseHelper(context)

    // 插入事件
    fun insertEvent(startTime: Date, endTime: Date, categoryId: Long, notes: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        values.put(EventDatabaseHelper.COLUMN_START_TIME, dateFormat.format(startTime))
        values.put(EventDatabaseHelper.COLUMN_END_TIME, dateFormat.format(endTime))
        values.put(EventDatabaseHelper.COLUMN_CATEGORY_ID, categoryId)
        values.put(EventDatabaseHelper.COLUMN_NOTES, notes)
        db.insert(EventDatabaseHelper.TABLE_NAME, null, values)
        db.close()
    }

    // 插入分类
    fun insertCategory(categoryName: String, categoryColor: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(EventDatabaseHelper.CATEGORY_COLUMN_NAME, categoryName)
        values.put(EventDatabaseHelper.CATEGORY_COLUMN_COLOR, categoryColor)
        val id = db.insert(EventDatabaseHelper.CATEGORY_TABLE_NAME, null, values)
        db.close()
        return id
    }

    // 获取所有分类
    fun getAllCategories(): List<Category> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            EventDatabaseHelper.CATEGORY_TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        return getCategoriesFromCursor(cursor)
    }

    private fun getCategoriesFromCursor(cursor: Cursor): List<Category> {
        val categories = mutableListOf<Category>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(EventDatabaseHelper.CATEGORY_COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(EventDatabaseHelper.CATEGORY_COLUMN_NAME))
            val color = cursor.getString(cursor.getColumnIndexOrThrow(EventDatabaseHelper.CATEGORY_COLUMN_COLOR))
            categories.add(Category(id, name, color))
        }
        cursor.close()
        return categories
    }

    // 插入默认分类
    fun insertDefaultCategories() {
        val defaultCategories = mapOf(
            "睡觉" to "#4CAF50",
            "锻炼" to "#4CAF50",
            "吃饭" to "#4CAF50",
            "休息" to "#4CAF50",
            "上课" to "#2196F3",
            "课内学习" to "#2196F3",
            "知识管理" to "#2196F3",
            "科研生活" to "#2196F3",
            "读书" to "#2196F3",
            "业余探索" to "#2196F3",
            "其他" to "#9E9D24",
            "娱乐" to "#FF4081",
            "垃圾时间" to "#B71C1C"
        )

        for ((name, color) in defaultCategories) {
            insertCategory(name, color)
        }
    }

    // 按年查询事件
    fun getEventsByYear(date: Date): List<Event> {
        val db = dbHelper.readableDatabase
        val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val year = dateFormat.format(date)
        val selection = "${EventDatabaseHelper.COLUMN_START_TIME} LIKE ?"
        val selectionArgs = arrayOf("$year%")
        // 添加排序规则
        val orderBy = "${EventDatabaseHelper.COLUMN_START_TIME} ASC" 
        val cursor = db.query(
            EventDatabaseHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        )
        return getEventsFromCursor(cursor)
    }
    // 按月查询事件
    fun getEventsByMonth(date: Date): List<Event> {
        val db = dbHelper.readableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val month = dateFormat.format(date)
        val selection = "${EventDatabaseHelper.COLUMN_START_TIME} LIKE ?"
        val selectionArgs = arrayOf("$month%")
        // 添加排序规则
        val orderBy = "${EventDatabaseHelper.COLUMN_START_TIME} ASC" 
        val cursor = db.query(
            EventDatabaseHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        )
        return getEventsFromCursor(cursor)
    }
    // 按日查询事件
    fun getEventsByDay(date: Date): List<Event> {
        val db = dbHelper.readableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val day = dateFormat.format(date)
        val selection = "${EventDatabaseHelper.COLUMN_START_TIME} LIKE ?"
        val selectionArgs = arrayOf("$day%")
        // 添加排序规则
        val orderBy = "${EventDatabaseHelper.COLUMN_START_TIME} ASC" 
        val cursor = db.query(
            EventDatabaseHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        )
        return getEventsFromCursor(cursor)
    }
    // 按事件类型查询事件
    fun getEventsByCategory(categoryId: Long): List<Event> {
        val db = dbHelper.readableDatabase
        val selection = "${EventDatabaseHelper.COLUMN_CATEGORY_ID} = ?"
        val selectionArgs = arrayOf(categoryId.toString())
        // 添加排序规则
        val orderBy = "${EventDatabaseHelper.COLUMN_START_TIME} ASC" 
        val cursor = db.query(
            EventDatabaseHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        )
        return getEventsFromCursor(cursor)
    }
    private fun getEventsFromCursor(cursor: Cursor): List<Event> {
        val events = mutableListOf<Event>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(EventDatabaseHelper.COLUMN_ID))
                val startTime = try {
                    dateFormat.parse(it.getString(it.getColumnIndexOrThrow(EventDatabaseHelper.COLUMN_START_TIME)))
                } catch (e: Exception){
                    Log.e("EventDao", "Failed to parse date", e)
                    null
                }
                val endTime = try {
                    dateFormat.parse(it.getString(it.getColumnIndexOrThrow(EventDatabaseHelper.COLUMN_END_TIME)))
                } catch (e: Exception){
                    Log.e("EventDao", "Failed to parse date", e)
                    null
                }
                val categoryId = it.getLong(it.getColumnIndexOrThrow(EventDatabaseHelper.COLUMN_CATEGORY_ID))
                val notes = it.getString(it.getColumnIndexOrThrow(EventDatabaseHelper.COLUMN_NOTES))
                if (startTime!= null && endTime!= null){
                    events.add(Event(id, startTime, endTime, categoryId, notes))
                }
            }
        }
        return events
    }
}

data class Event(
    val id: Long,
    val startTime: Date,
    val endTime: Date,
    val categoryId: Long,
    val notes: String
)

data class Category(
    val id: Long,
    val name: String,
    val color: String
)
