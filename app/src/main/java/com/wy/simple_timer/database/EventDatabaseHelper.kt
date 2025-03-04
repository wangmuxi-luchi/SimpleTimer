package com.wy.simple_timer.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EventDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "event_database.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "events"
        const val COLUMN_ID = "_id"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_CATEGORY_ID = "category_id" // 修改为使用分类id
        const val COLUMN_NOTES = "notes"

        // 新增分类表相关常量
        const val CATEGORY_TABLE_NAME = "categories"
        const val CATEGORY_COLUMN_ID = "_id"
        const val CATEGORY_COLUMN_NAME = "category_name"
        const val CATEGORY_COLUMN_COLOR = "category_color"
    }

    private val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_START_TIME TEXT NOT NULL,
            $COLUMN_END_TIME TEXT NOT NULL,
            $COLUMN_CATEGORY_ID INTEGER NOT NULL,
            $COLUMN_NOTES TEXT,
            FOREIGN KEY ($COLUMN_CATEGORY_ID) REFERENCES $CATEGORY_TABLE_NAME($CATEGORY_COLUMN_ID)
        )
    """.trimIndent()

    private val CREATE_CATEGORY_TABLE = """
        CREATE TABLE $CATEGORY_TABLE_NAME (
            $CATEGORY_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $CATEGORY_COLUMN_NAME TEXT NOT NULL,
            $CATEGORY_COLUMN_COLOR TEXT NOT NULL
        )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_CATEGORY_TABLE)
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $CATEGORY_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}
