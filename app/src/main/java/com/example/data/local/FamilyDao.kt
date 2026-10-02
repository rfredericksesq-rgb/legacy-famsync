package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Query("SELECT * FROM families LIMIT 1")
    fun getFamily(): Flow<FamilyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity): Long

    @Update
    suspend fun updateFamily(family: FamilyEntity)

    @Query("SELECT * FROM family_members WHERE familyId = :familyId")
    fun getFamilyMembers(familyId: Int): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE familyId = :familyId")
    suspend fun getMembersOnce(familyId: Int): List<FamilyMemberEntity>

    @Query("DELETE FROM family_members WHERE name IN (:names)")
    suspend fun deleteMembersByNames(names: List<String>)

    @Query("DELETE FROM family_events WHERE memberName IN (:names)")
    suspend fun deleteEventsByMemberNames(names: List<String>)

    @Query("DELETE FROM family_tasks WHERE assignedMemberName IN (:names)")
    suspend fun deleteTasksByMemberNames(names: List<String>)

    @Query("UPDATE families SET name = 'My Family', inviteCode = 'LEGACY-SYNC' WHERE name = 'The Williams Family'")
    suspend fun sanitizeLegacyFamilyName()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<FamilyMemberEntity>)

    @Update
    suspend fun updateMember(member: FamilyMemberEntity)

    @Delete
    suspend fun deleteMember(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteMemberById(id: Int)

    @Query("DELETE FROM family_members")
    suspend fun deleteAllMembers()

    @Query("DELETE FROM family_events")
    suspend fun deleteAllEvents()

    @Query("DELETE FROM family_tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM family_alarms")
    suspend fun deleteAllAlarms()

    @Query("DELETE FROM diary_entries")
    suspend fun deleteAllDiaryEntries()

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAllShoppingItems()

    // Events
    @Query("SELECT * FROM family_events WHERE familyId = :familyId ORDER BY date ASC, startTime ASC")
    fun getAllEvents(familyId: Int): Flow<List<FamilyEventEntity>>

    @Query("SELECT * FROM family_events WHERE familyId = :familyId AND date = :date ORDER BY startTime ASC")
    fun getEventsByDate(familyId: Int, date: String): Flow<List<FamilyEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: FamilyEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<FamilyEventEntity>)

    @Update
    suspend fun updateEvent(event: FamilyEventEntity)

    @Delete
    suspend fun deleteEvent(event: FamilyEventEntity)

    @Query("DELETE FROM family_events WHERE isGoogleCalendarImport = 1")
    suspend fun deleteGoogleCalendarEvents()

    // Tasks
    @Query("SELECT * FROM family_tasks WHERE familyId = :familyId ORDER BY isCompleted ASC, dueDate ASC, dueTime ASC")
    fun getAllTasks(familyId: Int): Flow<List<FamilyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FamilyTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<FamilyTaskEntity>)

    @Update
    suspend fun updateTask(task: FamilyTaskEntity)

    @Delete
    suspend fun deleteTask(task: FamilyTaskEntity)

    // Alarms
    @Query("SELECT * FROM family_alarms WHERE familyId = :familyId ORDER BY time ASC")
    fun getAllAlarms(familyId: Int): Flow<List<FamilyAlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: FamilyAlarmEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<FamilyAlarmEntity>)

    @Update
    suspend fun updateAlarm(alarm: FamilyAlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: FamilyAlarmEntity)

    // Diary
    @Query("SELECT * FROM diary_entries WHERE familyId = :familyId ORDER BY date DESC, id DESC")
    fun getAllDiaryEntries(familyId: Int): Flow<List<DiaryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntries(entries: List<DiaryEntryEntity>)

    @Update
    suspend fun updateDiaryEntry(entry: DiaryEntryEntity)

    @Delete
    suspend fun deleteDiaryEntry(entry: DiaryEntryEntity)

    // Shopping
    @Query("SELECT * FROM shopping_items WHERE familyId = :familyId ORDER BY isPurchased ASC, id DESC")
    fun getAllShoppingItems(familyId: Int): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItems(items: List<ShoppingItemEntity>)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE familyId = :familyId AND isPurchased = 1")
    suspend fun clearPurchasedShoppingItems(familyId: Int)

    // Announcements
    @Query("SELECT * FROM announcements WHERE familyId = :familyId ORDER BY id DESC")
    fun getAllAnnouncements(familyId: Int): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    // Important Dates
    @Query("SELECT * FROM important_dates WHERE familyId = :familyId ORDER BY date ASC")
    fun getAllImportantDates(familyId: Int): Flow<List<ImportantDateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportantDate(date: ImportantDateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportantDates(dates: List<ImportantDateEntity>)

    // Notifications
    @Query("SELECT * FROM app_notifications WHERE familyId = :familyId ORDER BY id DESC")
    fun getAllNotifications(familyId: Int): Flow<List<AppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<AppNotificationEntity>)

    @Update
    suspend fun updateNotification(notification: AppNotificationEntity)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE familyId = :familyId")
    suspend fun markAllNotificationsRead(familyId: Int)

    @Query("DELETE FROM app_notifications WHERE familyId = :familyId")
    suspend fun clearAllNotifications(familyId: Int)
}
