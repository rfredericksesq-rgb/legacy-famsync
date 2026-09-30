package com.example.data.repository

import com.example.data.local.FamilyDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ScheduleConflict(
    val event1: FamilyEventEntity,
    val event2: FamilyEventEntity,
    val description: String
)

class FamilyRepository(private val dao: FamilyDao) {

    val family: Flow<FamilyEntity?> = dao.getFamily()
    fun getMembers(familyId: Int = 1): Flow<List<FamilyMemberEntity>> = dao.getFamilyMembers(familyId)

    fun getAllEvents(familyId: Int = 1): Flow<List<FamilyEventEntity>> = dao.getAllEvents(familyId)
    fun getEventsByDate(date: String, familyId: Int = 1): Flow<List<FamilyEventEntity>> = dao.getEventsByDate(familyId, date)

    val allTasks: Flow<List<FamilyTaskEntity>> = dao.getAllTasks(1)
    val allAlarms: Flow<List<FamilyAlarmEntity>> = dao.getAllAlarms(1)
    val allDiaryEntries: Flow<List<DiaryEntryEntity>> = dao.getAllDiaryEntries(1)
    val allShoppingItems: Flow<List<ShoppingItemEntity>> = dao.getAllShoppingItems(1)
    val allAnnouncements: Flow<List<AnnouncementEntity>> = dao.getAllAnnouncements(1)
    val allImportantDates: Flow<List<ImportantDateEntity>> = dao.getAllImportantDates(1)
    val allNotifications: Flow<List<AppNotificationEntity>> = dao.getAllNotifications(1)

    // Schedule conflicts detection Flow
    fun detectConflicts(familyId: Int = 1): Flow<List<ScheduleConflict>> {
        return dao.getAllEvents(familyId).map { events ->
            val conflicts = mutableListOf<ScheduleConflict>()
            val byDate = events.groupBy { it.date }
            for ((_, dateEvents) in byDate) {
                for (i in dateEvents.indices) {
                    for (j in i + 1 until dateEvents.size) {
                        val e1 = dateEvents[i]
                        val e2 = dateEvents[j]
                        // Check if time intervals overlap
                        if (isTimeOverlap(e1.startTime, e1.endTime, e2.startTime, e2.endTime)) {
                            conflicts.add(
                                ScheduleConflict(
                                    event1 = e1,
                                    event2 = e2,
                                    description = "${e1.memberName} has \"${e1.title}\" at ${e1.startTime} while ${e2.memberName} has \"${e2.title}\" at ${e2.startTime}."
                                )
                            )
                        }
                    }
                }
            }
            conflicts
        }
    }

    private fun isTimeOverlap(s1: String, e1: String, s2: String, e2: String): Boolean {
        // e.g. "15:00", "16:00"
        return s1 < e2 && s2 < e1
    }

    // CRUD methods with automatic real-time sync notifications
    suspend fun insertEvent(event: FamilyEventEntity): Long {
        val id = dao.insertEvent(event)
        dao.insertNotification(
            AppNotificationEntity(
                familyId = event.familyId,
                title = "New Family Event Added",
                description = "${event.title} on ${event.date} at ${event.startTime} (${event.memberName})",
                timestamp = "Just now",
                iconType = "event"
            )
        )
        return id
    }

    suspend fun updateEvent(event: FamilyEventEntity) {
        dao.updateEvent(event)
        dao.insertNotification(
            AppNotificationEntity(
                familyId = event.familyId,
                title = "Event Updated",
                description = "${event.title} is now set for ${event.date} at ${event.startTime}",
                timestamp = "Just now",
                iconType = "event"
            )
        )
    }

    suspend fun deleteEvent(event: FamilyEventEntity) = dao.deleteEvent(event)

    suspend fun insertTask(task: FamilyTaskEntity): Long {
        val id = dao.insertTask(task)
        dao.insertNotification(
            AppNotificationEntity(
                familyId = task.familyId,
                title = "New Task Assigned",
                description = "\"${task.title}\" assigned to ${task.assignedMemberName} (Due: ${task.dueTime})",
                timestamp = "Just now",
                iconType = "task"
            )
        )
        return id
    }

    suspend fun updateTask(task: FamilyTaskEntity) {
        dao.updateTask(task)
        if (task.isCompleted) {
            dao.insertNotification(
                AppNotificationEntity(
                    familyId = task.familyId,
                    title = "Task Completed! 🎉",
                    description = "${task.assignedMemberName} completed \"${task.title}\"",
                    timestamp = "Just now",
                    iconType = "task"
                )
            )
        }
    }

    suspend fun deleteTask(task: FamilyTaskEntity) = dao.deleteTask(task)

    suspend fun insertAlarm(alarm: FamilyAlarmEntity) = dao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: FamilyAlarmEntity) = dao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: FamilyAlarmEntity) = dao.deleteAlarm(alarm)

    suspend fun insertDiaryEntry(entry: DiaryEntryEntity): Long {
        val id = dao.insertDiaryEntry(entry)
        dao.insertNotification(
            AppNotificationEntity(
                familyId = entry.familyId,
                title = if (entry.milestone) "New Family Milestone 🌟" else "New Family Diary Entry 📔",
                description = "${entry.authorName}: \"${entry.title}\"",
                timestamp = "Just now",
                iconType = "diary"
            )
        )
        return id
    }

    suspend fun updateDiaryEntry(entry: DiaryEntryEntity) = dao.updateDiaryEntry(entry)
    suspend fun deleteDiaryEntry(entry: DiaryEntryEntity) = dao.deleteDiaryEntry(entry)

    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long = dao.insertShoppingItem(item)
    suspend fun updateShoppingItem(item: ShoppingItemEntity) = dao.updateShoppingItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItemEntity) = dao.deleteShoppingItem(item)
    suspend fun clearPurchasedShopping(familyId: Int = 1) = dao.clearPurchasedShoppingItems(familyId)

    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long {
        val id = dao.insertAnnouncement(announcement)
        dao.insertNotification(
            AppNotificationEntity(
                familyId = announcement.familyId,
                title = "Important Family Announcement 📢",
                description = "${announcement.authorName}: \"${announcement.content}\"",
                timestamp = "Just now",
                iconType = "announcement"
            )
        )
        return id
    }

    suspend fun updateAnnouncement(announcement: AnnouncementEntity) = dao.updateAnnouncement(announcement)

    suspend fun insertImportantDate(date: ImportantDateEntity) = dao.insertImportantDate(date)

    suspend fun insertMember(member: FamilyMemberEntity) = dao.insertMember(member)
    suspend fun deleteMember(member: FamilyMemberEntity) = dao.deleteMember(member)

    suspend fun markAllNotificationsRead(familyId: Int = 1) = dao.markAllNotificationsRead(familyId)
    suspend fun clearAllNotifications(familyId: Int = 1) = dao.clearAllNotifications(familyId)
}
