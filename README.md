# Medical Study Companion 📚🩺✨

**Medical Study Companion** is a visually stunning, fully offline, client-side Android application designed specifically for Post Graduates in Medicine preparing for major Finals (MD/MS) and competitive super-speciality exams (like NEET SS). It helps resident doctors maximize their focus, set structured targets, review high-stakes clinical cases, and track review consistency through layered, offline analytics.

Built completely with **Kotlin, Jetpack Compose, and Room Database (SQLite)** following clean architecture protocols. Absolutely zero external AI integrations are used in-app for privacy, and the interface is saturated with medical-oriented motivational themes.

---

## 🌟 Major Highlights & Modules

### 1. Medical Board Prep Analytics & Habits Tracker (Dashboard)
- **Clinical Habit Indicators**: Deep analytical computation of study habits over the **Past Week** and **Past Month**. Tracks:
  - **Study Duration**: Cumulative hours of intense prep.
  - **Preparation Frequency**: Number of distinct study sessions logged.
  - **Consistency Index**: Fraction of days with active logs (e.g. *x out of y days active*) to measure spaced persistence.
- **Chief Resident Appraisal reports**: A warm, encouraging appraisal widget that updates periodically based on study frequency, pushing doctors to sharpen their diagnostic precision.
- **Residency Board Targets Checklist**: Define custom micro-goals (e.g., *Harrison's Cardiology Chapter 10*, *Solve 50 Nephrology MCQ*) with prefilled clinical specialty chips. Check them complete post-reflections.

### 2. Clinical Inspiration Widget
- **Doctor's Affirmations**: Hand-picked clinical and academic quotes designed for high-pressure medical preparation. Rotate top-tier mantras by simply tapping the banner (e.g., masters of Harrison, Osler legacies, clinical prompt updates).

### 3. Explore & Study Logs (Dashboard)
- **Logs**: Record completed study blocks detailing clinical specialty, duration, reflections, and context-aware mindset/mood states (e.g., *Focused*, *Relaxed*, *Energized*, or *Challenged*). Predefined subject prefill chips include *Harrison's*, *Cardiology*, *Neurology*, and *Clinical MCQ*.

### 4. Built-in Pomodoro Focus Timer
- **Circular Progress Countdown**: Dynamic, glowing visual countdown ring mapped exactly to fractional second states.
- **Mode Toggle Presets**: Seamlessly slide between standard blocks (Focus for 25m, Short Break for 5m, Long Break for 15m).
- **Custom Duration Support**: Set your own customized time limits in minutes as clinical review demands.
- **Completed Sets counter**: Monitor deep-work intervals logged throughout the day.

### 5. Active Recall Flashcards
- **Custom Medical Decks**: Create separate flashcard decks (e.g., *Internal Medicine*, *Cardiology*, *Neurology*, *Clinical MCQ*).
- **Interactive 3D Flip Cards**: Click individual study cards to trigger a realistic, gratifying Y-axis rotating flip animation that swaps between clinical questions and correct diagnostic criteria.
- **Score Logging**: Rate retrieval success by tapping "Wrong, study more" vs "Got it, simple!" to increment review counts.

### 6. Clinical Core Navigation Drawer (App Drawer)
- **High-Fidelity Branding**: Beautiful custom circular PNG companion header logo mapped onto a soft purple layered elevated surface with dynamic drop-shadows.
- **Unified Navigation Hub**: Access quick navigation links matching bottom bar configurations, plus an active resident clinic level status indicator (`STATUS: Chief Resident 🩺`).
- **Footer Clinical Tips**: Integrated dynamic medical review tip drawer container reminding doctors of spaced recall benefits.

### 7. Spaced Reminders & Alarms (Alarms)
- **Spaced Repetition Integration**: Set reminders based on review intervals (custom quick presets of 1 min, 10 mins, or days).
- **Exact System Alarms**: Schedules precise OS alerts utilizing Android's `AlarmManager` and custom `ReminderReceiver` broadcast configurations.
- **Local Push Notifications**: Dispatches push alerts with clinical motivational text prompts even when the app is completely closed.

---

## 💡 Styling Choices & Creative Design
- **Inspirational Sunset/Indigo Theme**: Replaces generic, dry gray backgrounds with layered glass-morphic cards, linear gradient backgrounds, and golden color palettes.
- **Material Design 3**: Complete adherence to accessibility guidelines including Material ripples, minimum 48dp tappable fields including sliding App Drawer, and adaptive spacing models.
- **Unified Avatar Brand**: The premium companion PNG image is unified across all screens as a custom-styled, interactive avatar button that opens the Navigation Drawer.

---

## 🛠️ Stack & Architecture
- **Language**: Kotlin 100%
- **Database**: Android Room (with Kotlin Symbol Processing - KSP compiler)
- **State Pattern**: Jetpack ViewModel & MutableStateFlow reactive streams
- **UI Framework**: Modern Jetpack Compose with Edge-to-Edge display configurations
