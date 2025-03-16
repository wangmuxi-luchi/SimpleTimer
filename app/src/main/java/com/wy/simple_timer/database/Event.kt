package com.wy.simple_timer.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Calendar,
    val endTime: Calendar,
    val categoryId: Long,
    val notes: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        Calendar.getInstance().apply { timeInMillis = parcel.readLong() },
        Calendar.getInstance().apply { timeInMillis = parcel.readLong() },
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(startTime.timeInMillis)
        parcel.writeLong(endTime.timeInMillis)
        parcel.writeLong(categoryId)
        parcel.writeString(notes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }
}