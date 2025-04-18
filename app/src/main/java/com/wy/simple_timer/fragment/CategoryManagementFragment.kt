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
    private var onRecycleViewClick: (View) -> Unit = {}
    private lateinit var categotyWithEventInfMutableStateFlow : MutableStateFlow<Flow<List<CategoryWithEventInf>>>
    private var startCalendar: Calendar = Calendar.getInstance()
    private var endCalendar: Calendar = Calendar.getInstance()
    private var listenerIsCategorySelected: (Long) -> Boolean = { _ ->true} // 用于传递分类选择状态的回调函数,由分类管理fragment负责处理


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
    inner class OnRecycleViewClickListener: View.OnClickListener {
        override fun onClick(v: View?) {
            v?.let {
                onRecycleViewClick(it)
            }
        }
    }
    fun setOnRecycleViewClickListener(listener: (View) -> Unit) {
        onRecycleViewClick = listener
    }

    fun isCategorySelected(categoryID: Long): Boolean {
        return listenerIsCategorySelected(categoryID)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CategoryWEIViewModel::class.java]

        startCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)
        endCalendar.add(Calendar.MILLISECOND, -1)

        viewModel.setDateMode(CategoryWEIViewModel.DATA_MODE_ALL_UNARCHIVED, startCalendar, endCalendar)
        categotyWithEventInfMutableStateFlow = MutableStateFlow(viewModel.get_Categories())
        Log.d("CategoryManagementFragment", "setupViewModel: startCalendar: ${startCalendar.time} endCalendar: ${endCalendar.time}")
    }

    private fun setupRecyclerView() {
        binding.categoryListRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = CategoryAdapterCMF().also {categoryAdapter = it}

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
    fun refreshCategoryWithEventInf() {
        startCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)
        endCalendar.add(Calendar.MILLISECOND, -1)

        viewModel.setDateMode(CategoryWEIViewModel.DATA_MODE_ALL_UNARCHIVED, startCalendar, endCalendar)
        categotyWithEventInfMutableStateFlow.value = viewModel.get_Categories()
        Log.d("CategoryManagementFragment", "refreshCategory: startCalendar: ${startCalendar.time} endCalendar: ${endCalendar.time}")
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCategories() {
        var shouldTriggerCollect = true
        viewLifecycleOwner.lifecycleScope.launch {
            categotyWithEventInfMutableStateFlow.flatMapLatest {

                Log.d("CategoryManagementFragment", "categories flatMapLatest:${it}")
                it
//                emit("")
            }.collect { categories ->
//                Log.d("CategoryManagementFragment", "categories collect:")
                withContext(Dispatchers.Main) {
                    categoryAdapter.setData(categories)
                }
//                Log.d("CategoryManagementFragment", "categories collect: $categories")
            }


//            if (shouldTriggerCollect) {
//                categotyWithEventInfMutableStateFlow.value.collect { categories ->
//                    categoryAdapter.setData(categories)
//                    Log.d("CategoryManagementFragment", "categories collect: $categories")
//                }
//            }else{
//                // 不触发collect
//                Log.d("CategoryManagementFragment", "categories collect: not trigger")
//            }
        }
        Log.d("CategoryManagementFragment", "setOnItemMovedListener")
//        categoryAdapter.setOnItemMovedListener { fromPosition, toPosition ->
//            shouldTriggerCollect = false
//            for (i in maxOf(fromPosition, toPosition) downTo minOf(fromPosition, toPosition)) {
//                val category = categoryAdapter.getCategory(i)
//                Log.d("CategoryManagementFragment", "category position update:" +
//                        "categoryname: ${category.categoryName}; oldposition: ${category.position} position:${i}")
//                category.position = i
//                viewModel.updateCategory(category)
//            }
//            shouldTriggerCollect = true
//        }
    }

    // callbacks of adapter
    private var onSCCListener: () -> Unit = {}//onSelectedCategoryChangedListener
    fun setOnSCCListener(listener:() -> Unit){
        onSCCListener = listener
    }
    private fun SCCListener(){
        onSCCListener()
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
                categoryWithEventInfList.withIndex().forEach{ (position, categoryWithEventInf) ->
                    categoryWithEventInf.apply {
                        if (position != category.position) {
                            Log.d("CategoryManagementFragment", "category position update:" +
                                    "categoryname: ${categoryWithEventInf.category.categoryName}; oldposition: ${categoryWithEventInf.category.position} position:${position}")
                            category.position = position
                            viewModel.updateCategory(category)
                        }
                    }
                }
            }
            // 调用adapter的函数来实现类型选中判断
            listenerIsCategorySelected = { categoryID ->
                isSelected(categoryID)
            }

            setOnSCChangedListener { SCCListener() }
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
            if (startTime.timeInMillis>Calendar.getInstance().apply { add(Calendar.MINUTE, -1) }.timeInMillis) {
                //Toast
                Toast.makeText(requireContext(), "时长不能小于一分钟", Toast.LENGTH_SHORT).show()
                categoryAdapter.notifyItemChanged(position)
                return@launch
            }
//            currentCalendar.apply {
//                add(Calendar.DAY_OF_WEEK, -1)
//                // 如果startTime距离当前时间相差24小时以上，设置为当天的0点
//                if (startTime.timeInMillis< timeInMillis) {
//                    add(Calendar.DAY_OF_WEEK, 1)
//                    set(Calendar.HOUR_OF_DAY, 0)
//                    set(Calendar.MINUTE, 0)
//                    set(Calendar.SECOND, 0)
//                    return@apply
//                }
//                add(Calendar.DAY_OF_WEEK, 1)
//                add(Calendar.MINUTE, -1)
//                // 如果startTime距离当前时间相差1分钟以内，return
//                if (startTime.timeInMillis>timeInMillis) {
//                    //Toast
//                    Toast.makeText(requireContext(), "时长不能小于一分钟", Toast.LENGTH_SHORT).show()
//                    categoryAdapter.notifyItemChanged(position)
//                    return@launch
//                }
//            }
//            val startTime = Date(_startTime)

            val remark = ""
//            setOnLastItemClickListener { showAddCategoryDialog() }
            // 保存记录到数据库
            viewModel.insertEvent(startTime, endTime, cateegoryID, remark)
//            val event = Event(0, startTime, endTime, cateegoryID, remark)
//            val intent = Intent(requireContext(), DatabaseManagementService::class.java).apply {
//                action = "INSERT_EVENT"
//                putExtra("object", event)
//            }
//            requireActivity().startService(intent)
        }
    }
}