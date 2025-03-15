package com.wy.simple_timer.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapterEL(private val context: AppCompatActivity) : BaseEventAdapterRV() {
    protected var maxDuration: Long = 0

    @SuppressLint("NotifyDataSetChanged")
    override fun setData(newEvents: List<Event>) {
        // 计算总时长
        events = getEvents(newEvents)
        Log.d("EventAdapter", "submitList")
        submitList(events)

        val _maxDuration = events.maxOf { it.endTime.time - it.startTime.time }
        if (_maxDuration != maxDuration) {
            maxDuration = _maxDuration
            Log.d("EventAdapter", "updateItemsBackground duration: ${maxDuration}")
            notifyDataSetChanged()
        }
    }
    inner class ViewHolder(itemView: View) : BaseEventAdapterRV.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val activityTextView: TextView = itemView.findViewById(R.id.activity)
        private val durationTextView: TextView = itemView.findViewById(R.id.duration)
        private val colorDotImageView: ImageView = itemView.findViewById(R.id.color_dot)
        private var categoryColor: Int = -1
        private var categoryId: Long = -1
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
            // 计算活动持续时间
            val duration = event.endTime.time - event.startTime.time
            val hours = duration / (1000 * 60 * 60)
            val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
            try {
                durationTextView.text = if (hours > 0) {
                    String.format("%2d小时%02d分钟", hours, minutes)
                } else {
                    String.format("%2d分钟", minutes)
                }
            } catch (e: Exception) {
                Log.e("EventAdapter", "Error calculating duration", e)
                durationTextView.text = "时间格式错误"
            }
            if (categoryId != event.categoryId) {
                updateCategory(event, duration)
            }else{
                // 更新背景的宽度
                Log.d("EventAdapter", "updateItemsBackground duration: $duration")
                val level = duration.toFloat() / maxDuration.toFloat()
                updateItemsBackground(itemView, level, categoryColor)
            }
        }

        private fun updateCategory(event: Event, duration: Long){
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
                        categoryColor = Color.parseColor(category.categoryColor)
                        colorDotImageView.setColorFilter(categoryColor)
                        // 更新背景的宽度
                        Log.d("EventAdapter", "updateItemsBackground duration: $duration")
                        val level = duration.toFloat() / maxDuration.toFloat()
                        updateItemsBackground(itemView, level, categoryColor)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    }
                    category.categoryName.let {
                        activityTextView.text = it
                    }
                }
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


    private fun updateItemsBackground(itemView: View, level: Float, color: Int){
        itemView.apply{
            val levelWidth = (width * level * 0.85).toInt()

            // 更新背景的宽度
            val backgroundDrawable = ContextCompat.getDrawable(context,
                R.drawable.background_item_main) as LayerDrawable

            val colorBackgroundIndex = backgroundDrawable.findIndexByLayerId(R.id.color_background)
            val colorBackgroundDrawable = backgroundDrawable.getDrawable(colorBackgroundIndex) as GradientDrawable
            colorBackgroundDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            backgroundDrawable.setLayerWidth(colorBackgroundIndex, levelWidth)

            background = backgroundDrawable
            invalidate() // 刷新视图
        }
    }

}