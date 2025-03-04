package com.wy.simple_timer

import android.app.Application
import android.content.SharedPreferences
import com.wy.simple_timer.database.EventDao

class SimpleTimerApplication : Application() {
    private val PREF_NAME = "SimpleTimerPrefs"
    private val KEY_INITIALIZED = "isInitialized"

    override fun onCreate() {
        super.onCreate()
        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val isInitialized = prefs.getBoolean(KEY_INITIALIZED, false)

        if (!isInitialized) {
            val eventDao = EventDao(this)
            eventDao.insertDefaultCategories()

            // 标记为已初始化
            val editor = prefs.edit()
            editor.putBoolean(KEY_INITIALIZED, true)
            editor.apply()
        }
    }
}
