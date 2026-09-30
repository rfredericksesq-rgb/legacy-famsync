package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.ai.AssistantAction

@Composable
fun ActionConfirmationDialog(
    action: AssistantAction,
    promptMessage: String?,
    onConfirm: (AssistantAction) -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Legacy AI Action",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Legacy Assistant Action",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = promptMessage ?: "Would you like me to confirm this action?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Details Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        when (action) {
                            is AssistantAction.CreateEvent -> {
                                Text(
                                    text = "Event: ${action.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("For: ${action.memberName}", style = MaterialTheme.typography.bodySmall)
                                Text("Date & Time: ${action.date} at ${action.startTime}", style = MaterialTheme.typography.bodySmall)
                                if (action.location.isNotBlank()) Text("Location: ${action.location}", style = MaterialTheme.typography.bodySmall)
                                if (action.recurrence != "None") Text("Recurrence: ${action.recurrence}", style = MaterialTheme.typography.bodySmall)
                            }
                            is AssistantAction.CreateAlarm -> {
                                Text(
                                    text = "Alarm: ${action.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("Time: ${action.time} (${action.daysOfWeek})", style = MaterialTheme.typography.bodySmall)
                            }
                            is AssistantAction.CreateTask -> {
                                Text(
                                    text = "Task: ${action.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("Assigned to: ${action.assignedMemberName}", style = MaterialTheme.typography.bodySmall)
                                Text("Due: ${action.dueDate} at ${action.dueTime}", style = MaterialTheme.typography.bodySmall)
                            }
                            is AssistantAction.CreateShoppingItem -> {
                                Text(
                                    text = "Item: ${action.name}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("Category: ${action.category}", style = MaterialTheme.typography.bodySmall)
                            }
                            is AssistantAction.CreateAnnouncement -> {
                                Text(
                                    text = "Announcement:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("\"${action.content}\"", style = MaterialTheme.typography.bodySmall)
                            }
                            is AssistantAction.CreateDiaryEntry -> {
                                Text(
                                    text = "Diary: ${action.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("\"${action.text}\"", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("action_cancel_button")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(action) },
                        modifier = Modifier.testTag("action_confirm_button")
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
