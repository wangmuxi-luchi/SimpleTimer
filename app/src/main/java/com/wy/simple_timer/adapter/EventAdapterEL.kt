package com.wy.simple_timer.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
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
            itemView.setOnClickListener {
                onItemClickListener(event.id)
            }
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
            context.lifecycleScope.launch {
                // 检查分类是否存在
                val categoryFirst = categoryDao.getCategoryById(event.categoryId).firstOrNull()
                // 如果分类存在，则设置活动类型
                if (categoryFirst == null) {
                    // 该分类已被删除，删除该类别下的所有事件
                    Log.d("EventAdapter", "Category deleted, deleting events: ${event}, ${categoryFirst}")
                    val eventDao = MyDatabase.getDatabase(context).eventDao()
                    eventDao.deleteEventsByCategory(event.categoryId)
                    // TODO: 好像没有成功删除
                }
                categoryFirst?.let { category ->
                    try {
                        val color = Color.parseColor(category.categoryColor)
                        colorDotImageView.setColorFilter(color)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    }
                    category.categoryName.let {
                        activityTextView.text = it
                    }
                }
            }
            // 计算活动持续时间
            try {
                val duration = event.endTime.time - event.startTime.time
                val hours = duration / (1000 * 60 * 60)
                val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
                
                durationTextView.text = if (hours > 0) {
                    String.format("%2d小时%02d分钟", hours, minutes)
                } else {
                    String.format("%2d分钟", minutes)
                }
            } catch (e: Exception) {
                Log.e("EventAdapter", "Error calculating duration", e)
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

    // 在 EventAdapterEL 类中添加：
    private var onItemClickListener: (Long) -> Unit = {}
    
    // 新增设置点击监听器的方法
    fun setOnItemClickListener(listener: (Long) -> Unit) {
        onItemClickListener = listener
    }

}