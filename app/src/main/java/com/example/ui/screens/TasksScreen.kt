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
import com.example.data.model.FamilyMemberEntity
import com.example.data.model.FamilyTaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<FamilyTaskEntity>,
    members: List<FamilyMemberEntity>,
    currentMember: FamilyMemberEntity?,
    onToggleTask: (FamilyTaskEntity) -> Unit,
    onAddTask: (
        title: String,
        assignedMemberName: String,
        dueDate: String,
        dueTime: String,
        priority: String,
        category: String,
        recurrence: String
    ) -> Unit,
    onDeleteTask: (FamilyTaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOnlyMine by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val choreTemplates = listOf(
        "Take rubbish out",
        "Clean bedroom",
        "Wash dishes",
        "Feed pets",
        "Grocery shopping",
        "Laundry"
    )

    val filteredTasks = remember(tasks, showOnlyMine, selectedCategory, currentMember) {
        tasks.filter { task ->
            (!showOnlyMine || task.assignedMemberName.equals(currentMember?.name, ignoreCase = true)) &&
            (selectedCategory == "All" || task.category.equals(selectedCategory, ignoreCase = true))
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
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
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showOnlyMine,
                    onClick = { showOnlyMine = false },
                    label = { Text("All Family Tasks") },
                    modifier = Modifier.testTag("filter_all_tasks")
                )
                FilterChip(
                    selected = showOnlyMine,
                    onClick = { showOnlyMine = true },
                    label = { Text("My Tasks (${currentMember?.name ?: ""})") },
                    modifier = Modifier.testTag("filter_my_tasks")
                )
            }

            // Quick Chores Preset Chips
            Text(
                text = "QUICK CHORE TEMPLATES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(choreTemplates) { chore ->
                    SuggestionChip(
                        onClick = {
                            onAddTask(
                                chore,
                                currentMember?.name ?: "Daniel",
                                "2026-09-30",
                                "19:00",
                                "Normal",
                                "Chores",
                                "Weekly"
                            )
                        },
                        label = { Text(chore) }
                    )
                }
            }

            // Category Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                val cats = listOf("All", "Chores", "School", "Work", "Errands")
                items(cats) { c ->
                    FilterChip(
                        selected = selectedCategory == c,
                        onClick = { selectedCategory = c },
                        label = { Text(c) }
                    )
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tasks found in this view. Great job!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredTasks) { task ->
                        TaskCardItem(
                            task = task,
                            onToggle = { onToggleTask(task) },
                            onDelete = { onDeleteTask(task) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                members = members,
                onDismiss = { showAddDialog = false },
                onConfirm = { title, assigned, date, time, prio, cat, rec ->
                    onAddTask(title, assigned, date, time, prio, cat, rec)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TaskCardItem(
    task: FamilyTaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_row_${task.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold
                    ),
                    color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "👤 ${task.assignedMemberName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "⏰ ${task.dueTime} (${task.dueDate})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    members: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        assignedMemberName: String,
        dueDate: String,
        dueTime: String,
        priority: String,
        category: String,
        recurrence: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var assigned by remember { mutableStateOf(members.firstOrNull()?.name ?: "Daniel") }
    var dueDate by remember { mutableStateOf("2026-09-30") }
    var dueTime by remember { mutableStateOf("19:00") }
    var priority by remember { mutableStateOf("Normal") }
    var category by remember { mutableStateOf("Chores") }
    var recurrence by remember { mutableStateOf("None") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Family Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task / Chore Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Text("Assign to", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(members) { m ->
                        FilterChip(
                            selected = assigned == m.name,
                            onClick = { assigned = m.name },
                            label = { Text(m.name) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Due Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, assigned, dueDate, dueTime, priority, category, recurrence)
                    }
                },
                modifier = Modifier.testTag("submit_task_button")
            ) {
                Text("Assign Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
