package com.wy.simple_timer.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wy.simple_timer.R
import com.wy.simple_timer.database.CategoryViewModel
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.EventDao
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapterEL(private val context: AppCompatActivity) : BaseEventAdapterRV() {
    inner class ViewHolder(itemView: View) : BaseEventAdapterRV.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val activityTextView: TextView = itemView.findViewById(R.id.activity)
        private val durationTextView: TextView = itemView.findViewById(R.id.duration)
        private val colorDotImageView: ImageView = itemView.findViewById(R.id.color_dot)

        override fun bind(event: Event, payloads: MutableList<Any>) {
            // 定义时间格式化器
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            // 格式化开始时间和结束时间
            val startTimeFormatted = timeFormat.format(event.startTime)
            val endTimeFormatted = timeFormat.format(event.endTime)
            // 设置时间
            timeTextView.text =
                context.getString(R.string.time_range, startTimeFormatted, endTimeFormatted)

            // 通过类型 ID 查询活动类型
            val categoryDao = MyDatabase.getDatabase(context).categoryDao()
            val category = categoryDao.getCategoryById(event.categoryId)
            context.lifecycleScope.launch {
                category.collect { category ->
                    category?.let { category1 ->
                        try {
                            val color = Color.parseColor(category1.categoryColor)
                            colorDotImageView.setColorFilter(color)
                        } catch (e: IllegalArgumentException) {
                            e.printStackTrace()
                        }
                        category1.categoryName.let {
                            activityTextView.text = it
                            return@collect
                        }
                    }
                }
                // 该分类已被删除，删除该类别下的所有事件
                val eventDao = MyDatabase.getDatabase(context).eventDao()
                eventDao.deleteEventsByCategory(event.categoryId)
            }
            // 计算活动持续时间
            try {
                val duration = event.endTime.time - event.startTime.time
                val minutes = duration / (1000 * 60)
                durationTextView.text = context.getString(R.string.minutes, minutes)
            } catch (e: Exception) {
                Log.e("EventAdapter", "Error calculating duration", e)
                e.printStackTrace()
                durationTextView.text = "时间格式错误"
            }

        }
    }
    override fun getEvents(newEvents: List<Event>): List<Event> {
        return newEvents
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_eventlist, parent, false)
        return ViewHolder(view)
    }
}