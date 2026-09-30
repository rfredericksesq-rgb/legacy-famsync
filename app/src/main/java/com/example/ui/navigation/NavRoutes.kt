package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showInBottomBar: Boolean = false
) {
    TODAY("today", "Today", Icons.Filled.Today, Icons.Outlined.Today, showInBottomBar = true),
    CALENDAR("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, showInBottomBar = true),
    TASKS("tasks", "Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, showInBottomBar = true),
    FAMILY("family", "Family", Icons.Filled.Groups, Icons.Outlined.Groups, showInBottomBar = true),
    ASSISTANT("assistant", "Assistant", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, showInBottomBar = true),
    ALARMS("alarms", "Alarms", Icons.Filled.Alarm, Icons.Outlined.Alarm),
    DIARY("diary", "Diary", Icons.Filled.Book, Icons.Outlined.Book),
    SHOPPING("shopping", "Shopping", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    NOTIFICATIONS("notifications", "Notifications", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    ONBOARDING("onboarding", "Onboarding", Icons.Filled.Info, Icons.Outlined.Info)
}
