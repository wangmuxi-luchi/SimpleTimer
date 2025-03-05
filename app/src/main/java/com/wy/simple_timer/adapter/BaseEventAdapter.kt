package com.wy.simple_timer.adapter

import android.content.Context
import android.widget.ArrayAdapter
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.EventDao

open class BaseEventAdapter(
    context: Context,
    source: Int = 0
) : ArrayAdapter<Event>(context, source){
    init {
        setmobjects()
        AdapterList.add(this)
    }

    companion object {
        val AdapterList = mutableListOf<BaseEventAdapter>()
        val events = mutableListOf<Event>()
        var eventschange = true
        public fun updateData(){
            eventschange = true
            for (adapter in AdapterList){
                adapter.setmobjects()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setmobjects() {
        // 重新加载event数据
        if(eventschange){
            loadEventsFromDatabase()
        }
        // 清空当前实例的event列表数据
        clear()
        addAll(events)
    }

    // 从数据库加载event数据
    private fun loadEventsFromDatabase() {
        val eventDao = EventDao(context)
        events.clear()
        // 从数据库中获取所有event
        events.addAll(eventDao.getAllEvents())

        eventschange = false
    }
//    private class ViewHolder {
//        lateinit var timeTextView: TextView
//        lateinit var activityTextView: TextView
//        lateinit var durationTextView: TextView
//        lateinit var colorDotImageView: ImageView
//    }

}