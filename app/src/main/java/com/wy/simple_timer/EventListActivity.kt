package com.wy.simple_timer

//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.wy.simple_timer.adapter.EventAdapterEL
//import com.wy.simple_timer.viewmodel.EventViewModel
//import com.wy.simple_timer.databinding.ActivityEventListBinding
//import kotlinx.coroutines.launch
@Deprecated(
    message = "BaseCategoryViewModel 已弃用",
    replaceWith = ReplaceWith("BaseCategoryViewModel 已弃用"),
    level = DeprecationLevel.ERROR
)
class EventListActivity
//    : AppCompatActivity() {
//    private lateinit var binding: ActivityEventListBinding
//    private lateinit var eventadapterel: EventAdapterEL
//    private lateinit var eventviewmodel: EventViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setupBinding()
//        handleWindowInsets()
//        setupViewModel()
//        setupRecyclerView()
//        observeEvents()
//    }
//
//    private fun setupBinding() {
//        binding = ActivityEventListBinding.inflate(layoutInflater)
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
//        eventviewmodel = ViewModelProvider(this)[EventViewModel::class.java]
//        eventviewmodel.refreshEvents { it.getAllEvents()}
//    }
//
//    private fun setupRecyclerView() {
//        binding.eventListView.apply {
//            layoutManager = LinearLayoutManager(this@EventListActivity)
//            adapter = EventAdapterEL(this@EventListActivity).also {
//                eventadapterel = it
//            }
//        }
//    }
//
//    private fun observeEvents() {
//        lifecycleScope.launch {
//            eventviewmodel.getEvents().collect { events ->
//                eventadapterel.setData(events)
//            }
//        }
//    }
//}
