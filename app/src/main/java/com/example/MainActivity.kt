package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.navigation.AppDestination
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FamilyViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FamilyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FamSyncRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FamSyncRoot(viewModel: FamilyViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    var bypassSignIn by remember { mutableStateOf(false) }

    if (currentUser == null && !bypassSignIn) {
        SignInScreen(
            onGoogleSignIn = { idToken ->
                viewModel.signInWithGoogle(idToken)
            },
            onContinueAsGuest = {
                bypassSignIn = true
            },
            isLoading = authLoading,
            errorMessage = authError
        )
    } else {
        MainAppScreen(viewModel = viewModel)
    }
}

@Composable
fun MainAppScreen(viewModel: FamilyViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.TODAY) }
    var isOnboardingActive by remember { mutableStateOf(false) }

    // Dialog & Sheet States
    var showMemberSwitcher by remember { mutableStateOf(false) }
    var showQuickActionSheet by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddAlarmDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }

    // State Collection
    val family by viewModel.family.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val currentMember by viewModel.currentMember.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val diaryEntries by viewModel.diaryEntries.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val importantDates by viewModel.importantDates.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val conflicts by viewModel.conflicts.collectAsStateWithLifecycle()

    val aiBriefing by viewModel.aiBriefing.collectAsStateWithLifecycle()
    val isEveningBriefing by viewModel.isEveningBriefing.collectAsStateWithLifecycle()
    val isGeneratingBriefing by viewModel.isGeneratingBriefing.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val pendingAction by viewModel.pendingAction.collectAsStateWithLifecycle()
    val pendingActionPrompt by viewModel.pendingActionPrompt.collectAsStateWithLifecycle()

    val isImportingCalendar by viewModel.isImportingCalendar.collectAsStateWithLifecycle()
    val calendarImportPreview by viewModel.calendarImportPreview.collectAsStateWithLifecycle()
    val calendarImportStatus by viewModel.calendarImportStatus.collectAsStateWithLifecycle()

    val unreadNotifications = remember(notifications) { notifications.count { !it.isRead } }

    // Handle Android system back gesture to return to Today tab
    if (currentDestination != AppDestination.TODAY && !isOnboardingActive) {
        BackHandler {
            currentDestination = AppDestination.TODAY
        }
    }

    if (isOnboardingActive) {
        OnboardingScreen(
            currentName = currentMember?.name ?: "",
            currentEmail = currentMember?.email ?: "",
            currentFamilyName = family?.name ?: "",
            onFinish = { userName, userEmail, familyName ->
                viewModel.setupUserFamily(userName, userEmail, familyName)
                isOnboardingActive = false
            }
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Wide Screen Navigation Rail (Tablets & Desktops)
            if (isWideScreen) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(84.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    AppDestination.values().filter { it != AppDestination.ONBOARDING }.forEach { dest ->
                        NavigationRailItem(
                            selected = currentDestination == dest,
                            onClick = { currentDestination = dest },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination == dest) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.title
                                )
                            },
                            label = { Text(dest.title) },
                            modifier = Modifier.testTag("rail_tab_${dest.route}")
                        )
                    }
                }
            }

            // Main Scaffold Content
            Scaffold(
                topBar = {
                    FamilyTopBar(
                        familyName = family?.name ?: "My Family",
                        currentMember = currentMember,
                        unreadNotificationsCount = unreadNotifications,
                        onOpenMemberSwitcher = { showMemberSwitcher = true },
                        onOpenNotifications = { currentDestination = AppDestination.NOTIFICATIONS },
                        onOpenAssistant = { currentDestination = AppDestination.ASSISTANT }
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            AppDestination.values().filter { it.showInBottomBar }.forEach { dest ->
                                NavigationBarItem(
                                    selected = currentDestination == dest,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentDestination == dest) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.title
                                        )
                                    },
                                    label = { Text(dest.title) },
                                    modifier = Modifier.testTag("bottom_tab_${dest.route}")
                                )
                            }

                            // More Actions Item
                            NavigationBarItem(
                                selected = currentDestination in listOf(
                                    AppDestination.ALARMS,
                                    AppDestination.DIARY,
                                    AppDestination.SHOPPING,
                                    AppDestination.SETTINGS
                                ),
                                onClick = { showQuickActionSheet = true },
                                icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                                label = { Text("More") },
                                modifier = Modifier.testTag("bottom_tab_more")
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        AppDestination.TODAY -> {
                            TodayScreen(
                                currentMember = currentMember,
                                members = members,
                                events = events,
                                tasks = tasks,
                                announcements = announcements,
                                conflicts = conflicts,
                                aiBriefing = aiBriefing,
                                isEveningBriefing = isEveningBriefing,
                                isGeneratingBriefing = isGeneratingBriefing,
                                onToggleBriefingMode = { viewModel.toggleBriefingMode() },
                                onRefreshBriefing = { viewModel.refreshAiBriefing() },
                                onToggleTask = { viewModel.toggleTask(it) },
                                onReactToAnnouncement = { ann, type -> viewModel.reactToAnnouncement(ann, type) },
                                onResolveConflict = { conf, chosen -> viewModel.resolveConflict(conf, chosen) },
                                onAddEventClick = { showAddEventDialog = true },
                                onSetAlarmClick = { showAddAlarmDialog = true },
                                onAddTaskClick = { showAddTaskDialog = true },
                                onAskLegacyClick = { currentDestination = AppDestination.ASSISTANT },
                                onOpenCalendar = { currentDestination = AppDestination.CALENDAR }
                            )
                        }
                        AppDestination.CALENDAR -> {
                            CalendarScreen(
                                events = events,
                                members = members,
                                currentMember = currentMember,
                                userEmail = viewModel.currentUser.value?.email,
                                isImportingCalendar = isImportingCalendar,
                                calendarImportPreview = calendarImportPreview,
                                calendarImportStatus = calendarImportStatus,
                                onLoadGoogleCalendarEvents = { viewModel.loadGoogleCalendarEventsForImport() },
                                onToggleImportItemSelection = { viewModel.toggleImportItemSelection(it) },
                                onSetAllImportItemsSelected = { viewModel.setAllImportItemsSelected(it) },
                                onImportGoogleCalendarEvents = { targetMemberId, targetMemberName, overrideCat, onSuccess ->
                                    viewModel.importGoogleCalendarEvents(targetMemberId, targetMemberName, overrideCat, onSuccess)
                                },
                                onClearGoogleCalendarEvents = { viewModel.clearGoogleCalendarEvents() },
                                onAddEvent = { title, member, date, start, end, loc, cat, prio, priv, rem, rec ->
                                    viewModel.addEvent(title, member, date, start, end, loc, cat, prio, priv, rem, rec)
                                },
                                onUpdateEvent = { viewModel.updateEvent(it) },
                                onDeleteEvent = { viewModel.deleteEvent(it) }
                            )
                        }
                        AppDestination.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                members = members,
                                currentMember = currentMember,
                                onToggleTask = { viewModel.toggleTask(it) },
                                onAddTask = { title, assigned, date, time, prio, cat, rec ->
                                    viewModel.addTask(title, assigned, date, time, prio, cat, rec)
                                },
                                onDeleteTask = { viewModel.deleteTask(it) }
                            )
                        }
                        AppDestination.ALARMS -> {
                            AlarmsScreen(
                                alarms = alarms,
                                onToggleAlarm = { viewModel.toggleAlarm(it) },
                                onAddAlarm = { title, time, days, rec ->
                                    viewModel.addAlarm(title, time, days, rec)
                                },
                                onDeleteAlarm = { viewModel.deleteAlarm(it) }
                            )
                        }
                        AppDestination.DIARY -> {
                            DiaryScreen(
                                entries = diaryEntries,
                                onAddEntry = { title, text, milestone, priv ->
                                    viewModel.addDiaryEntry(title, text, milestone, priv)
                                },
                                onLikeEntry = { viewModel.likeDiaryEntry(it) },
                                onDeleteEntry = { viewModel.deleteDiaryEntry(it) }
                            )
                        }
                        AppDestination.SHOPPING -> {
                            ShoppingScreen(
                                items = shoppingItems,
                                onToggleItem = { viewModel.toggleShoppingItem(it) },
                                onAddItem = { name, cat, qty -> viewModel.addShoppingItem(name, cat, qty) },
                                onClearPurchased = { viewModel.clearPurchasedShopping() }
                            )
                        }
                        AppDestination.FAMILY -> {
                            FamilyScreen(
                                family = family,
                                members = members,
                                announcements = announcements,
                                importantDates = importantDates,
                                onSelectMember = {
                                    viewModel.switchActiveMember(it)
                                },
                                onAddMember = { name, email, role, age, emoji, color ->
                                    viewModel.addMember(name, email, role, age, emoji, color)
                                },
                                onUpdateMember = { member ->
                                    viewModel.updateMember(member)
                                },
                                onDeleteMember = { member ->
                                    viewModel.deleteMember(member)
                                },
                                onUpdateFamily = { name, code ->
                                    viewModel.updateFamily(name, code)
                                },
                                onAddAnnouncement = { viewModel.addAnnouncement(it) },
                                onReactToAnnouncement = { ann, type -> viewModel.reactToAnnouncement(ann, type) }
                            )
                        }
                        AppDestination.ASSISTANT -> {
                            AssistantScreen(
                                messages = chatMessages,
                                onSendMessage = { viewModel.sendMessageToAssistant(it) },
                                onConfirmAction = { viewModel.confirmPendingAction() },
                                onCancelAction = { viewModel.cancelPendingAction() }
                            )
                        }
                        AppDestination.NOTIFICATIONS -> {
                            NotificationsScreen(
                                notifications = notifications,
                                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                                onClearAll = { viewModel.clearAllNotifications() }
                            )
                        }
                        AppDestination.SETTINGS -> {
                            SettingsScreen(
                                family = family,
                                currentMember = currentMember,
                                userEmail = viewModel.currentUser.value?.email,
                                onUpdateFamily = { name, code ->
                                    viewModel.updateFamily(name, code)
                                },
                                onUpdateCurrentMember = { name, email, role, age, emoji ->
                                    viewModel.updateCurrentMember(name, email, role, age, emoji)
                                },
                                onSignOut = { viewModel.signOut() },
                                onOpenOnboarding = { isOnboardingActive = true }
                            )
                        }
                        AppDestination.ONBOARDING -> {
                            OnboardingScreen(
                                currentName = currentMember?.name ?: "",
                                currentEmail = currentMember?.email ?: "",
                                currentFamilyName = family?.name ?: "",
                                onFinish = { name, email, fam ->
                                    viewModel.setupUserFamily(name, email, fam)
                                    currentDestination = AppDestination.TODAY
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheets & Dialogs
    if (showMemberSwitcher) {
        MemberSwitcherSheet(
            members = members,
            currentMember = currentMember,
            onSelectMember = {
                viewModel.switchActiveMember(it)
                showMemberSwitcher = false
            },
            onAddNewMember = {
                currentDestination = AppDestination.FAMILY
                showMemberSwitcher = false
            },
            onDismiss = { showMemberSwitcher = false }
        )
    }

    if (showQuickActionSheet) {
        QuickActionSheet(
            onAddEvent = { showAddEventDialog = true },
            onSetAlarm = { showAddAlarmDialog = true },
            onAddTask = { showAddTaskDialog = true },
            onAddAnnouncement = { showAddAnnouncementDialog = true },
            onDismiss = { showQuickActionSheet = false }
        )
    }

    if (showAddEventDialog) {
        AddEventDialog(
            members = members,
            onDismiss = { showAddEventDialog = false },
            onConfirm = { title, member, date, start, end, loc, cat, prio, priv, rem, rec ->
                viewModel.addEvent(title, member, date, start, end, loc, cat, prio, priv, rem, rec)
                showAddEventDialog = false
            }
        )
    }

    if (showAddAlarmDialog) {
        AddAlarmDialog(
            onDismiss = { showAddAlarmDialog = false },
            onConfirm = { title, time, days, rec ->
                viewModel.addAlarm(title, time, days, rec)
                showAddAlarmDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            members = members,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, assigned, date, time, prio, cat, rec ->
                viewModel.addTask(title, assigned, date, time, prio, cat, rec)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddAnnouncementDialog) {
        AddAnnouncementDialog(
            onDismiss = { showAddAnnouncementDialog = false },
            onConfirm = { text ->
                viewModel.addAnnouncement(text)
                showAddAnnouncementDialog = false
            }
        )
    }

    // Structured Action Confirmation Modal
    pendingAction?.let { action ->
        ActionConfirmationDialog(
            action = action,
            promptMessage = pendingActionPrompt,
            onConfirm = { viewModel.confirmPendingAction() },
            onCancel = { viewModel.cancelPendingAction() }
        )
    }
}

@Composable
fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var content by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broadcast Family Announcement") },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Announcement message") },
                modifier = Modifier.fillMaxWidth().testTag("announcement_input"),
                minLines = 3
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(content.trim())
                    }
                },
                modifier = Modifier.testTag("broadcast_announcement_btn")
            ) {
                Text("Broadcast")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
