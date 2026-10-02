package com.example.data.firebase

import android.util.Log
import com.example.data.model.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreFamilyRepository(
    private val databaseId: String
) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)

    // --- USER PROFILE ---
    suspend fun saveUserProfile(
        userId: String,
        displayName: String,
        email: String,
        role: String = "Family Administrator",
        familyId: String = "my_family",
        avatarEmoji: String = "👑"
    ) {
        val userDoc = db.collection("users").document(userId)
        val data = hashMapOf<String, Any>(
            "userId" to userId,
            "displayName" to displayName,
            "email" to email,
            "role" to role,
            "familyId" to familyId,
            "avatarEmoji" to avatarEmoji,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            userDoc.set(data).await()
            Log.d("FirestoreRepo", "User profile saved: $userId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDoc.path)
        }
    }

    fun observeUserProfile(userId: String): Flow<Map<String, Any>?> = callbackFlow {
        val userDoc = db.collection("users").document(userId)
        val listener = userDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, userDoc.path)
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.data)
        }
        awaitClose { listener.remove() }
    }

    // --- EVENTS ---
    suspend fun saveEvent(event: FamilyEventEntity, userId: String, familyId: String = "my_family") {
        val docId = if (event.id > 0) "event_${event.id}" else "event_${System.currentTimeMillis()}"
        val docRef = db.collection("events").document(docId)
        val data = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to userId,
            "familyId" to familyId,
            "title" to event.title,
            "startTime" to event.startTime,
            "endTime" to event.endTime,
            "date" to event.date,
            "location" to event.location,
            "category" to event.category,
            "priority" to event.priority,
            "reminderMinutes" to event.reminderMinutes.toLong(),
            "assignedTo" to event.memberName,
            "isRecurring" to (event.recurrence != "None"),
            "recurrenceRule" to event.recurrence,
            "notes" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            Log.d("FirestoreRepo", "Event saved to Firestore: $docId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun deleteEvent(eventId: String, userId: String) {
        val docRef = db.collection("events").document(eventId)
        try {
            docRef.delete().await()
            Log.d("FirestoreRepo", "Event deleted from Firestore: $eventId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    fun observeEvents(userId: String): Flow<List<FamilyEventEntity>> = callbackFlow {
        val query = db.collection("events").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "events")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val idStr = doc.getString("id") ?: doc.id
                    val numericId = idStr.replace("event_", "").toIntOrNull() ?: 1
                    FamilyEventEntity(
                        id = numericId,
                        familyId = 1,
                        title = doc.getString("title") ?: "",
                        memberId = 1,
                        memberName = doc.getString("assignedTo") ?: "Family",
                        date = doc.getString("date") ?: "",
                        startTime = doc.getString("startTime") ?: "",
                        endTime = doc.getString("endTime") ?: "",
                        location = doc.getString("location") ?: "",
                        category = doc.getString("category") ?: "Family",
                        priority = doc.getString("priority") ?: "Normal",
                        reminderMinutes = (doc.getLong("reminderMinutes") ?: 15L).toInt(),
                        recurrence = doc.getString("recurrenceRule") ?: "None"
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // --- TASKS ---
    suspend fun saveTask(task: FamilyTaskEntity, userId: String, familyId: String = "my_family") {
        val docId = if (task.id > 0) "task_${task.id}" else "task_${System.currentTimeMillis()}"
        val docRef = db.collection("tasks").document(docId)
        val data = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to userId,
            "familyId" to familyId,
            "title" to task.title,
            "assignedTo" to task.assignedMemberName,
            "dueDate" to task.dueDate,
            "dueTime" to task.dueTime,
            "isCompleted" to task.isCompleted,
            "isRecurring" to (task.recurrence != "None"),
            "recurrencePattern" to task.recurrence,
            "category" to task.category,
            "priority" to task.priority,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            Log.d("FirestoreRepo", "Task saved to Firestore: $docId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun deleteTask(taskId: String, userId: String) {
        val docRef = db.collection("tasks").document(taskId)
        try {
            docRef.delete().await()
            Log.d("FirestoreRepo", "Task deleted from Firestore: $taskId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    fun observeTasks(userId: String): Flow<List<FamilyTaskEntity>> = callbackFlow {
        val query = db.collection("tasks").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "tasks")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val idStr = doc.getString("id") ?: doc.id
                    val numericId = idStr.replace("task_", "").toIntOrNull() ?: 1
                    FamilyTaskEntity(
                        id = numericId,
                        familyId = 1,
                        title = doc.getString("title") ?: "",
                        assignedMemberId = 1,
                        assignedMemberName = doc.getString("assignedTo") ?: "Family Member",
                        dueDate = doc.getString("dueDate") ?: "",
                        dueTime = doc.getString("dueTime") ?: "19:00",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        priority = doc.getString("priority") ?: "Normal",
                        category = doc.getString("category") ?: "Chores",
                        recurrence = doc.getString("recurrencePattern") ?: "None"
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // --- ALARMS ---
    suspend fun saveAlarm(alarm: FamilyAlarmEntity, userId: String, familyId: String = "my_family") {
        val docId = if (alarm.id > 0) "alarm_${alarm.id}" else "alarm_${System.currentTimeMillis()}"
        val docRef = db.collection("alarms").document(docId)
        val data = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to userId,
            "familyId" to familyId,
            "title" to alarm.title,
            "time" to alarm.time,
            "days" to alarm.daysOfWeek,
            "isEnabled" to alarm.isEnabled,
            "isRecurring" to alarm.isRecurring,
            "type" to if (alarm.isSmartRecommendation) "REMINDER" else "ALARM",
            "soundName" to "Chimes",
            "vibrate" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            Log.d("FirestoreRepo", "Alarm saved to Firestore: $docId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun deleteAlarm(alarmId: String, userId: String) {
        val docRef = db.collection("alarms").document(alarmId)
        try {
            docRef.delete().await()
            Log.d("FirestoreRepo", "Alarm deleted from Firestore: $alarmId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    fun observeAlarms(userId: String): Flow<List<FamilyAlarmEntity>> = callbackFlow {
        val query = db.collection("alarms").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "alarms")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val idStr = doc.getString("id") ?: doc.id
                    val numericId = idStr.replace("alarm_", "").toIntOrNull() ?: 1
                    FamilyAlarmEntity(
                        id = numericId,
                        familyId = 1,
                        title = doc.getString("title") ?: "",
                        time = doc.getString("time") ?: "",
                        isEnabled = doc.getBoolean("isEnabled") ?: true,
                        daysOfWeek = doc.getString("days") ?: "Weekdays",
                        isRecurring = doc.getBoolean("isRecurring") ?: true,
                        isSmartRecommendation = (doc.getString("type") == "REMINDER"),
                        memberId = 1
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // --- DIARY ---
    suspend fun saveDiaryEntry(entry: DiaryEntryEntity, userId: String, familyId: String = "my_family") {
        val docId = if (entry.id > 0) "diary_${entry.id}" else "diary_${System.currentTimeMillis()}"
        val docRef = db.collection("diary").document(docId)
        val data = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to userId,
            "familyId" to familyId,
            "authorName" to entry.authorName,
            "authorRole" to "Mom",
            "date" to entry.date,
            "title" to entry.title,
            "content" to entry.text,
            "moodEmoji" to "🌟",
            "hasPhoto" to entry.photoResName.isNotEmpty(),
            "tags" to if (entry.milestone) "Milestone" else "Family",
            "reactions" to "${entry.likesCount} ❤️",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            Log.d("FirestoreRepo", "Diary saved to Firestore: $docId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun deleteDiaryEntry(entryId: String, userId: String) {
        val docRef = db.collection("diary").document(entryId)
        try {
            docRef.delete().await()
            Log.d("FirestoreRepo", "Diary deleted from Firestore: $entryId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    fun observeDiary(userId: String): Flow<List<DiaryEntryEntity>> = callbackFlow {
        val query = db.collection("diary").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "diary")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val idStr = doc.getString("id") ?: doc.id
                    val numericId = idStr.replace("diary_", "").toIntOrNull() ?: 1
                    DiaryEntryEntity(
                        id = numericId,
                        familyId = 1,
                        authorId = 1,
                        authorName = doc.getString("authorName") ?: "Family Admin",
                        date = doc.getString("date") ?: "",
                        title = doc.getString("title") ?: "",
                        text = doc.getString("content") ?: "",
                        milestone = doc.getString("tags")?.contains("Milestone") == true,
                        isPrivate = false,
                        likesCount = 0,
                        photoResName = ""
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // --- SHOPPING ---
    suspend fun saveShoppingItem(item: ShoppingItemEntity, userId: String, familyId: String = "my_family") {
        val docId = if (item.id > 0) "shop_${item.id}" else "shop_${System.currentTimeMillis()}"
        val docRef = db.collection("shopping").document(docId)
        val data = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to userId,
            "familyId" to familyId,
            "name" to item.name,
            "quantity" to item.quantity,
            "category" to item.category,
            "addedBy" to item.addedByMemberName,
            "isPurchased" to item.isPurchased,
            "urgent" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            Log.d("FirestoreRepo", "Shopping item saved to Firestore: $docId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun deleteShoppingItem(itemId: String, userId: String) {
        val docRef = db.collection("shopping").document(itemId)
        try {
            docRef.delete().await()
            Log.d("FirestoreRepo", "Shopping item deleted from Firestore: $itemId")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    fun observeShopping(userId: String): Flow<List<ShoppingItemEntity>> = callbackFlow {
        val query = db.collection("shopping").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "shopping")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val idStr = doc.getString("id") ?: doc.id
                    val numericId = idStr.replace("shop_", "").toIntOrNull() ?: 1
                    ShoppingItemEntity(
                        id = numericId,
                        familyId = 1,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "Groceries",
                        quantity = doc.getString("quantity") ?: "1",
                        isPurchased = doc.getBoolean("isPurchased") ?: false,
                        addedByMemberName = doc.getString("addedBy") ?: "Mom"
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }
}
