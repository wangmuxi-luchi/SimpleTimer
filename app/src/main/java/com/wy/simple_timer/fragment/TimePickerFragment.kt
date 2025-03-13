package com.wy.simple_timer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.wy.simple_timer.databinding.FragmentTimePickerBinding
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import android.util.Log
import java.util.Locale


class TimePickerFragment : Fragment() {
    private lateinit var onFragmentReadyListener: ()->Unit
    private lateinit var binding: FragmentTimePickerBinding
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun setOnFragmentReadyListener(listener: ()->Unit) {
        this.onFragmentReadyListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDatePickers()
        setupTimePickers()
        setupDateClickListeners()
        onFragmentReadyListener()
    }

    private fun setupDatePickers() {
        binding.startDateText.text = dateFormat.format(startCalendar.time)
        binding.endDateText.text = dateFormat.format(endCalendar.time)
    }

    private fun setupDateClickListeners() {
        binding.startDateText.setOnClickListener { showDatePicker(true) }
        binding.endDateText.setOnClickListener { showDatePicker(false) }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = if (isStartDate) startCalendar else endCalendar
        DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(year, month, day)
            updateDateTimeDisplay()
        }, calendar.get(Calendar.YEAR), 
           calendar.get(Calendar.MONTH),
           calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupTimePickers() {
        val (startPicker, endPicker) = listOf(binding.startTimePicker, binding.endTimePicker)
        val timeChangedListener = TimePicker.OnTimeChangedListener { picker, hour, minute ->
            val isStart = picker == binding.startTimePicker
            val calendar = if (isStart) startCalendar else endCalendar

            val originalDay = calendar.get(Calendar.DAY_OF_MONTH)
            // 处理跨日逻辑
            if(hour == 0 && calendar.get(Calendar.HOUR_OF_DAY) == 23){
                calendar.set(Calendar.HOUR_OF_DAY, 24)
            }
            else if(hour == 23 && calendar.get(Calendar.HOUR_OF_DAY) == 0){
                calendar.set(Calendar.HOUR_OF_DAY, -1)
            }
            else{
                calendar.set(Calendar.HOUR_OF_DAY, hour)
            }
            calendar.set(Calendar.MINUTE, minute)
            
            if (originalDay != calendar.get(Calendar.DAY_OF_MONTH)) {
                updateDateTimeDisplay()
            }
            
            updateDurationText()
        }
        
        startPicker.apply {
            setIs24HourView(true)
            setOnTimeChangedListener(timeChangedListener)
        }

        endPicker.apply {
            setIs24HourView(true)
            setOnTimeChangedListener(timeChangedListener)
        }
    }

    private fun updateDateTimeDisplay() {
        binding.startDateText.text = dateFormat.format(startCalendar.time)
        binding.endDateText.text = dateFormat.format(endCalendar.time)
        updateDurationText()
    }
    
    // 获取时间的方法
    fun getStartTime(): Date = startCalendar.time
    fun getEndTime(): Date = endCalendar.time
    
    // 设置时间的方法
    fun setStartTime(time: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = time
        binding.startTimePicker.apply {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }
        startCalendar.time = time
        updateDateTimeDisplay()
    }

    fun setEndTime(time: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = time
        binding.endTimePicker.apply {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }
        endCalendar.time = time
        updateDateTimeDisplay()
    }


    private fun updateDurationText() {
        val startTime = getStartTime()
        val endTime = getEndTime()
        val duration = endTime.time - startTime.time
        
        val hours = TimeUnit.MILLISECONDS.toHours(duration).toInt()
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(duration) % 60).toInt()
        
        binding.durationText.text = when {
            hours > 0 -> "${hours}小时${minutes.toString().padStart(2, '0')}分钟"
            else -> "${minutes}分钟"
        }
    }
}
