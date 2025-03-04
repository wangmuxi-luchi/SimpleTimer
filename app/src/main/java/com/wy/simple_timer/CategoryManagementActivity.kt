package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.EventDao

class CategoryManagementActivity : AppCompatActivity() {
    private lateinit var categoryList: ListView
    private lateinit var categoryAdapter: ArrayAdapter<Category>
    private val eventDao = EventDao(this)
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_management)

        categoryList = findViewById(R.id.category_list)

        // 从数据库读取分类数据
        loadCategoriesFromDatabase()

        // 添加“添加”项
        val addCategory = Category(-1, "添加", "#808080")
        categories.add(addCategory)

        categoryAdapter = object : ArrayAdapter<Category>(this, android.R.layout.simple_list_item_1, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                val category = getItem(position)
                if (category != null) {
                    textView.text = category.name
                }
                return view
            }
        }
        categoryList.adapter = categoryAdapter

        // 添加点击事件监听器
        categoryList.setOnItemClickListener { _, _, position, _ ->
            if (position == categories.size - 1) {
                // 处理“添加”项的点击事件
                showAddCategoryDialog()
            } else {
                val category = categories[position]
                val intent = Intent(this, CategoryDetailActivity::class.java)
                intent.putExtra("category", category.name)
                startActivity(intent)
            }
        }
    }

    private fun loadCategoriesFromDatabase() {
        val dbCategories = eventDao.getAllCategories()
        categories.addAll(dbCategories)
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
                    eventDao.insertCategory(newCategoryName, "#808080")
                    // 重新加载分类数据
                    categories.clear()
                    loadCategoriesFromDatabase()
                    categories.add(Category(-1, "添加", "#808080"))
                    categoryAdapter.notifyDataSetChanged()
                }
            }
           .setNegativeButton("取消", null)
           .create()
        dialog.show()
    }
}
