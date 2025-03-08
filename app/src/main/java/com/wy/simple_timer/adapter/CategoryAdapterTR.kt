package com.wy.simple_timer.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Category


class CategoryAdapterTR : BaseCategoryAdapterRV() {
    private var onLastItemClickListener: () -> Unit = {}
    
    // 新增：设置最后一个元素的回调函数
    fun setOnLastItemClickListener(listener: () -> Unit) {
        onLastItemClickListener = listener
    }
    // 新增：记录当前选中的位置
    private var currentPosition = 0
    // 获取当前选中的位置
    fun getCurrentPosition(): Int {
        return currentPosition
    }
    // 获取当前选中的分类
    fun getCurrentCategory(): Long? {
        return if (currentPosition != -1 && currentPosition < categories.size) {
            categories[currentPosition].id
        } else {
            null
        }
    }
    
    override fun getCategories(newCategories: List<Category>): List<Category> {
        // 在最后添加一个 "编辑分类"
        return newCategories + Category(0, "编辑分类", "#808080", newCategories.size, false, -1)
    }

    inner class ViewHolder(itemView: View) : BaseCategoryAdapterRV.ViewHolder(itemView)
    {
        private val categoryText: TextView = itemView.findViewById(R.id.category_text)
        private val dot1: ImageView = itemView.findViewById(R.id.dot1)
        private val dot2: ImageView = itemView.findViewById(R.id.dot2)
        override fun bind(position: Int, payloads: MutableList<Any>) {
            val category: Category = categories[position]
            for (payload in payloads) {
//                if (payload is String) {
                    when ((payload as ArrayList<*>)[0]) {
                        "UPDATE_SELECTION" -> {
                            // 刷新小圆点的可见性
                            val isSelected = absoluteAdapterPosition == currentPosition
                            dot1.visibility = if (isSelected) View.VISIBLE else View.GONE
                            dot2.visibility = if (isSelected) View.VISIBLE else View.GONE
                            Log.d("CategoryAdapter", "update dot")
                            return
                        }
                        "" -> {
                            // 默认情况，直接全体刷新
                            Log.d("CategoryAdapter", "update all")
                        }
                        else -> {
                            // 其他情况，报错,打印payloads的类型
                            Log.e("CategoryAdapter", "Invalid payloads: ${payloads.javaClass.simpleName}")
                            Log.e("CategoryAdapter", "Invalid payload: ${payload.javaClass.simpleName}")
                            throw IllegalArgumentException("Invalid payload: $payload")
                        }
                    }
//                }
//                else {
//                    // 其他情况，报错,打印payloads的类型
//                    Log.e("CategoryAdapter", "Invalid payload: ${payloads.javaClass.simpleName}")
//                    throw IllegalArgumentException("Invalid payload2: $payloads")
//                }
            }
            try {
                categoryText.setBackgroundColor(android.graphics.Color.parseColor(category.categoryColor))
                categoryText.textSize = 12f
                categoryText.setSingleLine(true)
                categoryText.ellipsize = android.text.TextUtils.TruncateAt.END
                categoryText.text = category.categoryName
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            // 新增：根据是否选中设置小圆点的可见性
            val isSelected = absoluteAdapterPosition == currentPosition
            dot1.visibility = if (isSelected) View.VISIBLE else View.GONE
            dot2.visibility = if (isSelected) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                if (absoluteAdapterPosition == categories.size - 1)
                    onLastItemClickListener()
                else if( absoluteAdapterPosition!= RecyclerView.NO_POSITION
                    &&   absoluteAdapterPosition!= currentPosition ) {
                    val oldPosition = currentPosition
                    // 更新当前选中的位置
                    currentPosition = absoluteAdapterPosition
                    // 刷新视图
                    notifyItemChanged(oldPosition, mutableListOf("UPDATE_SELECTION"))
                    notifyItemChanged(currentPosition, mutableListOf("UPDATE_SELECTION"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timerecord_category_list, parent, false)
        return ViewHolder(view)
    }

}