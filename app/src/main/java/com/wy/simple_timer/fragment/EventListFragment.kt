package com.wy.simple_timer.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wy.simple_timer.EventEditActivity
import com.wy.simple_timer.adapter.EventAdapterEL
import com.wy.simple_timer.databinding.FragmentEventListBinding
import com.wy.simple_timer.viewmodel.EventViewModel
import kotlinx.coroutines.launch

class EventListFragment : Fragment() {
    private lateinit var binding: FragmentEventListBinding
    private lateinit var eventAdapterEL: EventAdapterEL
    private lateinit var eventViewModel: EventViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        observeEvents()
        handleWindowInsets()
    }

    private fun setupViewModel() {
        eventViewModel = ViewModelProvider(this)[EventViewModel::class.java]
        eventViewModel.setEvents(eventViewModel.getEventDao().getAllEvents())
    }

    private fun setupRecyclerView() {
        binding.eventListView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = EventAdapterEL(requireActivity() as androidx.appcompat.app.AppCompatActivity).also {
                eventAdapterEL = it
            }
        }
    }

    // 在 observeEvents() 方法中添加适配器点击监听：
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            eventViewModel.getEvents()?.collect { events ->
                eventAdapterEL.setData(events)
                // 新增点击监听
                eventAdapterEL.setOnItemClickListener { eventId ->
                    val intent = Intent(requireContext(), EventEditActivity::class.java).apply {
                        putExtra("eventId", eventId)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}