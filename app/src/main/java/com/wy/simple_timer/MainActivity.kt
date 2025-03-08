package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.wy.simple_timer.database.EventViewModel
import com.wy.simple_timer.database.MyDatabase
import com.wy.simple_timer.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var eventviewmodel: EventViewModel

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
            // 获取Dao实例
            val eventDao = MyDatabase.getDatabase(application).eventDao()
            // 异步查询事件,并打开记录页面
            lifecycleScope.launch {
                // 启动一个异步任务，获取事件
                val allEvents = eventDao.getAllEvents().firstOrNull()
                //回到主线程,找到结束时间最大的项目
                val latestEvent = allEvents?.maxByOrNull { it.endTime.time }
                // 启动记录页面
                val intent = Intent(this@MainActivity, TimeRecordActivity::class.java)
                latestEvent?.let {
                    intent.putExtra("startTime", it.endTime.time)
                }
                startActivity(intent)
            }
        }

        // 为打开事件列表页面按钮添加点击事件监听器
        binding.openEventListActivityButton.setOnClickListener {
            val intent = Intent(this, EventListActivity::class.java)
            startActivity(intent)
        }
    }
}