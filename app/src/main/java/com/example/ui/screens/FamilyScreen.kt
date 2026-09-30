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
import com.example.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    family: FamilyEntity?,
    members: List<FamilyMemberEntity>,
    announcements: List<AnnouncementEntity>,
    importantDates: List<ImportantDateEntity>,
    onSelectMember: (FamilyMemberEntity) -> Unit,
    onAddMember: (name: String, email: String, role: MemberRole, age: AgeCategory, emoji: String, color: String) -> Unit,
    onUpdateMember: (FamilyMemberEntity) -> Unit,
    onDeleteMember: (FamilyMemberEntity) -> Unit,
    onUpdateFamily: (name: String, inviteCode: String) -> Unit,
    onAddAnnouncement: (content: String) -> Unit,
    onReactToAnnouncement: (AnnouncementEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showEditFamilyDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<FamilyMemberEntity?>(null) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Family Banner with Edit Family button
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("family_banner_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = family?.name ?: "My Family",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                IconButton(
                                    onClick = { showEditFamilyDialog = true },
                                    modifier = Modifier.size(32.dp).testTag("edit_family_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Family Name",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Invite Code: ${family?.inviteCode ?: "LEGACY-SYNC"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { showAddMemberDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.testTag("add_member_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Member", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Family Members List Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FAMILY MEMBERS (${members.size})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showAddMemberDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Member")
                }
            }
        }

        if (members.isEmpty()) {
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
                        Text(text = "👨‍👩‍👧‍👦", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No family members added yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Add your family members to start synchronizing schedules and tasks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { showAddMemberDialog = true }) {
                            Text("Add Your First Member")
                        }
                    }
                }
            }
        }

        items(members) { member ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectMember(member) }
                    .testTag("member_card_${member.name.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = member.avatarEmoji, fontSize = 26.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (member.role) {
                                    MemberRole.ADMIN -> MaterialTheme.colorScheme.primary
                                    MemberRole.ADULT -> MaterialTheme.colorScheme.secondary
                                    MemberRole.CHILD -> MaterialTheme.colorScheme.tertiary
                                    MemberRole.GUEST -> Color.Gray
                                }
                            ) {
                                Text(
                                    text = member.role.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (member.email.isNotBlank()) {
                            Text(
                                text = member.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = when (member.role) {
                                MemberRole.ADMIN -> "Full administrator permissions"
                                MemberRole.ADULT -> "Calendar, reminders, tasks permissions"
                                MemberRole.CHILD -> "Assigned tasks & activities view"
                                MemberRole.GUEST -> "Limited schedule access"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { editingMember = member },
                        modifier = Modifier.testTag("edit_member_btn_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Member",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Announcements Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FAMILY ANNOUNCEMENTS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(onClick = { showAnnouncementDialog = true }) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Post")
                }
            }
        }

        items(announcements) { ann ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ann.authorName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = ann.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = ann.content, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = { onReactToAnnouncement(ann, "heart") },
                            label = { Text("❤️ ${ann.heartCount}") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onReactToAnnouncement(ann, "thumbsUp") },
                            label = { Text("👍 ${ann.thumbsUpCount}") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onReactToAnnouncement(ann, "check") },
                            label = { Text("✅ ${ann.checkCount}") }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Edit Family Name & Code
    if (showEditFamilyDialog) {
        var familyName by remember { mutableStateOf(family?.name ?: "My Family") }
        var inviteCode by remember { mutableStateOf(family?.inviteCode ?: "LEGACY-SYNC") }

        AlertDialog(
            onDismissRequest = { showEditFamilyDialog = false },
            title = { Text("Edit Family Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = familyName,
                        onValueChange = { familyName = it },
                        label = { Text("Family Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it },
                        label = { Text("Family Invite Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (familyName.isNotBlank()) {
                            onUpdateFamily(familyName.trim(), inviteCode.trim())
                            showEditFamilyDialog = false
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditFamilyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Add Member
    if (showAddMemberDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(MemberRole.ADULT) }
        var ageCategory by remember { mutableStateOf(AgeCategory.ADULT) }
        var selectedEmoji by remember { mutableStateOf("🧑") }
        val emojis = listOf("👑", "👩", "👨", "👧", "👦", "👵", "👴", "🧑", "👶", "🐕", "🐱")

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Family Member") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("new_member_name_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Avatar Icon", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { emoji ->
                            Surface(
                                shape = CircleShape,
                                color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { selectedEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    Text("Family Role", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MemberRole.values().forEach { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = { role = r },
                                label = { Text(r.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Age Category", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AgeCategory.values().forEach { a ->
                            FilterChip(
                                selected = ageCategory == a,
                                onClick = { ageCategory = a },
                                label = { Text(a.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddMember(
                                name.trim(),
                                email.trim(),
                                role,
                                ageCategory,
                                selectedEmoji,
                                "#2563EB"
                            )
                            showAddMemberDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_member_btn")
                ) {
                    Text("Add Member")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Edit Existing Member
    editingMember?.let { member ->
        var name by remember(member) { mutableStateOf(member.name) }
        var email by remember(member) { mutableStateOf(member.email) }
        var role by remember(member) { mutableStateOf(member.role) }
        var ageCategory by remember(member) { mutableStateOf(member.ageCategory) }
        var selectedEmoji by remember(member) { mutableStateOf(member.avatarEmoji) }
        var showDeleteConfirm by remember { mutableStateOf(false) }
        val emojis = listOf("👑", "👩", "👨", "👧", "👦", "👵", "👴", "🧑", "👶", "🐕", "🐱")

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete ${member.name}?") },
                text = { Text("Are you sure you want to remove this family member from your household?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteMember(member)
                            showDeleteConfirm = false
                            editingMember = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Member")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { editingMember = null },
                title = { Text("Edit Family Member") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Avatar Icon", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(emojis) { emoji ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(40.dp)
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
                                    selected = role == r,
                                    onClick = { role = r },
                                    label = { Text(r.name, fontSize = 11.sp) }
                                )
                            }
                        }

                        Text("Age Category", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AgeCategory.values().forEach { a ->
                                FilterChip(
                                    selected = ageCategory == a,
                                    onClick = { ageCategory = a },
                                    label = { Text(a.name, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remove Member from Family")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onUpdateMember(
                                    member.copy(
                                        name = name.trim(),
                                        email = email.trim(),
                                        role = role,
                                        ageCategory = ageCategory,
                                        avatarEmoji = selectedEmoji
                                    )
                                )
                                editingMember = null
                            }
                        }
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingMember = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // Dialog: Post Announcement
    if (showAnnouncementDialog) {
        var content by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAnnouncementDialog = false },
            title = { Text("Broadcast Family Announcement") },
            text = {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Announcement message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (content.isNotBlank()) {
                            onAddAnnouncement(content.trim())
                            showAnnouncementDialog = false
                        }
                    }
                ) {
                    Text("Broadcast")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnnouncementDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
