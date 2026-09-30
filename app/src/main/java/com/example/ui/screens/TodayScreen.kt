package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.ScheduleConflict
import com.example.ui.theme.*

@Composable
fun TodayScreen(
    currentMember: FamilyMemberEntity?,
    members: List<FamilyMemberEntity>,
    events: List<FamilyEventEntity>,
    tasks: List<FamilyTaskEntity>,
    announcements: List<AnnouncementEntity>,
    conflicts: List<ScheduleConflict>,
    aiBriefing: String,
    isEveningBriefing: Boolean,
    isGeneratingBriefing: Boolean,
    onToggleBriefingMode: () -> Unit,
    onRefreshBriefing: () -> Unit,
    onToggleTask: (FamilyTaskEntity) -> Unit,
    onReactToAnnouncement: (AnnouncementEntity, String) -> Unit,
    onResolveConflict: (ScheduleConflict, String) -> Unit,
    onAddEventClick: () -> Unit,
    onSetAlarmClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onAskLegacyClick: () -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = "2026-09-30"
    val todayEvents = remember(events) { events.filter { it.date == today } }
    val todayTasks = remember(tasks) { tasks.filter { it.dueDate == today } }
    val latestAnnouncement = announcements.firstOrNull()

    // Determine next event
    val nextEvent = todayEvents.firstOrNull { it.startTime >= "14:00" } ?: todayEvents.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Greeting
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = if (isEveningBriefing) "Good evening, ${currentMember?.name ?: "Sarah"} 🌙"
                    else "Good morning, ${currentMember?.name ?: "Sarah"} 👋",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Here's your family's day • Wednesday, Sep 30",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. AI Daily Briefing Card ("LEGACY SAYS")
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("ai_briefing_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LEGACY SAYS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = onToggleBriefingMode,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("briefing_mode_toggle")
                            ) {
                                Text(
                                    text = if (isEveningBriefing) "Morning" else "Evening",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            IconButton(
                                onClick = onRefreshBriefing,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("refresh_briefing_button")
                            ) {
                                if (isGeneratingBriefing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Briefing",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "“$aiBriefing”",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 3. Smart Schedule Conflict Banner (if any conflict)
        if (conflicts.isNotEmpty()) {
            item {
                val conflict = conflicts.first()
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEF2F2),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_conflict_banner")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Conflict",
                                tint = Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Schedule Conflict Detected",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF991B1B)
                            )
                        }

                        Text(
                            text = conflict.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF7F1D1D),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = "Assign coverage:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF991B1B)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Button(
                                onClick = { onResolveConflict(conflict, "Mark (Dad)") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("resolve_assign_dad")
                            ) {
                                Text("Assign Dad", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onResolveConflict(conflict, "Sarah (Mom)") },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("resolve_assign_mom")
                            ) {
                                Text("Assign Mom", fontSize = 12.sp, color = Color(0xFF991B1B))
                            }
                            TextButton(
                                onClick = { /* leave unresolved */ },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Unresolved", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // 4. Next Event & Departure Reminder
        if (nextEvent != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_event_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "UP NEXT AT ${nextEvent.startTime}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = nextEvent.memberName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = nextEvent.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (nextEvent.location.isNotBlank()) {
                                Text(
                                    text = "📍 ${nextEvent.location} • Leave in ~20m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Quick Actions Row
        item {
            Column {
                Text(
                    text = "QUICK ACTIONS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Add,
                        label = "Add Event",
                        testTag = "quick_action_event",
                        modifier = Modifier.weight(1f),
                        onClick = onAddEventClick
                    )
                    QuickActionButton(
                        icon = Icons.Default.Alarm,
                        label = "Set Alarm",
                        testTag = "quick_action_alarm",
                        modifier = Modifier.weight(1f),
                        onClick = onSetAlarmClick
                    )
                    QuickActionButton(
                        icon = Icons.Default.Check,
                        label = "Add Task",
                        testTag = "quick_action_task",
                        modifier = Modifier.weight(1f),
                        onClick = onAddTaskClick
                    )
                    QuickActionButton(
                        icon = Icons.Default.AutoAwesome,
                        label = "Ask Legacy",
                        testTag = "quick_action_assistant",
                        modifier = Modifier.weight(1f),
                        onClick = onAskLegacyClick
                    )
                }
            }
        }

        // 6. Today's Overview Timeline
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "TODAY'S OVERVIEW",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onOpenCalendar,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("View Full Calendar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (todayEvents.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No more activities scheduled for today.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(todayEvents) { event ->
                TodayTimelineCard(event = event)
            }
        }

        // 7. Family Members Activity Overview
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "FAMILY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        members.forEach { member ->
                            val memberEventCount = todayEvents.count { it.memberName.contains(member.name, ignoreCase = true) || it.memberName == "Family" }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = member.avatarEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$memberEventCount events",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8. Pending Tasks Overview
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TODAY'S TASKS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${todayTasks.count { it.isCompleted }}/${todayTasks.size} done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                todayTasks.take(4).forEach { task ->
                    TodayTaskRow(task = task, onToggle = { onToggleTask(task) })
                }
            }
        }

        // 9. Latest Family Announcement
        if (latestAnnouncement != null) {
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "FAMILY ANNOUNCEMENT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📢 ${latestAnnouncement.authorName}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = latestAnnouncement.timestamp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = latestAnnouncement.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Reactions
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ReactionButton(
                                    emoji = "❤️",
                                    count = latestAnnouncement.heartCount,
                                    onClick = { onReactToAnnouncement(latestAnnouncement, "heart") }
                                )
                                ReactionButton(
                                    emoji = "👍",
                                    count = latestAnnouncement.thumbsUpCount,
                                    onClick = { onReactToAnnouncement(latestAnnouncement, "thumbsUp") }
                                )
                                ReactionButton(
                                    emoji = "✅",
                                    count = latestAnnouncement.checkCount,
                                    onClick = { onReactToAnnouncement(latestAnnouncement, "check") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayTimelineCard(event: FamilyEventEntity) {
    val categoryColor = when (event.category) {
        "School" -> Emerald600
        "Work" -> Indigo600
        "Sports" -> Amber600
        "Health" -> Rose600
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_item_${event.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(54.dp)
            ) {
                Text(
                    text = event.startTime,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = event.endTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Category Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = event.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 3.dp)
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (event.reminderMinutes > 0) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Reminder active",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TodayTaskRow(
    task: FamilyTaskEntity,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("task_item_${task.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                    ),
                    color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "For ${task.assignedMemberName} • Due ${task.dueTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReactionButton(
    emoji: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
