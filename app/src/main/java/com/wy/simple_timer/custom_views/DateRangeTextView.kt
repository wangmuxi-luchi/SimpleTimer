package com.wy.simple_timer.custom_views

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.wy.simple_timer.database.resetToStartOfPeriod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("ClickableViewAccessibility")
class DateRangeTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
    androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    private var onTimeTextChangedListener: ((Calendar, Calendar) -> Unit) = { _, _ ->}
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var lastTouchX = 0f
    private var widgetWidth: Float = 0f

    private val startCalendar: Calendar = Calendar.getInstance()
    private val endCalendar: Calendar = Calendar.getInstance()

    init {
        // 初始化日期
        setDateRange(System.currentTimeMillis(), System.currentTimeMillis())

        setOnTouchListener{v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                lastTouchX = event.x
                widgetWidth = v.width.toFloat()
            }
            false
        }
        setOnClickListener {
            // 判断点击位置
            if(lastTouchX < widgetWidth / 2){
                Log.d("DateRangeTextView", "pickStartDate clicked")
                pickStartDate()
            }else{
                Log.d("DateRangeTextView", "pickEndDate clicked")
                pickEndDate()
            }
        }
    }
    // 刷新文本
    private fun refreshText() {
        //设置日期格式
        val startDateText = dateFormat.format(startCalendar.time)
        val endDateText = dateFormat.format(endCalendar.time)
        //设置文本
        if(startDateText == endDateText){
            val currentDate = Date(System.currentTimeMillis())
            if(dateFormat.format(currentDate) == startDateText){
                text = "今天"
            }else{
                text = startDateText
            }
        }else{
            "$startDateText - $endDateText".also { text = it }
        }
        onTimeTextChanged()
    }

    fun onTimeTextChanged() {
        onTimeTextChangedListener(startCalendar, endCalendar)
    }

    fun setOnTimeTextChangedListener(listener: (Calendar, Calendar) -> Unit) {
        onTimeTextChangedListener = listener
    }

    // 用于设置日期范围
    fun setDateRange(start: Long, end: Long) {
        startCalendar.timeInMillis = start
        endCalendar.timeInMillis = end
        refreshText()
    }

    private fun setDateRange(year: Int, month: Int, dayOfMonth: Int, unit: Int) {
        startCalendar.set(year, month, dayOfMonth)
        endCalendar.set(year, month, dayOfMonth)
        startCalendar.resetToStartOfPeriod(unit)
        endCalendar.apply {
            resetToStartOfPeriod(unit)
            add(unit, 1)
            add(Calendar.MILLISECOND, -1)
        }
    }

    private fun pickStartDate() {
        CustomDatePickerDialog(getContext(), { year, month, day, range ->
            if(range == -1) startCalendar.set(year, month, day)
            else setDateRange(year, month, day, range)
            refreshText()
        }, startCalendar.get(Calendar.YEAR),
            startCalendar.get(Calendar.MONTH),
            startCalendar.get(Calendar.DAY_OF_MONTH)).apply { setPromptText("你可以选择起始日期\n也可以选择时间范围") }.show()
    }
    private fun pickEndDate() {
        CustomDatePickerDialog(getContext(), { year, month, day, range ->
            if(range == -1) endCalendar.set(year, month, day)
            else setDateRange(year, month, day, range)
            refreshText()
        }, endCalendar.get(Calendar.YEAR),
            endCalendar.get(Calendar.MONTH),
            endCalendar.get(Calendar.DAY_OF_MONTH)).apply { setPromptText("你可以选择结束日期\n也可以选择时间范围") }.show()
    }
}