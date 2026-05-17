package com.example.daveai.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChatMessageEntity::class, ChatSessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DaveDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: DaveDatabase? = null

        fun getDatabase(context: Context): DaveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DaveDatabase::class.java,
                    "dave_database"
                )
                .fallbackToDestructiveMigration() // For simplicity in this redesign
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
