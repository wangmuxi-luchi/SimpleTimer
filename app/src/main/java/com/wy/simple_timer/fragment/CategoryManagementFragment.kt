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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


class CategoryManagementFragment : Fragment() {
    private lateinit var binding: FragmentCategoryManagementBinding
    private lateinit var viewModel: CategoryWEIViewModel
    private lateinit var categoryAdapter: CategoryAdapterCMF

    //    private lateinit var onCreatedListener: () -> Unit
//    private lateinit var onBlankClickListener: () -> Unit
    private lateinit var categotyWithEventInfMutableStateFlow: MutableStateFlow<Flow<List<CategoryWithEventInf>>>
    private var startCalendar: Calendar = Calendar.getInstance()
    private var endCalendar: Calendar = Calendar.getInstance()


//    fun setOnBlankClickListener(listener: () -> Unit) {
//        onBlankClickListener = listener
//    }
//    fun setOnCreatedListener(listener: () -> Unit) {
//        onCreatedListener = listener
//    }

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
//        onCreatedListener()
        binding.categoryListRecycleView.setOnClickListener(OnRecycleViewClickListener())
    }

    // 设置RecycleView的点击事件回调
    inner class OnRecycleViewClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            v?.let {
                onRecycleViewClick(it)
            }
        }
    }

//    fun isCategorySelected(categoryID: Long): Boolean {
//        return listenerIsCategorySelected(categoryID)
//    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CategoryWEIViewModel::class.java]

        startCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)
        endCalendar.add(Calendar.MILLISECOND, -1)

        viewModel.setDateMode(
            CategoryWEIViewModel.DATA_MODE_ALL_UNARCHIVED,
            startCalendar,
            endCalendar
        )
        categotyWithEventInfMutableStateFlow = MutableStateFlow(viewModel.get_Categories())
        Log.d(
            "CategoryManagementFragment",
            "setupViewModel: startCalendar: ${startCalendar.time} endCalendar: ${endCalendar.time}"
        )
    }

    private fun setupRecyclerView() {
        binding.categoryListRecycleView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = CategoryAdapterCMF().also { categoryAdapter = it }

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

    private fun refreshCategoryWithEventInf() {
        startCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)
        endCalendar.add(Calendar.MILLISECOND, -1)

        viewModel.setDateMode(
            CategoryWEIViewModel.DATA_MODE_ALL_UNARCHIVED,
            startCalendar,
            endCalendar
        )
        categotyWithEventInfMutableStateFlow.value = viewModel.get_Categories()
        Log.d(
            "CategoryManagementFragment",
            "refreshCategory: startCalendar: ${startCalendar.time} endCalendar: ${endCalendar.time}"
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categotyWithEventInfMutableStateFlow.flatMapLatest {

                Log.d("CategoryManagementFragment", "categories flatMapLatest:${it}")
                it
            }.collect { categories ->
                withContext(Dispatchers.Main) {
                    categoryAdapter.setData(categories)
                }
            }
        }
        Log.d("CategoryManagementFragment", "setOnItemMovedListener")
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

            setOnBindViewHolder { categoryWithEventInf, position ->
                // 最开始的版本中在这里更新 position,现在这个功能已经在其他位置实现了
//                Log.d("CategoryManagementFragment", "category position update:" +
//                        "categoryname: ${categoryWithEventInf.category.categoryName}; oldposition: ${categoryWithEventInf.category.position} position:${position}")
//                categoryWithEventInf.apply {
//                    if (position != category.position) {
//                        category.position = position
//                        viewModel.updateCategory(category)
//                    }
//                }
            }
            setOnUpdateCPListener { categoryWithEventInfList ->
                categoryWithEventInfList.withIndex().forEach { (position, categoryWithEventInf) ->
                    categoryWithEventInf.apply {
                        if (position != category.position) {
                            Log.d(
                                "CategoryManagementFragment", "category position update:" +
                                        "categoryname: ${categoryWithEventInf.category.categoryName}; oldposition: ${categoryWithEventInf.category.position} position:${position}"
                            )
                            category.position = position
                            viewModel.updateCategory(category)
                        }
                    }
                }
            }

            setOnSCChangedListener { onSCCListener() }

            categoryAdapter.setOnWorkModeChangeListener { workMode ->
                onWorkModeChangeListener(
                    workMode
                )
            }

            // adapter的功能接口
            isCategorySelected = { categoryID ->
                isSelected(categoryID)
            }
            unSelectAllCategory = {unSelectAll()}
        }
    }

    var selectedColor = "#808080" // 默认颜色
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
        // 初始名为空字符串，默认颜色使用当前选中颜色
        categoryDialog.show("", selectedColor)
    }

    private fun fastSaveRecord(cateegoryID: Long, position: Int) {
        lifecycleScope.launch {
            val eventDao = MyDatabase.getDatabase(requireContext()).eventDao()
            val allEvents = eventDao.getAllEvents().firstOrNull()
            val latestEvent = allEvents?.maxByOrNull { it.endTime.timeInMillis }

            // 结束时间为当前时间
            val endTime = Calendar.getInstance()

            val dayStartCalendar = Calendar.getInstance()
            dayStartCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val startTime = (latestEvent?.endTime ?: dayStartCalendar)
            if (startTime.timeInMillis > Calendar.getInstance()
                    .apply { add(Calendar.MINUTE, -1) }.timeInMillis
            ) {
                //Toast
                Toast.makeText(requireContext(), "时长不能小于一分钟", Toast.LENGTH_SHORT).show()
                categoryAdapter.notifyItemChanged(position)
                return@launch
            }
            val remark = ""
            // 保存记录到数据库
            viewModel.insertEvent(startTime, endTime, cateegoryID, remark)
        }
    }

    // 设置回调函数，如果要实现对应的功能，需要调用以下函数设置对应的回调
    private var onRecycleViewClick: (View) -> Unit = {}
    fun setOnRecycleViewClickListener(listener: (View) -> Unit) {// 整个recycleview区域被点击
        onRecycleViewClick = listener
    }

    // adapter的回调中本层没有处理的部分，将其包装后传递到上一层
    private var onSCCListener: () -> Unit = {}//onSelectedCategoryChangedListener,更新选中的分类列表回调
    fun setOnSCCListener(listener: () -> Unit) {
        onSCCListener = listener
    }

    private var onWorkModeChangeListener: (WorkMode) -> Unit = { _ -> }//更新工作模式回调，目前主要用来通知是否出于选中状态
    fun setOnWorkModeChangeListener(listener: (WorkMode) -> Unit) {
        onWorkModeChangeListener = listener
    }


    // adapter对外接口,将adapter的功能暴露给外界
    var isCategorySelected: (Long) -> Boolean = { _ -> true } // 用于传递分类选择状态的回调函数,由分类管理fragment负责处理
    var unSelectAllCategory: () -> Unit = {}
}