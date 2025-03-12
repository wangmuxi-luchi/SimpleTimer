package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.MyDatabase
import com.wy.simple_timer.databinding.ActivityEventEditBinding
import com.wy.simple_timer.fragment.CategoryPickerFragment
import com.wy.simple_timer.fragment.TimePickerFragment
import com.wy.simple_timer.viewmodel.EventViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class EventEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventEditBinding
    private lateinit var timePickerFragment: TimePickerFragment
    private lateinit var categoryPickerFragment: CategoryPickerFragment
    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        handleWindowInsets()
        setupTimePickerFragment()
        setupCategoryPickerFragment()
        setupButtonListeners()
        getEventFromIntent()
    }

    private fun setupBinding() {
        binding = ActivityEventEditBinding.inflate(layoutInflater)
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

    private fun setupTimePickerFragment() {
        timePickerFragment = TimePickerFragment()
        supportFragmentManager.beginTransaction()
           .replace(R.id.time_picker_container, timePickerFragment)
           .commit()
    }

    private fun setupCategoryPickerFragment() {
        categoryPickerFragment = CategoryPickerFragment()
        supportFragmentManager.beginTransaction()
           .replace(R.id.category_picker_container, categoryPickerFragment)
           .commit()
    }

    private fun setupButtonListeners() {
        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        listOf(binding.saveButtonTop, binding.saveButtonBtm).forEach { btn ->
            btn.setOnClickListener { saveEditedEvent() }
        }
        
        // 新增删除按钮监听
        binding.deleteButton.setOnClickListener {
            deleteCurrentEvent()
        }
    }

    private fun deleteCurrentEvent() {
        val deleteIntent = Intent(this, DatabaseManagementService::class.java).apply {
            action = "DELETE_EVENT"
            putExtra("eventId", event.id)
        }
        startService(deleteIntent)
        Toast.makeText(
            this@EventEditActivity,
            "删除请求已提交",
            Toast.LENGTH_SHORT
        ).show()
        onBackPressedDispatcher.onBackPressed()
    }

    // 修改 getEventFromIntent 方法：
    private fun getEventFromIntent() {
        val eventId = intent.getLongExtra("eventId", -1L)
        if (eventId == -1L) throw IllegalArgumentException("Event ID must be provided")
        
        lifecycleScope.launch {
            val eventDao = MyDatabase.getDatabase(this@EventEditActivity).eventDao()
            event = eventDao.getEventById(eventId).firstOrNull() ?: throw IllegalArgumentException("Event not found")
            initializeUIWithEvent()
        }
    }

    private fun initializeUIWithEvent() {
        timePickerFragment.setStartTime(event.startTime)
        timePickerFragment.setEndTime(event.endTime)
        Log.d("EventEditActivity", "Event : ${event}")
        categoryPickerFragment.setCurrentCategory(event.categoryId)
        binding.notesEditText.setText(event.notes)
    }

    private fun saveEditedEvent() {
        val startTime = timePickerFragment.getStartTime()
        val endTime = timePickerFragment.getEndTime()

        if (startTime.after(endTime)) {
            Toast.makeText(this, "开始时间不能晚于结束时间", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategoryId = categoryPickerFragment.getCurrentCategory()

        if (selectedCategoryId == null) {
            Toast.makeText(this, "未找到对应的分类", Toast.LENGTH_SHORT).show()
            return
        }
        val notes = binding.notesEditText.text.toString()

        // 更新记录到数据库
        val eventViewModel = EventViewModel(application)
        lifecycleScope.launch {
            eventViewModel.updateEvent(
                event.id,
                startTime,
                endTime,
                selectedCategoryId,
                notes
            )
            Toast.makeText(this@EventEditActivity, "保存成功", Toast.LENGTH_SHORT).show()
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
