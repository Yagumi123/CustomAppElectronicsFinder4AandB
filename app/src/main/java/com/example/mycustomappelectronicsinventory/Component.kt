package com.example.mycustomappelectronicsinventory

import android.os.Parcel
import android.os.Parcelable

data class Component(
    val id: String,
    val name: String,
    val category: String,
    val type: String,
    val value: String,
    val quantity: Int,
    val location: String,
    val unit: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeString(type)
        parcel.writeString(value)
        parcel.writeInt(quantity)
        parcel.writeString(location)
        parcel.writeString(unit)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Component> {
        override fun createFromParcel(parcel: Parcel): Component {
            return Component(parcel)
        }

        override fun newArray(size: Int): Array<Component?> {
            return arrayOfNulls(size)
        }
    }
}
