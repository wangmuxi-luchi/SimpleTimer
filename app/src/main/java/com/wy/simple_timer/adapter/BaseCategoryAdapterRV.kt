package com.wy.simple_timer.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.database.Category

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
    protected var categories = emptyList <Category>()


    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
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
        holder.bind(position, mutableListOf(""))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(position, payloads)
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}
