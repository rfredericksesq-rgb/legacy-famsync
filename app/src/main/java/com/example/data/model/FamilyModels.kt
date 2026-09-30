package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MemberRole {
    ADMIN,
    ADULT,
    CHILD,
    GUEST
}

enum class AgeCategory {
    ADULT,
    TEEN,
    CHILD,
    SENIOR
}

@Entity(tableName = "families")
data class FamilyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "The Williams Family",
    val inviteCode: String = "WILLIAMS-2026",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val name: String,
    val role: MemberRole,
    val ageCategory: AgeCategory,
    val avatarEmoji: String,
    val colorHex: String,
    val email: String,
    val allowsMorningBriefing: Boolean = true,
    val allowsEveningSummary: Boolean = true,
    val allowsSmartReminders: Boolean = true
)

@Entity(tableName = "family_events")
data class FamilyEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val title: String,
    val memberId: Int,
    val memberName: String,
    val date: String, // YYYY-MM-DD
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val location: String = "",
    val category: String = "Family", // Family, Work, School, Health, Sports, Travel, Birthday, Personal
    val priority: String = "Normal", // Low, Normal, High
    val isPrivate: Boolean = false,
    val reminderMinutes: Int = 15,
    val recurrence: String = "None" // None, Daily, Weekly, Weekdays, Monthly
)

@Entity(tableName = "family_tasks")
data class FamilyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val title: String,
    val assignedMemberId: Int,
    val assignedMemberName: String,
    val dueDate: String, // YYYY-MM-DD
    val dueTime: String = "19:00",
    val isCompleted: Boolean = false,
    val priority: String = "Normal", // Low, Normal, High
    val category: String = "Chores", // Chores, School, Work, Errands
    val recurrence: String = "None"
)

@Entity(tableName = "family_alarms")
data class FamilyAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val title: String,
    val time: String, // HH:mm
    val isEnabled: Boolean = true,
    val daysOfWeek: String = "Weekdays", // Everyday, Weekdays, Weekends, Monday, etc.
    val isRecurring: Boolean = true,
    val isSmartRecommendation: Boolean = false,
    val memberId: Int = 1
)

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val authorId: Int,
    val authorName: String,
    val date: String,
    val title: String,
    val text: String,
    val milestone: Boolean = false,
    val isPrivate: Boolean = false,
    val likesCount: Int = 0,
    val photoResName: String = ""
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val name: String,
    val category: String = "Groceries", // Groceries, Household, School, Pharmacy, Other
    val quantity: String = "1",
    val isPurchased: Boolean = false,
    val addedByMemberName: String = "Sarah"
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val authorName: String,
    val content: String,
    val timestamp: String,
    val heartCount: Int = 2,
    val thumbsUpCount: Int = 3,
    val checkCount: Int = 1
)

@Entity(tableName = "important_dates")
data class ImportantDateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val title: String,
    val date: String,
    val memberName: String,
    val type: String // Birthday, Anniversary, School, Holiday, Milestone
)

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: Int = 1,
    val title: String,
    val description: String,
    val timestamp: String,
    val iconType: String = "event", // event, task, alarm, announcement, birthday
    val isRead: Boolean = false
)
