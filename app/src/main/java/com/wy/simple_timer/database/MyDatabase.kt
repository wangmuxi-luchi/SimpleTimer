package com.wy.simple_timer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

const val DATABASE_VERSION = 1

@Database(entities = [Event::class, Category::class], version = DATABASE_VERSION)
@TypeConverters(DateConverter::class) // 添加类型转换器
abstract class MyDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}