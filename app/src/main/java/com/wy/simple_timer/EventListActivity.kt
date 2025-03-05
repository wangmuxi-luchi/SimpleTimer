package com.wy.simple_timer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.EventDao
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.wy.simple_timer.databinding.ActivityEventListBinding // 导入 View Binding 类

class EventListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventListBinding // 声明 View Binding 实例
    private val eventDao = EventDao(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventListBinding.inflate(layoutInflater) // 初始化 View Binding
        setContentView(binding.root) // 设置布局
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 使用 View Binding 访问视图
        binding.eventListView.apply {
            // 按年获取所有事件
            val allEvents = eventDao.getEventsByYear(Calendar.getInstance().time).toMutableList()
            // 移除排序代码
            // allEvents.sortBy { it.startTime }
            
            // 创建自定义适配器
            val adapter = EventListAdapter(this@EventListActivity, allEvents)
            
            // 设置适配器
            this.adapter = adapter
        }
    }

    private class EventListAdapter(
        context: AppCompatActivity,
        private val events: List<Event>
    ) : ArrayAdapter<Event>(context, R.layout.item_eventlist, events) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_eventlist, parent, false)

            val event = events[position]

            val timeTextView = view.findViewById<TextView>(R.id.time)
            val activityTextView = view.findViewById<TextView>(R.id.activity)
            val durationTextView = view.findViewById<TextView>(R.id.duration)
            // 新增：获取圆点 ImageView
            val colorDotImageView = view.findViewById<ImageView>(R.id.color_dot) 

            // 定义时间格式化器
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            // 格式化开始时间和结束时间
            val startTimeFormatted = timeFormat.format(event.startTime)
            val endTimeFormatted = timeFormat.format(event.endTime)
            // 设置时间
            timeTextView.text = "$startTimeFormatted~$endTimeFormatted"

            // 通过类型 ID 查询活动类型
            val eventDao = EventDao(context)
            val category = eventDao.getAllCategories().find { it.id == event.categoryId }
            activityTextView.text = category?.name ?: "未知类型"

            // 新增：设置圆点颜色
            if (category != null) {
                try {
                    val color = Color.parseColor(category.color)
                    colorDotImageView.setColorFilter(color)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }

            // 计算活动持续时间
            try {
                val duration = event.endTime.time - event.startTime.time
                val minutes = duration / (1000 * 60)
                durationTextView.text = "${minutes}分钟"
            } catch (e: Exception) {
                durationTextView.text = "时间格式错误"
            }

            return view
        }
    }
}
