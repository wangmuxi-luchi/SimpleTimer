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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.MyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class _EventDiffUtilCallback : DiffUtil.ItemCallback<_Event>() {
    override fun areItemsTheSame(oldItem: _Event, newItem: _Event): Boolean {
        // 比较项目的唯一标识符
        return oldItem.event.id == newItem.event.id
    }
    override fun areContentsTheSame(oldItem: _Event, newItem: _Event): Boolean {
        // 比较项目的实际内容
        return oldItem == newItem
    }
}

data class _Event(
    val event: Event,
    val categoryName: String,
    val categoryColor: Int,
    val duration: Long,
    val level: Float)


class EventAdapterEL(private val context: AppCompatActivity) : ListAdapter<_Event, EventAdapterEL.ViewHolder>(_EventDiffUtilCallback()) {
//    private var events = emptyList<Event>()
    private var _events = emptyList<_Event>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val activityTextView: TextView = itemView.findViewById(R.id.activity)
        private val durationTextView: TextView = itemView.findViewById(R.id.duration)
        private val colorDotImageView: ImageView = itemView.findViewById(R.id.color_dot)
        fun bind(_event: _Event, payloads: MutableList<Any> = mutableListOf()) {
            val event = _event.event
            itemView.setOnClickListener {
                onItemClickListener(event.id)
            }
            // 定义时间格式化器
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            // 格式化开始时间和结束时间
            val startTimeFormatted = timeFormat.format(event.startTime.time)
            val endTimeFormatted = timeFormat.format(event.endTime.time)
            // 设置时间
            timeTextView.text =
                context.getString(R.string.time_range, startTimeFormatted, endTimeFormatted)
            // 计算活动持续时间
            val hours = _event.duration / (1000 * 60 * 60)
            val minutes = (_event.duration % (1000 * 60 * 60)) / (1000 * 60)
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
            colorDotImageView.setColorFilter(_event.categoryColor)
            // 更新背景的宽度
            itemView.post {
                updateItemsBackground(itemView, _event.level, _event.categoryColor)
            }
            activityTextView.text = _event.categoryName
        }

    }
//    fun getEvents(newEvents: List<Event>): List<Event> {
//        return newEvents
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_eventlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventAdapterEL.ViewHolder, position: Int) {
        holder.bind(_events[position])
    }
    override fun onBindViewHolder(holder: EventAdapterEL.ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(_events[position], payloads)
    }

    override fun getItemCount(): Int {
        return _events.size
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

//            Log.d("EventAdapter", "updateItemsBackground levelWidth: $level, $levelWidth")
            background = backgroundDrawable
            invalidate() // 刷新视图
        }
    }

    fun setData(newEvents: List<Event>) {
        // 计算总时长
//        Log.d("EventAdapter", "setData newEvents: $newEvents")
        if(!newEvents.isEmpty()){
            val _maxDuration = newEvents.maxOf { it.endTime.timeInMillis - it.startTime.timeInMillis }
//            Log.d("EventAdapter", "_maxDuration")
            context.lifecycleScope.launch {
                val categoryDao = MyDatabase.getDatabase(context).categoryDao()
                _events = newEvents.map{
                    // 检查分类是否存在
                    val categoryFirst = categoryDao.getCategoryById(it.categoryId).firstOrNull()
                    // 如果分类存在，则设置活动类型
                    if (categoryFirst == null) {
                        // 该分类已被删除，删除该类别下的所有事件
//                        Log.d("EventAdapter", "Category deleted, deleting events: ${it}, ${categoryFirst}")
                        val eventDao = MyDatabase.getDatabase(context).eventDao()
                        eventDao.deleteEventsByCategory(it.categoryId)
                        // TODO: 好像没有成功删除
                    }
                    val categoryColor = Color.parseColor(categoryFirst?.categoryColor?: "#000000")
                    val categoryName = categoryFirst?.categoryName ?: "错误类别"
                    val duration = it.endTime.timeInMillis - it.startTime.timeInMillis
                    if (_maxDuration != 0L){
                        val level = duration.toFloat() / _maxDuration.toFloat()
                        _Event(it, categoryName, categoryColor, duration, level)
                    }else{
                        _Event(it, categoryName, categoryColor, duration, 0f)
                    }
                }
//                Log.d("EventAdapter", "finish1: $_events")
                withContext(Dispatchers.Main) {
                    submitList(_events)
//                    Log.d("EventAdapter", "finish2: $_events")
                    return@withContext
                }
            }
        }else{
            Log.d("EventAdapter", "empty")
            _events = emptyList()
            submitList(_events)
            return
        }

    }
}