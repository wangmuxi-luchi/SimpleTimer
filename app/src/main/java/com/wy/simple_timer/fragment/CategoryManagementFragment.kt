package com.wy.simple_timer.fragment

import CategoryDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.wy.simple_timer.SimpleTimerApplication
import com.wy.simple_timer.CategoryDetailActivity
import com.wy.simple_timer.adapter.CategoryAdapterCMF
import com.wy.simple_timer.adapter.WorkMode
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.CategoryWithEventInf
import com.wy.simple_timer.database.MyDatabase
import com.wy.simple_timer.databinding.FragmentCategoryManagementBinding
import com.wy.simple_timer.utils.ItemTouchCallbackCMF
import com.wy.simple_timer.utils.resetToStartOfPeriod
import com.wy.simple_timer.viewmodel.CategoryWEIViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


class CategoryManagementFragment : Fragment() {
    private lateinit var binding: FragmentCategoryManagementBinding
    private lateinit var viewModel: CategoryWEIViewModel
    private lateinit var categoryAdapter: CategoryAdapterCMF

    // 全局状态管理器
    private val categorySelectionState by lazy {
        (requireContext().applicationContext as SimpleTimerApplication).categorySelectionState
    }

    private lateinit var categotyWithEventInfMutableStateFlow: MutableStateFlow<Flow<List<CategoryWithEventInf>>>
    private var startCalendar: Calendar = Calendar.getInstance()
    private var endCalendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupAdapterCallbacks()
        setupStateObservers()
        binding.categoryListRecycleView.setOnClickListener(OnRecycleViewClickListener())
    }

    inner class OnRecycleViewClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            v?.let {
                onRecycleViewClick(it)
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CategoryWEIViewModel::class.java]

        startCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)
        endCalendar.add(Calendar.MILLISECOND, -1)

        val initialShowArchived = categorySelectionState.showArchived.value
        val dataMode = if (initialShowArchived) {
            CategoryWEIViewModel.DATA_MODE_ALL_ARCHIVED
        } else {
            CategoryWEIViewModel.DATA_MODE_ALL_UNARCHIVED
        }

        viewModel.setDateMode(dataMode, startCalendar, endCalendar)
        categotyWithEventInfMutableStateFlow = MutableStateFlow(viewModel.get_Categories())
    }

    private fun setupRecyclerView() {
        binding.categoryListRecycleView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = CategoryAdapterCMF().also { 
                categoryAdapter = it
                // 注入全局状态管理器
                categoryAdapter.categorySelectionState = categorySelectionState
            }

            val itemTouchHelper = ItemTouchHelper(ItemTouchCallbackCMF(categoryAdapter))
            itemTouchHelper.attachToRecyclerView(this)
        }
        observeCategories()
    }

    fun setTimeRange(start: Calendar, end: Calendar) {
        startCalendar = start
        endCalendar = end
        refreshCategoryWithEventInf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categotyWithEventInfMutableStateFlow.flatMapLatest {
                it
            }.collect { categories ->
                withContext(Dispatchers.Main) {
                    categoryAdapter.setData(categories)
                }
            }
        }
    }

    private fun setupAdapterCallbacks() {
        categoryAdapter.apply {
            setOnItemClickListener { categoryID ->
                startActivity(Intent(requireContext(), CategoryDetailActivity::class.java).apply {
                    putExtra("categoryID", categoryID)
                })
            }

            setOnSwipedListener { category, position ->
                fastSaveRecord(category.id, position)
            }

            setOnUpdateCPListener { categoryWithEventInfList ->
                categoryWithEventInfList.withIndex().forEach { (position, categoryWithEventInf) ->
                    categoryWithEventInf.apply {
                        if (position != category.position) {
                            category.position = position
                            viewModel.updateCategory(category)
                        }
                    }
                }
            }

            // adapter 的功能接口 - 使用 StateFlow
            isCategorySelected = { categoryID ->
                categorySelectionState.isSelected(categoryID)
            }
            unSelectAllCategory = {
                categorySelectionState.clearAllSelection()
            }
        }
    }

    // 观察 StateFlow 状态变化 - 状态变化时自动刷新相关组件
    private fun setupStateObservers() {
        // 观察选中状态变化 - 自动刷新 adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categorySelectionState.selectedCategoryIds.collect {
                    // 刷新 adapter 视图以反映选中状态变化
                    categoryAdapter.notifyDataSetChanged()
                }
            }
        }

        // 观察工作模式变化 - MainActivity 直接订阅 StateFlow，这里无需处理

        // 观察归档状态变化 - 自动刷新分类列表
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categorySelectionState.showArchived.collect {
                    refreshCategoryWithEventInf()
                }
            }
        }

        // 观察时间范围变化 - 自动刷新分类列表（同时监听开始和结束时间）
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    categorySelectionState.startCalendar,
                    categorySelectionState.endCalendar
                ) { start, end ->
                    Pair(start, end)
                }.collect { (start, end) ->
                    startCalendar = start
                    endCalendar = end
                    refreshCategoryWithEventInf()
                }
            }
        }
    }

    var selectedColor = "#808080"
    fun showAddCategoryDialog() {
        val categoryDialog = CategoryDialog(requireActivity()).apply {
            setListener(object : CategoryDialog.CategoryDialogListener {
                override fun onConfirmEdit(newName: String, newColor: String) {
                    if (newName.isNotEmpty()) {
                        val newCategory = Category(
                            0,
                            newName,
                            newColor,
                            categoryAdapter.itemCount - 1,
                            false,
                            -1
                        )
                        viewModel.insertCategory(newCategory)
                    }
                }

                override fun onPickColor(newColor: String) {
                    selectedColor = newColor
                }
            })
        }
        categoryDialog.show("", selectedColor)
    }

    private fun fastSaveRecord(cateegoryID: Long, position: Int) {
        lifecycleScope.launch {
            val eventDao = MyDatabase.getDatabase(requireContext()).eventDao()
            val allEvents = eventDao.getAllEvents().firstOrNull()
            val latestEvent = allEvents?.maxByOrNull { it.endTime.timeInMillis }

            val endTime = Calendar.getInstance()

            val dayStartCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var startTime = dayStartCalendar
            if (latestEvent != null) {
                val timeDiff = endTime.timeInMillis - latestEvent.endTime.timeInMillis
                val twentyFourHours = 24 * 60 * 60 * 1000L

                if (timeDiff <= twentyFourHours) {
                    startTime = latestEvent.endTime
                }
            }

            if (startTime.timeInMillis > endTime.timeInMillis - 60 * 1000) {
                Toast.makeText(requireContext(), "时长不能小于一分钟", Toast.LENGTH_SHORT).show()
                categoryAdapter.notifyItemChanged(position)
                return@launch
            }
            val remark = ""
            viewModel.insertEvent(startTime, endTime, cateegoryID, remark)
        }
    }

    // RecyclerView 点击回调
    private var onRecycleViewClick: (View) -> Unit = {}
    fun setOnRecycleViewClickListener(listener: (View) -> Unit) {
        onRecycleViewClick = listener
    }

    

    // 对外接口 - 切换归档状态
    fun toggleArchiveStatus(): Boolean {
        return categorySelectionState.toggleArchiveStatus()
    }

    // 对外接口 - 获取当前归档状态
    fun getShowArchived(): Boolean {
        return categorySelectionState.showArchived.value
    }

    // 对外接口 - 检查分类是否被选中
    fun isCategorySelected(categoryId: Long): Boolean {
        return categorySelectionState.isSelected(categoryId)
    }

    // 对外接口 - 选中分类
    fun selectCategory(categoryId: Long) {
        categorySelectionState.selectCategory(categoryId)
    }

    // 对外接口 - 取消选中分类
    fun unSelectCategory(categoryId: Long) {
        categorySelectionState.unSelectCategory(categoryId)
    }

    // 对外接口 - 取消所有选中
    fun unSelectAll() {
        categorySelectionState.clearAllSelection()
    }

    private fun refreshCategoryWithEventInf() {
        startCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)
        endCalendar.add(Calendar.MILLISECOND, -1)

        val dataMode = if (categorySelectionState.showArchived.value) {
            CategoryWEIViewModel.DATA_MODE_ALL_ARCHIVED
        } else {
            CategoryWEIViewModel.DATA_MODE_ALL_UNARCHIVED
        }

        viewModel.setDateMode(dataMode, startCalendar, endCalendar)
        categotyWithEventInfMutableStateFlow.value = viewModel.get_Categories()
    }
}