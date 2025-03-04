package com.wy.simple_timer.adapter

import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Event

class BaseEventAdapter(
    context: AppCompatActivity,
) : ArrayAdapter<Event>(context, R.layout.item_eventlist){

}