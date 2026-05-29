package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Study Sessions ---
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Delete
    suspend fun deleteSession(session: StudySession)

    // --- Study Reminders ---
    @Query("SELECT * FROM study_reminders ORDER BY reminderTime ASC")
    fun getAllReminders(): Flow<List<StudyReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: StudyReminder): Long

    @Delete
    suspend fun deleteReminder(reminder: StudyReminder)

    @Query("UPDATE study_reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateReminderStatus(id: Int, isCompleted: Boolean)

    // --- Flashcards ---
    @Query("SELECT * FROM flashcards ORDER BY timestamp DESC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Query("SELECT DISTINCT deckName FROM flashcards ORDER BY deckName ASC")
    fun getDistinctDecks(): Flow<List<String>>

    @Query("SELECT * FROM flashcards WHERE deckName = :deckName ORDER BY id ASC")
    fun getFlashcardsByDeck(deckName: String): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long

    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard)

    @Query("UPDATE flashcards SET timesReviewed = :reviews, timesCorrect = :correct WHERE id = :id")
    suspend fun updateFlashcardProgress(id: Int, reviews: Int, correct: Int)

    // --- Session Goals ---
    @Query("SELECT * FROM session_goals ORDER BY timestamp DESC")
    fun getAllGoals(): Flow<List<SessionGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SessionGoal): Long

    @Delete
    suspend fun deleteGoal(goal: SessionGoal)

    @Query("UPDATE session_goals SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateGoalStatus(id: Int, isCompleted: Boolean)
}
