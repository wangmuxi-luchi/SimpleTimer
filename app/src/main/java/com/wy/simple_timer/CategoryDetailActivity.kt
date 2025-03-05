package com.wy.simple_timer

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class CategoryDetailActivity : AppCompatActivity() {
    private lateinit var categoryDetailListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)

//        categoryDetailListView = findViewById(R.id.category_detail_list_view)

        // 后续可添加逻辑来填充 ListView 数据
        
    }
}
