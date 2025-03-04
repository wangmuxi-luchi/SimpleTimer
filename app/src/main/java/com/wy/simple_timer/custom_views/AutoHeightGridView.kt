package com.wy.simple_timer.custom_views

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

class AutoHeightGridView(context: Context, attrs: AttributeSet) : GridView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 计算一个足够大的高度，让 GridView 能显示所有元素
        val expandSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }
}
