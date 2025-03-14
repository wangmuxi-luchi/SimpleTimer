package com.wy.simple_timer.custom_views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

class CustomRecycleView(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 2130903957) : RecyclerView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 2130903957)
    override fun performClick(): Boolean {
        return super.performClick().also { Log.d("CustomRecycleView", "performClick: $it") }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // 触发点击的事件
        if (event.action == MotionEvent.ACTION_DOWN) {
            performClick()
        }
        // 按默认行为继续向下传递事件
        return super.onInterceptTouchEvent(event)
//        val childView = findChildViewUnder(event.x, event.y)
//        if (childView == null) { // 点击在空白处
//            Log.d("CustomRecycleView", "Touch blank")
//            return intercepted
//        }else{
//            // 点击在子项上
//            Log.d("CustomRecycleView", "Touch child view: $event")
//            return intercepted
//        }
    }

//    override fun performLongClick(): Boolean {
//        val handled = super.performLongClick()
//        Log.d("CustomRecycleView", "performLongClick: $handled")
//        return handled
//    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val handled = super.onTouchEvent(event)
//        Log.d("CustomRecycleView", "onTouchEvent: ${event.action}, handled: $handled")
//        if (event.action == MotionEvent.ACTION_DOWN) {
//            performClick()
//        }
//        return handled
//    }
}