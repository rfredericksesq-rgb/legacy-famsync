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
    version = 4,
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
                    .fallbackToDestructiveMigration()
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
            // Initialize with custom family ready for user to configure
            dao.insertFamily(
                FamilyEntity(
                    id = 1,
                    name = "My Family",
                    inviteCode = "FAM-" + (1000..9999).random()
                )
            )
            dao.insertMember(
                FamilyMemberEntity(
                    id = 1,
                    familyId = 1,
                    name = "Family Admin",
                    role = MemberRole.ADMIN,
                    ageCategory = AgeCategory.ADULT,
                    avatarEmoji = "👑",
                    colorHex = "#2563EB",
                    email = ""
                )
            )
        }
    }
}
