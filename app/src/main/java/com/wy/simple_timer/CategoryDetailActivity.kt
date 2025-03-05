package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wy.simple_timer.databinding.ActivityCategoryDetailBinding

class CategoryDetailActivity : AppCompatActivity() {
    private lateinit var CD_Binding: ActivityCategoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CD_Binding = DataBindingUtil.setContentView(this, R.layout.activity_category_detail)
        CD_Binding.totalTimes = 0
        CD_Binding.totalMinutes = 0
        CD_Binding.totalDays = 0
        CD_Binding.minutesPerDay = 0


    }
}
