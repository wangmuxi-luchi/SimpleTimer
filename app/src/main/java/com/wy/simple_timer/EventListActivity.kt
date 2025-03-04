package com.wy.simple_timer

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.wy.simple_timer.database.EventDao
import java.util.Calendar

class EventListActivity : AppCompatActivity() {
    private lateinit var eventListView: ListView
    private val eventDao = EventDao(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        eventListView = findViewById(R.id.event_list_view)

        // 按年获取所有事件
        val allEvents = eventDao.getEventsByYear(Calendar.getInstance().time).toMutableList()
        // 对列表进行排序
        allEvents.sortBy { it.startTime }

        // 创建适配器
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, allEvents.map {
            "开始时间: ${it.startTime}, 结束时间: ${it.endTime}, 分类ID: ${it.categoryId}, 备注: ${it.notes}"
        })

        // 设置适配器
        eventListView.adapter = adapter
    }
}
