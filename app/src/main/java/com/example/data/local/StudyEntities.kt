package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val mood: String = "Focused" // e.g. "Focused", "Relaxed", "Challenged", "Energized"
)

@Entity(tableName = "study_reminders")
data class StudyReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val reminderTime: Long, // Epoch millis
    val isCompleted: Boolean = false,
    val message: String = "", // Inspirational reminder tip
    val reminderType: String = "Review" // "Review", "Break", "ExamPrep"
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckName: String,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timesReviewed: Int = 0,
    val timesCorrect: Int = 0
)

@Entity(tableName = "session_goals")
data class SessionGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val subjectTag: String = ""
)

