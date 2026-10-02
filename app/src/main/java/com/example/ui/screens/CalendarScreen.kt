package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.FamilyEventEntity
import com.example.data.model.FamilyMemberEntity
import com.example.data.model.GoogleCalendarImportItem

enum class CalendarViewMode {
    AGENDA,
    DAY,
    WEEK,
    MONTH
}

enum class CalendarSourceFilter {
    ALL,
    GOOGLE_CALENDAR,
    FAMSYNC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    events: List<FamilyEventEntity>,
    members: List<FamilyMemberEntity>,
    currentMember: FamilyMemberEntity? = null,
    userEmail: String? = null,
    isImportingCalendar: Boolean = false,
    calendarImportPreview: List<GoogleCalendarImportItem> = emptyList(),
    calendarImportStatus: String? = null,
    onLoadGoogleCalendarEvents: () -> Unit = {},
    onToggleImportItemSelection: (String) -> Unit = {},
    onSetAllImportItemsSelected: (Boolean) -> Unit = {},
    onImportGoogleCalendarEvents: (targetMemberId: Int, targetMemberName: String, overrideCategory: String?, onSuccess: (Int) -> Unit) -> Unit = { _, _, _, _ -> },
    onClearGoogleCalendarEvents: () -> Unit = {},
    onAddEvent: (
        title: String,
        memberName: String,
        date: String,
        startTime: String,
        endTime: String,
        location: String,
        category: String,
        priority: String,
        isPrivate: Boolean,
        reminderMinutes: Int,
        recurrence: String
    ) -> Unit,
    onUpdateEvent: (FamilyEventEntity) -> Unit = {},
    onDeleteEvent: (FamilyEventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.AGENDA) }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedMember by remember { mutableStateOf("All") }
    var sourceFilter by remember { mutableStateOf(CalendarSourceFilter.ALL) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<FamilyEventEntity?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Family", "Work", "School", "Health", "Sports", "Birthday")

    val googleEventCount = remember(events) { events.count { it.isGoogleCalendarImport } }

    val filteredEvents = remember(events, selectedCategory, selectedMember, sourceFilter) {
        events.filter { event ->
            val matchesCategory = selectedCategory == "All" || event.category.equals(selectedCategory, ignoreCase = true)
            val matchesMember = selectedMember == "All" || event.memberName.contains(selectedMember, ignoreCase = true) || event.memberName == "Family"
            val matchesSource = when (sourceFilter) {
                CalendarSourceFilter.ALL -> true
                CalendarSourceFilter.GOOGLE_CALENDAR -> event.isGoogleCalendarImport
                CalendarSourceFilter.FAMSYNC -> !event.isGoogleCalendarImport
            }
            matchesCategory && matchesMember && matchesSource
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Google Calendar Import FAB
                SmallFloatingActionButton(
                    onClick = {
                        onLoadGoogleCalendarEvents()
                        showImportDialog = true
                    },
                    containerColor = Color(0xFFE8F0FE),
                    contentColor = Color(0xFF1A73E8),
                    modifier = Modifier.testTag("import_gcal_fab")
                ) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Import Google Calendar")
                }

                // Add Custom Event FAB
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_event_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        },
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Google Calendar Integration Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0F4F9),
                border = BorderStroke(1.dp, Color(0xFFD2E3FC)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gcal_integration_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1A73E8),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Google Calendar",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Google Calendar",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1F1F1F)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFCEEAD6)
                                ) {
                                    Text(
                                        text = "SYNCED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D652D),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (googleEventCount > 0)
                                    "$googleEventCount Google event(s) unified in schedule"
                                else
                                    "Import events to see family & work in one place",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF444746),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                onLoadGoogleCalendarEvents()
                                showImportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A73E8),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("open_import_gcal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (googleEventCount > 0) "Sync" else "Import", fontSize = 12.sp)
                        }

                        if (googleEventCount > 0) {
                            IconButton(
                                onClick = { showClearConfirmDialog = true },
                                modifier = Modifier.size(32.dp).testTag("clear_gcal_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Imported Events",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. View Mode Selector (Day, Week, Month, Agenda)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CalendarViewMode.values().forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CalendarViewMode.values().size),
                        label = { Text(mode.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("calendar_view_${mode.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Source Filter (All / Google Calendar / FamSync)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = sourceFilter == CalendarSourceFilter.ALL,
                        onClick = { sourceFilter = CalendarSourceFilter.ALL },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AllInclusive, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = { Text("All Sources (${events.size})", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_source_all")
                    )
                }
                item {
                    FilterChip(
                        selected = sourceFilter == CalendarSourceFilter.GOOGLE_CALENDAR,
                        onClick = { sourceFilter = CalendarSourceFilter.GOOGLE_CALENDAR },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(14.dp))
                        },
                        label = { Text("Google Calendar ($googleEventCount)", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_source_gcal")
                    )
                }
                item {
                    FilterChip(
                        selected = sourceFilter == CalendarSourceFilter.FAMSYNC,
                        onClick = { sourceFilter = CalendarSourceFilter.FAMSYNC },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = { Text("FamSync Direct (${events.size - googleEventCount})", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_source_famsync")
                    )
                }
            }

            // 4. Member & Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedMember == "All",
                        onClick = { selectedMember = "All" },
                        label = { Text("All Family", fontSize = 11.sp) }
                    )
                }
                items(members) { m ->
                    FilterChip(
                        selected = selectedMember == m.name,
                        onClick = { selectedMember = m.name },
                        label = { Text("${m.avatarEmoji} ${m.name}", fontSize = 11.sp) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                items(categories.drop(1)) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) "All" else category },
                        label = { Text(category, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 5. Main Calendar Content Based on ViewMode
            when (viewMode) {
                CalendarViewMode.AGENDA -> {
                    AgendaView(
                        events = filteredEvents,
                        onEdit = { editingEvent = it },
                        onDelete = { onDeleteEvent(it) },
                        onOpenImport = {
                            onLoadGoogleCalendarEvents()
                            showImportDialog = true
                        }
                    )
                }
                CalendarViewMode.DAY -> {
                    DayScheduleView(
                        events = filteredEvents,
                        onEdit = { editingEvent = it }
                    )
                }
                CalendarViewMode.WEEK -> {
                    WeekScheduleView(
                        events = filteredEvents,
                        onEdit = { editingEvent = it }
                    )
                }
                CalendarViewMode.MONTH -> {
                    MonthScheduleView(
                        events = filteredEvents,
                        onEdit = { editingEvent = it }
                    )
                }
            }
        }
    }

    // Add Custom Event Dialog
    if (showAddDialog) {
        AddEventDialog(
            members = members,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, member, date, start, end, loc, cat, prio, priv, rem, rec ->
                onAddEvent(title, member, date, start, end, loc, cat, prio, priv, rem, rec)
                showAddDialog = false
            }
        )
    }

    // Edit Event Dialog
    editingEvent?.let { event ->
        EditEventDialog(
            event = event,
            members = members,
            onDismiss = { editingEvent = null },
            onSave = { updated ->
                onUpdateEvent(updated)
                editingEvent = null
            },
            onDelete = {
                onDeleteEvent(event)
                editingEvent = null
            }
        )
    }

    // Google Calendar Import Dialog
    if (showImportDialog) {
        GoogleCalendarImportDialog(
            userEmail = userEmail,
            members = members,
            currentMember = currentMember,
            isImporting = isImportingCalendar,
            previewItems = calendarImportPreview,
            statusMessage = calendarImportStatus,
            onToggleItem = onToggleImportItemSelection,
            onSetAllSelected = onSetAllImportItemsSelected,
            onReloadEvents = onLoadGoogleCalendarEvents,
            onConfirmImport = { targetMemberId, targetMemberName, overrideCat ->
                onImportGoogleCalendarEvents(targetMemberId, targetMemberName, overrideCat) { count ->
                    showImportDialog = false
                    snackbarMessage = "Imported $count event(s) from Google Calendar into family schedule!"
                }
            },
            onDismiss = { showImportDialog = false }
        )
    }

    // Confirm Clear Google Calendar Events Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Google Calendar Events?") },
            text = {
                Text("This will remove all $googleEventCount imported Google Calendar events from your FamSync family schedule. You can re-import or sync them anytime.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearGoogleCalendarEvents()
                        showClearConfirmDialog = false
                        snackbarMessage = "Cleared Google Calendar events from schedule."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Events")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AgendaView(
    events: List<FamilyEventEntity>,
    onEdit: (FamilyEventEntity) -> Unit,
    onDelete: (FamilyEventEntity) -> Unit,
    onOpenImport: () -> Unit
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(text = "📅", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No events in this view",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add family events or import directly from Google Calendar to unify your schedule.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenImport,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                    modifier = Modifier.testTag("empty_state_import_gcal")
                ) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Google Calendar")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(events) { event ->
                AgendaEventCard(
                    event = event,
                    onEdit = { onEdit(event) },
                    onDelete = { onDelete(event) }
                )
            }
        }
    }
}

@Composable
fun DayScheduleView(
    events: List<FamilyEventEntity>,
    onEdit: (FamilyEventEntity) -> Unit
) {
    val hours = (7..21).map { String.format("%02d:00", it) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hours) { hour ->
            val matchingEvents = events.filter {
                val eventHour = it.startTime.take(2)
                val targetHour = hour.take(2)
                eventHour == targetHour
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = hour,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(50.dp)
                )

                if (matchingEvents.isEmpty()) {
                    Divider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                } else {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingEvents.forEach { ev ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (ev.isGoogleCalendarImport) Color(0xFFE8F0FE) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                border = if (ev.isGoogleCalendarImport) BorderStroke(1.dp, Color(0xFF4285F4)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEdit(ev) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (ev.isGoogleCalendarImport) {
                                                Icon(
                                                    imageVector = Icons.Default.Cloud,
                                                    contentDescription = "Google",
                                                    tint = Color(0xFF1A73E8),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = ev.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (ev.isGoogleCalendarImport) Color(0xFF174EA6) else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Text(
                                            text = "${ev.startTime} - ${ev.endTime} • 👤 ${ev.memberName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (ev.isGoogleCalendarImport) Color(0xFFD2E3FC) else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = if (ev.isGoogleCalendarImport) "GCal" else ev.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (ev.isGoogleCalendarImport) Color(0xFF174EA6) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekScheduleView(
    events: List<FamilyEventEntity>,
    onEdit: (FamilyEventEntity) -> Unit
) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDayIndex by remember { mutableStateOf(2) } // Wednesday default

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            daysOfWeek.forEachIndexed { idx, day ->
                val isSelected = selectedDayIndex == idx
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clickable { selectedDayIndex = idx }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${28 + idx}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events) { event ->
                AgendaEventCard(
                    event = event,
                    onEdit = { onEdit(event) },
                    onDelete = {}
                )
            }
        }
    }
}

@Composable
fun MonthScheduleView(
    events: List<FamilyEventEntity>,
    onEdit: (FamilyEventEntity) -> Unit
) {
    var selectedDay by remember { mutableStateOf(30) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "September 2026",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        // Day of week headers
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Mini Grid Rows (Days 20 to 30)
        val calendarDays = (20..30).toList()
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(calendarDays) { day ->
                val isSelected = selectedDay == day
                val hasGoogle = events.any { it.isGoogleCalendarImport }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .size(46.dp)
                        .clickable { selectedDay = day }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            if (hasGoogle) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(Color(0xFF1A73E8), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Schedule for Sep $selectedDay, 2026",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events) { ev ->
                AgendaEventCard(
                    event = ev,
                    onEdit = { onEdit(ev) },
                    onDelete = {}
                )
            }
        }
    }
}

@Composable
fun AgendaEventCard(
    event: FamilyEventEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = if (event.isGoogleCalendarImport) BorderStroke(1.dp, Color(0xFFD2E3FC)) else null,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() }
            .testTag("event_item_${event.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (event.isGoogleCalendarImport) Color(0xFFE8F0FE) else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${event.startTime} - ${event.endTime}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (event.isGoogleCalendarImport) Color(0xFF174EA6) else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (event.isGoogleCalendarImport) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F0FE),
                            border = BorderStroke(0.5.dp, Color(0xFF4285F4))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = "Google Calendar",
                                    tint = Color(0xFF1A73E8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Google Calendar",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1A73E8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    text = event.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = event.category,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "👤 ${event.memberName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.location.isNotBlank()) {
                    Text(
                        text = " • 📍 ${event.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Event", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete Event", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleCalendarImportDialog(
    userEmail: String?,
    members: List<FamilyMemberEntity>,
    currentMember: FamilyMemberEntity?,
    isImporting: Boolean,
    previewItems: List<GoogleCalendarImportItem>,
    statusMessage: String?,
    onToggleItem: (String) -> Unit,
    onSetAllSelected: (Boolean) -> Unit,
    onReloadEvents: () -> Unit,
    onConfirmImport: (targetMemberId: Int, targetMemberName: String, overrideCategory: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        onReloadEvents()
    }

    var selectedMemberName by remember {
        mutableStateOf(currentMember?.name ?: members.firstOrNull()?.name ?: "Family Admin")
    }
    var overrideCategory by remember { mutableStateOf("Auto") }
    val categories = listOf("Auto", "Family", "Work", "School", "Health", "Sports")

    val selectedCount = remember(previewItems) { previewItems.count { it.isSelected } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1A73E8),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Import Google Calendar",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Unified Family Schedule Sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Account & Permission Status Card
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0F4F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF1A73E8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = userEmail?.takeIf { it.isNotBlank() } ?: "r.fredericks.esq@gmail.com",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE8F0FE)
                                ) {
                                    Text(
                                        text = "OAuth Connected",
                                        fontSize = 10.sp,
                                        color = Color(0xFF1A73E8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!hasPermission) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("grant_calendar_permission_btn"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Grant Device Calendar Access", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Target Family Member Assignment
                item {
                    Text(
                        text = "Assign Imported Events To:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(members) { m ->
                            FilterChip(
                                selected = selectedMemberName == m.name,
                                onClick = { selectedMemberName = m.name },
                                label = { Text("${m.avatarEmoji} ${m.name}", fontSize = 11.sp) },
                                modifier = Modifier.testTag("assign_member_${m.name.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // Category Mapping
                item {
                    Text(
                        text = "Category Assignment:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = overrideCategory == cat,
                                onClick = { overrideCategory = cat },
                                label = { Text(if (cat == "Auto") "Auto-detect (Smart)" else cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Select All / Deselect All Controls
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CALENDAR EVENTS ($selectedCount/${previewItems.size} selected)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row {
                            TextButton(
                                onClick = { onSetAllSelected(true) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("All", fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = { onSetAllSelected(false) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("None", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Status Message if any
                statusMessage?.let { msg ->
                    item {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // List of Preview Items
                if (previewItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            } else {
                                Text(
                                    text = "No events found in Google Calendar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(previewItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (item.isSelected) Color(0xFFF0F4F9) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (item.isSelected) Color(0xFF4285F4) else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleItem(item.id) }
                                .testTag("import_item_${item.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { onToggleItem(item.id) },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${item.date} • ${item.startTime} - ${item.endTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.location.isNotBlank()) {
                                        Text(
                                            text = "📍 ${item.location}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE8F0FE)
                                ) {
                                    Text(
                                        text = item.suggestedCategory,
                                        fontSize = 10.sp,
                                        color = Color(0xFF174EA6),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetMember = members.find { it.name == selectedMemberName }
                    val targetId = targetMember?.id ?: 1
                    onConfirmImport(targetId, selectedMemberName, if (overrideCategory == "Auto") null else overrideCategory)
                },
                enabled = selectedCount > 0 && !isImporting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                modifier = Modifier.testTag("confirm_gcal_import_btn")
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text("Import $selectedCount Event(s)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditEventDialog(
    event: FamilyEventEntity,
    members: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onSave: (FamilyEventEntity) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember(event) { mutableStateOf(event.title) }
    var selectedMember by remember(event) { mutableStateOf(event.memberName) }
    var startTime by remember(event) { mutableStateOf(event.startTime) }
    var endTime by remember(event) { mutableStateOf(event.endTime) }
    var date by remember(event) { mutableStateOf(event.date) }
    var location by remember(event) { mutableStateOf(event.location) }
    var category by remember(event) { mutableStateOf(event.category) }
    var priority by remember(event) { mutableStateOf(event.priority) }
    var isPrivate by remember(event) { mutableStateOf(event.isPrivate) }
    var recurrence by remember(event) { mutableStateOf(event.recurrence) }

    val categories = listOf("Family", "Work", "School", "Health", "Sports", "Birthday")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Edit Event")
                if (event.isGoogleCalendarImport) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE8F0FE)
                    ) {
                        Text(
                            text = "Google Calendar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A73E8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Assigned Member", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(members) { m ->
                            FilterChip(
                                selected = selectedMember == m.name,
                                onClick = { selectedMember = m.name },
                                label = { Text(m.name) }
                            )
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start (HH:mm)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End (HH:mm)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Category", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Event")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            event.copy(
                                title = title.trim(),
                                memberName = selectedMember,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                date = date.trim(),
                                location = location.trim(),
                                category = category,
                                priority = priority,
                                isPrivate = isPrivate,
                                recurrence = recurrence
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddEventDialog(
    members: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        memberName: String,
        date: String,
        startTime: String,
        endTime: String,
        location: String,
        category: String,
        priority: String,
        isPrivate: Boolean,
        reminderMinutes: Int,
        recurrence: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf(members.firstOrNull()?.name ?: "Family") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var date by remember { mutableStateOf("2026-10-01") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Family") }
    var priority by remember { mutableStateOf("Normal") }
    var isPrivate by remember { mutableStateOf(false) }
    var reminderMinutes by remember { mutableStateOf(15) }
    var recurrence by remember { mutableStateOf("None") }

    val categories = listOf("Family", "Work", "School", "Health", "Sports", "Birthday")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Calendar Event") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_title_input")
                    )
                }

                item {
                    Text("Family Member", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(members) { m ->
                            FilterChip(
                                selected = selectedMember == m.name,
                                onClick = { selectedMember = m.name },
                                label = { Text(m.name) }
                            )
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Category", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPrivate, onCheckedChange = { isPrivate = it })
                        Text("Private event (visible only to me)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedMember, date, startTime, endTime, location, category, priority, isPrivate, reminderMinutes, recurrence)
                    }
                },
                modifier = Modifier.testTag("submit_event_button")
            ) {
                Text("Add Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
