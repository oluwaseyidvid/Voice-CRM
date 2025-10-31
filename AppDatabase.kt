package com.neuralic.voicecrm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ActionLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun actionLogDao(): ActionLogDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "voicecrm.db").build()
                }
            }
            return INSTANCE!!
        }
    }
}
