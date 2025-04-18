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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryWithEventInf
import java.util.Collections

// 对比两个元素是否相同，用于支持ListAdapter的submitList功能
class CategoryWithEventInfDiffUtilCallback : DiffUtil.ItemCallback<CategoryWithEventInf>() {
    override fun areItemsTheSame(oldItem: CategoryWithEventInf, newItem: CategoryWithEventInf): Boolean {
        return oldItem.category.id == newItem.category.id
    }
    override fun areContentsTheSame(oldItem: CategoryWithEventInf, newItem: CategoryWithEventInf): Boolean {
        return oldItem == newItem
    }

}

// 定义一个枚举类型作为workMode的取值
enum class WorkMode {
    NORMAL,
    SELECT
}

class CategoryAdapterCMF :ListAdapter<CategoryWithEventInf, CategoryAdapterCMF.CategoryViewHolder>(
    CategoryWithEventInfDiffUtilCallback() ) {

    // 修改workMode的类型为枚举类型
    private var workMode: WorkMode = WorkMode.NORMAL // 工作模式，默认为普通模式
    private var mulSelectFlag:Boolean = false // 多选标志位
    private var mulSelectStart:Int = 0 // 多选起始位置
    private var selectedCategoryList: MutableList<Long> = mutableListOf() // 选中的分类列表
    private var categoryWithEventInfList: List<CategoryWithEventInf> = emptyList()
    private var onItemClickListener: (Long) -> Unit = {_ ->}
    private var onSwipedListener: (Category, Int) -> Unit = {_, _ ->}
    private var onBindViewHolder: (CategoryWithEventInf, Int) -> Unit = { _, _ -> }
    private var onSCCListener: () -> Unit = {}//onSelectedCategoryChangedListener

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorDot: ImageView = itemView.findViewById(R.id.color_dot)
        private val categoryText: TextView = itemView.findViewById(R.id.category_name)
//        private val lock = Any()
        fun bind(position: Int) {
            Log.d("CategoryAdapterCMF", "bind start")
            val categoryWithEventInf = categoryWithEventInfList[position]
//            Log.d("CategoryAdapterCMF", "bind: $categoryWithEventInf")
            val color = Color.parseColor(categoryWithEventInf.category.categoryColor)
            colorDot.setColorFilter(color)
            categoryText.text = categoryWithEventInf.category.categoryName
            // TODO: 当点击取消选中的按钮时取消选中
            itemView.setOnClickListener {
                if (workMode == WorkMode.NORMAL){
                    onItemClickListener(categoryWithEventInf.category.id)
                } else if (workMode == WorkMode.SELECT){
                    // 选中模式下被点击不调用回调函数，而是修改选中状态
                    mulSelectFlag = false
                    if (isSelected(categoryWithEventInf.category)) { // 取消选中
                        unSelect(categoryWithEventInf.category)
                    }else{
                        select(categoryWithEventInf.category)
                    }
                }
            }
            onBindViewHolder(categoryWithEventInf, absoluteAdapterPosition)

            var selected = false
            if (workMode == WorkMode.SELECT && isSelected(categoryWithEventInf.category)) {
                selected = true
            }
           Log.d("CategoryAdapterCMF", "selected:${selected}")
            itemView.post {
                updateItemsBackground(itemView, categoryWithEventInf.timeRatioToMax, color, selected)
            }
            Log.d("CategoryAdapterCMF", "bind finish")
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

    fun setOnSCChangedListener(listener: () -> Unit){
        onSCCListener = listener
    }

    fun setData(newData: List<CategoryWithEventInf>) {
        categoryWithEventInfList = newData.filter { it.category.parentId == -1L } // 只显示一级分类
//        submitList(categoryWithEventInfList.map{ CategoryWithEventInf(it, true) })
        submitList(categoryWithEventInfList)
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
        // TODO: 目前只交换了视图，没有更新数据库
        Collections.swap(categoryWithEventInfList, i, j)
        submitList(categoryWithEventInfList)
    }

    fun select(category: Category){
        if (workMode == WorkMode.NORMAL){ // 普通模式下，修改工作模式为选中模式
            workMode = WorkMode.SELECT
        }
        if (!selectedCategoryList.contains(category.id)) {// 选中处理
            selectedCategoryList.add(category.id)
            onSCCListener()
        }
        val position = categoryWithEventInfList.indexOfFirst { it.category.id == category.id }// 确定选中的分类的位置
        if (position != -1) { // 选中的分类存在于列表中
            notifyItemChanged(position) // 刷新选中的分类的视图
        }
       Log.d("CategoryAdapterCMF", "select: $selectedCategoryList")
    }
    private fun mulSelect(mulPosition: Int){
        if (workMode == WorkMode.NORMAL){ // 普通模式下，修改工作模式为选中模式
            workMode = WorkMode.SELECT
        }
        val startIdx = minOf(mulSelectStart, mulPosition)
        val endIdx = maxOf(mulSelectStart, mulPosition)
        for (i in startIdx..endIdx){
            val category = categoryWithEventInfList[i].category
            if (!selectedCategoryList.contains(category.id)) {// 选中处理
                selectedCategoryList.add(category.id)
            }
            val position = categoryWithEventInfList.indexOfFirst { it.category.id == category.id }// 确定选中的分类的位置
            if (position != -1) { // 选中的分类存在于列表中
                notifyItemChanged(position) // 刷新选中的分类的视图
            }
        }
        onSCCListener()
    }
    fun unSelect(category: Category){
        if (workMode == WorkMode.NORMAL){ // 普通模式下，修改工作模式为选中模式
            return
        }
        if (selectedCategoryList.contains(category.id)) {// 取消选中处理
            val position = categoryWithEventInfList.indexOfFirst { it.category.id == category.id }// 确定选中的分类的位置
            if (position!= -1) { // 选中的分类存在于列表中
                selectedCategoryList.removeAll { it == category.id }
                if (selectedCategoryList.isEmpty()){ // 取消选中后，列表为空，修改工作模式为普通模式
                    workMode = WorkMode.NORMAL
                }
                notifyItemChanged(position) // 刷新选中的分类的视图
                onSCCListener()
            }
        }
    }

    fun isSelected(category: Category): Boolean {
        return selectedCategoryList.contains(category.id)
    }
    fun isSelected(categoryID: Long): Boolean {
        if (workMode == WorkMode.SELECT){
            return selectedCategoryList.contains(categoryID)
        } else{
            return true
        }
    }
    fun isSelected(position: Int): Boolean {
        return selectedCategoryList.contains(categoryWithEventInfList[position].category.id) 
    }
    // fun hasSelected(): Boolean {
    //     return selectedCategoryList.isNotEmpty() 
    // }


    fun swipeSelectCategory(category: Category, position: Int) {
        // 右滑修改选中状态，修改选中list
        mulSelectFlag = ! mulSelectFlag
        select(category)
        // 多选处理
        if (mulSelectFlag){
            mulSelectStart = position
        }else{
            mulSelect(position)
//            val start_idx = minOf(mulSelectStart, position)
//            val end_idx = maxOf(mulSelectStart, position)
//            for (i in start_idx..end_idx){
//                select(categoryWithEventInfList[i].category)
//            }
        }
    }

    fun onItemSwiped(position: Int, direction:Int) {
        if (direction == ItemTouchHelper.LEFT){ // 左滑
            onSwipedListener(categoryWithEventInfList[position].category, position)
            Log.d("CategoryAdapterCMF", "onItemSwipedLEFT: $position, $direction")
        }
        else if (direction == ItemTouchHelper.RIGHT){ // 右滑
            swipeSelectCategory(categoryWithEventInfList[position].category, position)
            Log.d("CategoryAdapterCMF", "onItemSwipedRIGHT: $position, $direction")
        }
    }

    private fun updateItemsBackground(itemView: View, level: Float, color: Int, selected: Boolean){
//        Log.d("CategoryAdapterCMF", "updateItemsBackground: view: $itemView, level: $level, color: $color")
        Log.d("CategoryAdapterCMF", "update Background start")
        itemView.apply{
            val levelWidth = (width * level * 0.85).toInt()

            // 更新背景的宽度
            val backgroundDrawable = ContextCompat.getDrawable(context,
                R.drawable.background_item_main) as LayerDrawable

            val colorLevelBar = backgroundDrawable.findIndexByLayerId(R.id.color_level_bar)
            val colorLevalBarDrawable = backgroundDrawable.getDrawable(colorLevelBar) as GradientDrawable
            colorLevalBarDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC)
            backgroundDrawable.setLayerWidth(colorLevelBar, levelWidth)
            // 选中模式下需要修改背景颜色
            val drawableBackground = backgroundDrawable.findIndexByLayerId(R.id.background)
            val drawableBackgroundDrawable = backgroundDrawable.getDrawable(drawableBackground) as GradientDrawable
            if (workMode == WorkMode.SELECT && selected){ // 选中模式下需要修改背景颜色
                Log.d("CategoryAdapterCMF", "update Background selected:${selected}")
                val backgroundColor = R.color.blue_42eeff
                drawableBackgroundDrawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context,
                    backgroundColor), PorterDuff.Mode.SRC)
            }else{// 非选中模式下背景颜色为透明
                Log.d("CategoryAdapterCMF", "update Background selected:${selected}")
                val backgroundColor = R.color.transparent
                drawableBackgroundDrawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context,backgroundColor),
                    PorterDuff.Mode.SRC)
            }

//            Log.d("EventAdapter", "updateItemsBackground levelWidth: $level, $levelWidth")
            background = backgroundDrawable
            invalidate() // 刷新视图
//            Log.d("CategoryAdapterCMF", "updateItemsBackground finish levelWidth: $level, $levelWidth")
        }
        Log.d("CategoryAdapterCMF", "update Background end")
    }
}