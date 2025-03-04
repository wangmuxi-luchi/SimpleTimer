package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wy.simple_timer.databinding.ActivityMainBinding
import com.wy.simple_timer.database.EventDao
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用 View Binding 绑定布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 为打开记录页面按钮添加点击事件监听器
        binding.openRecordActivityButton.setOnClickListener {
            val eventDao = EventDao(this)
            // 查询结束时间最大的项目
            val allEvents = eventDao.getEventsByYear(Calendar.getInstance().time)
            val latestEvent = allEvents.maxByOrNull { it.endTime.time }
            
            val intent = Intent(this, TimeRecordActivity::class.java)
            latestEvent?.let {
                intent.putExtra("startTime", it.endTime.time)
            }
            startActivity(intent)
        }

        // 为打开事件列表页面按钮添加点击事件监听器
        binding.openEventListActivityButton.setOnClickListener {
            val intent = Intent(this, EventListActivity::class.java)
            startActivity(intent)
        }
    }
}