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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.example.data.model.AgeCategory
import com.example.data.model.FamilyEntity
import com.example.data.model.FamilyMemberEntity
import com.example.data.model.MemberRole

@Composable
fun SettingsScreen(
    family: FamilyEntity?,
    currentMember: FamilyMemberEntity? = null,
    userEmail: String? = null,
    onUpdateFamily: (name: String, inviteCode: String) -> Unit = { _, _ -> },
    onUpdateCurrentMember: (name: String, email: String, role: MemberRole, age: AgeCategory, emoji: String) -> Unit = { _, _, _, _, _ -> },
    onSignOut: () -> Unit = {},
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var morningBriefingEnabled by remember { mutableStateOf(true) }
    var eveningSummaryEnabled by remember { mutableStateOf(true) }
    var smartRemindersEnabled by remember { mutableStateOf(true) }
    var departureAlertsEnabled by remember { mutableStateOf(true) }
    var childFilterEnabled by remember { mutableStateOf(true) }

    var showEditFamilyDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

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

        // Family & Profile Editor Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("family_profile_editor_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "FAMILY & PROFILE DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Family Name Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Family Hub",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = family?.name ?: "My Family",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Invite Code: ${family?.inviteCode ?: "LEGACY-SYNC"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { showEditFamilyDialog = true },
                            modifier = Modifier.testTag("settings_edit_family_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Active Member Profile Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentMember?.avatarEmoji ?: "👑", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Your Profile",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currentMember?.name ?: "Family Admin",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentMember?.role?.name ?: "ADMIN"} • ${currentMember?.email?.ifBlank { userEmail ?: "No email set" } ?: "No email"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("settings_edit_profile_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile")
                        }
                    }
                }
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

    // Edit Family Dialog
    if (showEditFamilyDialog) {
        var familyNameInput by remember { mutableStateOf(family?.name ?: "My Family") }
        var inviteCodeInput by remember { mutableStateOf(family?.inviteCode ?: "LEGACY-SYNC") }

        AlertDialog(
            onDismissRequest = { showEditFamilyDialog = false },
            title = { Text("Edit Family Hub Name") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = familyNameInput,
                        onValueChange = { familyNameInput = it },
                        label = { Text("Family Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_family_name_field")
                    )
                    OutlinedTextField(
                        value = inviteCodeInput,
                        onValueChange = { inviteCodeInput = it },
                        label = { Text("Invite Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_family_code_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (familyNameInput.isNotBlank()) {
                            onUpdateFamily(familyNameInput.trim(), inviteCodeInput.trim())
                            showEditFamilyDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditFamilyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Current Member Profile Dialog
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(currentMember?.name ?: "Family Admin") }
        var emailInput by remember { mutableStateOf(currentMember?.email ?: userEmail ?: "") }
        var selectedEmoji by remember { mutableStateOf(currentMember?.avatarEmoji ?: "👑") }
        var selectedRole by remember { mutableStateOf(currentMember?.role ?: MemberRole.ADMIN) }
        var selectedAge by remember { mutableStateOf(currentMember?.ageCategory ?: AgeCategory.ADULT) }
        val emojis = listOf("👑", "👩", "👨", "👧", "👦", "👵", "👴", "🐱", "🐶", "⭐")

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Your Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_field")
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_email_field")
                    )
                    Text("Avatar Icon", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { emoji ->
                            Surface(
                                shape = CircleShape,
                                color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { selectedEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                    Text("Role", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MemberRole.values().forEach { r ->
                            FilterChip(
                                selected = selectedRole == r,
                                onClick = { selectedRole = r },
                                label = { Text(r.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onUpdateCurrentMember(nameInput.trim(), emailInput.trim(), selectedRole, selectedAge, selectedEmoji)
                            showEditProfileDialog = false
                        }
                    }
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
