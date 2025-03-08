package com.wy.simple_timer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wy.simple_timer.database.EventViewModel
import com.wy.simple_timer.databinding.ActivityCategoryDetailBinding
import kotlinx.coroutines.launch

class CategoryDetailActivity : AppCompatActivity() {
    private lateinit var cdBinding: ActivityCategoryDetailBinding
    private var categoryID = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cdBinding = DataBindingUtil.setContentView(this, R.layout.activity_category_detail)

        // 获取传递过来的 categoryID
        val intent = intent
        categoryID = intent.getLongExtra("categoryID", -1)
        if (categoryID == -1L) {
            // 处理 categoryID 无效的情况
            Toast.makeText(this, "无效的 categoryID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 调用 updateData 方法更新数据
        updateData()
    }

    private fun updateData(){
        // 读取当前 Category 对应的所有 Event
        val viewmodel = ViewModelProvider(this)[EventViewModel::class.java]
        viewmodel.setEvents(viewmodel.getEventDao().getEventsByCategory(categoryID))
        lifecycleScope.launch {
            viewmodel.getEvents()?.collect { events ->
                // 遍历 Event，计算总时间，总天数，平均每天时间
                val totalTimes = events.size
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
                val minutesPerDay: Long
                minutesPerDay = if (totalDays <= 0) {
                    0L
                } else {
                    totalMinutes / totalDays
                }
                // 切换到主线程，更新 UI
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    cdBinding.totalTimes = totalTimes
                    cdBinding.totalMinutes = totalMinutes.toInt()
                    cdBinding.totalDays = totalDays
                    cdBinding.minutesPerDay = minutesPerDay.toInt()

                }
            }
        }
    }
}
