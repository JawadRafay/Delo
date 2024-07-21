package com.statussaver.dele.database


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.statussaver.dele.model.StatusModel

@Dao
interface UserDao {

    @Query("Select * FROM filePaths Where type = :currentType")
    fun getPhotos(currentType: String?): List<StatusModel?>?

    @Insert
    fun insertItem(model: StatusModel?)

    @Delete
    fun deleteUser(model: StatusModel?)

}