package com.wy.simple_timer

import CategoryDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.gzuliyujiang.colorpicker.ColorPicker
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryDao
import com.wy.simple_timer.database.EventDao
import com.wy.simple_timer.viewmodel.EventViewModel
import com.wy.simple_timer.database.MyDatabase
import com.wy.simple_timer.databinding.ActivityCategoryDetailBinding
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CategoryDetailActivity : AppCompatActivity() {
    private lateinit var cdBinding: ActivityCategoryDetailBinding
    private lateinit var category: Category
    private var categoryID = 0L
    private lateinit var categoryDao: CategoryDao
    private lateinit var eventDao: EventDao
    private lateinit var editCategoryDialog: AlertDialog
    private lateinit var editCategoryInput: EditText

    private lateinit var selectedColor: String
    private lateinit var newCategoryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        cdBinding = DataBindingUtil.setContentView(this, R.layout.activity_category_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 初始化 categoryDao
        categoryDao = MyDatabase.getDatabase(this).categoryDao()
        // 初始化 eventDao
        eventDao = MyDatabase.getDatabase(this).eventDao()

        // 获取传递过来的 categoryID
        val intent = intent
        categoryID = intent.getLongExtra("categoryID", -1)
        if (categoryID == -1L) {
            // 处理 categoryID 无效的情况
            Toast.makeText(this, "无效的 categoryID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // 异步根据 categoryID 获取对应的 Category
        lifecycleScope.launch {
            categoryDao.getCategoryById(categoryID).firstOrNull()?.let {
                category = it
                newCategoryName = category.categoryName
                selectedColor = category.categoryColor
                // 切换到主线程，绑定编辑按钮回调函数
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    // 为编辑按钮添加点击事件监听器
                    cdBinding.btnEdit.setOnClickListener {
                        showEditCategoryDialog()
                    }
                    // 为归档按钮添加点击事件监听器
                    cdBinding.btnArchive.setOnClickListener {
                        archiveCategory()
                    }
                }
            }
        }

        // 调用 updateData 方法更新数据
        updateData()


        // 为删除按钮添加点击事件监听器
        cdBinding.btnDelete.setOnClickListener {
            deleteCategory()
        }
    }

    private fun updateData(){
        // 读取当前 Category 对应的所有 Event
        val viewmodel = ViewModelProvider(this)[EventViewModel::class.java]
        viewmodel.refreshEvents { it.getEventsByCategory(categoryID)}
        lifecycleScope.launch {
            viewmodel.refreshEvents {
                it.getEventsByCategory(categoryID)}.collect { events ->
                // 遍历 Event，计算总时间，总天数，平均每天时间
                val totalTimes = events.size
                var totalMinutes = 0L
                var totalDays = 0
                var nowday = 0L
                for (event in events) {
                    totalMinutes += (event.endTime.time - event.startTime.time) / 1000 / 60
                    val endday = event.endTime.time / 1000 / 60 / 60 / 24
                    if (endday > nowday) {
                        totalDays += 1
                        nowday = endday
                    }
                }
                val minutesPerDay: Long
                minutesPerDay = if (totalDays <= 0) {
                    0L
                } else {
                    totalMinutes / totalDays
                }
                // 切换到主线程，更新 UI
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    cdBinding.totalTimes = totalTimes
                    cdBinding.totalMinutes = totalMinutes.toInt()
                    cdBinding.totalDays = totalDays
                    cdBinding.minutesPerDay = minutesPerDay.toInt()

                }
            }
        }
    }

    private fun showEditCategoryDialog() {
        val categoryDialog = CategoryDialog(this).apply {
            setListener(object : CategoryDialog.CategoryDialogListener {
                override fun onConfirmEdit(newName: String, newColor: String) {
                    newCategoryName = newName
                    selectedColor = newColor
                    lifecycleScope.launch {
                        categoryDao.updateCategory(
                            categoryID, 
                            newName,
                            newColor,
                            category.position,
                            category.archived,
                            category.parentId
                        )
                        updateData()
                    }
                }

                override fun onPickColor(newColor: String) {
                    selectedColor = newColor
                }
            })
        }
        categoryDialog.show(category.categoryName, category.categoryColor)
    }

    private fun deleteCategory() {
        val deleteCascadeIntent = Intent(this, DatabaseManagementService::class.java).apply {
            action = "DELETE_CATEGORY_AND_SUBCATEGORIES"
            putExtra("parentId", categoryID) // 要删除的父分类ID
        }
        startService(deleteCascadeIntent)
        finish()
    }

    private fun archiveCategory() {
        lifecycleScope.launch {
            val category = categoryDao.getCategoryById(categoryID).firstOrNull()
            val newcategory = category?.let {
                Category(
                    it.id,
                    it.categoryName,
                    it.categoryColor,
                    it.position,
                    true,
                    it.parentId
                )
            }
            val updateCategoryIntent = Intent(this@CategoryDetailActivity, DatabaseManagementService::class.java).apply {
                action = "UPDATE_CATEGORY"
                putExtra("object", newcategory) // Category 对象
            }
            startService(updateCategoryIntent)
        }
    }
}
