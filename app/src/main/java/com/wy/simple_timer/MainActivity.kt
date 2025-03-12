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
        setupBinding()
        handleWindowInsets()
        setupButtonListeners()
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
        binding.openEventListActivityButton.setOnClickListener { launchEventListActivity() }
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

    private fun launchEventListActivity() {
        startActivity(Intent(this, EventListActivity::class.java))
    }
}