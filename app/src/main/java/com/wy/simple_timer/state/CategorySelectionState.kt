package com.wy.simple_timer.state

import com.wy.simple_timer.adapter.WorkMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class CategorySelectionState {
    // 选中的分类 ID 列表
    private val _selectedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCategoryIds: StateFlow<Set<Long>> = _selectedCategoryIds.asStateFlow()

    // 工作模式
    private val _workMode = MutableStateFlow<WorkMode>(WorkMode.NORMAL)
    val workMode: StateFlow<WorkMode> = _workMode.asStateFlow()

    // 时间范围
    private val _startCalendar = MutableStateFlow<Calendar>(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    })
    val startCalendar: StateFlow<Calendar> = _startCalendar.asStateFlow()

    private val _endCalendar = MutableStateFlow<Calendar>(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    })
    val endCalendar: StateFlow<Calendar> = _endCalendar.asStateFlow()

    // 归档状态
    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    // 选中分类
    fun selectCategory(categoryId: Long) {
        _selectedCategoryIds.value = _selectedCategoryIds.value + categoryId
        // 如果之前是普通模式，切换到选择模式
        if (_workMode.value == WorkMode.NORMAL) {
            _workMode.value = WorkMode.SELECT
        }
    }

    // 取消选中分类
    fun unSelectCategory(categoryId: Long) {
        val newSelection = _selectedCategoryIds.value - categoryId
        _selectedCategoryIds.value = newSelection
        // 如果取消后没有选中项，切换回普通模式
        if (newSelection.isEmpty()) {
            _workMode.value = WorkMode.NORMAL
        }
    }

    // 切换分类选中状态
    fun toggleCategory(categoryId: Long) {
        val current = _selectedCategoryIds.value
        _selectedCategoryIds.value = if (current.contains(categoryId)) {
            val newSelection = current - categoryId
            if (newSelection.isEmpty()) {
                _workMode.value = WorkMode.NORMAL
            }
            newSelection
        } else {
            _workMode.value = WorkMode.SELECT
            current + categoryId
        }
    }

    // 批量选中分类（用于连续右滑）
    fun selectCategories(categoryIds: List<Long>) {
        _selectedCategoryIds.value = _selectedCategoryIds.value + categoryIds.toSet()
        _workMode.value = WorkMode.SELECT
    }

    // 取消所有选中
    fun clearAllSelection() {
        _selectedCategoryIds.value = emptySet()
        _workMode.value = WorkMode.NORMAL
    }

    // 检查分类是否被选中
    fun isSelected(categoryId: Long): Boolean {
        // NORMAL 模式下显示所有事件（所有分类都视为选中）
        if (_workMode.value == WorkMode.NORMAL) {
            return true
        }
        return _selectedCategoryIds.value.contains(categoryId)
    }

    // 设置工作模式
    fun setWorkMode(mode: WorkMode) {
        _workMode.value = mode
        if (mode == WorkMode.NORMAL) {
            clearAllSelection()
        }
    }

    // 设置时间范围
    fun setTimeRange(start: Calendar, end: Calendar) {
        _startCalendar.value = Calendar.getInstance().apply {
            timeInMillis = start.timeInMillis
        }
        _endCalendar.value = Calendar.getInstance().apply {
            timeInMillis = end.timeInMillis
        }
    }

    // 切换归档状态
    fun toggleArchiveStatus(): Boolean {
        _showArchived.value = !_showArchived.value
        return _showArchived.value
    }

    // 设置归档状态
    fun setShowArchived(show: Boolean) {
        _showArchived.value = show
    }
}