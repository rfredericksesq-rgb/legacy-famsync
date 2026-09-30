package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyEventEntity
import com.example.data.model.FamilyMemberEntity
import com.example.ui.theme.*

enum class CalendarViewMode {
    AGENDA,
    DAY,
    WEEK,
    MONTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    events: List<FamilyEventEntity>,
    members: List<FamilyMemberEntity>,
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
    onDeleteEvent: (FamilyEventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.AGENDA) }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedMember by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Family", "Work", "School", "Health", "Sports", "Birthday")

    val filteredEvents = remember(events, selectedCategory, selectedMember) {
        events.filter { event ->
            (selectedCategory == "All" || event.category.equals(selectedCategory, ignoreCase = true)) &&
            (selectedMember == "All" || event.memberName.contains(selectedMember, ignoreCase = true) || event.memberName == "Family")
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_event_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // View Mode Switcher
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                CalendarViewMode.values().forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CalendarViewMode.values().size)
                    ) {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("filter_cat_$cat")
                    )
                }
            }

            // Member Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedMember == "All",
                        onClick = { selectedMember = "All" },
                        label = { Text("All Family") }
                    )
                }
                items(members) { member ->
                    FilterChip(
                        selected = selectedMember == member.name,
                        onClick = { selectedMember = member.name },
                        label = { Text("${member.avatarEmoji} ${member.name}") }
                    )
                }
            }

            // Event List
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No events found for this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Group by date
                    val grouped = filteredEvents.groupBy { it.date }
                    grouped.forEach { (date, eventGroup) ->
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (date == "2026-09-30") "Today • September 30, 2026"
                                    else if (date == "2026-10-01") "Tomorrow • October 1, 2026"
                                    else "Date: $date",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        items(eventGroup) { event ->
                            CalendarEventItem(event = event, onDelete = { onDeleteEvent(event) })
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddEventDialog(
                members = members,
                onDismiss = { showAddDialog = false },
                onConfirm = { title, memberName, date, start, end, loc, cat, prio, priv, rem, rec ->
                    onAddEvent(title, memberName, date, start, end, loc, cat, prio, priv, rem, rec)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun CalendarEventItem(
    event: FamilyEventEntity,
    onDelete: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { showDetails = !showDetails }
            .testTag("calendar_event_${event.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Time
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = "${event.startTime} - ${event.endTime}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

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
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (event.recurrence != "None") {
                    Text(
                        text = "🔄 ${event.recurrence}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showDetails) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority: ${event.priority} • Reminder: ${event.reminderMinutes} min prior",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Event",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var date by remember { mutableStateOf("2026-09-30") }
    var startTime by remember { mutableStateOf("15:00") }
    var endTime by remember { mutableStateOf("16:00") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Family") }
    var recurrence by remember { mutableStateOf("None") }
    var priority by remember { mutableStateOf("Normal") }
    var reminderMinutes by remember { mutableStateOf(15) }
    var isPrivate by remember { mutableStateOf(false) }

    val categories = listOf("Family", "Work", "School", "Health", "Sports", "Birthday")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Event") },
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
