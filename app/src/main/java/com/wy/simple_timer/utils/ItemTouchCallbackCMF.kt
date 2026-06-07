package com.wy.simple_timer.utils

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.wy.simple_timer.adapter.CategoryAdapterCMF

class ItemTouchCallbackCMF(private val adapter: CategoryAdapterCMF): ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        Log.d("ItemTouchCallback", "onMove")
//        adapter.collectionsSwap(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
//        adapter.notifyItemMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        adapter.onItemSwap(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Log.d("ItemTouchCallback", "onSwiped")
        // 直接将视图滑回原位，使用平滑动画
        viewHolder.itemView.animate()
            .translationX(0f)           // 回正X坐标
            .alpha(1f)                   // 恢复透明度
            .setDuration(300)            // 动画时长300ms
            .start()
        
        
        // 触发滑动业务逻辑（左滑记录、右滑多选）
        adapter.onItemSwiped(viewHolder.bindingAdapterPosition, direction)

        Log.d("ItemTouchCallback", "onSwiped:$(viewHolder.bindingAdapterPosition)")
    }

}