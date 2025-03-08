package com.wy.simple_timer.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.database.Event

class EventDiffUtilCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        // 比较项目的唯一标识符
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        // 比较项目的实际内容
        return oldItem == newItem
    }
}

abstract class BaseEventAdapterRV : ListAdapter<Event, BaseEventAdapterRV.ViewHolder>(EventDiffUtilCallback()) {
    private var events = emptyList<Event>()

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(event: Event, payloads: MutableList<Any> = mutableListOf(""))
    }

    // 获取数据列表
    abstract fun getEvents(newEvents: List<Event>): List<Event>

//    // 新增：记录当前选中的位置
//    protected var currentPosition = -1
//
//    // 获取当前选中的位置
//    fun getCurrentPosition(): Int {
//        return currentPosition
//    }
//
//    // 获取当前选中的事件
//    fun getCurrentEvent(): Event? {
//        return if (currentPosition != -1 && currentPosition < events.size) {
//            events[currentPosition]
//        } else {
//            null
//        }
//    }

    // 设置数据
    fun setData(newEvents: List<Event>) {
        events = getEvents(newEvents)
        submitList(events)
    }

    // 更新数据
    fun updateData(newEvents: List<Event>) {
        events = getEvents(newEvents)
        submitList(events)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(events[position], payloads)
    }

    override fun getItemCount(): Int {
        return events.size
    }
}
