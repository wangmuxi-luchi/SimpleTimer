package com.wy.simple_timer.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.EventDao

open class BaseCategoryAdapter(
    context: Context,
    private val AddLastItem : Boolean = false,
    private val AddLastItemText : String = "添加",
    private val AddLastItemColor : String = "#808080"
) : ArrayAdapter<Category>(context, 0) {
//    private lateinit var categories: MutableList<Category>
    protected var mobjectschange = true
    init {
        setmobjects()
        AdapterList.add(this)
    }

    companion object {
        val AdapterList = mutableListOf<BaseCategoryAdapter>()
        val categories = mutableListOf<Category>()
        var categorieschange = true
        public fun updateData(){
            Log.d("BaseCategoryAdapter", "updateData")
            categorieschange = true
            for (adapter in AdapterList){
                adapter.setmobjects()
                Log.d("BaseCategoryAdapter", adapter.toString())
                adapter.notifyDataSetChanged()
            }
        }
    }

    protected class ViewHolder {
        lateinit var categoryText: TextView
        lateinit var dot1: ImageView
        lateinit var dot2: ImageView
    }

    public fun insertCategory(editCategory: Category) {
        this.insert(editCategory, categories.size - 2)
        // 通知适配器数据已改变
        this.notifyDataSetChanged()
    }

    public fun removeCategory(index: Int) {
        // 移除指定位置的分类
        this.remove(getItem(index))
        // 通知适配器数据已改变
        this.notifyDataSetChanged()
    }

    // 设置分类列表数据
    private fun setmobjects() {
//        val eventDao = EventDao(context)
//        categories.clear()
//        // 从数据库中获取所有分类
//        categories.addAll(eventDao.getAllCategories())
////        categories = eventDao.getAllCategories().toMutableList()
//        // 添加“添加”项
//        if(AddLastItem){
//            val addCategory = Category(-1, AddLastItemText, AddLastItemColor)
//            categories.add(addCategory)
//        }
        // 重新加载分类数据
        if(categorieschange){
            loadCategoriesFromDatabase()
        }
        // 清空当前实例的分类列表数据
        clear()
        addAll(categories)
        add(Category(-1, AddLastItemText, AddLastItemColor))

    }

    // 从数据库加载分类数据
    private fun loadCategoriesFromDatabase() {
        val eventDao = EventDao(context)
        categories.clear()
        // 从数据库中获取所有分类
        categories.addAll(eventDao.getAllCategories())

        categorieschange = false
    }


//    override fun notifyDataSetChanged() {
//        super.notifyDataSetChanged()
//        loadCategoriesFromDatabase()
//        Log.d("BaseCategoryAdapter", "notifyDataSetChanged")
//    }
}