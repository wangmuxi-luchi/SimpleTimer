package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.gzuliyujiang.colorpicker.ColorPicker
import com.wy.simple_timer.adapter.CategoryAdapterCM
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.viewmodel.CategoryViewModel
import com.wy.simple_timer.databinding.ActivityCategoryManagementBinding
import kotlinx.coroutines.launch

class CategoryManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryManagementBinding
    private lateinit var viewmodel: CategoryViewModel
    private lateinit var categoryAdapter: CategoryAdapterCM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        handleWindowInsets()
        setupViewModel()
        setupRecyclerView()
        setupAdapterCallbacks()
    }

    private fun setupBinding() {
        binding = ActivityCategoryManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewModel() {
        viewmodel = ViewModelProvider(this)[CategoryViewModel::class.java]
        viewmodel.setCategories(viewmodel.getCategoryDao().getUnarchivedRootCategoriesOrderedByPosition())
    }

    private fun setupRecyclerView() {
        binding.categoryList.apply {
            layoutManager = LinearLayoutManager(
                this@CategoryManagementActivity,
                LinearLayoutManager.VERTICAL, 
                false
            )
            adapter = CategoryAdapterCM().also { 
                categoryAdapter = it 
            }
        }
        observeCategories()
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            viewmodel.getCategories()?.collect { categories ->
                categoryAdapter.setData(categories)
            }
        }
    }

    private fun setupAdapterCallbacks() {
        categoryAdapter.apply {
            setOnOtherItemClickListener { categoryID ->
                startActivity(
                    Intent(this@CategoryManagementActivity, CategoryDetailActivity::class.java).apply {
                        putExtra("categoryID", categoryID)
                    }
                )
            }
            
            setOnLastItemClickListener { showAddCategoryDialog() }
            
            setOnBindViewHolder { category, position ->
                if (position != category.position) {
                    Log.d("CategoryManagementActivity", "updateCategoryPosition: $category, $position")
                    viewmodel.updateCategory(
                        category.id,
                        category.categoryName,
                        category.categoryColor,
                        position,
                        category.archived,
                        category.parentId
                    )
                }
            }
        }
    }
    private lateinit var addCategoryDialog: AlertDialog
    private lateinit var addCategoryInput: EditText
    var selectedColor = "#808080" // 默认颜色
    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "分类名称"
        addCategoryInput = EditText(this)
        val textColor = android.graphics.Color.parseColor(selectedColor)
        addCategoryInput.setTextColor(textColor)
        addCategoryInput.setHintTextColor(textColor)
        val builder = AlertDialog.Builder(this)
            .setTitle("添加分类")
            .setView(addCategoryInput)
            .setPositiveButton("确定") { _, _ ->
                val newCategoryName = addCategoryInput.text.toString()
                if (newCategoryName.isNotEmpty()) {
                    // 插入新分类到数据库
                    val newCategory = Category(0, newCategoryName, selectedColor, categoryAdapter.itemCount - 1, false, -1)
                    viewmodel.insertCategory(newCategory)
                }
            }
            .setNegativeButton("取消", null)
            .setNeutralButton("选择颜色") { _, _ ->
                // 打开颜色选择对话框
                showColorPickerDialog()


            }

        addCategoryDialog = builder.create()
        addCategoryDialog.show()
        Log.d("CategoryManagementActivity", "showAddCategoryDialog")
    }
    private fun showColorPickerDialog() {
        val picker = ColorPicker(this)
        picker.setInitColor(-0x800801)
        picker.setOnColorPickListener { pickedColor ->
            // 提取 RGB 部分
            val red = (pickedColor shr 16) and 0xFF
            val green = (pickedColor shr 8) and 0xFF
            val blue = pickedColor and 0xFF

            // 转换为 #RRGGBB 格式的字符串
            selectedColor = String.format("#%02X%02X%02X", red, green, blue)
            val textColor = android.graphics.Color.parseColor(selectedColor)
            addCategoryInput.setTextColor(textColor)
            addCategoryInput.setHintTextColor(textColor)
            // 再次显示添加分类对话框，保留输入数据
            addCategoryDialog.show()
        }
        picker.setOnDismissListener {
            // 如果颜色选择对话框关闭但未选择颜色，也显示添加分类对话框
            addCategoryDialog.show()
        }
        picker.show()

    }
}
