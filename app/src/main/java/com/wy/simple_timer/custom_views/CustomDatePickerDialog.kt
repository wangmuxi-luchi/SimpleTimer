package com.wy.simple_timer.custom_views

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.icu.util.Calendar
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.wy.simple_timer.databinding.DialogCustomDatepickerBinding

class CustomDatePickerDialog(
    context: Context,
    private val listener: (year: Int, month: Int, dayOfMonth: Int, range: Int)->Unit = { _, _, _, _ ->},
    private val year: Int,
    private val month: Int,
    private val dayOfMonth: Int
): AlertDialog(context){
    private var binding: DialogCustomDatepickerBinding = DialogCustomDatepickerBinding.inflate(layoutInflater)

    init {
        setView(binding.root)
        binding.datePicker.init(year, month, dayOfMonth, null)
        setupButtonsClickListeners()
    }

    // 设置提示文本
    fun setPromptText(text: String){
        binding.setDateText.text = text
    }

    private fun setupButtonsClickListeners(){
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        binding.okButton.setOnClickListener {
            binding.datePicker.clearFocus()
            listener(
                binding.datePicker.year,
                binding.datePicker.month,
                binding.datePicker.dayOfMonth,
                -1
            )
            dismiss()
        }
        binding.dayButton.setOnClickListener {
            binding.datePicker.clearFocus()
            listener(
                binding.datePicker.year,
                binding.datePicker.month,
                binding.datePicker.dayOfMonth,
                Calendar.DAY_OF_WEEK
            )
            dismiss()
        }
        binding.weekButton.setOnClickListener {
            binding.datePicker.clearFocus()
            listener(
                binding.datePicker.year,
                binding.datePicker.month,
                binding.datePicker.dayOfMonth,
                Calendar.WEEK_OF_MONTH
            )
            dismiss()
        }
        binding.monthButton.setOnClickListener {
            binding.datePicker.clearFocus()
            listener(
                binding.datePicker.year,
                binding.datePicker.month,
                binding.datePicker.dayOfMonth,
                Calendar.MONTH
            )
            dismiss()
        }
        binding.yearButton.setOnClickListener {
            binding.datePicker.clearFocus()
            listener(
                binding.datePicker.year,
                binding.datePicker.month,
                binding.datePicker.dayOfMonth,
                Calendar.YEAR
            )
            dismiss()
        }
        binding.todayButton.setOnClickListener {
            binding.datePicker.clearFocus()
            val currentCalendar = Calendar.getInstance()
            listener(
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH),
                Calendar.DAY_OF_WEEK
            )
            dismiss()
        }

    }

}
