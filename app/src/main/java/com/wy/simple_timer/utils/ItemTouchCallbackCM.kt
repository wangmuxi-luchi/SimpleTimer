package com.wy.simple_timer.utils

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.adapter.CategoryAdapterCM
import java.util.Collections

class ItemTouchCallbackCM(private val adapter: CategoryAdapterCM):
    ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.collectionsSwap(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        adapter.notifyItemMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // TODO: 处理滑动事件
        Log.d("ItemTouchCallback", "onSwiped")
        adapter.onItemSwiped(viewHolder.bindingAdapterPosition)

        adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
    }
}