package com.statussaver.dele.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.statussaver.dele.model.StatusModel


@Database(entities = [StatusModel::class], version = 1)
abstract class RoomDB : RoomDatabase() {

    companion object{
        val DATABASE_NAME = "Dele"
        private var instanceVar: RoomDB? = null
        @Synchronized
        fun getInstance(context: Context): RoomDB? {
            if (instanceVar == null) {
                instanceVar = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDB::class.java, DATABASE_NAME
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instanceVar
        }
    }

    abstract fun userDao(): UserDao?

}