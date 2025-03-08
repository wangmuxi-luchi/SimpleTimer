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
        binding = ActivityTimeRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        categoryviewmodel = ViewModelProvider( this )[CategoryViewModel::class.java]
        categoryviewmodel.setCategories(categoryviewmodel.getCategoryDao().getUnarchivedRootCategoriesOrderedByPosition())
        categoryAdapter = CategoryAdapterTR()

        // 设置 RecyclerView 的布局管理器为 GridLayoutManager，每列显示 4 个元素
        val recyclerView = binding.categoryList
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = categoryAdapter

        val startTimePicker = binding.startTimePicker
        val endTimePicker = binding.endTimePicker

        startTimePicker.setIs24HourView(true)
        endTimePicker.setIs24HourView(true)

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // 获取传入的开始时间
        val startTimeMillis = intent.getLongExtra("startTime", -1)
        if (startTimeMillis != -1L) {
            calendar.timeInMillis = startTimeMillis
            startTimePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            startTimePicker.minute = calendar.get(Calendar.MINUTE)
        } else {
            startTimePicker.hour = currentHour
            startTimePicker.minute = currentMinute
        }

        endTimePicker.hour = currentHour
        endTimePicker.minute = currentMinute

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        lifecycleScope.launch {
            categoryviewmodel.getCategories()?.collect { categories ->
                categoryAdapter.setData(categories)
            }
        }

        binding.saveButtonTop.setOnClickListener {
            saveRecord(startTimePicker, endTimePicker)
        }
        binding.saveButtonBtm.setOnClickListener {
            saveRecord(startTimePicker, endTimePicker)
        }

        // 为 GridView 的最后一个元素添加点击事件监听器
        categoryAdapter.setOnLastItemClickListener{
            val intent = Intent(this, CategoryManagementActivity::class.java)
            startActivity(intent)
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