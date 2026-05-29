package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.local.Flashcard
import com.example.data.local.StudyReminder
import com.example.data.local.StudySession
import com.example.ui.viewmodel.PomodoroMode
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class AppTab(val title: String, val iconSelected: androidx.compose.ui.graphics.vector.ImageVector, val iconUnselected: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Explore", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    POMODORO("Focus", Icons.Filled.Timer, Icons.Outlined.Timer),
    FLASHCARDS("Cards", Icons.Filled.Style, Icons.Outlined.Style),
    REMINDERS("Alarms", Icons.Filled.NotificationsActive, Icons.Outlined.Notifications)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: StudyViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Dialog state controllers
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    // Request Notifications Permission for Android 13+
    var hasNotificationPermission by remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Notification permission denied. In-app reminders will still work, but custom push alerts will be silenced.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Gradient Background Palette
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.background
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFFFDF8FF),
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Drawer Header with Companion PNG Image and medical status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape),
                            color = Color(0xFFEADDFF),
                            shadowElevation = 4.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.study_companion_icon_1780075626828),
                                contentDescription = "Companion Header Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Study Companion",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D),
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "MD/MS & NEET SS Portal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6750A4)
                            )
                            Text(
                                text = "STATUS: Chief Resident 🩺",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFCAC4D0).copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CLINICAL NAVIGATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Drawer Items
                    AppTab.values().forEach { tab ->
                        NavigationDrawerItem(
                            label = { 
                                Text(
                                    tab.title, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp
                                ) 
                            },
                            selected = currentTab == tab,
                            onClick = {
                                currentTab = tab
                                scope.launch { drawerState.close() }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == tab) tab.iconSelected else tab.iconUnselected,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFFE8DEF8),
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = Color(0xFF1D192B),
                                unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                                selectedTextColor = Color(0xFF1D192B),
                                unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(100)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Inspiration Note inside Drawer Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3EDF7), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.MenuBook,
                                    contentDescription = "Clinician tip",
                                    tint = Color(0xFF6750A4),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CLINICAL TIP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6750A4)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Rapid active recall (swiping flashcards) is the best way to master Harrison's content for board exams. Do 10 minutes daily!",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFFF3EDF7),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .drawBehind {
                            drawLine(
                                color = Color(0xFFCAC4D0),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    AppTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == tab) tab.iconSelected else tab.iconUnselected,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(tab.title, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1D192B),
                                unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                                selectedTextColor = Color(0xFF1D192B),
                                unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                                indicatorColor = Color(0xFFE8DEF8)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFFDF8FF)) // Unified Artistic Flair Canvas Background
            ) {
                Crossfade(targetState = currentTab, label = "tabSearch") { tab ->
                    when (tab) {
                        AppTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onAddSessionClick = { showAddSessionDialog = true },
                            onNavigateToTab = { currentTab = it },
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )
                        AppTab.POMODORO -> PomodoroScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )
                        AppTab.FLASHCARDS -> FlashcardsScreen(
                            viewModel = viewModel,
                            onAddCardClick = { showAddCardDialog = true },
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )
                        AppTab.REMINDERS -> RemindersScreen(
                            viewModel = viewModel,
                            onAddReminderClick = { showAddReminderDialog = true },
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )
                    }
                }

            // Dialogs
            if (showAddSessionDialog) {
                AddStudySessionDialog(
                    onDismiss = { showAddSessionDialog = false },
                    onConfirm = { subject, duration, notes, mood ->
                        viewModel.addStudySession(subject, duration, notes, mood)
                        showAddSessionDialog = false
                    }
                )
            }

            if (showAddCardDialog) {
                AddFlashcardDialog(
                    onDismiss = { showAddCardDialog = false },
                    availableDecks = viewModel.distinctDecks.collectAsStateWithLifecycle().value,
                    onConfirm = { deck, question, answer ->
                        viewModel.addFlashcard(deck, question, answer)
                        showAddCardDialog = false
                    }
                )
            }

            if (showAddReminderDialog) {
                AddReminderDialog(
                    onDismiss = { showAddReminderDialog = false },
                    onConfirm = { subject, interval, message ->
                        viewModel.addReminder(context, subject, interval, message)
                        showAddReminderDialog = false
                    }
                )
            }
        }
    }
}
}

// ==========================================
// 1. DASHBOARD SCREEN & SESSION LOGS
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    onAddSessionClick: () -> Unit,
    onNavigateToTab: (AppTab) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val sessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val goals by viewModel.sessionGoals.collectAsStateWithLifecycle()
    val quote = viewModel.currentQuote
    
    val totalTimeStudied = sessions.sumOf { it.durationMinutes }
    val uniqueSubjects = sessions.map { it.subject.lowercase().trim() }.distinct().size

    // Time calculations
    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L
    val sevenDaysAgo = now - (7 * oneDayMs)
    val thirtyDaysAgo = now - (30 * oneDayMs)

    // filter sessions for last 7 days and last 30 days
    val weeklySessions = sessions.filter { it.timestamp >= sevenDaysAgo }
    val monthlySessions = sessions.filter { it.timestamp >= thirtyDaysAgo }

    val weeklyDuration = weeklySessions.sumOf { it.durationMinutes }
    val weeklyFrequency = weeklySessions.size
    
    // consistency past 7 days calculation:
    val weeklyDaysActive = weeklySessions.map { 
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date(it.timestamp))
    }.distinct().size
    val weeklyConsistency = (weeklyDaysActive * 100) / 7

    val monthlyDuration = monthlySessions.sumOf { it.durationMinutes }
    val monthlyFrequency = monthlySessions.size
    val monthlyDaysActive = monthlySessions.map {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date(it.timestamp))
    }.distinct().size
    val monthlyConsistency = (monthlyDaysActive * 100) / 30

    // Encouraging Appraisal based on study habit consistency/frequency
    val (appraisalTitle, appraisalMsg) = when {
        sessions.isEmpty() -> {
            Pair(
                "A Doctor's Journey Begins",
                "Every clinician's mastery of Harrison's begins with a single focused hour. Add your first study block or start our deep work timer to begin practicing clinic-level recall, doctor!"
            )
        }
        weeklyFrequency >= 5 || weeklyConsistency >= 70 -> {
            Pair(
                "Super-Speciality High Performance!",
                "Incredible dedication, doctor! You are building top-tier consistency required for NEET SS. Your rapid and sharp medical recall in finals is guaranteed at this pace!"
            )
        }
        weeklyFrequency in 1..4 -> {
            Pair(
                "Steady Clinical Rounds",
                "Decent progress, senior resident! To truly secure these medical topics in your long-term memory for final boards, let's aim for a couple more logs or active recall card rounds this week."
            )
        }
        else -> {
            Pair(
                "Reignite Your Focus Arc",
                "It looks like clinical duty has been highly demanding. Even 15 minutes of spaced repetition or cardiology card swipe a day keeps your diagnostic skills sharp. Let's do a minor 15m review to stay on top!"
            )
        }
    }

    // Determine counts and descriptions for shortcut cards
    val flashcardCount = flashcards.size
    val activeReminderCount = reminders.filter { !it.isCompleted }.size
    val nextReminder = reminders.filter { !it.isCompleted }.minByOrNull { it.reminderTime }
    val nextReminderText = if (nextReminder != null) {
        "Next: ${nextReminder.subject}"
    } else {
        "None scheduled"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Custom Artistic Header: Rise & Refine ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Rise &",
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = "Refine.",
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = "SESSION • ${if (totalTimeStudied < 60) "${totalTimeStudied}m" else "${String.format("%.1f", totalTimeStudied / 60.0)}h"} TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 1.6.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEADDFF))
                        .graphicsLayer()
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF6750A4),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                        .clickable { onOpenDrawer() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.study_companion_icon_1780075626828),
                        contentDescription = "User avatar logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // --- Motivational Quote Banner (Saturated deep purple and gold text) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6750A4)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .clickable { viewModel.rotateQuote() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = "Quote hint",
                            tint = Color(0xFFFFE082),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SPARK OF MOTIVATION",
                            fontSize = 10.sp,
                            color = Color(0xFFEADDFF),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Filled.Autorenew,
                            contentDescription = "Rotate",
                            tint = Color(0xFFEADDFF).copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "\"${quote.text}\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "— ${quote.author}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFE082)
                    )
                }
            }
        }

        // --- Shortcut Grid Cards (Exact specs from mockup layout) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Flashcards shortcut card (lilac styling)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clickable { onNavigateToTab(AppTab.FLASHCARDS) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Style,
                            contentDescription = "Flashcards shortcut",
                            tint = Color(0xFF21005D),
                            modifier = Modifier.size(30.dp)
                        )
                        Column {
                            Text(
                                text = "Flashcards",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (flashcardCount == 0) "No terms to review" else "$flashcardCount term(s) to review",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF49454F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Reminders shortcut card (mustard gold styling)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clickable { onNavigateToTab(AppTab.REMINDERS) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE082)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Reminders shortcut",
                            tint = Color(0xFF4527A0),
                            modifier = Modifier.size(30.dp)
                        )
                        Column {
                            Text(
                                text = "Reminders",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4527A0),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (activeReminderCount == 0) "No queue scheduled" else "$nextReminderText",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // --- Standard Metrics Row (Time & Subjects) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Study Time",
                    value = if (totalTimeStudied < 60) "${totalTimeStudied}m" else "${String.format("%.1f", totalTimeStudied / 60.0)}h",
                    subtitle = "All blocks",
                    icon = Icons.Outlined.CheckCircle,
                    color = Color(0xFF6750A4)
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Subjects",
                    value = uniqueSubjects.toString(),
                    subtitle = "Explored",
                    icon = Icons.Outlined.AutoStories,
                    color = Color(0xFFFFE082)
                )
            }
        }

        // --- RESIDENCY STUDY HABITS ANALYTICS PANEL ---
        item {
            var selectedPeriodTab by remember { mutableStateOf(0) } // 0 = Past Week, 1 = Past Month
            
            val activeDuration = if (selectedPeriodTab == 0) weeklyDuration else monthlyDuration
            val activeFrequency = if (selectedPeriodTab == 0) weeklyFrequency else monthlyFrequency
            val activeConsistency = if (selectedPeriodTab == 0) weeklyConsistency else monthlyConsistency
            val daysCount = if (selectedPeriodTab == 0) 7 else 30
            val activeDays = if (selectedPeriodTab == 0) weeklyDaysActive else monthlyDaysActive

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.QueryStats,
                                contentDescription = "Query Stats",
                                tint = Color(0xFF21005D),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Clinical habits analytics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )
                        }
                        
                        // Tab Selector
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFE8DEF8), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "WEEK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPeriodTab == 0) Color.White else Color(0xFF49454F),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedPeriodTab == 0) Color(0xFF6750A4) else Color.Transparent)
                                    .clickable { selectedPeriodTab = 0 }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Text(
                                text = "MONTH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPeriodTab == 1) Color.White else Color(0xFF49454F),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedPeriodTab == 1) Color(0xFF6750A4) else Color.Transparent)
                                    .clickable { selectedPeriodTab = 1 }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Three columns metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("DURATION", fontSize = 10.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (activeDuration < 60) "${activeDuration}m" else "${String.format("%.1f", activeDuration / 60.0)}h",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF21005D)
                            )
                            Text("Study time", fontSize = 9.sp, color = Color(0xFF49454F).copy(alpha = 0.7f))
                        }

                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFCAC4D0)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("FREQUENCY", fontSize = 10.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$activeFrequency blocks",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF21005D)
                            )
                            Text("Sessions", fontSize = 9.sp, color = Color(0xFF49454F).copy(alpha = 0.7f))
                        }

                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFCAC4D0)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("CONSISTENCY", fontSize = 10.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$activeConsistency%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF21005D)
                            )
                            Text("$activeDays/$daysCount days active", fontSize = 9.sp, color = Color(0xFF49454F).copy(alpha = 0.7f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chief Resident Appraisal box (encapsulated in a light gold bubble)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFE082).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocalHospital,
                                    contentDescription = "Chief appraiser",
                                    tint = Color(0xFF4527A0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = appraisalTitle.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4527A0),
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = appraisalMsg,
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037),
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // --- ACTIVE STUDY GOALS / TARGETS CHECKLIST ---
        item {
            var newGoalText by remember { mutableStateOf("") }
            val quickMedicalTags = listOf("Cardiology", "Neurology", "Harrison's", "NEET SS MCQ", "Pathology")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrackChanges,
                                contentDescription = "Active Goals",
                                tint = Color(0xFF6750A4),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Session Board Targets",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                        }
                        
                        // Active count
                        val pendingCount = goals.filter { !it.isCompleted }.size
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEADDFF), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$pendingCount PENDING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF21005D)
                            )
                        }
                    }

                    Text(
                        text = "Define micro-targets for this study block. Check them complete after focus session finishes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF49454F),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Text Field Row to add Custom Goal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newGoalText,
                            onValueChange = { newGoalText = it },
                            placeholder = { Text("e.g. Solve 50 Harrison MCQs", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )
                        IconButton(
                            onClick = {
                                if (newGoalText.isNotBlank()) {
                                    viewModel.addSessionGoal(newGoalText.trim())
                                    newGoalText = ""
                                }
                            },
                            modifier = Modifier
                                .background(Color(0xFF6750A4), CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Goal",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Quick suggestive clinical tags that auto pre-fills or inserts
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 6.dp)
                    ) {
                        quickMedicalTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
                                    .clickable {
                                        newGoalText = "Review $tag: "
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+ $tag",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6750A4)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Goals list
                    if (goals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFDF8FF), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No session targets defined. Add targets to boost board consistency! 🎯",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F).copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            goals.forEach { goal ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFDF8FF), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = goal.isCompleted,
                                        onCheckedChange = { isChecked ->
                                            viewModel.toggleGoalCompleted(goal.id, isChecked)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF6750A4),
                                            uncheckedColor = Color(0xFF49454F)
                                        )
                                    )
                                    
                                    Spacer(modifier = Modifier.width(6.dp))
                                    
                                    Text(
                                        text = goal.text,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (goal.isCompleted) Color(0xFF1C1B1F).copy(alpha = 0.5f) else Color(0xFF1C1B1F),
                                        modifier = Modifier.weight(1f),
                                        textDecoration = if (goal.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )

                                    IconButton(
                                        onClick = { viewModel.deleteGoal(goal) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Delete target",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Recent Sessions Header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sessions",
                    fontFamily = FontFamily.Serif,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "ADD SESSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF6750A4),
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable { onAddSessionClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }

        // --- Study Sessions List or Empty State ---
        if (sessions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Empty list",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No study logs recorded yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remember, the expert in anything was once a beginner. Start logging your first study block today!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                SessionCard(
                    session = session,
                    onDelete = { viewModel.deleteStudySession(session) }
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.15f), CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SessionCard(session: StudySession, onDelete: () -> Unit) {
    val dateString = remember(session.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(session.timestamp))
    }
    val xpEarned = session.durationMinutes * 2

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3EDF7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.subject,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "$dateString • ${session.durationMinutes}m • ${session.mood}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF49454F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (session.notes.isNotBlank()) {
                    Text(
                        text = session.notes,
                        fontSize = 12.sp,
                        color = Color(0xFF49454F).copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+$xpEarned XP",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color(0xFF1D192B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete Session",
                    tint = Color.Red.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}

// ==========================================
// 2. POMODORO TIMER SCREEN (FOCUS CONTROL)
// ==========================================
@Composable
fun PomodoroScreen(viewModel: StudyViewModel, onOpenDrawer: () -> Unit) {
    val currentMode = viewModel.pomodoroMode
    val secondsRemaining = viewModel.timerSecondsRemaining
    val totalSeconds = viewModel.timerTotalSeconds
    val isRunning = viewModel.isTimerRunning
    val completedBlocks = viewModel.focusBlocksCompletedCount

    // Circular Progress Calculation
    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds else 1f

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    var showCustomDialog by remember { mutableStateOf(false) }
    var customMinutesInput by remember { mutableStateOf("25") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Artistic Screen Header ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Focus &",
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = "Intervals.",
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = "COMPLETED SETS • $completedBlocks BLOCKS TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 1.6.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEADDFF))
                        .graphicsLayer()
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF6750A4),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                        .clickable { onOpenDrawer() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.study_companion_icon_1780075626828),
                        contentDescription = "User avatar logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // --- Beautiful Deep Work Countdown Card (Exact mockup specification) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF6750A4))
                .drawBehind {
                    // Decorative top-right circle
                    drawCircle(
                        color = Color(0xFFD0BCFF).copy(alpha = 0.15f),
                        radius = 160.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width + 40.dp.toPx(), -20.dp.toPx())
                    )
                    // Decorative bottom-left circular border
                    drawCircle(
                        color = Color(0xFFD0BCFF).copy(alpha = 0.25f),
                        radius = 96.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(-10.dp.toPx(), size.height + 10.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Minimal progress circle track
                    val radius = size.minDimension / 3.4f
                    val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    drawCircle(
                        color = Color(0xFFD0BCFF).copy(alpha = 0.1f),
                        radius = radius,
                        center = centerOffset,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (currentMode) {
                        PomodoroMode.FOCUS -> "DEEP WORK PHASE"
                        PomodoroMode.SHORT_BREAK -> "BRAIN BREATHING PHASE"
                        PomodoroMode.LONG_BREAK -> "RECHARGE MIND PHASE"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEADDFF),
                    letterSpacing = 2.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-2).sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentMode.displayName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // --- Controls and Preset Selection ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Preset Tabs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PomodoroMode.values().forEach { mode ->
                    Button(
                        onClick = { viewModel.changePomodoroMode(mode) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentMode == mode) Color(0xFFE8DEF8) else Color(0xFFF3EDF7),
                            contentColor = Color(0xFF21005D)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = mode.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .background(Color(0xFFF3EDF7), CircleShape)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestartAlt,
                        contentDescription = "Restart Timer",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Play / Pause FAB
                FloatingActionButton(
                    onClick = {
                        if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                    },
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Custom Interval Length
                IconButton(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier
                        .background(Color(0xFFF3EDF7), CircleShape)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Custom length",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Custom Time Dialog
        if (showCustomDialog) {
            AlertDialog(
                onDismissRequest = { showCustomDialog = false },
                title = { Text("Configure Focus Block", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
                text = {
                    Column {
                        Text("Add customized focus span in minutes:", modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = customMinutesInput,
                            onValueChange = { customMinutesInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val mins = customMinutesInput.toIntOrNull() ?: 25
                            if (mins > 0) {
                                viewModel.setCustomTimerDuration(mins)
                            }
                            showCustomDialog = false
                        }
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomDialog = false }) {
                        Text("Cancel", color = Color(0xFF6750A4).copy(alpha = 0.6f))
                    }
                }
            )
        }
    }
}

// ==========================================
// 3. FLASHCARDS SCREEN & ANIMATED REVIEW
// ==========================================
@Composable
fun FlashcardsScreen(viewModel: StudyViewModel, onAddCardClick: () -> Unit, onOpenDrawer: () -> Unit) {
    val context = LocalContext.current
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    val distinctDecks by viewModel.distinctDecks.collectAsStateWithLifecycle()
    val selectedDeck by viewModel.selectedDeck.collectAsStateWithLifecycle()
    val deckFlashcards by viewModel.deckFlashcards.collectAsStateWithLifecycle()

    if (selectedDeck == null) {
        // --- DECKS SELECTION PANEL ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("flashcards_decks_list"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Recall &",
                            fontSize = 38.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Black,
                            lineHeight = 36.sp,
                            color = Color(0xFF21005D)
                        )
                        Text(
                            text = "Retain.",
                            fontSize = 38.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Black,
                            lineHeight = 36.sp,
                            color = Color(0xFF21005D)
                        )
                        Text(
                            text = "ACTIVE LEARNING DECKS • ${flashcards.size} TOTAL CARDS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F),
                            letterSpacing = 1.6.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEADDFF))
                            .graphicsLayer()
                            .drawBehind {
                                drawCircle(
                                    color = Color(0xFF6750A4),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                            .clickable { onOpenDrawer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.study_companion_icon_1780075626828),
                            contentDescription = "User avatar logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            // Quick Actions Block
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Decks",
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Text(
                        text = "NEW CARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6750A4),
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .clickable { onAddCardClick() }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }

            if (distinctDecks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Empty Decks",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Unlock Active Learning",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TranslateInspireText(
                            text = "Flashcards are proven to enhance long-term memory. Create custom question-answer cards to begin testing yourself!",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                items(distinctDecks) { deck ->
                    val cardsInDeck = flashcards.filter { it.deckName == deck }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectDeck(deck) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8DEF8), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Collections,
                                    contentDescription = "Deck Icon",
                                    tint = Color(0xFF21005D)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = deck,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1F)
                                )
                                Text(
                                    text = "${cardsInDeck.size} cards • ${cardsInDeck.sumOf { it.timesReviewed }} reviews",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF49454F)
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowForwardIos,
                                contentDescription = "Open deck",
                                tint = Color(0xFF1C1B1F).copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // --- CARDS ACTIVE REVIEW PRACTICE ---
        var currentCardIndex by remember { mutableStateOf(0) }
        var isCardFlipped by remember { mutableStateOf(false) }

        LaunchedEffect(selectedDeck) {
            currentCardIndex = 0
            isCardFlipped = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.selectDeck(null) },
                    modifier = Modifier.background(Color(0xFFF3EDF7), CircleShape).size(40.dp)
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go back", tint = Color(0xFF21005D))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = selectedDeck ?: "Deck Study",
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = "Card ${if (deckFlashcards.isEmpty()) 0 else currentCardIndex + 1} of ${deckFlashcards.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF49454F)
                    )
                }
                IconButton(
                    onClick = {
                        val activeCard = deckFlashcards.getOrNull(currentCardIndex)
                        if (activeCard != null) {
                            viewModel.deleteFlashcard(activeCard)
                            // If index is out of bounds, slide it back
                            if (currentCardIndex >= deckFlashcards.size - 1) {
                                currentCardIndex = (deckFlashcards.size - 2).coerceAtLeast(0)
                            }
                            isCardFlipped = false
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red.copy(alpha = 0.8f)),
                    modifier = Modifier.background(Color(0xFFF9DEDC), CircleShape).size(40.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete Flashcard", modifier = Modifier.size(18.dp))
                }
            }

            if (deckFlashcards.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No cards remain in this deck.",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F)
                    )
                }
            } else {
                val activeCard = deckFlashcards.getOrNull(currentCardIndex)
                if (activeCard == null) {
                    LaunchedEffect(deckFlashcards.size) {
                        currentCardIndex = (deckFlashcards.size - 1).coerceAtLeast(0)
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6750A4))
                    }
                } else {
                    // Flip animation components using Y-axis rotation
                val rotationY by animateFloatAsState(
                    targetValue = if (isCardFlipped) 180f else 0f,
                    animationSpec = tween(durationMillis = 400),
                    label = "flashcardFloatY"
                )

                // Flashcard Canvas Body
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 20.dp)
                        .graphicsLayer {
                            this.rotationY = rotationY
                            this.cameraDistance = 12f * density
                        }
                        .clickable { isCardFlipped = !isCardFlipped },
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCardFlipped) Color(0xFFFFE082) else Color(0xFFF3EDF7)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotationY <= 90f) {
                            // FRONT VALUE (Question - Lilac Theme)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "QUESTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF21005D),
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = activeCard.question,
                                    fontFamily = FontFamily.Serif,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF1D192B),
                                    lineHeight = 28.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE8DEF8), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = activeCard.deckName.uppercase(Locale.ROOT), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEADDFF), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "HIGH YIELD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFF0C2), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "ACTIVE RECALL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5C00))
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "👉 Tap card to flip & reveal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D).copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            // BACK VALUE (Answer - Mustard Gold Theme)
                            Column(
                                modifier = Modifier.graphicsLayer { this.rotationY = 180f },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "ANSWER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF5D4037),
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = activeCard.answer,
                                    fontFamily = FontFamily.Serif,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF5D4037),
                                    lineHeight = 28.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF5D4037).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = activeCard.deckName.uppercase(Locale.ROOT), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF8D6E63).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "RATED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFC8E6C9), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "MEMORIZED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Tap to view question again",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037).copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Interactive Score Assessment Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "How did you do?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.recordFlashcardScore(activeCard, isCorrect = false)
                                Toast.makeText(context, "Saved! Practice makes progress.", Toast.LENGTH_SHORT).show()
                                isCardFlipped = false
                                if (currentCardIndex < deckFlashcards.size - 1) {
                                    currentCardIndex++
                                } else {
                                    currentCardIndex = 0
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF9DEDC),
                                contentColor = Color(0xFF410E0B)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Incorrect", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hard, practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.recordFlashcardScore(activeCard, isCorrect = true)
                                Toast.makeText(context, "Awesome work! Keep it up.", Toast.LENGTH_SHORT).show()
                                isCardFlipped = false
                                if (currentCardIndex < deckFlashcards.size - 1) {
                                    currentCardIndex++
                                } else {
                                    currentCardIndex = 0
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6750A4),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = "Correct", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Got it, easy!", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                }
            }
            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}

// ==========================================
// 4. SPACED REMINDERS / ALARMS MANAGER SCREEN
// ==========================================
@Composable
fun RemindersScreen(viewModel: StudyViewModel, onAddReminderClick: () -> Unit, onOpenDrawer: () -> Unit) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reminders_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Custom Artistic Header: Schedules & Reminders ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Schedules &",
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = "Reminders.",
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = "SPACED REPETITION QUEUE • ${reminders.size} ACTIVE ALERTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 1.6.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEADDFF))
                        .graphicsLayer()
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF6750A4),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                        .clickable { onOpenDrawer() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.study_companion_icon_1780075626828),
                        contentDescription = "User avatar logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // Spaced Repetition power helper card (styled in mustard gold)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFE082)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4527A0), CircleShape)
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = "Spaced Repetitions",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Spaced Repetition Power",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4527A0)
                        )
                        Text(
                            text = "Alarms help schedule review windows at 1-day, 3-day, or week intervals to secure concepts in long-term memory.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }
        }

        // Subtitle section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Reminders",
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Text(
                        text = "Next alerts on queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF49454F)
                    )
                }
                Text(
                    text = "SCHEDULE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF6750A4),
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable { onAddReminderClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }

        if (reminders.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsOff,
                        contentDescription = "No alarms",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No study reminders scheduled yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use study boundaries to trigger motivational alerts. Click 'Schedule' above to add a dynamic spaced review window!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onToggleComplete = { isChecked ->
                        viewModel.toggleReminderCompleted(reminder.id, isChecked)
                    },
                    onDelete = { viewModel.deleteReminder(reminder) }
                )
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: StudyReminder,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val triggerString = remember(reminder.reminderTime) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(reminder.reminderTime))
    }

    val isPassed = reminder.reminderTime < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) Color(0xFFF3EDF7).copy(alpha = 0.6f) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = onToggleComplete,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6750A4), uncheckedColor = Color(0xFF49454F))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Review: ${reminder.subject}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (reminder.isCompleted) Color(0xFF1C1B1F).copy(alpha = 0.5f) else Color(0xFF1C1B1F)
                )
                Text(
                    text = reminder.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF49454F),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPassed) Icons.Filled.CheckCircleOutline else Icons.Filled.Schedule,
                        contentDescription = "Status icon",
                        tint = if (isPassed) Color(0xFF4527A0) else Color(0xFF6750A4),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPassed) "Alert dispatched • $triggerString" else "Alert scheduled • $triggerString",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF49454F).copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete alarm",
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ==========================================
// 5. HELPER TRANSLATE TEXT BLOCK
// ==========================================
@Composable
fun TranslateInspireText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = modifier
    )
}

// ==========================================
// 6. POPUP DIALOG FOR ADDING STUDY SESSION
// ==========================================
@Composable
fun AddStudySessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("Focused") }
    
    val moods = listOf("Focused", "Relaxed", "Challenged", "Energized", "Fatigued")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Study Block", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Subject / Clinical Specialty:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = { Text("e.g. Harrison's Internal Medicine, Cardiology") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Harrison's", "Cardiology", "Neurology", "Clinical MCQ").forEach { topic ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
                                .clickable { subject = topic }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = topic,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4)
                            )
                        }
                    }
                }

                Text("Duration (Minutes):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    placeholder = { Text("e.g. 25, 60, 120") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Current Mood / State of Mind:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    moods.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (mood == item) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { mood = item }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = item,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mood == item) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Text("Study Reflections (Notes):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("What progress did you make? Any bottlenecks?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val durationVal = durationMinutes.toIntOrNull() ?: 25
                    if (subject.isNotBlank()) {
                        onConfirm(subject.trim(), durationVal, notes.trim(), mood)
                    }
                },
                enabled = subject.isNotBlank()
            ) {
                Text("Record Log", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// 7. POPUP DIALOG FOR ADDING FLASHCARDS
// ==========================================
@Composable
fun AddFlashcardDialog(
    onDismiss: () -> Unit,
    availableDecks: List<String>,
    onConfirm: (String, String, String) -> Unit
) {
    var deckName by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Flashcard", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select / Create Deck Name:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    placeholder = { Text("e.g. Internal Medicine, Cardiology") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Deck suggestions
                val defaultPresets = listOf(
                    "Internal Med", "Cardiology", "Neurology", "Anatomy", 
                    "Pathology", "Pharmacology", "Pediatrics", "Surgery", 
                    "Clinical MCQ", "Microbiology"
                )
                val deckSuggestions = (availableDecks + defaultPresets).distinct().take(12)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    deckSuggestions.forEach { existing ->
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .clickable { deckName = existing }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = existing, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Text("Question on front:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    placeholder = { Text("e.g. What is the Big O complexity of Mergesort?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text("Correct Answer on back:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    placeholder = { Text("e.g. O(n log n) in all cases.") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank() && answer.isNotBlank()) {
                        onConfirm(deckName.trim(), question.trim(), answer.trim())
                    }
                },
                enabled = question.isNotBlank() && answer.isNotBlank()
            ) {
                Text("Save Card", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// 8. POPUP DIALOG FOR ADDING ALARMS
// ==========================================
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var intervalMinutes by remember { mutableStateOf("1") }
    var message by remember { mutableStateOf("") }

    val presetIntervals = listOf(
        Pair("1 min", 1),
        Pair("10 mins", 10),
        Pair("1 hour", 60),
        Pair("1 day", 1440),
        Pair("3 days", 4320)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Study Reminder", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Subject / Concept to review:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = { Text("e.g. Spaced Repetition Chemistry") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Review interval:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetIntervals.forEach { (label, value) ->
                        val isSelected = intervalMinutes == value.toString()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { intervalMinutes = value.toString() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text("Message / Inspirational prompt:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Ex: Spend 10m reviewing organic structure formulas! 🧪 Keep coding.") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minutesVal = intervalMinutes.toIntOrNull() ?: 1
                    if (subject.isNotBlank()) {
                        onConfirm(subject.trim(), minutesVal, message.trim())
                    }
                },
                enabled = subject.isNotBlank()
            ) {
                Text("Schedule Alarm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
