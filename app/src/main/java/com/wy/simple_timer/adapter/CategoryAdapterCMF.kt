package com.wy.simple_timer.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryWithEventInf
import java.util.Collections

class CategoryWithEventInfDiffUtilCallback : DiffUtil.ItemCallback<CategoryWithEventInf>() {
    override fun areItemsTheSame(oldItem: CategoryWithEventInf, newItem: CategoryWithEventInf): Boolean {
        return oldItem.category.id == newItem.category.id
    }
    override fun areContentsTheSame(oldItem: CategoryWithEventInf, newItem: CategoryWithEventInf): Boolean {
        return oldItem == newItem
    }

}

class CategoryAdapterCMF :ListAdapter<CategoryWithEventInf, CategoryAdapterCMF.CategoryViewHolder>(
    CategoryWithEventInfDiffUtilCallback() ) {

    private var categoryWithEventInfList: List<CategoryWithEventInf> = emptyList()
    private var onItemClickListener: (Long) -> Unit = {_ ->}
    private var onBindViewHolder: (CategoryWithEventInf, Int) -> Unit = { _, _ -> }
    private var onSwipedListener: (Category, Int) -> Unit = {_, _ ->}

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorDot: ImageView = itemView.findViewById(R.id.color_dot)
        private val categoryText: TextView = itemView.findViewById(R.id.category_name)
        fun bind(position: Int) {
            val categoryWithEventInf = categoryWithEventInfList[position]
            Log.d("CategoryAdapter", "bind: $categoryWithEventInf")
            val color = Color.parseColor(categoryWithEventInf.category.categoryColor)
            colorDot.setColorFilter(color)
            categoryText.text = categoryWithEventInf.category.categoryName
            itemView.setOnClickListener {
                onItemClickListener(categoryWithEventInf.category.id)
            }
            onBindViewHolder(categoryWithEventInf, absoluteAdapterPosition)

            itemView.post {
                updateItemsBackground(itemView, categoryWithEventInf.timeRatioToMax, color)
            }
            Log.d("CategoryAdapter", "bind finish")
        }
    }

    fun setOnItemClickListener(listener: (Long) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnBindViewHolder(listener: (CategoryWithEventInf, Int) -> Unit) {
        onBindViewHolder = listener
    }

    fun setOnSwipedListener(listener: (Category, Int) -> Unit) {
        onSwipedListener = listener
    }

    fun setData(newData: List<CategoryWithEventInf>) {
        categoryWithEventInfList = newData.filter { it.category.parentId == -1L } // 只显示一级分类
        submitList(categoryWithEventInfList.map{ CategoryWithEventInf(it, true) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_management, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        Log.d("CategoryAdapter", "onBindViewHolder: $position")
        holder.bind(position)
    }


    fun collectionsSwap(i: Int, j: Int) {
        // TODO: 只交换了视图，没有更新数据库
        Collections.swap(categoryWithEventInfList, i, j)
        submitList(categoryWithEventInfList)
    }

    fun onItemSwiped(position: Int) {
        onSwipedListener(categoryWithEventInfList[position].category, position)
    }

    private fun updateItemsBackground(itemView: View, level: Float, color: Int){
        Log.d("CategoryAdapter", "updateItemsBackground: view: $itemView, level: $level, color: $color")
        itemView.apply{
            val levelWidth = (width * level * 0.85).toInt()

            // 更新背景的宽度
            val backgroundDrawable = ContextCompat.getDrawable(context,
                R.drawable.background_item_main) as LayerDrawable

            val colorBackgroundIndex = backgroundDrawable.findIndexByLayerId(R.id.color_background)
            val colorBackgroundDrawable = backgroundDrawable.getDrawable(colorBackgroundIndex) as GradientDrawable
            colorBackgroundDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            backgroundDrawable.setLayerWidth(colorBackgroundIndex, levelWidth)

//            Log.d("EventAdapter", "updateItemsBackground levelWidth: $level, $levelWidth")
            background = backgroundDrawable
            invalidate() // 刷新视图
            Log.d("CategoryAdapter", "updateItemsBackground finish levelWidth: $level, $levelWidth")
        }
    }
}