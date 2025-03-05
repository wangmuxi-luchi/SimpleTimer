package com.wy.simple_timer.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.wy.simple_timer.R
import com.wy.simple_timer.database.EventDao
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapterEL(
    context: Context,
) : BaseEventAdapter(context, R.layout.item_eventlist) {
    private class ViewHolder {
        lateinit var timeTextView: TextView
        lateinit var activityTextView: TextView
        lateinit var durationTextView: TextView
        lateinit var colorDotImageView: ImageView
    }


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