package com.wy.simple_timer.adapter//package com.wy.simple_timer.adapter
//
//import android.content.Context
//import android.util.Log
//import android.widget.ArrayAdapter
//import android.widget.ImageView
//import android.widget.TextView
//import com.wy.simple_timer.database.Category
//import com.wy.simple_timer.database.EventDao
///**
// * 由于数据库已经改用room，布局类型改为recycleview，该类已弃用。
// * 使用 [BaseCategoryAdapterRV] 替代。
// */
//@Deprecated(
//    message = "BaseCategoryAdapter 已弃用，请使用 BaseCategoryAdapterRV 替代。",
//    replaceWith = ReplaceWith("BaseCategoryAdapterRV"),
//    level = DeprecationLevel.ERROR
//)
//open class BaseCategoryAdapter(
//    context: Context,
//    private val AddLastItem : Boolean = false,
//    private val AddLastItemText : String = "添加",
//    private val AddLastItemColor : String = "#808080"
//) : ArrayAdapter<Category>(context, 0) {
////    private lateinit var categories: MutableList<Category>
////    protected var mobjectschange = true
//    init {
//        setmobjects()
//        AdapterList.add(this)
//    }
//
//    companion object {
//        val AdapterList = mutableListOf<BaseCategoryAdapter>()
//        val categories = mutableListOf<Category>()
//        var categorieschange = true
//        public fun updateData(){
//            Log.d("BaseCategoryAdapter", "updateData")
//            categorieschange = true
//            for (adapter in AdapterList){
//                adapter.setmobjects()
//                Log.d("BaseCategoryAdapter", adapter.toString())
//                adapter.notifyDataSetChanged()
//            }
//        }
//    }
//
////    protected class ViewHolder {
////        lateinit var categoryText: TextView
////        lateinit var dot1: ImageView
////        lateinit var dot2: ImageView
////    }
//
//    // 设置分类列表数据
//    private fun setmobjects() {
//        // 重新加载分类数据
//        if(categorieschange){
//            loadCategoriesFromDatabase()
//        }
//        // 清空当前实例的分类列表数据
//        clear()
//        addAll(categories)
//        // 添加“添加”项
//        if(AddLastItem){
//            val addCategory = Category(-1, AddLastItemText, AddLastItemColor)
//            add(addCategory)
//        }
//
//    }
//
//    // 从数据库加载分类数据
//    private fun loadCategoriesFromDatabase() {
//        val eventDao = EventDao(context)
//        categories.clear()
//        // 从数据库中获取所有分类
//        categories.addAll(eventDao.getAllCategories())
//
//        categorieschange = false
//    }
//}