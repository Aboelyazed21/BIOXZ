package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BioProject::class,
        AnalysisRecord::class,
        ResearchPaper::class,
        NotebookEntry::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bioProjectDao(): BioProjectDao
    abstract fun analysisRecordDao(): AnalysisRecordDao
    abstract fun researchPaperDao(): ResearchPaperDao
    abstract fun notebookEntryDao(): NotebookEntryDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bioinformatics_ai_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
