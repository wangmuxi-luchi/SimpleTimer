package com.wy.simple_timer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wy.simple_timer.adapter.EventAdapterEL
import com.wy.simple_timer.database.EventDao
import java.util.Calendar
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
            val adapter = EventAdapterEL(this@EventListActivity)
            
            // 设置适配器
            this.adapter = adapter
        }
    }

}
