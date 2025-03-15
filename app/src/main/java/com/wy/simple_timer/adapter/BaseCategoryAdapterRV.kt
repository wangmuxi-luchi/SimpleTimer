package com.wy.simple_timer.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.wy.simple_timer.DatabaseManagementService
import com.wy.simple_timer.database.Category

import java.util.Collections

class CategoryDiffUtilCallback : DiffUtil.ItemCallback<Category>() {

    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        // 比较项目的唯一标识符
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        // 比较项目的实际内容
        return oldItem == newItem
    }
}
abstract class BaseCategoryAdapterRV : ListAdapter<Category, BaseCategoryAdapterRV.ViewHolder>(CategoryDiffUtilCallback()) {
    private var onItemMovedListener: ((Int, Int) -> Unit) = { _, _ ->}
    private var onSwipedListener: (Category) -> Unit = {}
    init {
        // 创建一个 AdapterDataObserver 并实现 onItemsMoved 方法
        val observer: AdapterDataObserver = object : AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                Log.d("CategoryAdapter", "onItemRangeMoved: fromPosition: $fromPosition, toPosition: $toPosition, itemCount: $itemCount")
                onItemMovedListener(fromPosition, toPosition)
            }
        }
        // 将观察者注册到适配器
        this.registerAdapterDataObserver(observer)
    }

    fun setOnSwipedListener(listener: (Category) -> Unit) {
        onSwipedListener = listener
    }

    fun setOnItemMovedListener(listener: (Int, Int) -> Unit) {
        onItemMovedListener = listener
    }

    protected var categories = emptyList <Category>()


    abstract class ViewHolder(val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView){
        abstract fun bind(position: Int, payloads: MutableList<Any>)

    }
    // 获取数据列表
    abstract fun getCategories(newCategories: List<Category>): List<Category>
    // 设置数据
    fun setData(newCategories: List<Category>) {
        categories = getCategories(newCategories)
        submitList(categories)
    }
    // 更新数据
    fun updateData(newCategories: List<Category>) {
        categories = getCategories(newCategories)
        submitList(categories)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("CategoryAdapter", "onBindViewHolder: position: $position")
        updateSelection(holder, position)
        holder.bind(position, mutableListOf(""))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        Log.d("CategoryAdapter", "onBindViewHolder-payloads: position: $position")
        updateSelection(holder, position)
        holder.bind(position, payloads)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun updateSelection(holder: ViewHolder, position: Int) {
        // 修正category的position
        val category = categories[position]
        if (category.position != position) {
            // 调用service更新position
            Log.d("CategoryAdapter", "update position: category: ${category.categoryName}, " +
                    "oldposition: ${category.position}, position: $position")
            category.position = position
            val updateCategoryIntent = Intent(holder.context, DatabaseManagementService::class.java).apply {
                action = "UPDATE_CATEGORY"
                putExtra("object", category) // Category 对象
            }
            holder.context.startService(updateCategoryIntent)
        }
    }

    fun getCategory(position: Int): Category = categories[position]

    fun collectionsSwap(i: Int, j: Int) {
        Collections.swap(categories, i, j)
    }

    fun onItemSwiped(position: Int) {
        onSwipedListener(categories[position])
    }
}

