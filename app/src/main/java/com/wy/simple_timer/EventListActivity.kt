package com.wy.simple_timer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wy.simple_timer.adapter.EventAdapterEL
import com.wy.simple_timer.database.EventViewModel
import com.wy.simple_timer.databinding.ActivityEventListBinding
import kotlinx.coroutines.launch

class EventListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventListBinding
    private lateinit var eventadapterel: EventAdapterEL
    private lateinit var eventviewmodel: EventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        eventviewmodel = ViewModelProvider( this )[EventViewModel::class.java]
        eventviewmodel.setEvents(eventviewmodel.getEventDao().getAllEvents())
        eventadapterel = EventAdapterEL(this)
        binding.eventListView.layoutManager = LinearLayoutManager(this)
        binding.eventListView.adapter = eventadapterel
        lifecycleScope.launch {
            eventviewmodel.getEvents()?.collect { events ->
                eventadapterel.setData(events)
            }
        }
//        val allEvents = eventDao.getEventsByYear(Calendar.getInstance().time).toMutableList()
//
//        val adapter = BaseEventAdapterRV(this, allEvents)
//        binding.eventListView.layoutManager = LinearLayoutManager(this)
//        binding.eventListView.adapter = adapter
    }
}
