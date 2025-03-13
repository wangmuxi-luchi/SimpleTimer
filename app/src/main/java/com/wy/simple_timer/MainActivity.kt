package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.wy.simple_timer.viewmodel.EventViewModel
import com.wy.simple_timer.database.MyDatabase
import com.wy.simple_timer.databinding.ActivityMainBinding
import com.wy.simple_timer.fragment.EventListFragment // 假设你有这个 Fragment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var eventviewmodel: EventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        handleWindowInsets()
        setupButtonListeners()
        loadEventListFragment() // 加载 EventListFragment
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupButtonListeners() {
        binding.openRecordActivityButton.setOnClickListener { launchTimeRecordActivity() }
        // 修改后的按钮监听
        binding.openCategoryManagementButton.setOnClickListener { launchCategoryManagementActivity() }
    }

    private fun launchTimeRecordActivity() {
        lifecycleScope.launch {
            val eventDao = MyDatabase.getDatabase(application).eventDao()
            val allEvents = eventDao.getAllEvents().firstOrNull()
            val latestEvent = allEvents?.maxByOrNull { it.endTime.time }

            Intent(this@MainActivity, TimeRecordActivity::class.java).apply {
                latestEvent?.let { putExtra("startTime", it.endTime.time) }
                startActivity(this)
            }
        }
    }

    // 新增分类管理跳转方法
    private fun launchCategoryManagementActivity() {
        startActivity(Intent(this, CategoryManagementActivity::class.java))
    }

    private fun loadEventListFragment() {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, EventListFragment())
        fragmentTransaction.commit()
    }
}