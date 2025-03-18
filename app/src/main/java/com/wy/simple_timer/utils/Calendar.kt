package com.wy.simple_timer.utils

import java.util.Calendar

fun Calendar.resetToStartOfPeriod(unit: Int) {
    when (unit) {
        Calendar.YEAR -> set(Calendar.DAY_OF_YEAR, 1)
        Calendar.MONTH -> set(Calendar.DAY_OF_MONTH, 1)
        Calendar.WEEK_OF_MONTH -> {
            // 设置为本周的周一
            if(get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) add(Calendar.DAY_OF_WEEK, -1)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        Calendar.DAY_OF_WEEK -> Unit // 已经由调用方设置
    }
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.isSameDay(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}