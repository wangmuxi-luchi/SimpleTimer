package com.wy.simple_timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.wy.simple_timer.utils.isSameDay
import java.util.Calendar

// 监听系统日期变化
class MyBroadcastReceiver: BroadcastReceiver() {
    private var onDateChangedListener: ()->Unit = {}
//    private val currentCalendar: Calendar = Calendar.getInstance()
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("MyBroadcastReceiver", "onReceive: intent ${intent}" )
        when(intent?.action){
            Intent.ACTION_DATE_CHANGED -> {
                onDateChangedListener()
                Log.d("DateChangeReceiver", "onReceive: DATE_CHANGED")
                Toast.makeText(context, "新的一天", Toast.LENGTH_SHORT).show()
            }
        }
//        if (intent?.action == Intent.ACTION_TIME_TICK) {
//            if(!currentCalendar.isSameDay(Calendar.getInstance())){
//                onDateChangedListener()
//                Log.d("DateChangeReceiver", "onReceive: TIME_TICK")
//                Toast.makeText(context, "新的一天", Toast.LENGTH_SHORT).show()
//            }
//        }
    }
    fun setOnDateChangedListener(listener: ()->Unit) {
        onDateChangedListener = listener
    }

}
