package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bio_projects")
data class BioProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "analysis_records")
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val type: String, // "DNA", "RNA", "PROTEIN"
    val sequenceName: String,
    val sequence: String,
    val resultsJson: String, // Structured analysis parameters stored as JSON
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "research_papers")
data class ResearchPaper(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val authors: String,
    val abstractText: String,
    val fullText: String,
    val pmid: String = "",
    val journal: String = "",
    val pubYear: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notebook_entries")
data class NotebookEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionName: String = "Default Session",
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
