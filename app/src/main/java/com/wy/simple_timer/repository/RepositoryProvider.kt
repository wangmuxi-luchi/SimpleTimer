package com.wy.simple_timer.repository

import android.content.Context
import com.wy.simple_timer.database.MyDatabase

object RepositoryProvider {

    fun provideEventRepository(context: Context): EventRepository {
        val db = MyDatabase.getDatabase(context)
        return EventRepository(db.eventDao())
    }

    fun provideCategoryRepository(context: Context): CategoryRepository {
        val db = MyDatabase.getDatabase(context)
        return CategoryRepository(db.categoryDao())
    }
}