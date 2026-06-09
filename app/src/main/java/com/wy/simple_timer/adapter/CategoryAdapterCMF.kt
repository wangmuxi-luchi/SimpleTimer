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
import com.wy.simple_timer.state.CategorySelectionState
import java.util.Collections
import java.util.Timer
import java.util.TimerTask

class CategoryWithEventInfDiffUtilCallback : DiffUtil.ItemCallback<CategoryWithEventInf>() {
    override fun areItemsTheSame(oldItem: CategoryWithEventInf, newItem: CategoryWithEventInf): Boolean {
        return oldItem.category.id == newItem.category.id
    }
    override fun areContentsTheSame(oldItem: CategoryWithEventInf, newItem: CategoryWithEventInf): Boolean {
        return oldItem == newItem
    }
}

enum class WorkMode {
    NORMAL,
    SELECT
}

class CategoryAdapterCMF : ListAdapter<CategoryWithEventInf, CategoryAdapterCMF.CategoryViewHolder>(
    CategoryWithEventInfDiffUtilCallback()
) {
    // 注入全局状态管理器（替代本地状态）
    lateinit var categorySelectionState: CategorySelectionState
    
    private var mulSelectFlag: Boolean = false
    private var mulSelectStart: Int = 0
    private var categoryWithEventInfList: List<CategoryWithEventInf> = emptyList()

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorDot: ImageView = itemView.findViewById(R.id.color_dot)
        private val categoryText: TextView = itemView.findViewById(R.id.category_name)
        private val categoryDuration: TextView = itemView.findViewById(R.id.total_duration)

        fun bind(position: Int) {
            val categoryWithEventInf = categoryWithEventInfList[position]
            val color = Color.parseColor(categoryWithEventInf.category.categoryColor)
            colorDot.setColorFilter(color)
            categoryText.text = categoryWithEventInf.category.categoryName

            val hours = categoryWithEventInf.totalDuration / (1000 * 60 * 60)
            val minutes = (categoryWithEventInf.totalDuration % (1000 * 60 * 60)) / (1000 * 60)
            try {
                categoryDuration.text = when {
                    hours > 0 -> String.format("%2d小时%02d分钟", hours, minutes)
                    minutes > 0 -> String.format("%2d分钟", minutes)
                    else -> ""
                }
            } catch (e: Exception) {
                Log.e("EventAdapter", "Error calculating duration", e)
                categoryDuration.text = "时间格式错误"
            }

            itemView.setOnClickListener {
                if (getWorkMode() == WorkMode.NORMAL) {
                    onItemClickListener(categoryWithEventInf.category.id)
                } else if (getWorkMode() == WorkMode.SELECT) {
                    mulSelectFlag = false
                    if (isSelected(categoryWithEventInf.category)) {
                        unSelect(categoryWithEventInf.category)
                    } else {
                        select(categoryWithEventInf.category)
                    }
                }
            }
            onBindViewHolder(categoryWithEventInf, absoluteAdapterPosition)

            val selected = isSelected(categoryWithEventInf.category)
            itemView.post {
                updateItemsBackground(itemView, categoryWithEventInf.timeRatioToMax, color, selected)
            }
        }
    }

    fun setData(newData: List<CategoryWithEventInf>) {
        categoryWithEventInfList = newData.filter { it.category.parentId == -1L }
        submitList(categoryWithEventInfList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_management, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(position)
    }

    // 选中项目管理 - 使用全局状态
    private fun getWorkMode(): WorkMode {
        return categorySelectionState.workMode.value
    }

    private fun setWorkMode(mode: WorkMode) {
        categorySelectionState.setWorkMode(mode)
    }

    fun select(category: Category) {
        // 使用全局状态管理
        categorySelectionState.selectCategory(category.id)
    }

    private fun mulSelect(mulPosition: Int) {
        val startIdx = minOf(mulSelectStart, mulPosition)
        val endIdx = maxOf(mulSelectStart, mulPosition)
        for (i in startIdx..endIdx) {
            val category = categoryWithEventInfList[i].category
            categorySelectionState.selectCategory(category.id)
        }
    }

    fun unSelect(category: Category) {
        categorySelectionState.unSelectCategory(category.id)
    }

    fun unSelectAll() {
        categorySelectionState.clearAllSelection()
    }

    fun isSelected(category: Category): Boolean {
        return categorySelectionState.isSelected(category.id)
    }

    fun isSelected(categoryID: Long): Boolean {
        return categorySelectionState.isSelected(categoryID)
    }

    fun isSelected(position: Int): Boolean {
        return categorySelectionState.isSelected(categoryWithEventInfList[position].category.id)
    }

    private fun swipeSelectCategory(category: Category, position: Int) {
        mulSelectFlag = !mulSelectFlag
        select(category)
        if (mulSelectFlag) {
            mulSelectStart = position
        } else {
            mulSelect(position)
        }
    }

    fun onItemSwiped(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.LEFT) {
            onSwipedListener(categoryWithEventInfList[position].category, position)
        } else if (direction == ItemTouchHelper.RIGHT) {
            swipeSelectCategory(categoryWithEventInfList[position].category, position)
        }
    }

    fun updateCategoryPosition() {
        onUpdateCPListener(categoryWithEventInfList)
    }

    private var lastMoveTime = 0L
    fun onItemSwap(position1: Int, position2: Int) {
        Collections.swap(categoryWithEventInfList, position1, position2)
        notifyItemMoved(position1, position2)
        lastMoveTime = System.currentTimeMillis()
        Timer().schedule(
            object : TimerTask() {
                val startTime = lastMoveTime
                override fun run() {
                    if (startTime == lastMoveTime) {
                        updateCategoryPosition()
                    }
                }
            }, 1000
        )
    }

    private fun updateItemsBackground(itemView: View, level: Float, color: Int, selected: Boolean) {
        itemView.apply {
            val levelWidth = (width * level * 0.85).toInt()
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.background_item_category) as LayerDrawable
            val colorLevelBar = backgroundDrawable.findIndexByLayerId(R.id.color_level_bar)
            val colorLevalBarDrawable = backgroundDrawable.getDrawable(colorLevelBar) as GradientDrawable
            colorLevalBarDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC)
            backgroundDrawable.setLayerWidth(colorLevelBar, levelWidth)

            val drawableBackground = backgroundDrawable.findIndexByLayerId(R.id.background)
            val drawableBackgroundDrawable = backgroundDrawable.getDrawable(drawableBackground) as GradientDrawable

            if (getWorkMode() == WorkMode.SELECT && selected) {
                val backgroundColor = R.color.blue_42eeff
                drawableBackgroundDrawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, backgroundColor), PorterDuff.Mode.SRC)
            } else {
                val backgroundColor = R.color.transparent
                drawableBackgroundDrawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, backgroundColor), PorterDuff.Mode.SRC)
            }

            background = backgroundDrawable
            invalidate()
        }
    }

    // 设置回调函数
    private var onItemClickListener: (Long) -> Unit = { _ -> }
    fun setOnItemClickListener(listener: (Long) -> Unit) {
        onItemClickListener = listener
    }

    private var onBindViewHolder: (CategoryWithEventInf, Int) -> Unit = { _, _ -> }
    fun setOnBindViewHolder(listener: (CategoryWithEventInf, Int) -> Unit) {
        onBindViewHolder = listener
    }

    private var onSwipedListener: (Category, Int) -> Unit = { _, _ -> }
    fun setOnSwipedListener(listener: (Category, Int) -> Unit) {
        onSwipedListener = listener
    }

    // 移除 onSCCListener，因为状态已经通过 StateFlow 自动同步
    // private var onSCCListener: () -> Unit = {}
    // fun setOnSCChangedListener(listener: () -> Unit) {
    //     onSCCListener = listener
    // }

    private var onUpdateCPListener: (List<CategoryWithEventInf>) -> Unit = { _ -> }
    fun setOnUpdateCPListener(listener: (List<CategoryWithEventInf>) -> Unit) {
        onUpdateCPListener = listener
    }

    private var onWorkModeChangeListener: (WorkMode) -> Unit = { _ -> }
    fun setOnWorkModeChangeListener(listener: (WorkMode) -> Unit) {
        onWorkModeChangeListener = listener
    }

    // 外部功能接口（保持向后兼容）
    var isCategorySelected: (Long) -> Boolean = { _ -> true }
    var unSelectAllCategory: () -> Unit = {}
}