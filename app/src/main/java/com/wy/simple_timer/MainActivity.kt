package com.wy.simple_timer

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.wy.simple_timer.viewmodel.EventViewModel
import com.wy.simple_timer.database.MyDatabase
import com.wy.simple_timer.databinding.ActivityMainBinding
import com.wy.simple_timer.fragment.EventListFragment // 假设你有这个 Fragment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


// 在文件顶部新增导入
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.wy.simple_timer.fragment.CategoryManagementFragment

// 在类外部定义 DataStore
private val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var eventviewmodel: EventViewModel
    private var selectedFragment = 2 // 1 表示事件列表，2 表示分类管理
    private lateinit var eventListFragment: EventListFragment
    private lateinit var categoryManagementFragment: CategoryManagementFragment



//    private lateinit var timeTickReceiver: TimeRickReceiver
    private lateinit var myBroadcastReceiver: MyBroadcastReceiver

    // 备份和恢复的 Launcher
    private val backupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                lifecycleScope.launch {
                    saveLastDirectoryUri(uri)
                    contentResolver.takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    Intent(this@MainActivity, DatabaseManagementService::class.java).apply {
                        action = "BACKUP_DATA"
                        putExtra("outputUri", uri)
                        startService(this)
                    }
                }
            }
        }
    }

    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                lifecycleScope.launch {
                    saveLastDirectoryUri(uri)
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    Intent(this@MainActivity, DatabaseManagementService::class.java).apply {
                        action = "RESTORE_DATA"
                        putExtra("inputUri", uri)
                        startService(this)
                    }
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        handleWindowInsets()
        loadEventListFragment() // 加载事件列表
        loadCategoryManagementFragment() // 加载分类管理
        setupTimeRange()
        setupListeners()

        // 监听日期变化
//        val intentFilter = android.content.IntentFilter()
//        intentFilter.addAction(Intent.ACTION_TIME_TICK)
//        timeTickReceiver = TimeRickReceiver()
//        registerReceiver(timeTickReceiver, intentFilter)

        // 接收广播
        val myIntentFilter = android.content.IntentFilter()
//        myIntentFilter.addAction(Intent.ACTION_USER_PRESENT)
        myIntentFilter.addAction(Intent.ACTION_DATE_CHANGED)
//        myIntentFilter.addAction(Intent.ACTION_TIME_TICK)
        myBroadcastReceiver = MyBroadcastReceiver()
        registerReceiver(myBroadcastReceiver, myIntentFilter)
        myBroadcastReceiver.setOnDateChangedListener {
            Log.d("MainActivity", "onCreate: onDateChangedListener")
            binding.dateRangeTextView.refreshView()
        }
        Log.d("MainActivity", "onCreate: ")
    }
    override fun onResume() {
        super.onResume()
        binding.dateRangeTextView.refreshView()
        Log.d("MainActivity", "onResume: ")
    }
    override fun onDestroy() {
        super.onDestroy()
//        unregisterReceiver(timeTickReceiver)
        unregisterReceiver(myBroadcastReceiver)
    }

    inner class TimeRickReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
//            Log.d("TimeTickReceiver", "onReceive:  intent ${intent}" )
            if (intent?.action == Intent.ACTION_TIME_TICK) {
                binding.dateRangeTextView.refreshView()
            }
        }

    }

    private fun setupTimeRange() {
        binding.dateRangeTextView.setOnTimeTextChangedListener { start, end ->
            // 在这里处理时间范围的变化
            // 例如，更新事件列表
            eventListFragment.setTimeRange(start, end)
            categoryManagementFragment.setTimeRange(start, end)
        }
    }

    // 加载事件列表Fragment到第一个容器
    private fun loadEventListFragment() {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container1, EventListFragment().also {
            eventListFragment = it
        })
        fragmentTransaction.commit()
    }


    // 加载分类管理Fragment到第二个容器
    private fun loadCategoryManagementFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container2, CategoryManagementFragment().also {
               categoryManagementFragment = it
            })
            .commit()
    }
    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.openRecordActivityButton.setOnClickListener { launchTimeRecordActivity() }
//        binding.openCategoryManagementButton.setOnClickListener { launchCategoryManagementActivity() }
        binding.BackupDataButton.setOnClickListener { backupData() }
        binding.RestoreDataButton.setOnClickListener { restoreData() }
        binding.addCategoryButton.setOnClickListener{ categoryManagementFragment.showAddCategoryDialog() }
        categoryManagementFragment.setOnRecycleViewClickListener { changeSelectedFragment(2) }
        eventListFragment.setOnClickListener { changeSelectedFragment(1) }
        eventListFragment.setIsCategorySelectedListener { categoryId ->
            categoryManagementFragment.isCategorySelected(categoryId) }
        categoryManagementFragment.setOnSCCListener { eventListFragment.onSCC() }

    }

    private fun launchTimeRecordActivity() {
        lifecycleScope.launch {
            val eventDao = MyDatabase.getDatabase(application).eventDao()
            val allEvents = eventDao.getAllEvents().firstOrNull()
            val latestEvent = allEvents?.maxByOrNull { it.endTime.timeInMillis }

            Intent(this@MainActivity, TimeRecordActivity::class.java).apply {
                latestEvent?.let { putExtra("startTime", it.endTime.timeInMillis) }
                startActivity(this)
            }
        }
    }

    // 新增分类管理跳转方法
//    private fun launchCategoryManagementActivity() {
//        startActivity(Intent(this, CategoryManagementActivity::class.java))
//    }

    // preferencesDataStore 操作，保存文件夹路径URL
    private suspend fun saveLastDirectoryUri(uri: Uri) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("last_directory_uri")] = uri.toString()
        }
    }

    private suspend fun getLastDirectoryUri(): Uri? {
        return dataStore.data
            .firstOrNull()
            ?.get(stringPreferencesKey("last_directory_uri"))
            ?.let { Uri.parse(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun backupData() {
        lifecycleScope.launch {
            // 创建日期格式化器
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.getDefault())
            val formattedTime = sdf.format(java.util.Date())
            
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "backup_${formattedTime}.json") // 修改文件名格式
                // 异步获取最后目录
                getLastDirectoryUri()?.let {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, it)
                }
            }
            backupLauncher.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun restoreData() {
        lifecycleScope.launch {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                // 异步获取最后目录
                getLastDirectoryUri()?.let {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, it)
                }
            }
            restoreLauncher.launch(intent)
        }
    }

    fun updateShadowDivider(){
        val gradientDrawable = binding.fragmentContainers.dividerDrawable as GradientDrawable
        if (selectedFragment == 1) {
            gradientDrawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        }else{
            gradientDrawable.orientation = GradientDrawable.Orientation.RIGHT_LEFT
        }
        binding.fragmentContainers.invalidate()

    }
    private fun changeSelectedFragment(index: Int) {
        selectedFragment = index
        updateShadowDivider()
    }
}