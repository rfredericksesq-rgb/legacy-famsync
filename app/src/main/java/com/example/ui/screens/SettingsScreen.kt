package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyEntity

@Composable
fun SettingsScreen(
    family: FamilyEntity?,
    userEmail: String? = null,
    onSignOut: () -> Unit = {},
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var morningBriefingEnabled by remember { mutableStateOf(true) }
    var eveningSummaryEnabled by remember { mutableStateOf(true) }
    var smartRemindersEnabled by remember { mutableStateOf(true) }
    var departureAlertsEnabled by remember { mutableStateOf(true) }
    var childFilterEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "SETTINGS & FAMILY ADMIN",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage subscriptions, AI preferences, and family security.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Subscription Tier Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscription_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Current Plan: FAMILY TIER",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Active • Unlimited members & AI Assistant",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Includes: Shared Family Calendar, Smart Alarms & Departure Reminders, Family Diary, Live Realtime Sync, and Legacy AI Assistant.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // AI Assistant Preferences
        item {
            Text(
                text = "AI PREFERENCES",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSwitchRow(
                        title = "Daily Morning Briefing",
                        subtitle = "Personalized schedule summary delivered every morning",
                        checked = morningBriefingEnabled,
                        onCheckedChange = { morningBriefingEnabled = it }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    SettingsSwitchRow(
                        title = "Evening Reflection & Summary",
                        subtitle = "Recap of completed tasks and preview of tomorrow",
                        checked = eveningSummaryEnabled,
                        onCheckedChange = { eveningSummaryEnabled = it }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    SettingsSwitchRow(
                        title = "Smart Conflict & Bag Reminders",
                        subtitle = "Proactive AI suggestions for school trips and schedule conflicts",
                        checked = smartRemindersEnabled,
                        onCheckedChange = { smartRemindersEnabled = it }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    SettingsSwitchRow(
                        title = "Travel & Leave-Time Alerts",
                        subtitle = "Calculate suggested departure time based on travel",
                        checked = departureAlertsEnabled,
                        onCheckedChange = { departureAlertsEnabled = it }
                    )
                }
            }
        }

        // Privacy & Security
        item {
            Text(
                text = "PRIVACY & SECURITY",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSwitchRow(
                        title = "Child Profile Privacy Protection",
                        subtitle = "Hide private adult medical appointments from children's view",
                        checked = childFilterEnabled,
                        onCheckedChange = { childFilterEnabled = it }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Database Encryption & Sync", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("AES-256 local & transport encryption active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Google Account & Cloud Sync
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GOOGLE ACCOUNT & CLOUD SYNC",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userEmail ?: "Google Account Connected",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Firebase Firestore Real-time Sync Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth().testTag("sign_out_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Sign Out")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out of Google Account")
                    }
                }
            }
        }

        // Walkthrough Tour
        item {
            OutlinedButton(
                onClick = onOpenOnboarding,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("open_tour_button")
            ) {
                Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Revisit Welcome & Onboarding Tour")
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
