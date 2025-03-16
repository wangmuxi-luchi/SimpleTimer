package com.wy.simple_timer.fragment

import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wy.simple_timer.EventEditActivity
import com.wy.simple_timer.R
import com.wy.simple_timer.adapter.EventAdapterEL
import com.wy.simple_timer.database.Event
import com.wy.simple_timer.database.getEventsByDay
import com.wy.simple_timer.database.getEventsInRange
import com.wy.simple_timer.database.resetToStartOfPeriod
import com.wy.simple_timer.databinding.FragmentEventListBinding
import com.wy.simple_timer.viewmodel.EventViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class EventListFragment : Fragment() {
    private lateinit var binding: FragmentEventListBinding
    private lateinit var eventAdapterEL: EventAdapterEL
    private lateinit var eventViewModel: EventViewModel
    private lateinit var onCreatedListener: () -> Unit
    private var startCalendar: Calendar = Calendar.getInstance().apply { time = Date()  }
    private var endCalendar: Calendar = Calendar.getInstance().apply { time = Date() }
    private lateinit var eventsMutableStateFlow : MutableStateFlow<Flow<List<Event>>>

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
        onCreatedListener()
    }

    fun setOnCreatedListener(listener: () -> Unit) {
        onCreatedListener = listener
    }

    fun setOnClickListener(listener:View. OnClickListener) {
        binding.eventListView.setOnClickListener(listener)
    }

    private fun setupViewModel() {
        eventViewModel = ViewModelProvider(this)[EventViewModel::class.java]

        eventViewModel.refreshEvents { it.getEventsByDay(Calendar.getInstance().time)}
            .also { eventsMutableStateFlow = MutableStateFlow(it) }
    }

    fun setTimeRange(start: Calendar, end: Calendar) {
        startCalendar = start
        endCalendar = end
        refreshEvents()
    }

    fun refreshEvents() {
        startCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.resetToStartOfPeriod(Calendar.DAY_OF_MONTH)
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)
        endCalendar.add(Calendar.MILLISECOND, -1)

        eventViewModel.refreshEvents { it.getEventsInRange(startCalendar, endCalendar)}
            .also { eventsMutableStateFlow.value = it }
    }

    private fun setupRecyclerView() {
        binding.eventListView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = EventAdapterEL(requireActivity() as androidx.appcompat.app.AppCompatActivity).also {
                eventAdapterEL = it
            }
            eventAdapterEL.setOnItemClickListener { eventId ->
                val intent = Intent(requireContext(), EventEditActivity::class.java).apply {
                    putExtra("eventId", eventId)
                }
                startActivity(intent)
            }
        }
    }

    // 在 observeEvents() 方法中添加适配器点击监听：
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            eventsMutableStateFlow.flatMapLatest {it}.collect {
                eventAdapterEL.setData(it)}
        }
    }


}