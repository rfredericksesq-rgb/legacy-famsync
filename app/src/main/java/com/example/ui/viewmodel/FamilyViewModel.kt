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
    private val _aiBriefing = MutableStateFlow<String>("Good morning, Sarah. You have 4 activities today. The children need to be at school by 08:00, you have a meeting at 10:30, and Daniel has soccer practice at 17:00. Remember to leave home by 16:30.")
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
        // Auto-select first member when loaded
        viewModelScope.launch {
            members.collect { memberList ->
                if (_currentMember.value == null && memberList.isNotEmpty()) {
                    _currentMember.value = memberList.first()
                    refreshAiBriefing()
                }
            }
        }

        // Sync and listen to user profile in Firestore
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    val uid = user.uid
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
                    viewModelScope.launch {
                        firestoreRepo.saveUserProfile(
                            userId = firebaseUser.uid,
                            displayName = firebaseUser.displayName ?: "Sarah Williams",
                            email = firebaseUser.email ?: "",
                            role = "Family Administrator",
                            familyId = "williams_family",
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

    // CRUD Actions with Dual Room & Firestore Persistence
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

    fun deleteEvent(event: FamilyEventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            firestoreRepo.deleteEvent("event_${event.id}", currentUserId())
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

    fun deleteTask(task: FamilyTaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            firestoreRepo.deleteTask("task_${task.id}", currentUserId())
            refreshAiBriefing()
        }
    }

    fun toggleAlarm(alarm: FamilyAlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            firestoreRepo.saveAlarm(updated, currentUserId())
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
                likesCount = 1
            )
            repository.insertDiaryEntry(entity)
            firestoreRepo.saveDiaryEntry(entity, currentUserId())
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
        viewModelScope.launch {
            val entity = ShoppingItemEntity(
                familyId = 1,
                name = name,
                category = category,
                quantity = quantity,
                addedByMemberName = _currentMember.value?.name ?: "Sarah"
            )
            repository.insertShoppingItem(entity)
            firestoreRepo.saveShoppingItem(entity, currentUserId())
        }
    }

    fun toggleShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(isPurchased = !item.isPurchased)
            repository.updateShoppingItem(updated)
            firestoreRepo.saveShoppingItem(updated, currentUserId())
        }
    }

    fun clearPurchasedShopping() {
        viewModelScope.launch {
            repository.clearPurchasedShopping(1)
        }
    }

    fun addAnnouncement(content: String) {
        val author = _currentMember.value?.name ?: "Sarah"
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

    fun inviteMember(name: String, email: String, role: MemberRole, ageCategory: AgeCategory) {
        viewModelScope.launch {
            repository.insertMember(
                FamilyMemberEntity(
                    familyId = 1,
                    name = name,
                    role = role,
                    ageCategory = ageCategory,
                    avatarEmoji = if (role == MemberRole.CHILD) "🧒" else "🧑",
                    colorHex = "#3B82F6",
                    email = email
                )
            )
        }
    }
}
