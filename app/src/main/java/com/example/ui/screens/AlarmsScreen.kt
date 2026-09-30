package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.FamilyAlarmEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    alarms: List<FamilyAlarmEntity>,
    onToggleAlarm: (FamilyAlarmEntity) -> Unit,
    onAddAlarm: (title: String, time: String, daysOfWeek: String, isRecurring: Boolean) -> Unit,
    onDeleteAlarm: (FamilyAlarmEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val smartRecommendations = remember(alarms) { alarms.filter { it.isSmartRecommendation } }
    val regularAlarms = remember(alarms) { alarms.filter { !it.isSmartRecommendation } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_alarm_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Alarm")
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "ALARMS & REMINDERS",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Smart family wake-ups, departure alerts and synchronized reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Smart AI Recommendations Card
            if (smartRecommendations.isNotEmpty()) {
                item {
                    Text(
                        text = "SMART AI RECOMMENDATIONS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(smartRecommendations) { rec ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Upcoming School Trip Suggested Reminder",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "You have a school trip tomorrow. Remind the family tonight at ${rec.time} to prepare bags?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onDeleteAlarm(rec) }
                                ) {
                                    Text("Dismiss", color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        onToggleAlarm(rec.copy(isEnabled = true, isSmartRecommendation = false))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Enable Reminder")
                                }
                            }
                        }
                    }
                }
            }

            // Regular Alarms Header
            item {
                Text(
                    text = "ACTIVE ALARMS & REMINDERS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(regularAlarms) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { onToggleAlarm(alarm) },
                    onDelete = { onDeleteAlarm(alarm) }
                )
            }
        }

        if (showAddDialog) {
            AddAlarmDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, time, days, rec ->
                    onAddAlarm(title, time, days, rec)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AlarmCard(
    alarm: FamilyAlarmEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_item_${alarm.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.time,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                Text(
                    text = "${alarm.daysOfWeek} • ${if (alarm.isRecurring) "Repeating" else "Once"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete")
            }

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("switch_alarm_${alarm.id}")
            )
        }
    }
}

@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, time: String, daysOfWeek: String, isRecurring: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("07:00") }
    var daysOfWeek by remember { mutableStateOf("Weekdays") }
    var isRecurring by remember { mutableStateOf(true) }

    val daysOptions = listOf("Weekdays", "Everyday", "Weekends", "Today Only")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Alarm or Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Label (e.g. School Wake-up)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Repeat", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    daysOptions.forEach { d ->
                        FilterChip(
                            selected = daysOfWeek == d,
                            onClick = {
                                daysOfWeek = d
                                isRecurring = d != "Today Only"
                            },
                            label = { Text(d) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, time, daysOfWeek, isRecurring)
                    }
                }
            ) {
                Text("Save Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
