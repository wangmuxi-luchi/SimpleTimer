package com.wy.simple_timer

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val DATA_STORE_NAME = "settings"
private val Context.dataStore by preferencesDataStore(name = DATA_STORE_NAME)

class SimpleTimerApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.IO)
    private val database by lazy { MyDatabase.getDatabase(this) }
    private val isInitializedKey = booleanPreferencesKey("is_initialized")

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            initializeIfNeeded()
        }
    }

    private suspend fun initializeIfNeeded() {
        val isInitialized = dataStore.data.first()[isInitializedKey] ?: false
        if (!isInitialized) {
            insertDefaultCategories()
            dataStore.edit { settings ->
                settings[isInitializedKey] = true
            }
        }
    }

    private  fun insertDefaultCategories() {
        val defaultCategories = arrayOf(
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

        defaultCategories.forEachIndexed { index, (name, color) ->
            database.categoryDao().insertDefaultCategory(name, color, index, false, -1)
        }
    }
}
