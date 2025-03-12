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


class TimePickerFragment : Fragment() {

    private lateinit var binding: FragmentTimePickerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTimePickers()
    }

    private fun setupTimePickers() {
        val calendar = Calendar.getInstance()
        val (startPicker, endPicker) = listOf(binding.startTimePicker, binding.endTimePicker)

        startPicker.apply {
            setIs24HourView(true)
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }

        endPicker.apply {
            setIs24HourView(true)
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }

        // 处理传入的开始时间
        arguments?.getLong("startTime", -1)?.takeIf { it != -1L }?.let {
            calendar.timeInMillis = it
            startPicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            startPicker.minute = calendar.get(Calendar.MINUTE)
        }

        val timeChangedListener = TimePicker.OnTimeChangedListener { _, _, _ ->
            updateDurationText()
        }

        binding.startTimePicker.setOnTimeChangedListener(timeChangedListener)
        binding.endTimePicker.setOnTimeChangedListener(timeChangedListener)
        
        // 初始化时更新一次
        updateDurationText()
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
    fun setStartTime(startTime: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = startTime
        binding.startTimePicker.apply {
            setIs24HourView(true)
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }
    }

    fun setEndTime(endTime: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = endTime
        binding.endTimePicker.apply {
            setIs24HourView(true)
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }
    }

    fun getStartTime(): java.util.Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, binding.startTimePicker.hour)
        calendar.set(Calendar.MINUTE, binding.startTimePicker.minute)
        return calendar.time
    }

    fun getEndTime(): java.util.Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, binding.endTimePicker.hour)
        calendar.set(Calendar.MINUTE, binding.endTimePicker.minute)
        return calendar.time
    }
}
