package com.wy.simple_timer

import CategoryDialog
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
    var selectedColor = "#808080" // 默认颜色
    private fun showAddCategoryDialog() {
        val categoryDialog = CategoryDialog(this).apply {
            setListener(object : CategoryDialog.CategoryDialogListener {
                override fun onConfirmEdit(newName: String, newColor: String) {
                    if (newName.isNotEmpty()) {
                        val newCategory = Category(
                            0, 
                            newName, 
                            newColor, 
                            categoryAdapter.itemCount - 1, 
                            false, 
                            -1
                        )
                        viewmodel.insertCategory(newCategory)
                    }
                }

                override fun onPickColor(newColor: String) {
                    selectedColor = newColor
                }
            })
        }
        // 初始名为空字符串，默认颜色使用当前选中颜色
        categoryDialog.show("", selectedColor)
    }
}
