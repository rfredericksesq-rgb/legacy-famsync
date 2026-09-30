package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.ai.AssistantAction
import com.example.data.ai.LegacyAiService
import com.example.data.firebase.FirestoreFamilyRepository
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FamilyRepository
import com.example.data.repository.ScheduleConflict
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val text: String,
    val isAssistant: Boolean = false,
    val timestamp: String = "Just now",
    val action: AssistantAction? = null,
    val confirmationPrompt: String? = null
)

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { authInstance ->
        trySend(authInstance.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class FamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = FamilyRepository(database.familyDao())
    private val aiService = LegacyAiService()

    private val databaseId: String = application.getString(R.string.firestore_database_id)
    private val firestoreRepo = FirestoreFamilyRepository(databaseId)
    private val auth: FirebaseAuth = Firebase.auth

    val currentUser: StateFlow<FirebaseUser?> = auth.authStateFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        auth.currentUser
    )

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _userProfile = MutableStateFlow<Map<String, Any>?>(null)
    val userProfile: StateFlow<Map<String, Any>?> = _userProfile.asStateFlow()

    val family = repository.family.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val members = repository.getMembers(1).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val events = repository.getAllEvents(1).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val alarms = repository.allAlarms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val diaryEntries = repository.allDiaryEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val shoppingItems = repository.allShoppingItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val announcements = repository.allAnnouncements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val importantDates = repository.allImportantDates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val conflicts = repository.detectConflicts(1).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Profile
    private val _currentMember = MutableStateFlow<FamilyMemberEntity?>(null)
    val currentMember: StateFlow<FamilyMemberEntity?> = _currentMember.asStateFlow()

    // AI Daily Briefing
    private val _aiBriefing = MutableStateFlow<String>("Welcome to FamSync! You can add your family schedule, assign chores, and manage reminders.")
    val aiBriefing: StateFlow<String> = _aiBriefing.asStateFlow()

    private val _isEveningBriefing = MutableStateFlow(false)
    val isEveningBriefing: StateFlow<Boolean> = _isEveningBriefing.asStateFlow()

    private val _isGeneratingBriefing = MutableStateFlow(false)
    val isGeneratingBriefing: StateFlow<Boolean> = _isGeneratingBriefing.asStateFlow()

    // Assistant Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                senderName = "Legacy Assistant",
                text = "Hello! I am Legacy, your family assistant. How can I help coordinate your schedule, chores, or reminders today?",
                isAssistant = true
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Active pending confirmation
    private val _pendingAction = MutableStateFlow<AssistantAction?>(null)
    val pendingAction: StateFlow<AssistantAction?> = _pendingAction.asStateFlow()

    private val _pendingActionPrompt = MutableStateFlow<String?>(null)
    val pendingActionPrompt: StateFlow<String?> = _pendingActionPrompt.asStateFlow()

    init {
        // Immediately purge any legacy default users and ensure clean editable family
        viewModelScope.launch {
            val initialUser = auth.currentUser
            val initialName = initialUser?.displayName?.takeIf { it.isNotBlank() }
                ?: initialUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: "Family Admin"
            repository.purgeDefaultUsersAndEnsureClean(
                defaultAdminName = initialName,
                defaultEmail = initialUser?.email ?: ""
            )
        }

        // Observe members: keep _currentMember in sync and guard against default users
        viewModelScope.launch {
            members.collect { memberList ->
                val hasDefaultUsers = memberList.any { it.name in listOf("Sarah", "Mark", "Daniel", "Emily", "Grandma Martha") }
                if (hasDefaultUsers) {
                    val user = auth.currentUser
                    val name = user?.displayName ?: "Family Admin"
                    repository.purgeDefaultUsersAndEnsureClean(name, user?.email ?: "")
                } else if (memberList.isEmpty()) {
                    val user = auth.currentUser
                    val name = user?.displayName ?: "Family Admin"
                    repository.ensurePrimaryMember(name, user?.email ?: "")
                } else {
                    if (_currentMember.value == null) {
                        _currentMember.value = memberList.first()
                        refreshAiBriefing()
                    } else {
                        val updatedCurrent = memberList.find { it.id == _currentMember.value?.id }
                        if (updatedCurrent != null) {
                            _currentMember.value = updatedCurrent
                        } else {
                            _currentMember.value = memberList.firstOrNull()
                            refreshAiBriefing()
                        }
                    }
                }
            }
        }

        // Initialize user-owned family and observe cloud profile if authenticated
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    val uid = user.uid
                    val userName = user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                        ?: "Family Admin"
                    val userEmail = user.email ?: ""

                    // If primary member has generic placeholder, update with authenticated user's name
                    val currentAdmin = members.value.find { it.role == MemberRole.ADMIN } ?: _currentMember.value
                    if (currentAdmin != null && (currentAdmin.name == "Family Admin" || currentAdmin.name == "Primary Member")) {
                        val updated = currentAdmin.copy(name = userName, email = userEmail)
                        repository.updateMember(updated)
                        _currentMember.value = updated
                    }

                    launch {
                        firestoreRepo.observeUserProfile(uid).collect { profile ->
                            _userProfile.value = profile
                        }
                    }
                } else {
                    _userProfile.value = null
                }
            }
        }
    }

    private fun currentUserId(): String {
        return auth.currentUser?.uid ?: "user_default"
    }

    // --- AUTHENTICATION ---
    fun signInWithGoogle(idToken: String) {
        _authLoading.value = true
        _authError.value = null
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                _authLoading.value = false
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val name = firebaseUser.displayName ?: "Family Admin"
                    viewModelScope.launch {
                        firestoreRepo.saveUserProfile(
                            userId = firebaseUser.uid,
                            displayName = name,
                            email = firebaseUser.email ?: "",
                            role = "Family Administrator",
                            familyId = "my_family",
                            avatarEmoji = "👑"
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FamilyViewModel", "Firebase auth failed", exception)
                _authLoading.value = false
                _authError.value = exception.localizedMessage ?: "Google Sign-In failed"
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun switchActiveMember(member: FamilyMemberEntity) {
        _currentMember.value = member
        refreshAiBriefing()
    }

    fun toggleBriefingMode() {
        _isEveningBriefing.value = !_isEveningBriefing.value
        refreshAiBriefing()
    }

    fun refreshAiBriefing() {
        val member = _currentMember.value ?: return
        viewModelScope.launch {
            _isGeneratingBriefing.value = true
            try {
                val briefing = aiService.generateDailyBriefing(
                    memberName = member.name,
                    events = events.value,
                    tasks = tasks.value,
                    isEvening = _isEveningBriefing.value
                )
                _aiBriefing.value = briefing
            } finally {
                _isGeneratingBriefing.value = false
            }
        }
    }

    // AI Assistant Messaging
    fun sendMessageToAssistant(text: String) {
        if (text.isBlank()) return
        val member = _currentMember.value ?: return

        val userMsg = ChatMessage(
            senderName = member.name,
            text = text,
            isAssistant = false
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            val response = aiService.processUserCommand(
                userMessage = text,
                currentMember = member,
                events = events.value,
                tasks = tasks.value
            )

            val assistantMsg = ChatMessage(
                senderName = "Legacy Assistant",
                text = response.messageText,
                isAssistant = true,
                action = response.pendingAction,
                confirmationPrompt = response.confirmationPrompt
            )
            _chatMessages.value = _chatMessages.value + assistantMsg

            if (response.pendingAction != null && response.confirmationPrompt != null) {
                _pendingAction.value = response.pendingAction
                _pendingActionPrompt.value = response.confirmationPrompt
            }
        }
    }

    fun confirmPendingAction() {
        val action = _pendingAction.value ?: return
        viewModelScope.launch {
            when (action) {
                is AssistantAction.CreateEvent -> {
                    addEvent(
                        title = action.title,
                        memberName = action.memberName,
                        date = action.date,
                        startTime = action.startTime,
                        endTime = action.endTime,
                        location = action.location,
                        category = action.category,
                        priority = "Normal",
                        isPrivate = false,
                        reminderMinutes = 15,
                        recurrence = action.recurrence
                    )
                    addAssistantReply("Done! Added \"${action.title}\" for ${action.memberName} on ${action.date} at ${action.startTime}.")
                }
                is AssistantAction.CreateTask -> {
                    addTask(
                        title = action.title,
                        assignedMemberName = action.assignedMemberName,
                        dueDate = action.dueDate,
                        dueTime = action.dueTime,
                        priority = action.priority,
                        category = action.category,
                        recurrence = "None"
                    )
                    addAssistantReply("Added chore: \"${action.title}\" assigned to ${action.assignedMemberName}.")
                }
                is AssistantAction.CreateAlarm -> {
                    addAlarm(
                        title = action.title,
                        time = action.time,
                        daysOfWeek = action.daysOfWeek,
                        isRecurring = action.isRecurring
                    )
                    addAssistantReply("Alarm set for ${action.time} (${action.daysOfWeek}).")
                }
                is AssistantAction.CreateShoppingItem -> {
                    addShoppingItem(
                        name = action.name,
                        category = action.category,
                        quantity = action.quantity
                    )
                    addAssistantReply("Added \"${action.name}\" to the family shopping list.")
                }
                is AssistantAction.CreateAnnouncement -> {
                    addAnnouncement(action.content)
                    addAssistantReply("Family announcement posted.")
                }
                is AssistantAction.CreateDiaryEntry -> {
                    addDiaryEntry(
                        title = action.title,
                        text = action.text,
                        milestone = action.milestone,
                        isPrivate = false
                    )
                    addAssistantReply("Saved to family diary: \"${action.title}\".")
                }
            }
            _pendingAction.value = null
            _pendingActionPrompt.value = null
        }
    }

    fun cancelPendingAction() {
        _pendingAction.value = null
        _pendingActionPrompt.value = null
        addAssistantReply("Action cancelled. What else would you like help with?")
    }

    private fun addAssistantReply(text: String) {
        _chatMessages.value = _chatMessages.value + ChatMessage(
            senderName = "Legacy Assistant",
            text = text,
            isAssistant = true
        )
    }

    // --- USER-EDITABLE FAMILY & MEMBER MANAGEMENT ---
    fun updateFamily(name: String, inviteCode: String) {
        viewModelScope.launch {
            val current = family.value ?: FamilyEntity(id = 1)
            repository.updateFamily(current.copy(name = name, inviteCode = inviteCode))
        }
    }

    fun addMember(
        name: String,
        email: String,
        role: MemberRole,
        ageCategory: AgeCategory,
        avatarEmoji: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val entity = FamilyMemberEntity(
                familyId = 1,
                name = name,
                role = role,
                ageCategory = ageCategory,
                avatarEmoji = avatarEmoji,
                colorHex = colorHex,
                email = email
            )
            repository.insertMember(entity)
            if (_currentMember.value == null) {
                _currentMember.value = entity
            }
            refreshAiBriefing()
        }
    }

    fun updateCurrentMember(
        name: String,
        email: String,
        role: MemberRole = MemberRole.ADMIN,
        ageCategory: AgeCategory = AgeCategory.ADULT,
        avatarEmoji: String = "👑"
    ) {
        val current = _currentMember.value ?: return
        val updated = current.copy(
            name = name.trim(),
            email = email.trim(),
            role = role,
            ageCategory = ageCategory,
            avatarEmoji = avatarEmoji
        )
        updateMember(updated)
    }

    fun setupUserFamily(userName: String, userEmail: String, familyName: String) {
        viewModelScope.launch {
            if (familyName.isNotBlank()) {
                val currentFam = family.value ?: FamilyEntity(id = 1)
                repository.updateFamily(currentFam.copy(name = familyName.trim()))
            }
            val current = _currentMember.value ?: members.value.firstOrNull()
            if (current != null) {
                val updated = current.copy(
                    name = if (userName.isNotBlank()) userName.trim() else current.name,
                    email = if (userEmail.isNotBlank()) userEmail.trim() else current.email
                )
                repository.updateMember(updated)
                _currentMember.value = updated
            } else {
                val newMember = FamilyMemberEntity(
                    id = 1,
                    familyId = 1,
                    name = if (userName.isNotBlank()) userName.trim() else "Family Admin",
                    role = MemberRole.ADMIN,
                    ageCategory = AgeCategory.ADULT,
                    avatarEmoji = "👑",
                    colorHex = "#2563EB",
                    email = userEmail.trim()
                )
                repository.insertMember(newMember)
                _currentMember.value = newMember
            }
            refreshAiBriefing()
        }
    }

    fun updateMember(member: FamilyMemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
            if (_currentMember.value?.id == member.id) {
                _currentMember.value = member
            }
            refreshAiBriefing()
        }
    }

    fun deleteMember(member: FamilyMemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
            if (_currentMember.value?.id == member.id) {
                _currentMember.value = members.value.find { it.id != member.id }
            }
            refreshAiBriefing()
        }
    }

    // --- USER-EDITABLE CRUD ACTIONS ---
    fun addEvent(
        title: String,
        memberName: String,
        date: String,
        startTime: String,
        endTime: String,
        location: String,
        category: String,
        priority: String,
        isPrivate: Boolean,
        reminderMinutes: Int,
        recurrence: String
    ) {
        viewModelScope.launch {
            val entity = FamilyEventEntity(
                familyId = 1,
                title = title,
                memberId = _currentMember.value?.id ?: 1,
                memberName = memberName,
                date = date,
                startTime = startTime,
                endTime = endTime,
                location = location,
                category = category,
                priority = priority,
                isPrivate = isPrivate,
                reminderMinutes = reminderMinutes,
                recurrence = recurrence
            )
            repository.insertEvent(entity)
            firestoreRepo.saveEvent(entity, currentUserId())
            refreshAiBriefing()
        }
    }

    fun updateEvent(event: FamilyEventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
            firestoreRepo.saveEvent(event, currentUserId())
            refreshAiBriefing()
        }
    }

    fun deleteEvent(event: FamilyEventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            firestoreRepo.deleteEvent("event_${event.id}", currentUserId())
            refreshAiBriefing()
        }
    }

    fun addTask(
        title: String,
        assignedMemberName: String,
        dueDate: String,
        dueTime: String,
        priority: String,
        category: String,
        recurrence: String
    ) {
        viewModelScope.launch {
            val entity = FamilyTaskEntity(
                familyId = 1,
                title = title,
                assignedMemberId = members.value.find { it.name == assignedMemberName }?.id ?: 1,
                assignedMemberName = assignedMemberName,
                dueDate = dueDate,
                dueTime = dueTime,
                isCompleted = false,
                priority = priority,
                category = category,
                recurrence = recurrence
            )
            repository.insertTask(entity)
            firestoreRepo.saveTask(entity, currentUserId())
            refreshAiBriefing()
        }
    }

    fun toggleTask(task: FamilyTaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
            firestoreRepo.saveTask(updated, currentUserId())
            refreshAiBriefing()
        }
    }

    fun updateTask(task: FamilyTaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            firestoreRepo.saveTask(task, currentUserId())
            refreshAiBriefing()
        }
    }

    fun deleteTask(task: FamilyTaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            firestoreRepo.deleteTask("task_${task.id}", currentUserId())
            refreshAiBriefing()
        }
    }

    fun addAlarm(title: String, time: String, daysOfWeek: String, isRecurring: Boolean) {
        viewModelScope.launch {
            val entity = FamilyAlarmEntity(
                familyId = 1,
                title = title,
                time = time,
                isEnabled = true,
                daysOfWeek = daysOfWeek,
                isRecurring = isRecurring
            )
            repository.insertAlarm(entity)
            firestoreRepo.saveAlarm(entity, currentUserId())
        }
    }

    fun toggleAlarm(alarm: FamilyAlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            firestoreRepo.saveAlarm(updated, currentUserId())
        }
    }

    fun updateAlarm(alarm: FamilyAlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
            firestoreRepo.saveAlarm(alarm, currentUserId())
        }
    }

    fun deleteAlarm(alarm: FamilyAlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            firestoreRepo.deleteAlarm("alarm_${alarm.id}", currentUserId())
        }
    }

    fun addDiaryEntry(title: String, text: String, milestone: Boolean, isPrivate: Boolean) {
        val member = _currentMember.value ?: return
        viewModelScope.launch {
            val entity = DiaryEntryEntity(
                familyId = 1,
                authorId = member.id,
                authorName = member.name,
                date = "2026-09-30",
                title = title,
                text = text,
                milestone = milestone,
                isPrivate = isPrivate,
                likesCount = 0
            )
            repository.insertDiaryEntry(entity)
            firestoreRepo.saveDiaryEntry(entity, currentUserId())
        }
    }

    fun updateDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.updateDiaryEntry(entry)
            firestoreRepo.saveDiaryEntry(entry, currentUserId())
        }
    }

    fun likeDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            val updated = entry.copy(likesCount = entry.likesCount + 1)
            repository.updateDiaryEntry(updated)
            firestoreRepo.saveDiaryEntry(updated, currentUserId())
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.deleteDiaryEntry(entry)
            firestoreRepo.deleteDiaryEntry("diary_${entry.id}", currentUserId())
        }
    }

    fun addShoppingItem(name: String, category: String, quantity: String) {
        val memberName = _currentMember.value?.name ?: "Family Admin"
        viewModelScope.launch {
            val entity = ShoppingItemEntity(
                familyId = 1,
                name = name,
                category = category,
                quantity = quantity,
                addedByMemberName = memberName
            )
            repository.insertShoppingItem(entity)
            firestoreRepo.saveShoppingItem(entity, currentUserId())
        }
    }

    fun updateShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.updateShoppingItem(item)
            firestoreRepo.saveShoppingItem(item, currentUserId())
        }
    }

    fun toggleShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(isPurchased = !item.isPurchased)
            repository.updateShoppingItem(updated)
            firestoreRepo.saveShoppingItem(updated, currentUserId())
        }
    }

    fun deleteShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
            firestoreRepo.deleteShoppingItem("shop_${item.id}", currentUserId())
        }
    }

    fun clearPurchasedShopping() {
        viewModelScope.launch {
            repository.clearPurchasedShopping(1)
        }
    }

    fun addAnnouncement(content: String) {
        val author = _currentMember.value?.name ?: "Family Admin"
        viewModelScope.launch {
            repository.insertAnnouncement(
                AnnouncementEntity(
                    familyId = 1,
                    authorName = author,
                    content = content,
                    timestamp = "Just now"
                )
            )
        }
    }

    fun reactToAnnouncement(announcement: AnnouncementEntity, reactionType: String) {
        viewModelScope.launch {
            val updated = when (reactionType) {
                "heart" -> announcement.copy(heartCount = announcement.heartCount + 1)
                "thumbsUp" -> announcement.copy(thumbsUpCount = announcement.thumbsUpCount + 1)
                "check" -> announcement.copy(checkCount = announcement.checkCount + 1)
                else -> announcement
            }
            repository.updateAnnouncement(updated)
        }
    }

    fun resolveConflict(conflict: ScheduleConflict, chosenMember: String) {
        viewModelScope.launch {
            val updated = conflict.event2.copy(memberName = chosenMember)
            repository.updateEvent(updated)
            firestoreRepo.saveEvent(updated, currentUserId())
            refreshAiBriefing()
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(1)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications(1)
        }
    }
}
