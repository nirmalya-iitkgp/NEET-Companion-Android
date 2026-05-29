package com.example.data.repository

import com.example.data.local.Flashcard
import com.example.data.local.StudyDao
import com.example.data.local.StudyReminder
import com.example.data.local.StudySession
import com.example.data.local.SessionGoal
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {

    val allSessions: Flow<List<StudySession>> = studyDao.getAllSessions()
    
    val allReminders: Flow<List<StudyReminder>> = studyDao.getAllReminders()
    
    val allFlashcards: Flow<List<Flashcard>> = studyDao.getAllFlashcards()
    
    val distinctDecks: Flow<List<String>> = studyDao.getDistinctDecks()

    val allGoals: Flow<List<SessionGoal>> = studyDao.getAllGoals()

    suspend fun insertSession(session: StudySession): Long {
        return studyDao.insertSession(session)
    }

    suspend fun deleteSession(session: StudySession) {
        studyDao.deleteSession(session)
    }

    suspend fun insertReminder(reminder: StudyReminder): Long {
        return studyDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(reminder: StudyReminder) {
        studyDao.deleteReminder(reminder)
    }

    suspend fun updateReminderStatus(id: Int, isCompleted: Boolean) {
        studyDao.updateReminderStatus(id, isCompleted)
    }

    fun getFlashcardsByDeck(deckName: String): Flow<List<Flashcard>> {
        return studyDao.getFlashcardsByDeck(deckName)
    }

    suspend fun insertFlashcard(flashcard: Flashcard): Long {
        return studyDao.insertFlashcard(flashcard)
    }

    suspend fun deleteFlashcard(flashcard: Flashcard) {
        studyDao.deleteFlashcard(flashcard)
    }

    suspend fun updateFlashcardProgress(id: Int, reviews: Int, correct: Int) {
        studyDao.updateFlashcardProgress(id, reviews, correct)
    }

    suspend fun insertGoal(goal: SessionGoal): Long {
        return studyDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: SessionGoal) {
        studyDao.deleteGoal(goal)
    }

    suspend fun updateGoalStatus(id: Int, isCompleted: Boolean) {
        studyDao.updateGoalStatus(id, isCompleted)
    }
}
