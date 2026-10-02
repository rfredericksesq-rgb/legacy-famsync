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
    onUpdateTask: (FamilyTaskEntity) -> Unit = {},
    onDeleteTask: (FamilyTaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOnlyMine by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<FamilyTaskEntity?>(null) }

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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tabs (All Tasks vs My Tasks)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showOnlyMine,
                    onClick = { showOnlyMine = false },
                    label = { Text("All Family Tasks (${tasks.size})") },
                    modifier = Modifier.testTag("tab_all_tasks")
                )
                FilterChip(
                    selected = showOnlyMine,
                    onClick = { showOnlyMine = true },
                    label = { Text("My Tasks (${tasks.count { it.assignedMemberName.equals(currentMember?.name, ignoreCase = true) }})") },
                    modifier = Modifier.testTag("tab_my_tasks")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Chore Templates Carousel
            Text(
                text = "QUICK CHORE TEMPLATES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(choreTemplates) { choreName ->
                    SuggestionChip(
                        onClick = {
                            val targetMember = currentMember?.name ?: members.firstOrNull()?.name ?: "Family"
                            onAddTask(
                                choreName,
                                targetMember,
                                "2026-09-30",
                                "19:00",
                                "Normal",
                                "Chores",
                                "None"
                            )
                        },
                        label = { Text(choreName) },
                        icon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            val categories = listOf("All", "Chores", "School", "Work", "Errands")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
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
                            onEdit = { editingTask = task },
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

        editingTask?.let { task ->
            EditTaskDialog(
                task = task,
                members = members,
                onDismiss = { editingTask = null },
                onSave = { updated ->
                    onUpdateTask(updated)
                    editingTask = null
                },
                onDelete = {
                    onDeleteTask(task)
                    editingTask = null
                }
            )
        }
    }
}

@Composable
fun TaskCardItem(
    task: FamilyTaskEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() }
            .testTag("task_row_${task.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "👤 ${task.assignedMemberName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ⏰ ${task.dueTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (task.priority) {
                            "High" -> MaterialTheme.colorScheme.errorContainer
                            "Low" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Task", modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Task",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EditTaskDialog(
    task: FamilyTaskEntity,
    members: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onSave: (FamilyTaskEntity) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember(task) { mutableStateOf(task.title) }
    var assignedMember by remember(task) { mutableStateOf(task.assignedMemberName) }
    var dueDate by remember(task) { mutableStateOf(task.dueDate) }
    var dueTime by remember(task) { mutableStateOf(task.dueTime) }
    var priority by remember(task) { mutableStateOf(task.priority) }
    var category by remember(task) { mutableStateOf(task.category) }

    val categories = listOf("Chores", "School", "Work", "Errands")
    val priorities = listOf("Low", "Normal", "High")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Assigned Family Member", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(members) { m ->
                        FilterChip(
                            selected = assignedMember == m.name,
                            onClick = { assignedMember = m.name },
                            label = { Text(m.name) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Due Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

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

                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Task")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            task.copy(
                                title = title.trim(),
                                assignedMemberName = assignedMember,
                                dueDate = dueDate.trim(),
                                dueTime = dueTime.trim(),
                                priority = priority,
                                category = category
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
    var assignedMember by remember { mutableStateOf(members.firstOrNull()?.name ?: "Family") }
    var dueDate by remember { mutableStateOf("2026-09-30") }
    var dueTime by remember { mutableStateOf("19:00") }
    var priority by remember { mutableStateOf("Normal") }
    var category by remember { mutableStateOf("Chores") }
    var recurrence by remember { mutableStateOf("None") }

    val categories = listOf("Chores", "School", "Work", "Errands")
    val priorities = listOf("Low", "Normal", "High")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Family Task") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                Text("Assign To", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(members) { m ->
                        FilterChip(
                            selected = assignedMember == m.name,
                            onClick = { assignedMember = m.name },
                            label = { Text(m.name) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Due Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

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

                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, assignedMember, dueDate, dueTime, priority, category, recurrence)
                    }
                },
                modifier = Modifier.testTag("submit_task_button")
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
