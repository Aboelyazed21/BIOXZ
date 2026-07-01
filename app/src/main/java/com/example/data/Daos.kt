package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BioProjectDao {
    @Query("SELECT * FROM bio_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<BioProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: BioProject): Long

    @Query("DELETE FROM bio_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}

@Dao
interface AnalysisRecordDao {
    @Query("SELECT * FROM analysis_records WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getRecordsForProject(projectId: Int): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analysis_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnalysisRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AnalysisRecord): Long

    @Query("DELETE FROM analysis_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)
}

@Dao
interface ResearchPaperDao {
    @Query("SELECT * FROM research_papers ORDER BY timestamp DESC")
    fun getAllPapers(): Flow<List<ResearchPaper>>

    @Query("SELECT * FROM research_papers WHERE id = :id")
    suspend fun getPaperById(id: Int): ResearchPaper?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: ResearchPaper): Long

    @Update
    suspend fun updatePaper(paper: ResearchPaper)

    @Query("DELETE FROM research_papers WHERE id = :id")
    suspend fun deletePaperById(id: Int)
}

@Dao
interface NotebookEntryDao {
    @Query("SELECT * FROM notebook_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<NotebookEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: NotebookEntry): Long

    @Update
    suspend fun updateEntry(entry: NotebookEntry)

    @Query("DELETE FROM notebook_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Int)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Query("SELECT DISTINCT sessionName FROM chat_messages")
    fun getAllSessions(): Flow<List<String>>

    @Query("SELECT * FROM chat_messages WHERE sessionName = :sessionName ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionName: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE sessionName = :sessionName")
    suspend fun clearSession(sessionName: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}
