package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }

    var accountName by remember { mutableStateOf("Sarah Williams") }
    var accountEmail by remember { mutableStateOf("sarah.williams@legacy.com") }
    var accountPassword by remember { mutableStateOf("••••••••") }

    var familyName by remember { mutableStateOf("The Williams Family") }

    var inviteEmail by remember { mutableStateOf("mark.williams@legacy.com") }

    var prefMorningBriefing by remember { mutableStateOf(true) }
    var prefEveningSummary by remember { mutableStateOf(true) }
    var prefSmartReminders by remember { mutableStateOf(true) }
    var prefBirthdayReminders by remember { mutableStateOf(true) }
    var prefFamilyNotifications by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..5) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (i == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(if (i == step) 28.dp else 12.dp)
                            .height(6.dp)
                    ) {}
                }
            }

            // Step Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Hero Banner
                            Image(
                                painter = painterResource(id = R.drawable.family_hero_banner_1790770929479),
                                contentDescription = "Family Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Welcome to Legacy",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "“Your family. Your schedule. Your legacy.”",
                                style = MaterialTheme.typography.titleMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Keep your family connected, organized, and prepared for what comes next. AI assistant, shared calendar, chores, smart alarms and family synchronization in one beautiful place.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    2 -> {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Step 2: Create Account",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Set up your secure family administrator account.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                            )

                            OutlinedTextField(
                                value = accountName,
                                onValueChange = { accountName = it },
                                label = { Text("Your Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = accountEmail,
                                onValueChange = { accountEmail = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = accountPassword,
                                onValueChange = { accountPassword = it },
                                label = { Text("Password (Encrypted)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    3 -> {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Step 3: Create Your Family",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Choose a name for your family synchronization hub.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                            )

                            OutlinedTextField(
                                value = familyName,
                                onValueChange = { familyName = it },
                                label = { Text("Family Name (e.g. The Williams Family)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "All family members share the same synchronized calendar, chores, and shopping list.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    4 -> {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Step 4: Invite Family Members",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Invite your spouse, children, or caregivers to join your family hub.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                            )

                            OutlinedTextField(
                                value = inviteEmail,
                                onValueChange = { inviteEmail = it },
                                label = { Text("Invite by email or phone") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Suggested Members:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("👨 Dad (Mark) • Adult Member", style = MaterialTheme.typography.bodySmall)
                            Text("👦 Daniel • Child", style = MaterialTheme.typography.bodySmall)
                            Text("👧 Emily • Child", style = MaterialTheme.typography.bodySmall)
                            Text("👵 Grandma Martha • Guest", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    5 -> {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Step 5: Choose Preferences",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Customize how Legacy assists your daily routine.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = prefMorningBriefing, onCheckedChange = { prefMorningBriefing = it })
                                Text("Daily morning AI briefing")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = prefEveningSummary, onCheckedChange = { prefEveningSummary = it })
                                Text("Evening summary & task reflection")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = prefSmartReminders, onCheckedChange = { prefSmartReminders = it })
                                Text("Smart conflict & bag reminders")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = prefBirthdayReminders, onCheckedChange = { prefBirthdayReminders = it })
                                Text("Birthday & milestone reminders")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = prefFamilyNotifications, onCheckedChange = { prefFamilyNotifications = it })
                                Text("Instant family sync notifications")
                            }
                        }
                    }
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(onClick = { step-- }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (step < 5) {
                    Button(
                        onClick = { step++ },
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text(if (step == 1) "Get Started" else "Next")
                    }
                } else {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.testTag("onboarding_finish_button")
                    ) {
                        Text("Enter Legacy")
                    }
                }
            }
        }
    }
}
