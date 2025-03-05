package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.EventDao
import com.wy.simple_timer.databinding.ActivityCategoryDetailBinding

class CategoryDetailActivity : AppCompatActivity() {
    private lateinit var CD_Binding: ActivityCategoryDetailBinding
    private var categoryId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CD_Binding = DataBindingUtil.setContentView(this, R.layout.activity_category_detail)

        // 获取传递过来的 categoryId
        val intent = intent
        categoryId = intent.getLongExtra("categoryId", -1)
        if (categoryId == -1L) {
            // 处理 categoryId 无效的情况
            Toast.makeText(this, "无效的 categoryId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 调用 updateData 方法更新数据
        updateData()
    }

    public fun updateData(){
        // 读取当前 Category 对应的所有 Event
        val eventDao = EventDao(this)
        val events = eventDao.getEventsByCategory(categoryId)
        // 遍历 Event，计算总时间，总天数，平均每天时间
        var totalTimes = events.size
        var totalMinutes = 0L
        var totalDays = 0
        var nowday = 0L
        for (event in events) {
            totalMinutes += (event.endTime.time - event.startTime.time) / 1000 / 60
            val endday = event.endTime.time / 1000 / 60 / 60 / 24
            if (endday > nowday) {
                totalDays += 1
                nowday = endday
            }
        }
        var minutesPerDay = 0L
        if (totalDays <= 0) {
            minutesPerDay = 0L
        }else{
            minutesPerDay = totalMinutes / totalDays
        }
        // 更新 UI
        CD_Binding.totalTimes = totalTimes
        CD_Binding.totalMinutes = totalMinutes.toInt()
        CD_Binding.totalDays = totalDays
        CD_Binding.minutesPerDay = minutesPerDay.toInt()
    }
}
