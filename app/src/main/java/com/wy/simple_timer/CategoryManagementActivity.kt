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
import com.wy.simple_timer.adapter.CategoryAdapterCM
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryViewModel
import com.wy.simple_timer.databinding.ActivityCategoryManagementBinding
import kotlinx.coroutines.launch

class CategoryManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryManagementBinding
    private lateinit var viewmodel: CategoryViewModel
    private lateinit var categoryAdapter: CategoryAdapterCM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 设置 RecyclerView 的布局管理器为 LinearLayoutManager, 垂直方向
        val recyclerView = binding.categoryList
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        // 设置适配器
        viewmodel = ViewModelProvider( this )[CategoryViewModel::class.java]
        viewmodel.setCategories(viewmodel.getCategoryDao().getUnarchivedRootCategoriesOrderedByPosition())
        categoryAdapter = CategoryAdapterCM()
        recyclerView.adapter = categoryAdapter
        // 将数据加载到适配器中
        lifecycleScope.launch {
            viewmodel.getCategories()?.collect { categories ->
                categoryAdapter.setData(categories)
            }
        }
        // 设置适配器的回调函数,打开CategoryDetailActivity
        categoryAdapter.setOnOtherItemClickListener { categoryID ->
            val intent = Intent(this, CategoryDetailActivity::class.java)
            intent.putExtra("categoryID", categoryID)
            startActivity(intent)
        }
        // 点击添加按钮,弹出添加分类的对话框
        categoryAdapter.setOnLastItemClickListener {
            showAddCategoryDialog()
        }
        // 绑定适配器的回调函数,更新分类的position
        categoryAdapter.setOnBindViewHolder { category, position ->
            if (position != category.position) {
                Log.d("CategoryManagementActivity", "updateCategoryPosition: $category, $position")
                viewmodel.updateCategory(category.id, category.categoryName, category.categoryColor, position, category.archived, category.parentId)
            }
        }

    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        val dialog = AlertDialog.Builder(this)
           .setTitle("添加分类")
           .setView(input)
           .setPositiveButton("确定") { _, _ ->
                val newCategoryName = input.text.toString()
                if (newCategoryName.isNotEmpty()) {
                    // 插入新分类到数据库
                    val newCategory = Category(0, newCategoryName, "#808080", categoryAdapter.itemCount-1, false, -1)
                    viewmodel.insertCategory(newCategory)
                }
            }
           .setNegativeButton("取消", null)
           .create()
        dialog.show()
        Log.d("CategoryManagementActivity", "showAddCategoryDialog")
    }
}
