package com.focussense.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.focussense.app.data.local.dao.StudySessionDao
import com.focussense.app.data.local.entity.StudySessionEntity

@Database(
    entities = [StudySessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FocusSenseDatabase : RoomDatabase() {

    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: FocusSenseDatabase? = null

        fun getInstance(context: Context): FocusSenseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FocusSenseDatabase::class.java,
                    "focus_sense.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
