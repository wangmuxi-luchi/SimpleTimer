package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.wy.simple_timer.adapter.CategoryAdapterTR
import com.wy.simple_timer.database.CategoryViewModel
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.EventViewModel
import com.wy.simple_timer.databinding.ActivityTimeRecordBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class TimeRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeRecordBinding
    private lateinit var categoryAdapter: CategoryAdapterTR
    private lateinit var categoryviewmodel: CategoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        handleWindowInsets()
        setupViewModel()
        setupRecyclerView()
        setupTimePickers()
        setupButtonListeners()
        observeCategories()
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

    private fun setupViewModel() {
        categoryviewmodel = ViewModelProvider(this)[CategoryViewModel::class.java]
        categoryviewmodel.setCategories(categoryviewmodel.getCategoryDao().getUnarchivedRootCategoriesOrderedByPosition())
    }

    private fun setupRecyclerView() {
        binding.categoryList.apply {
            layoutManager = GridLayoutManager(this@TimeRecordActivity, 4)
            adapter = CategoryAdapterTR().also {
                categoryAdapter = it
                it.setOnLastItemClickListener {
                    startActivity(Intent(this@TimeRecordActivity, CategoryManagementActivity::class.java))
                }
            }
        }
    }

    private fun setupTimePickers() {
        val calendar = Calendar.getInstance()
        val (startPicker, endPicker) = listOf(binding.startTimePicker, binding.endTimePicker)

        startPicker.apply {
            setIs24HourView(true)
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }

        endPicker.apply {
            setIs24HourView(true)
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }

        // 处理传入的开始时间
        intent.getLongExtra("startTime", -1).takeIf { it != -1L }?.let {
            calendar.timeInMillis = it
            startPicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            startPicker.minute = calendar.get(Calendar.MINUTE)
        }
    }

    private fun setupButtonListeners() {
        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        listOf(binding.saveButtonTop, binding.saveButtonBtm).forEach { btn ->
            btn.setOnClickListener { saveRecord(binding.startTimePicker, binding.endTimePicker) }
        }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            categoryviewmodel.getCategories()?.collect { categories ->
                categoryAdapter.setData(categories)
            }
        }
    }

    private fun saveRecord(startTimePicker: TimePicker, endTimePicker: TimePicker) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, startTimePicker.hour)
        calendar.set(Calendar.MINUTE, startTimePicker.minute)
        val startTime = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, endTimePicker.hour)
        calendar.set(Calendar.MINUTE, endTimePicker.minute)
        val endTime = calendar.time

        if (startTime.after(endTime)) {
            Toast.makeText(this, "开始时间不能晚于结束时间", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategoryId  = categoryAdapter.getCurrentCategory()

        if (selectedCategoryId == null) {
            Toast.makeText(this, "未找到对应的分类", Toast.LENGTH_SHORT).show()
            return
        }
        val remark = binding.notesEditText.text.toString()

        // 保存记录到数据库
        val event = Event(0, startTime, endTime, selectedCategoryId, remark)
        val eventviewmodel = ViewModelProvider( this )[EventViewModel::class.java]
        eventviewmodel.insertEvent(event)
        Log.d("TimeRecordActivity", "保存记录成功")
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()

//        eventDao.insertEvent(Event(0, startTime, endTime, categoryId, remark))


        onBackPressedDispatcher.onBackPressed()

    }
}