package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wy.simple_timer.adapter.BaseCategoryAdapter
import com.wy.simple_timer.adapter.CategoryAdapterCM
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.EventDao
import com.wy.simple_timer.databinding.ActivityCategoryManagementBinding

class CategoryManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryManagementBinding
    private lateinit var categoryList: ListView
    private lateinit var categoryAdapter: ArrayAdapter<Category>
    private val eventDao = EventDao(this)
//    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        categoryList = binding.categoryList

        // 从数据库读取分类数据
//        loadCategoriesFromDatabase()

        // 添加“添加”项
//        val addCategory = Category(-1, "添加", "#808080")
//        categories.add(addCategory)

//        categoryAdapter = object : ArrayAdapter<Category>(this, android.R.layout.simple_list_item_1, categories) {
//            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//                val view = super.getView(position, convertView, parent)
//                val textView = view.findViewById<TextView>(android.R.id.text1)
//                val category = getItem(position)
//                if (category != null) {
//                    textView.text = category.name
//                }
//                return view
//            }
//        }
        val adapter = CategoryAdapterCM(this, true, "添加分类", "#808080")
        categoryList.adapter = adapter

        // 添加点击事件监听器
        categoryList.setOnItemClickListener { _, _, position, _ ->
            if (position == categoryList.adapter.count - 1) {
                // 处理“添加”项的点击事件
                showAddCategoryDialog()
            } else {
                val category = position
                val intent = Intent(this, CategoryDetailActivity::class.java)
                intent.putExtra("category", position)
                startActivity(intent)
//                TODO: 打开分类详情页面时传输分类的ID
            }
        }
    }

//    private fun loadCategoriesFromDatabase() {
//        val dbCategories = eventDao.getAllCategories()
//        categories.addAll(dbCategories)
//    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        val dialog = AlertDialog.Builder(this)
           .setTitle("添加分类")
           .setView(input)
           .setPositiveButton("确定") { _, _ ->
                val newCategoryName = input.text.toString()
                if (newCategoryName.isNotEmpty()) {
                    // 插入新分类到数据库
                    eventDao.insertCategory(newCategoryName, "#808080")

                    // 重新加载分类数据
//                    categories.clear()
//                    loadCategoriesFromDatabase()
//                    categories.add(Category(-1, "添加", "#808080"))
//                    categoryAdapter.notifyDataSetChanged()
                    // 刷新列表
                    BaseCategoryAdapter.updateData()
                }
            }
           .setNegativeButton("取消", null)
           .create()
        dialog.show()
        Log.d("CategoryManagementActivity", "showAddCategoryDialog")
    }
}
