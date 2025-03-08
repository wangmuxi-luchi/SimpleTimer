package com.wy.simple_timer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.database.Category

class CategoryAdapterCM : BaseCategoryAdapterRV() {
    private var onLastItemClickListener: () -> Unit = {}
    private var onOtherItemClickListener: (Long) -> Unit = {}
    private var onBindViewHolder: (Category, Int) -> Unit = { _, _ -> }

    // 新增：设置最后一个元素的回调函数
    fun setOnLastItemClickListener(listener: () -> Unit) {
        onLastItemClickListener = listener
    }
    // 新增：设置其他元素的回调函数
    fun setOnOtherItemClickListener(listener: (Long) -> Unit) {
        onOtherItemClickListener = listener
    }
     // 新增：设置绑定回调函数
    fun setOnBindViewHolder(listener: (Category, Int) -> Unit) {
        onBindViewHolder = listener
    }

    override fun getCategories(newCategories: List<Category>): List<Category> {
        // 在最后添加一个 "添加分类"
        return newCategories + Category(0, "添加分类", "#808080", newCategories.size, false, -1)
    }

    inner class ViewHolder(itemView: View) : BaseCategoryAdapterRV.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(android.R.id.text1)
        override fun bind(position: Int, payloads: MutableList<Any>) {
            val category: Category = categories[position]
            // 调用回调函数
            onBindViewHolder(category, position)
            try {
                categoryText.text = category.categoryName
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            itemView.setOnClickListener {
                if (absoluteAdapterPosition == categories.size - 1) {
                    // 最后一个元素被点击，执行回调函数
                    onLastItemClickListener()
                }
                else if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    // 调用回调函数
                    onOtherItemClickListener(categories[absoluteAdapterPosition].id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

}

//class CategoryAdapterCM(
//    context: Context,
//    AddLastItem : Boolean = false,
//    AddLastItemText : String = "添加",
//    AddLastItemColor : String = "#808080"
//) : BaseCategoryAdapter(context, AddLastItem, AddLastItemText, AddLastItemColor){
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        var view = convertView
//
//        if (view == null) {
//            view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
//            if(view == null) {
//                throw RuntimeException("Failed to inflate layout")
//            }
//        }
//        val textView = view.findViewById<TextView>(android.R.id.text1)
//        val category = getItem(position)
//        if (category != null) {
//            textView.text = category.name
//        }
//        return view
//    }
//
//}