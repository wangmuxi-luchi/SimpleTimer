package com.wy.simple_timer.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.text.TextUtils
import com.wy.simple_timer.R
import com.wy.simple_timer.database.Category
import com.wy.simple_timer.databinding.ActivityTimeRecordBinding

class TimeRecordCategoryAdapter(
    context: Context,
    private val binding: ActivityTimeRecordBinding,
    private val categories: MutableList<Category>
) : ArrayAdapter<Category>(context, 0, categories) {

    private class ViewHolder {
        lateinit var categoryText: TextView
        lateinit var dot1: ImageView
        lateinit var dot2: ImageView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.timerecord_category_list_item, parent, false)
            if(view == null) {
                throw RuntimeException("Failed to inflate layout")
            }
            holder = ViewHolder()
            holder.categoryText = view.findViewById(R.id.category_text)
            holder.dot1 = view.findViewById(R.id.dot1)
            holder.dot2 = view.findViewById(R.id.dot2)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val category = getItem(position)
        if (category != null) {
            try {
                holder.categoryText.setBackgroundColor(Color.parseColor(category.color))
                holder.categoryText.textSize = 12f
                holder.categoryText.setSingleLine(true)
                holder.categoryText.ellipsize = TextUtils.TruncateAt.END
                holder.categoryText.text = category.name
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }

        // 根据是否选中设置小圆点的可见性
        val isSelected = position == binding.categoryList.checkedItemPosition
        holder.dot1.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.dot2.visibility = if (isSelected) View.VISIBLE else View.GONE

        return view
    }
}