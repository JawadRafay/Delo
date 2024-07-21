package com.statussaver.dele.database.convertors

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.statussaver.dele.model.StatusModel

class StatusConvertor {

    @TypeConverter
    fun toString(bank: StatusModel?): String? {
        return Gson().toJson(bank)
    }

    @TypeConverter
    fun fromString(bank: String?): StatusModel? {
        return Gson().fromJson(bank, StatusModel::class.java)
    }
}
