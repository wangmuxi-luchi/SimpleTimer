package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.databinding.ActivityTimeRecordBinding
import com.wy.simple_timer.fragment.CategoryPickerFragment
import com.wy.simple_timer.fragment.TimePickerFragment
import java.util.Date
import java.util.concurrent.TimeUnit

class TimeRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeRecordBinding
    private lateinit var timePickerFragment: TimePickerFragment
    private lateinit var categoryPickerFragment: CategoryPickerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        handleWindowInsets()
        setupTimePickerFragment()
        setupCategoryPickerFragment()
        setupButtonListeners()
    }

    private fun setupBinding() {
        binding = ActivityTimeRecordBinding.inflate(layoutInflater)
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
        timePickerFragment = TimePickerFragment().apply {
            setOnFragmentReadyListener {
                val startTime = intent.getLongExtra("startTime", -1)
                if (startTime != -1L) {
                    setStartTime(Date(startTime))
                }
            }

        }
        supportFragmentManager.beginTransaction()
           .replace(R.id.time_picker_container, timePickerFragment)
           .commit()
    }

    private fun setupCategoryPickerFragment() {
        categoryPickerFragment = CategoryPickerFragment().apply {
            setOnFragmentReadyListener {}
        }
        supportFragmentManager.beginTransaction()
           .replace(R.id.category_picker_container, categoryPickerFragment)
           .commit()
    }

    private fun setupButtonListeners() {
        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        listOf(binding.saveButtonTop, binding.saveButtonBtm).forEach { btn ->
            btn.setOnClickListener { saveRecord() }
        }
    }


    // 在保存按钮点击前添加时间验证
    private fun saveRecord() {
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
        val remark = binding.notesEditText.text.toString()

        // 保存记录到数据库
        val event = Event(0, startTime, endTime, selectedCategoryId, remark)
        val intent = Intent(this, DatabaseManagementService::class.java).apply {
            action = "INSERT_EVENT"
            putExtra("object", event)
        }
        this.startService(intent)
        onBackPressedDispatcher.onBackPressed()
    }
}