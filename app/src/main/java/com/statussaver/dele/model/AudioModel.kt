package com.statussaver.dele.model

import android.os.Parcel
import android.os.Parcelable
import java.io.File

data class AudioModel(val title : String,val paths : String) : Parcelable {

    lateinit var file : File
    var dates : Long = 0

    constructor(file : File,title : String, paths : String , dates : Long):this(title,paths){
        this.file = file
        this.dates = dates
    }

    constructor(parcel: Parcel) : this(parcel.readString()!!,parcel.readString()!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(paths)
        dest.writeString(title)
    }

    companion object CREATOR : Parcelable.Creator<AudioModel> {
        override fun createFromParcel(parcel: Parcel): AudioModel {
            return AudioModel(parcel)
        }

        override fun newArray(size: Int): Array<AudioModel?> {
            return arrayOfNulls(size)
        }
    }
}