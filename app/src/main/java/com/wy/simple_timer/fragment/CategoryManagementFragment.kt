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
import com.wy.simple_timer.DatabaseManagementService
import com.wy.simple_timer.adapter.CategoryAdapterCM
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.MyDatabase
import com.wy.simple_timer.databinding.FragmentCategoryManagementBinding
import com.wy.simple_timer.utils.ItemTouchCallbackCM
import com.wy.simple_timer.viewmodel.CategoryViewModel
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


class CategoryManagementFragment : Fragment() {
    private lateinit var binding: FragmentCategoryManagementBinding
    private lateinit var viewModel: CategoryViewModel
    private lateinit var categoryAdapter: CategoryAdapterCM
    private lateinit var onCreatedListener: () -> Unit
    private lateinit var onBlankClickListener: () -> Unit
    fun setOnBlankClickListener(listener: () -> Unit) {
        onBlankClickListener = listener
    }
    fun setOnCreatedListener(listener: () -> Unit) {
        onCreatedListener = listener
    }

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
        onCreatedListener()
    }


    fun setOnClickListener(listener: View.OnClickListener) {
        binding.categoryListRecycleView.setOnClickListener(listener)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        viewModel.setCategories(viewModel.getCategoryDao().getUnarchivedRootCategoriesOrderedByPosition())
    }

    private fun setupRecyclerView() {
        binding.categoryListRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = CategoryAdapterCM().also {categoryAdapter = it}

            val itemTouchHelper = ItemTouchHelper(ItemTouchCallbackCM(categoryAdapter))
            itemTouchHelper.attachToRecyclerView(this)
        }
        observeCategories()
    }

    private fun observeCategories() {
        var shouldTriggerCollect = true
        viewLifecycleOwner.lifecycleScope.launch {
            if (shouldTriggerCollect) {
                viewModel.getCategories()?.conflate()?.collect { categories ->
                    categoryAdapter.setData(categories)
                    Log.d("CategoryManagementFragment", "categories collect: $categories")
                }
            }else{
                // 不触发collect
                Log.d("CategoryManagementFragment", "categories collect: not trigger")
            }
        }
        Log.d("CategoryManagementFragment", "setOnItemMovedListener")
        categoryAdapter.setOnItemMovedListener { fromPosition, toPosition ->
            shouldTriggerCollect = false
            for (i in maxOf(fromPosition, toPosition) downTo minOf(fromPosition, toPosition)) {
                val category = categoryAdapter.getCategory(i)
                Log.d("CategoryManagementFragment", "category position update:" +
                        "categoryname: ${category.categoryName}; oldposition: ${category.position} position:${i}")
                category.position = i
                viewModel.updateCategory(category)
            }
            shouldTriggerCollect = true
        }
    }

    private fun setupAdapterCallbacks() {
        categoryAdapter.apply {
            setOnOtherItemClickListener { categoryID ->
                startActivity(Intent(requireContext(), CategoryDetailActivity::class.java).apply {
                    putExtra("categoryID", categoryID)
                })
            }
            
            setOnLastItemClickListener { showAddCategoryDialog() }

            setOnSwipedListener { category ->
                saveRecord(category.id)
            }
            
            setOnBindViewHolder { category, position ->
                // 在这里更新 position
                if (position != category.position) {
                    category.position = position
                    viewModel.updateCategory(category)
                }
            }
        }
    }
    var selectedColor = "#808080" // 默认颜色
    private fun showAddCategoryDialog() {
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

    private fun saveRecord(cateegoryID: Long) {
        lifecycleScope.launch {
            val eventDao = MyDatabase.getDatabase(requireContext()).eventDao()
            val allEvents = eventDao.getAllEvents().firstOrNull()
            val latestEvent = allEvents?.maxByOrNull { it.endTime.time }

            // 当前时间转换为Date
            val currentTime = System.currentTimeMillis()
            val currentDate = Date(currentTime)

            var _startTime = (latestEvent?.endTime ?: currentDate).time
            // 获取当前时间
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentTime
            // 如果startTime为-1，或者距离当前时间相差24小时以上，设置为当天的0点
            if (_startTime == -1L||_startTime+24*60*60*1000<currentTime) {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                _startTime = calendar.timeInMillis
            }else if(_startTime>currentTime){
                _startTime = currentTime
            }
            // 设置开始时间
            val endTime = currentDate
            val startTime = Date(_startTime)

            val remark = ""

            // 保存记录到数据库
            val event = Event(0, startTime, endTime, cateegoryID, remark)
            val intent = Intent(requireContext(), DatabaseManagementService::class.java).apply {
                action = "INSERT_EVENT"
                putExtra("object", event)
            }
            requireActivity().startService(intent)
        }
    }
}