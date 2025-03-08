package com.wy.simple_timer.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String,
    val categoryColor: String,
    val position: Int,
    val archived: Boolean,
    val parentId: Long = -1
)
