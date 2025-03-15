package com.wy.simple_timer.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String,
    val categoryColor: String,
    var position: Int,
    val archived: Boolean,
    val parentId: Long = -1
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(categoryName)
        parcel.writeString(categoryColor)
        parcel.writeInt(position)
        parcel.writeByte(if (archived) 1 else 0)
        parcel.writeLong(parentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}


// 新增：统计event信息的过后的流
data class CategoryWithEventInf(
    val id: Long = 0,
    val categoryName: String,
    val categoryColor: String,
    var position: Int,
    val archived: Boolean,
    val parentId: Long = -1,

    var eventCount: Int = 0,
    var totalDuration: Long = 0L,
    var totalDays: Int = 0
){
    constructor(category: Category, eventCount: Int, totalDuration: Long, totalDays: Int): this(
        category.id,
        category.categoryName,
        category.categoryColor,
        category.position,
        category.archived,
        category.parentId,
        eventCount,
        totalDuration,
        totalDays
    )
}

