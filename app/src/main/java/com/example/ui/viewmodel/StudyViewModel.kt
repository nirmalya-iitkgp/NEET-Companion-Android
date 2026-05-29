package com.example.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.Flashcard
import com.example.data.local.StudyReminder
import com.example.data.local.StudySession
import com.example.data.local.SessionGoal
import com.example.data.repository.StudyRepository
import com.example.receiver.ReminderReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PomodoroMode(val displayName: String, val defaultMinutes: Int) {
    FOCUS("Focus Block", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    // --- Study Sessions State ---
    val studySessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Reminders State ---
    val reminders: StateFlow<List<StudyReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Flashcards State ---
    val flashcards: StateFlow<List<Flashcard>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctDecks: StateFlow<List<String>> = repository.distinctDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Session Goals State ---
    val sessionGoals: StateFlow<List<SessionGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected deck for practicing flashcards
    private val _selectedDeck = MutableStateFlow<String?>(null)
    val selectedDeck: StateFlow<String?> = _selectedDeck.asStateFlow()

    private val _deckFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val deckFlashcards: StateFlow<List<Flashcard>> = _deckFlashcards.asStateFlow()

    private var deckJob: Job? = null

    fun selectDeck(deckName: String?) {
        _selectedDeck.value = deckName
        deckJob?.cancel()
        if (deckName != null) {
            deckJob = viewModelScope.launch {
                repository.getFlashcardsByDeck(deckName).collect { cards ->
                    _deckFlashcards.value = cards
                }
            }
        } else {
            _deckFlashcards.value = emptyList()
        }
    }

    // --- Pomodoro State ---
    var pomodoroMode by mutableStateOf(PomodoroMode.FOCUS)
        private set
    var timerSecondsRemaining by mutableStateOf(PomodoroMode.FOCUS.defaultMinutes * 60)
        private set
    var isTimerRunning by mutableStateOf(false)
        private set

    // Total seconds for progress calculation
    var timerTotalSeconds by mutableStateOf(PomodoroMode.FOCUS.defaultMinutes * 60)
        private set

    private var timerJob: Job? = null

    // Tracking the sessions completed during this session
    var focusBlocksCompletedCount by mutableStateOf(0)
        private set

    // --- Motivational State for Clinicians ---
    val motivationalQuotes = listOf(
        Quote("Clinical mastery is built in successive focus blocks. Every card you review today is a correct diagnosis tomorrow.", "Harrison Companion"),
        Quote("NEET SS requires deep focus. Master the pathophysiology, understand the core clinical signs, and the score will follow.", "Harrison Companion"),
        Quote("The finest physicians are refined through endless curiosity. Let every case study lead you to deeper knowledge.", "Harrison Companion"),
        Quote("Keep studying, Doctor. The future patients of your super-speciality are counting on your clinical decisions.", "Medical Finals Focus"),
        Quote("Repetition is the mother of retention. Secure your high-yield findings card-by-card and concept-by-concept.", "Spaced Repetition Maxim"),
        Quote("Don't just study to clear finals. Study to heal, to discover, and to master the elegant science of human life.", "Osler's Legacy"),
        Quote("Continuous concentration builds clinical competence. You are training the elite clinical judgment of a super-specialist.", "Medical Finals Focus"),
        Quote("Every card mastered and every block complete is an investment in your future clinical rounds. Stay focused.", "Spaced Repetition Maxim"),
        Quote("Success is the accumulation of quiet, focused hours of study. Harrison's Internal Medicine is mastered layout by layout.", "Chief Resident"),
        Quote("Let your motivation be the lives saved by your rapid clinical recall. Keep refined, stay steadfast.", "Chief Resident")
    )

    var currentQuote by mutableStateOf(motivationalQuotes.first())
        private set

    init {
        rotateQuote()
    }

    fun rotateQuote() {
        currentQuote = motivationalQuotes.random()
    }

    // --- Session Goals Actions ---
    fun addSessionGoal(text: String, subjectTag: String = "") {
        viewModelScope.launch {
            repository.insertGoal(
                SessionGoal(
                    text = text,
                    isCompleted = false,
                    subjectTag = subjectTag
                )
            )
        }
    }

    fun toggleGoalCompleted(goalId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateGoalStatus(goalId, isCompleted)
        }
    }

    fun deleteGoal(goal: SessionGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // --- Study Sessions actions ---
    fun addStudySession(subject: String, durationMinutes: Int, notes: String, mood: String) {
        viewModelScope.launch {
            repository.insertSession(
                StudySession(
                    subject = subject,
                    durationMinutes = durationMinutes,
                    notes = notes,
                    mood = mood
                )
            )
            rotateQuote()
        }
    }

    fun deleteStudySession(session: StudySession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    // --- Pomodoro Actions ---
    fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true
        timerJob = viewModelScope.launch {
            while (timerSecondsRemaining > 0) {
                delay(1000L)
                timerSecondsRemaining--
            }
            onTimerComplete()
        }
    }

    fun pauseTimer() {
        isTimerRunning = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        isTimerRunning = false
        timerJob?.cancel()
        timerSecondsRemaining = pomodoroMode.defaultMinutes * 60
        timerTotalSeconds = timerSecondsRemaining
    }

    fun setCustomTimerDuration(minutes: Int) {
        isTimerRunning = false
        timerJob?.cancel()
        timerSecondsRemaining = minutes * 60
        timerTotalSeconds = timerSecondsRemaining
    }

    fun changePomodoroMode(mode: PomodoroMode) {
        isTimerRunning = false
        timerJob?.cancel()
        pomodoroMode = mode
        timerSecondsRemaining = mode.defaultMinutes * 60
        timerTotalSeconds = timerSecondsRemaining
    }

    private fun onTimerComplete() {
        isTimerRunning = false
        if (pomodoroMode == PomodoroMode.FOCUS) {
            focusBlocksCompletedCount++
        }
        // Auto rotate quote upon completion to encourage user
        rotateQuote()
    }

    // --- Reminders actions ---
    fun addReminder(context: Context, subject: String, triggerIntervalMinutes: Int, message: String) {
        viewModelScope.launch {
            val triggerTime = System.currentTimeMillis() + (triggerIntervalMinutes * 60000L)
            
            // Generate standard message if none provided
            val finalMessage = message.ifBlank {
                "Take a deep breath and review your study notes for $subject! You got this! 🌟"
            }

            val id = repository.insertReminder(
                StudyReminder(
                    subject = subject,
                    reminderTime = triggerTime,
                    message = finalMessage,
                    isCompleted = false
                )
            )

            // Register system wide alarm
            scheduleAlarm(context, id.toInt(), subject, triggerTime, finalMessage)
        }
    }

    fun deleteReminder(reminder: StudyReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun toggleReminderCompleted(reminderId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateReminderStatus(reminderId, isCompleted)
        }
    }

    private fun scheduleAlarm(context: Context, id: Int, subject: String, triggerTimeMillis: Long, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("subject", subject)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d("StudyViewModel", "Successfully scheduled alarm for $subject at millis $triggerTimeMillis")
        } catch (e: SecurityException) {
            // Safe fallback for Android 14+ if permission exact alarms isn't enabled
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
            Log.w("StudyViewModel", "SecurityException scheduling exact alarm, fallback to standard: ${e.message}")
        }
    }

    // --- Flashcard actions ---
    fun addFlashcard(deckName: String, question: String, answer: String) {
        viewModelScope.launch {
            repository.insertFlashcard(
                Flashcard(
                    deckName = deckName.trim().ifBlank { "General" },
                    question = question,
                    answer = answer
                )
            )
        }
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcard)
            // If deleting last card in selected deck, return to decks list
            if (deckFlashcards.value.size <= 1 && selectedDeck.value == flashcard.deckName) {
                selectDeck(null)
            }
        }
    }

    fun recordFlashcardScore(flashcard: Flashcard, isCorrect: Boolean) {
        viewModelScope.launch {
            val newReviews = flashcard.timesReviewed + 1
            val newCorrect = if (isCorrect) flashcard.timesCorrect + 1 else flashcard.timesCorrect
            repository.updateFlashcardProgress(flashcard.id, newReviews, newCorrect)
        }
    }

    data class Quote(val text: String, val author: String)
}

// ViewModel Factory
class StudyViewModelFactory(private val repository: StudyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
