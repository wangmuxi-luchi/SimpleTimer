package com.wy.simple_timer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.wy.simple_timer.R
import com.wy.simple_timer.adapter.CategoryAdapterTR
import com.wy.simple_timer.databinding.FragmentCategoryPickerBinding
import com.wy.simple_timer.utils.SpacingItemDecoration
import com.wy.simple_timer.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

class CategoryPickerFragment : Fragment() {
    private lateinit var binding: FragmentCategoryPickerBinding
    private lateinit var categoryAdapter: CategoryAdapterTR
    private lateinit var categoryViewModel: CategoryViewModel

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
        binding.categoryList.addItemDecoration(SpacingItemDecoration(spacingInPixels))
    }

    private fun setupViewModel() {
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        categoryViewModel.setCategories(categoryViewModel.getCategoryDao().getUnarchivedRootCategoriesOrderedByPosition())
    }

    private fun setupRecyclerView() {
        binding.categoryList.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            categoryAdapter = CategoryAdapterTR().also {
                it.setOnLastItemClickListener {
                    requireActivity().startActivity(
                        android.content.Intent(
                            requireContext(),
                            com.wy.simple_timer.CategoryManagementActivity::class.java
                        )
                    )
                }
            }
            adapter = categoryAdapter
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.getCategories()?.collect { categories ->
                categoryAdapter.setData(categories)
            }
        }
    }

    fun getCurrentCategory(): Long? {
        return categoryAdapter.getCurrentCategory()
    }
}
