package com.wy.simple_timer.adapter

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.wy.simple_timer.R

class CategoryAdapterCM(
    context: Context,
    AddLastItem : Boolean = false,
    AddLastItemText : String = "添加",
    AddLastItemColor : String = "#808080"
) : BaseCategoryAdapter(context, AddLastItem, AddLastItemText, AddLastItemColor){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            if(view == null) {
                throw RuntimeException("Failed to inflate layout")
            }
        }
        val textView = view.findViewById<TextView>(android.R.id.text1)
        val category = getItem(position)
        if (category != null) {
            textView.text = category.name
        }
        return view
    }

}