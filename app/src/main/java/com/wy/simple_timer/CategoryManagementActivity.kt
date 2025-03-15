package com.wy.simple_timer

//import CategoryDialog
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.wy.simple_timer.adapter.CategoryAdapterCM
//import com.wy.simple_timer.database.Category
//import com.wy.simple_timer.viewmodel.CategoryViewModel
//import com.wy.simple_timer.databinding.ActivityCategoryManagementBinding
//import com.wy.simple_timer.viewmodel.CategoryViewModelCM
//import kotlinx.coroutines.launch
@Deprecated(
    message = "BaseCategoryViewModel 已弃用",
    replaceWith = ReplaceWith("BaseCategoryViewModel 已弃用"),
    level = DeprecationLevel.ERROR
)
class CategoryManagementActivity ()
//    : AppCompatActivity() {
//    private lateinit var binding: ActivityCategoryManagementBinding
//    private lateinit var viewModel: CategoryViewModelCM
//    private lateinit var categoryAdapter: CategoryAdapterCM
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setupBinding()
//        handleWindowInsets()
//        setupViewModel()
//        setupRecyclerView()
//        setupAdapterCallbacks()
//    }
//
//    private fun setupBinding() {
//        binding = ActivityCategoryManagementBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//    }
//
//    private fun handleWindowInsets() {
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//
//    private fun setupViewModel() {
//        viewModel = ViewModelProvider(this)[CategoryViewModelCM::class.java]
//    }
//
//    private fun setupRecyclerView() {
//        binding.categoryListRecycleView.apply {
//            layoutManager = LinearLayoutManager(
//                this@CategoryManagementActivity,
//                LinearLayoutManager.VERTICAL,
//                false
//            )
//            adapter = CategoryAdapterCM().also {
//                categoryAdapter = it
//            }
//        }
//        observeCategories()
//    }
//
//    private fun observeCategories() {
//        lifecycleScope.launch {
//            viewModel.get_Categories().collect { categories ->
//                categoryAdapter.setData(categories)
//            }
//        }
//    }
//
//    private fun setupAdapterCallbacks() {
//        categoryAdapter.apply {
//            setOnOtherItemClickListener { categoryID ->
//                startActivity(
//                    Intent(this@CategoryManagementActivity, CategoryDetailActivity::class.java).apply {
//                        putExtra("categoryID", categoryID)
//                    }
//                )
//            }
//
//            setOnLastItemClickListener { showAddCategoryDialog() }
//
//            setOnBindViewHolder { category, position ->
//                if (position != category.position) {
//                    Log.d("CategoryManagementActivity", "updateCategoryPosition: $category, $position")
//                    viewModel.updateCategory(
//                        category.id,
//                        category.categoryName,
//                        category.categoryColor,
//                        position,
//                        category.archived,
//                        category.parentId
//                    )
//                }
//            }
//        }
//    }
//    var selectedColor = "#808080" // 默认颜色
//    private fun showAddCategoryDialog() {
//        val categoryDialog = CategoryDialog(this).apply {
//            setListener(object : CategoryDialog.CategoryDialogListener {
//                override fun onConfirmEdit(newName: String, newColor: String) {
//                    if (newName.isNotEmpty()) {
//                        val newCategory = Category(
//                            0,
//                            newName,
//                            newColor,
//                            categoryAdapter.itemCount - 1,
//                            false,
//                            -1
//                        )
//                        viewModel.insertCategory(newCategory)
//                    }
//                }
//
//                override fun onPickColor(newColor: String) {
//                    selectedColor = newColor
//                }
//            })
//        }
//        // 初始名为空字符串，默认颜色使用当前选中颜色
//        categoryDialog.show("", selectedColor)
//    }
//}