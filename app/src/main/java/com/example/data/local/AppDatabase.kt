package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FamilyEntity::class,
        FamilyMemberEntity::class,
        FamilyEventEntity::class,
        FamilyTaskEntity::class,
        FamilyAlarmEntity::class,
        DiaryEntryEntity::class,
        ShoppingItemEntity::class,
        AnnouncementEntity::class,
        ImportantDateEntity::class,
        AppNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "legacy_famsync.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.familyDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: FamilyDao) {
            // 1. Family
            val familyId = dao.insertFamily(
                FamilyEntity(
                    id = 1,
                    name = "The Williams Family",
                    inviteCode = "WILLIAMS-2026"
                )
            ).toInt()

            // 2. Members
            val members = listOf(
                FamilyMemberEntity(
                    id = 1,
                    familyId = 1,
                    name = "Sarah",
                    role = MemberRole.ADMIN,
                    ageCategory = AgeCategory.ADULT,
                    avatarEmoji = "👩",
                    colorHex = "#2563EB",
                    email = "sarah.williams@legacy.com"
                ),
                FamilyMemberEntity(
                    id = 2,
                    familyId = 1,
                    name = "Mark",
                    role = MemberRole.ADULT,
                    ageCategory = AgeCategory.ADULT,
                    avatarEmoji = "👨",
                    colorHex = "#0D9488",
                    email = "mark.williams@legacy.com"
                ),
                FamilyMemberEntity(
                    id = 3,
                    familyId = 1,
                    name = "Daniel",
                    role = MemberRole.CHILD,
                    ageCategory = AgeCategory.CHILD,
                    avatarEmoji = "👦",
                    colorHex = "#D97706",
                    email = "daniel.w@family.net"
                ),
                FamilyMemberEntity(
                    id = 4,
                    familyId = 1,
                    name = "Emily",
                    role = MemberRole.CHILD,
                    ageCategory = AgeCategory.CHILD,
                    avatarEmoji = "👧",
                    colorHex = "#E11D48",
                    email = "emily.w@family.net"
                ),
                FamilyMemberEntity(
                    id = 5,
                    familyId = 1,
                    name = "Grandma Martha",
                    role = MemberRole.GUEST,
                    ageCategory = AgeCategory.SENIOR,
                    avatarEmoji = "👵",
                    colorHex = "#7C3AED",
                    email = "martha.w@legacy.com"
                )
            )
            dao.insertMembers(members)

            // 3. Events for Today (2026-09-30) and upcoming
            val today = "2026-09-30"
            val tomorrow = "2026-10-01"
            val friday = "2026-10-02"

            val events = listOf(
                FamilyEventEntity(
                    id = 1,
                    familyId = 1,
                    title = "Start the day & Breakfast",
                    memberId = 1,
                    memberName = "Family",
                    date = today,
                    startTime = "07:00",
                    endTime = "07:45",
                    location = "Kitchen",
                    category = "Family",
                    priority = "Normal"
                ),
                FamilyEventEntity(
                    id = 2,
                    familyId = 1,
                    title = "School Drop-off",
                    memberId = 3,
                    memberName = "Daniel & Emily",
                    date = today,
                    startTime = "08:00",
                    endTime = "08:30",
                    location = "Oakridge Elementary",
                    category = "School",
                    priority = "High",
                    reminderMinutes = 15
                ),
                FamilyEventEntity(
                    id = 3,
                    familyId = 1,
                    title = "Product Strategy Meeting",
                    memberId = 1,
                    memberName = "Sarah",
                    date = today,
                    startTime = "10:30",
                    endTime = "11:45",
                    location = "Design Studio / Zoom",
                    category = "Work",
                    priority = "High",
                    reminderMinutes = 10
                ),
                FamilyEventEntity(
                    id = 4,
                    familyId = 1,
                    title = "School Pickup",
                    memberId = 2,
                    memberName = "Mark",
                    date = today,
                    startTime = "14:30",
                    endTime = "15:00",
                    location = "Oakridge Elementary",
                    category = "School",
                    priority = "High",
                    reminderMinutes = 20
                ),
                FamilyEventEntity(
                    id = 5,
                    familyId = 1,
                    title = "Soccer Practice",
                    memberId = 3,
                    memberName = "Daniel",
                    date = today,
                    startTime = "17:00",
                    endTime = "18:15",
                    location = "Westside Athletic Fields",
                    category = "Sports",
                    priority = "High",
                    reminderMinutes = 30,
                    recurrence = "Weekly"
                ),
                FamilyEventEntity(
                    id = 6,
                    familyId = 1,
                    title = "Family Dinner",
                    memberId = 1,
                    memberName = "Family",
                    date = today,
                    startTime = "18:30",
                    endTime = "19:30",
                    location = "Dining Room",
                    category = "Family",
                    priority = "Normal"
                ),
                FamilyEventEntity(
                    id = 7,
                    familyId = 1,
                    title = "Homework & Reading",
                    memberId = 4,
                    memberName = "Emily",
                    date = today,
                    startTime = "20:00",
                    endTime = "20:45",
                    location = "Study Room",
                    category = "School",
                    priority = "Normal"
                ),
                FamilyEventEntity(
                    id = 8,
                    familyId = 1,
                    title = "Bedtime Routine",
                    memberId = 1,
                    memberName = "Family",
                    date = today,
                    startTime = "21:00",
                    endTime = "21:30",
                    location = "Home",
                    category = "Family",
                    priority = "Normal"
                ),
                // Potential conflict example:
                FamilyEventEntity(
                    id = 9,
                    familyId = 1,
                    title = "Doctor Appointment",
                    memberId = 1,
                    memberName = "Sarah",
                    date = friday,
                    startTime = "15:00",
                    endTime = "16:00",
                    location = "City Medical Clinic",
                    category = "Health",
                    priority = "High"
                ),
                FamilyEventEntity(
                    id = 10,
                    familyId = 1,
                    title = "Friday Early School Pickup",
                    memberId = 1,
                    memberName = "Daniel & Emily",
                    date = friday,
                    startTime = "15:00",
                    endTime = "15:30",
                    location = "Oakridge Elementary",
                    category = "School",
                    priority = "High"
                )
            )
            dao.insertEvents(events)

            // 4. Tasks & Chores
            val tasks = listOf(
                FamilyTaskEntity(
                    id = 1,
                    familyId = 1,
                    title = "Take out rubbish & recycling bins",
                    assignedMemberId = 3,
                    assignedMemberName = "Daniel",
                    dueDate = today,
                    dueTime = "19:00",
                    isCompleted = false,
                    priority = "High",
                    category = "Chores",
                    recurrence = "Weekly"
                ),
                FamilyTaskEntity(
                    id = 2,
                    familyId = 1,
                    title = "Clean and tidy bedroom",
                    assignedMemberId = 4,
                    assignedMemberName = "Emily",
                    dueDate = today,
                    dueTime = "16:00",
                    isCompleted = false,
                    priority = "Normal",
                    category = "Chores"
                ),
                FamilyTaskEntity(
                    id = 3,
                    familyId = 1,
                    title = "Feed pets & refill water bowls",
                    assignedMemberId = 4,
                    assignedMemberName = "Emily",
                    dueDate = today,
                    dueTime = "08:30",
                    isCompleted = true,
                    priority = "Normal",
                    category = "Chores",
                    recurrence = "Daily"
                ),
                FamilyTaskEntity(
                    id = 4,
                    familyId = 1,
                    title = "Wash and put away dinner dishes",
                    assignedMemberId = 3,
                    assignedMemberName = "Daniel",
                    dueDate = today,
                    dueTime = "20:30",
                    isCompleted = false,
                    priority = "Normal",
                    category = "Chores"
                ),
                FamilyTaskEntity(
                    id = 5,
                    familyId = 1,
                    title = "Review science project rubric",
                    assignedMemberId = 1,
                    assignedMemberName = "Sarah",
                    dueDate = tomorrow,
                    dueTime = "17:00",
                    isCompleted = false,
                    priority = "Normal",
                    category = "School"
                ),
                FamilyTaskEntity(
                    id = 6,
                    familyId = 1,
                    title = "Car tire pressure check",
                    assignedMemberId = 2,
                    assignedMemberName = "Mark",
                    dueDate = today,
                    dueTime = "12:00",
                    isCompleted = true,
                    priority = "Low",
                    category = "Errands"
                )
            )
            dao.insertTasks(tasks)

            // 5. Alarms & Reminders
            val alarms = listOf(
                FamilyAlarmEntity(
                    id = 1,
                    familyId = 1,
                    title = "Weekday Family Wake-up",
                    time = "06:30",
                    isEnabled = true,
                    daysOfWeek = "Weekdays",
                    isRecurring = true
                ),
                FamilyAlarmEntity(
                    id = 2,
                    familyId = 1,
                    title = "Leave for Soccer Practice",
                    time = "16:30",
                    isEnabled = true,
                    daysOfWeek = "Today",
                    isRecurring = false
                ),
                FamilyAlarmEntity(
                    id = 3,
                    familyId = 1,
                    title = "Take rubbish bins out tonight",
                    time = "19:00",
                    isEnabled = true,
                    daysOfWeek = "Wednesdays",
                    isRecurring = true
                ),
                FamilyAlarmEntity(
                    id = 4,
                    familyId = 1,
                    title = "School trip bags reminder (Smart Suggestion)",
                    time = "20:00",
                    isEnabled = false,
                    daysOfWeek = "Tonight",
                    isRecurring = false,
                    isSmartRecommendation = true
                )
            )
            dao.insertAlarms(alarms)

            // 6. Shopping items
            val shopping = listOf(
                ShoppingItemEntity(id = 1, familyId = 1, name = "Whole Milk (2 Gallons)", category = "Groceries", quantity = "2", isPurchased = false, addedByMemberName = "Sarah"),
                ShoppingItemEntity(id = 2, familyId = 1, name = "Whole Wheat Sliced Bread", category = "Groceries", quantity = "1", isPurchased = false, addedByMemberName = "Mark"),
                ShoppingItemEntity(id = 3, familyId = 1, name = "Pasture-Raised Eggs", category = "Groceries", quantity = "1 Dozen", isPurchased = false, addedByMemberName = "Sarah"),
                ShoppingItemEntity(id = 4, familyId = 1, name = "Organic Bananas & Apples", category = "Groceries", quantity = "1 Bag", isPurchased = false, addedByMemberName = "Emily"),
                ShoppingItemEntity(id = 5, familyId = 1, name = "Chicken Breasts", category = "Groceries", quantity = "1.5 kg", isPurchased = true, addedByMemberName = "Sarah"),
                ShoppingItemEntity(id = 6, familyId = 1, name = "Toilet Paper & Paper Towels", category = "Household", quantity = "1 Pack", isPurchased = false, addedByMemberName = "Mark"),
                ShoppingItemEntity(id = 7, familyId = 1, name = "College Ruled Notebooks", category = "School", quantity = "3", isPurchased = true, addedByMemberName = "Daniel"),
                ShoppingItemEntity(id = 8, familyId = 1, name = "Kids Chewable Vitamin C", category = "Pharmacy", quantity = "1 Bottle", isPurchased = false, addedByMemberName = "Sarah")
            )
            dao.insertShoppingItems(shopping)

            // 7. Diary Entries
            val diary = listOf(
                DiaryEntryEntity(
                    id = 1,
                    familyId = 1,
                    authorId = 1,
                    authorName = "Sarah",
                    date = today,
                    title = "Daniel's Soccer Milestone ⚽",
                    text = "Daniel scored his first goal at soccer today! The whole team cheered him on and Coach Ramirez gave him the game ball. Such a proud moment for our family!",
                    milestone = true,
                    likesCount = 4
                ),
                DiaryEntryEntity(
                    id = 2,
                    familyId = 1,
                    authorId = 2,
                    authorName = "Mark",
                    date = "2026-09-24",
                    title = "Emily's Science Volcano Exhibition 🌋",
                    text = "Emily got 100% on her science project exhibition. The clay volcano with baking soda and red food coloring was a total crowd favorite!",
                    milestone = true,
                    likesCount = 5
                ),
                DiaryEntryEntity(
                    id = 3,
                    familyId = 1,
                    authorId = 1,
                    authorName = "Sarah",
                    date = "2026-09-18",
                    title = "Pine Ridge Trail Family Hike 🌲",
                    text = "Spent Saturday morning on the mountain trail. Grandma Martha walked 3 full miles with us! The fresh pine air and sunny picnic was unforgettable.",
                    milestone = false,
                    likesCount = 6
                )
            )
            dao.insertDiaryEntries(diary)

            // 8. Announcements
            val announcements = listOf(
                AnnouncementEntity(
                    id = 1,
                    familyId = 1,
                    authorName = "Sarah (Mom)",
                    content = "Dinner will be at 18:30 tonight — homemade lasagna! Please have hands washed and homework packed up by 18:25.",
                    timestamp = "10:15 AM",
                    heartCount = 4,
                    thumbsUpCount = 3,
                    checkCount = 2
                )
            )
            dao.insertAnnouncements(announcements)

            // 9. Important Dates
            val importantDates = listOf(
                ImportantDateEntity(
                    id = 1,
                    familyId = 1,
                    title = "Grandma Martha's 75th Birthday 🎂",
                    date = "2026-10-03",
                    memberName = "Grandma Martha",
                    type = "Birthday"
                ),
                ImportantDateEntity(
                    id = 2,
                    familyId = 1,
                    title = "Emily's 9th Birthday 🎈",
                    date = "2026-10-18",
                    memberName = "Emily",
                    type = "Birthday"
                ),
                ImportantDateEntity(
                    id = 3,
                    familyId = 1,
                    title = "Mom & Dad's 14th Anniversary 💍",
                    date = "2026-11-12",
                    memberName = "Sarah & Mark",
                    type = "Anniversary"
                ),
                ImportantDateEntity(
                    id = 4,
                    familyId = 1,
                    title = "Daniel's Karate Blue Belt Test 🥋",
                    date = "2026-10-25",
                    memberName = "Daniel",
                    type = "Milestone"
                )
            )
            dao.insertImportantDates(importantDates)

            // 10. Notifications
            val notifications = listOf(
                AppNotificationEntity(
                    id = 1,
                    familyId = 1,
                    title = "New Family Event: Soccer Practice",
                    description = "Daniel has soccer practice today at 17:00 at Westside Fields.",
                    timestamp = "07:30 AM",
                    iconType = "event",
                    isRead = false
                ),
                AppNotificationEntity(
                    id = 2,
                    familyId = 1,
                    title = "Task Assigned: Take out rubbish",
                    description = "Assigned to Daniel. Due tonight by 19:00.",
                    timestamp = "08:00 AM",
                    iconType = "task",
                    isRead = false
                ),
                AppNotificationEntity(
                    id = 3,
                    familyId = 1,
                    title = "Birthday Approaching: Grandma Martha",
                    description = "Grandma Martha's birthday is in 3 days (Oct 3rd). Tap to prepare a family card or gift.",
                    timestamp = "08:15 AM",
                    iconType = "birthday",
                    isRead = false
                ),
                AppNotificationEntity(
                    id = 4,
                    familyId = 1,
                    title = "Announcement: Family Dinner",
                    description = "Sarah: Dinner is at 18:30 tonight (Lasagna).",
                    timestamp = "10:15 AM",
                    iconType = "announcement",
                    isRead = true
                )
            )
            dao.insertNotifications(notifications)
        }
    }
}
