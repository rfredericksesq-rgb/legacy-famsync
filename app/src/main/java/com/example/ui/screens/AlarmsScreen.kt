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
    onUpdateAlarm: (FamilyAlarmEntity) -> Unit = {},
    onDeleteAlarm: (FamilyAlarmEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<FamilyAlarmEntity?>(null) }

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
            item {
                Column {
                    Text(
                        text = "SMART ALARMS & REMINDERS",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Synchronized family wake-ups and proactive departure reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (alarms.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "⏰", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No alarms configured",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Tap the + button to create a family wake-up alarm or departure reminder.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Smart Departure Reminders
            if (smartRecommendations.isNotEmpty()) {
                item {
                    Text(
                        text = "AI SMART DEPARTURE REMINDERS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(smartRecommendations) { alarm ->
                    AlarmItemRow(
                        alarm = alarm,
                        onToggle = { onToggleAlarm(alarm) },
                        onEdit = { editingAlarm = alarm },
                        onDelete = { onDeleteAlarm(alarm) }
                    )
                }
            }

            // Regular Alarms
            if (regularAlarms.isNotEmpty()) {
                item {
                    Text(
                        text = "FAMILY ALARMS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(regularAlarms) { alarm ->
                    AlarmItemRow(
                        alarm = alarm,
                        onToggle = { onToggleAlarm(alarm) },
                        onEdit = { editingAlarm = alarm },
                        onDelete = { onDeleteAlarm(alarm) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddAlarmDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, time, days, isRecurring ->
                    onAddAlarm(title, time, days, isRecurring)
                    showAddDialog = false
                }
            )
        }

        editingAlarm?.let { alarm ->
            EditAlarmDialog(
                alarm = alarm,
                onDismiss = { editingAlarm = null },
                onSave = { updated ->
                    onUpdateAlarm(updated)
                    editingAlarm = null
                },
                onDelete = {
                    onDeleteAlarm(alarm)
                    editingAlarm = null
                }
            )
        }
    }
}

@Composable
fun AlarmItemRow(
    alarm: FamilyAlarmEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onEdit() }
            .testTag("alarm_item_${alarm.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.time,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${alarm.daysOfWeek} • ${if (alarm.isRecurring) "Recurring" else "One-time"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Alarm", modifier = Modifier.size(16.dp))
            }

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("toggle_alarm_${alarm.id}")
            )
        }
    }
}

@Composable
fun EditAlarmDialog(
    alarm: FamilyAlarmEntity,
    onDismiss: () -> Unit,
    onSave: (FamilyAlarmEntity) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember(alarm) { mutableStateOf(alarm.title) }
    var time by remember(alarm) { mutableStateOf(alarm.time) }
    var days by remember(alarm) { mutableStateOf(alarm.daysOfWeek) }
    var isRecurring by remember(alarm) { mutableStateOf(alarm.isRecurring) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Alarm Label *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Days (e.g. Weekdays, Everyday)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                    Text("Recurring alarm")
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Alarm")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            alarm.copy(
                                title = title.trim(),
                                time = time.trim(),
                                daysOfWeek = days.trim(),
                                isRecurring = isRecurring
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
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, time: String, daysOfWeek: String, isRecurring: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("07:00") }
    var days by remember { mutableStateOf("Weekdays") }
    var isRecurring by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Family Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Alarm Label *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Days (e.g. Weekdays, Everyday)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                    Text("Recurring alarm")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, time, days, isRecurring)
                    }
                }
            ) {
                Text("Save Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
