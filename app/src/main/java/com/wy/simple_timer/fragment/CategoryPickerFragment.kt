package com.wy.simple_timer.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.wy.simple_timer.R
import com.wy.simple_timer.adapter.CategoryAdapterTR
import com.wy.simple_timer.database.CategoryDao
import com.wy.simple_timer.database.MyDatabase
import com.wy.simple_timer.databinding.FragmentCategoryPickerBinding
import com.wy.simple_timer.utils.SpacingItemDecoration
import com.wy.simple_timer.viewmodel.CategoryViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CategoryPickerFragment(private val initCategoryId: Long = -1) : Fragment() {
//    private lateinit var onFragmentReadyListener: () -> Unit
    private lateinit var binding: FragmentCategoryPickerBinding
    private lateinit var categoryAdapter: CategoryAdapterTR
    private lateinit var categoryViewModel: CategoryViewModel

//    init {
//        if (initCategoryId!=-1L){
//            setCurrentCategory(initCategoryId)
//        }
//    }

//    fun setOnFragmentReadyListener(listener: () -> Unit) {
//        onFragmentReadyListener = listener
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        observeCategories()

        // 应用自定义的 ItemDecoration 并减小间隔值，这里假设减小到 4dp
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.small_spacing)
        binding.categoryListRecycleView.addItemDecoration(SpacingItemDecoration(spacingInPixels))

//        onFragmentReadyListener()
    }

    private fun setupViewModel() {
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
    }

    private fun setupRecyclerView() {
        binding.categoryListRecycleView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            categoryAdapter = CategoryAdapterTR(initCategoryId)
//                .also
//            {
//                it.setOnLastItemClickListener {
//                    requireActivity().startActivity(
//                        android.content.Intent(
//                            requireContext(),
//                            com.wy.simple_timer.CategoryManagementActivity::class.java
//                        )
//                    )
//                }
//            }
            adapter = categoryAdapter
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.refreshCategories {
                it.getUnarchivedRootCategoriesOrderedByPosition()
            }.collect { categories ->
                categoryAdapter.setData(categories)
            }
        }
    }

    fun getCurrentCategory(): Long {
        return categoryAdapter.getCurrentCategory()
    }

    // 在 设置当前选中的分类
    fun setCurrentCategory(categoryId: Long) {
        categoryAdapter.setCurrentPosition(categoryId)
    }
}
