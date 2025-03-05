package com.wy.simple_timer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wy.simple_timer.databinding.ActivityTimeRecordBinding
import java.util.Calendar
import android.content.Intent
import com.wy.simple_timer.adapter.CategoryAdapterTR
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.EventDao

class TimeRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeRecordBinding
    private val categoryList = mutableListOf<String>() // 假设这里是分类数据
    private val eventDao = EventDao(this)
    private lateinit var categoryListAdapter: CategoryAdapterTR

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

        // 从数据库读取分类信息, 并添加到 categoryList 中
        initCategoryList()

        binding.saveButtonTop.setOnClickListener {
            saveRecord(startTimePicker, endTimePicker)
        }
        binding.saveButtonBtm.setOnClickListener {
            saveRecord(startTimePicker, endTimePicker)
        }

        // 为 GridView 的最后一个元素添加点击事件监听器
        // 修改 setOnItemClickListener 方法
        binding.categoryList.setOnItemClickListener { _, _, position, _ ->
            val adapter = categoryListAdapter as ArrayAdapter<*>
            if (position == adapter.count - 1) {
                // 处理“编辑分类”的点击事件
                val intent = Intent(this, CategoryManagementActivity::class.java)
                startActivity(intent)
                if (adapter.count>0) {
                    // TODO:重新设置选中状态为上一次选择的项目
                    binding.categoryList.setItemChecked(-1, true)
                }
            } else {
                // 处理其他分类的点击事件
                // 更新所有项目的圆点显示状态
                for (i in 0 until adapter.count - 1) {
                    val view = binding.categoryList.getChildAt(i)
                    if (view != null) {
                        // 找到两个小圆点的 ImageView
                        val dot1 = view.findViewById<ImageView>(R.id.dot1)
                        val dot2 = view.findViewById<ImageView>(R.id.dot2)

                        // 根据是否选中设置小圆点的可见性
                        val isSelected = i == position
                        dot1.visibility = if (isSelected) View.VISIBLE else View.GONE
                        dot2.visibility = if (isSelected) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }



    private fun initCategoryList() {
//        val eventDao = EventDao(this)
//        val categories = eventDao.getAllCategories().toMutableList()
//
//        val editCategory = Category(-1, "编辑分类", "#808080")
//        categories.add(editCategory)
//
        categoryListAdapter = CategoryAdapterTR(this, binding, true, "编辑分类", "#808080")

        binding.categoryList.adapter = categoryListAdapter
        binding.categoryList.choiceMode = AbsListView.CHOICE_MODE_SINGLE
        if (categoryListAdapter.count>0) {
            binding.categoryList.setItemChecked(0, true)
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

        val selectedPosition = binding.categoryList.checkedItemPosition
        if (selectedPosition == categoryListAdapter.count - 1) {
            Toast.makeText(this, "请选择有效的分类", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedCategory  = if (selectedPosition != -1) categoryListAdapter.getItem(selectedPosition) else {
            Toast.makeText(this, "请选择分类", Toast.LENGTH_SHORT).show()
            return
        }
        // 根据分类名称获取分类ID
        val categoryId = selectedCategory?.id ?: -1L
        if (categoryId == -1L) {
            Toast.makeText(this, "未找到对应的分类ID", Toast.LENGTH_SHORT).show()
            return
        }
        val remark = binding.notesEditText.text.toString()

        eventDao.insertEvent(startTime, endTime, categoryId, remark)

        Log.d("TimeRecordActivity", "保存记录成功")

        onBackPressedDispatcher.onBackPressed()

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
    }
}