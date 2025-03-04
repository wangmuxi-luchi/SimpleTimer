package com.wy.simple_timer.adapter

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wy.simple_timer.R
import com.wy.simple_timer.databinding.ActivityTimeRecordBinding

class CategoryAdapterTR(context: Context,
                        private val binding: ActivityTimeRecordBinding,
                        AddLastItem : Boolean = false,
                        AddLastItemText : String = "添加",
                        AddLastItemColor : String = "#808080"
) : BaseCategoryAdapter(context, AddLastItem, AddLastItemText, AddLastItemColor) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_timerecord_category_list, parent, false)
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

//        // 根据是否选中设置小圆点的可见性
        val isSelected = position == binding.categoryList.checkedItemPosition
        holder.dot1.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.dot2.visibility = if (isSelected) View.VISIBLE else View.GONE

        return view
    }
}