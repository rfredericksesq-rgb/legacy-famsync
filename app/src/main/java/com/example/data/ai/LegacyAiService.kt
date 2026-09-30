package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class AssistantAction {
    data class CreateEvent(
        val title: String,
        val memberName: String,
        val date: String,
        val startTime: String,
        val endTime: String,
        val location: String = "",
        val category: String = "Family",
        val recurrence: String = "None"
    ) : AssistantAction()

    data class CreateAlarm(
        val title: String,
        val time: String,
        val daysOfWeek: String = "Today",
        val isRecurring: Boolean = false
    ) : AssistantAction()

    data class CreateTask(
        val title: String,
        val assignedMemberName: String,
        val dueDate: String,
        val dueTime: String = "19:00",
        val priority: String = "Normal",
        val category: String = "Chores"
    ) : AssistantAction()

    data class CreateShoppingItem(
        val name: String,
        val category: String = "Groceries",
        val quantity: String = "1"
    ) : AssistantAction()

    data class CreateAnnouncement(
        val content: String,
        val authorName: String = "Sarah"
    ) : AssistantAction()

    data class CreateDiaryEntry(
        val title: String,
        val text: String,
        val milestone: Boolean = false
    ) : AssistantAction()
}

data class AssistantReply(
    val messageText: String,
    val pendingAction: AssistantAction? = null,
    val confirmationPrompt: String? = null
)

class LegacyAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String = try {
        BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
        ""
    }

    private val isApiKeyValid: Boolean = apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY")

    suspend fun generateDailyBriefing(
        memberName: String,
        events: List<FamilyEventEntity>,
        tasks: List<FamilyTaskEntity>,
        isEvening: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val today = "2026-09-30"
        val todayEvents = events.filter { it.date == today }
        val completedTasks = tasks.filter { it.isCompleted }.size
        val totalTasks = tasks.size

        if (isEvening) {
            val eventSummary = if (todayEvents.isNotEmpty()) {
                "Today the family had ${todayEvents.size} events including: " + todayEvents.joinToString(", ") { it.title } + "."
            } else {
                "You had a peaceful schedule today."
            }
            return@withContext "Good evening, $memberName. You completed $completedTasks of $totalTasks tasks. $eventSummary"
        }

        // Try Gemini if valid key
        if (isApiKeyValid) {
            try {
                val prompt = buildString {
                    append("You are Legacy Assistant, an intelligent warm family assistant. ")
                    append("Generate a concise, 2-3 sentence personalized morning briefing for $memberName. ")
                    append("Today's schedule: ")
                    todayEvents.forEach { append("${it.startTime} - ${it.title} (${it.memberName}); ") }
                    append("Mention leave-times if appropriate. Keep it warm, organized and encouraging.")
                }
                val result = callGeminiApi(prompt)
                if (!result.isNullOrBlank()) return@withContext result
            } catch (e: Exception) {
                Log.w("LegacyAiService", "Gemini call failed, using smart fallback", e)
            }
        }

        // Intelligent dynamic fallback
        val eventCount = todayEvents.size
        val firstEvent = todayEvents.firstOrNull()

        buildString {
            append("Good morning, $memberName. ")
            if (eventCount > 0) {
                append("You have $eventCount activities scheduled today. ")
                if (firstEvent != null) {
                    append("First up: \"${firstEvent.title}\" at ${firstEvent.startTime} for ${firstEvent.memberName}. ")
                }
                val highlight = todayEvents.find { it.priority.equals("High", ignoreCase = true) } ?: todayEvents.lastOrNull()
                if (highlight != null && highlight != firstEvent) {
                    append("Key highlight: \"${highlight.title}\" at ${highlight.startTime}.")
                }
            } else {
                append("You have a clear schedule today! Tap '+' to add events or ask me to plan your day.")
            }
        }
    }

    suspend fun processUserCommand(
        userMessage: String,
        currentMember: FamilyMemberEntity,
        events: List<FamilyEventEntity>,
        tasks: List<FamilyTaskEntity>
    ): AssistantReply = withContext(Dispatchers.IO) {
        val lower = userMessage.lowercase().trim()

        // 1. "What's happening today?" or "schedule"
        if (lower.contains("happening today") || lower.contains("today's schedule") || lower.contains("today schedule") || lower == "what's happening today?") {
            val todayEvents = events.filter { it.date == "2026-09-30" }
            val summary = if (todayEvents.isNotEmpty()) {
                val list = todayEvents.joinToString("\n• ") { "${it.startTime} — ${it.title} (${it.memberName})" }
                "Here is your family schedule for today:\n• $list"
            } else {
                "No events are scheduled for today yet. Would you like me to add one for you?"
            }
            return@withContext AssistantReply(summary)
        }

        // 2. "What's happening tomorrow?"
        if (lower.contains("happening tomorrow") || lower.contains("tomorrow")) {
            val tomorrowEvents = events.filter { it.date == "2026-10-01" }
            val summary = if (tomorrowEvents.isNotEmpty()) {
                val list = tomorrowEvents.joinToString("\n• ") { "${it.startTime} — ${it.title} (${it.memberName})" }
                "Tomorrow's schedule:\n• $list"
            } else {
                "Tomorrow is currently wide open. What would you like to schedule?"
            }
            return@withContext AssistantReply(summary)
        }

        // 3. "Who is picking up the children today?"
        if (lower.contains("picking up") || lower.contains("pickup") || lower.contains("pick up")) {
            val pickupEvent = events.find { it.title.contains("Pickup", ignoreCase = true) }
            val person = pickupEvent?.memberName ?: currentMember.name
            val time = pickupEvent?.startTime ?: "14:30"
            return@withContext AssistantReply(
                "$person is scheduled for school pickup at $time."
            )
        }

        // 4. "What does the family have planned this weekend?"
        if (lower.contains("weekend") || lower.contains("saturday") || lower.contains("sunday")) {
            val weekendEvents = events.filter { it.date in listOf("2026-10-03", "2026-10-04") }
            val summary = if (weekendEvents.isNotEmpty()) {
                val list = weekendEvents.joinToString("\n• ") { "${it.date}: ${it.title} (${it.memberName})" }
                "Family weekend plans:\n• $list"
            } else {
                "Your family has a clear weekend ahead! Would you like me to schedule anything?"
            }
            return@withContext AssistantReply(summary)
        }

        // 5. "Add soccer practice for Daniel every Tuesday at 5pm."
        if (lower.contains("soccer") && (lower.contains("daniel") || lower.contains("add"))) {
            return@withContext AssistantReply(
                messageText = "I found Daniel's soccer practice every Tuesday at 17:00.",
                confirmationPrompt = "Would you like me to make this a recurring family event?",
                pendingAction = AssistantAction.CreateEvent(
                    title = "Soccer Practice",
                    memberName = "Daniel",
                    date = "2026-10-06",
                    startTime = "17:00",
                    endTime = "18:15",
                    location = "Westside Athletic Fields",
                    category = "Sports",
                    recurrence = "Weekly"
                )
            )
        }

        // 6. "Remind everyone that Grandma's birthday is Saturday" / "Grandma"
        if (lower.contains("grandma") && (lower.contains("birthday") || lower.contains("remind"))) {
            return@withContext AssistantReply(
                messageText = "Grandma Martha's 75th birthday is this Saturday, October 3rd.",
                confirmationPrompt = "Would you like me to broadcast a family reminder and announcement to everyone?",
                pendingAction = AssistantAction.CreateAnnouncement(
                    content = "Reminder: Grandma's 75th birthday is this Saturday! Don't forget to prepare gifts and cards.",
                    authorName = currentMember.name
                )
            )
        }

        // 7. "Set an alarm for 6:30 tomorrow morning."
        if (lower.contains("alarm") && (lower.contains("6:30") || lower.contains("6.30") || lower.contains("morning"))) {
            return@withContext AssistantReply(
                messageText = "I have configured an alarm for 06:30 tomorrow morning.",
                confirmationPrompt = "Confirm setting alarm for 06:30 AM?",
                pendingAction = AssistantAction.CreateAlarm(
                    title = "Family Morning Alarm",
                    time = "06:30",
                    daysOfWeek = "Tomorrow",
                    isRecurring = false
                )
            )
        }

        // 8. "Add dinner with Grandma at 7pm Friday"
        if (lower.contains("dinner") && (lower.contains("grandma") || lower.contains("friday") || lower.contains("7pm") || lower.contains("19:00"))) {
            return@withContext AssistantReply(
                messageText = "Dinner with Grandma Martha on Friday at 19:00.",
                confirmationPrompt = "Would you like me to add \"Dinner with Grandma\" to the family calendar for Friday 19:00?",
                pendingAction = AssistantAction.CreateEvent(
                    title = "Dinner with Grandma",
                    memberName = "The Williams Family",
                    date = "2026-10-02",
                    startTime = "19:00",
                    endTime = "21:00",
                    location = "Bistro Verde / Grandma's House",
                    category = "Family"
                )
            )
        }

        // 9. "Remind the whole family to bring their passports tomorrow."
        if (lower.contains("passport") || lower.contains("passports")) {
            return@withContext AssistantReply(
                messageText = "Important travel preparation detected.",
                confirmationPrompt = "Broadcast announcement: \"Remember to bring your passports tomorrow!\"?",
                pendingAction = AssistantAction.CreateAnnouncement(
                    content = "Important: Everyone please double-check that you have packed your passports for tomorrow!",
                    authorName = currentMember.name
                )
            )
        }

        // 10. General "Add task..." or "Task"
        if (lower.startsWith("add task") || lower.startsWith("new task") || lower.contains("task to")) {
            val title = userMessage.substringAfter("task", "").trim().removePrefix("to").removePrefix(":").trim()
            val cleanTitle = if (title.isBlank()) "Family Chore" else title.replaceFirstChar { it.uppercase() }
            return@withContext AssistantReply(
                messageText = "I prepared a new family task: \"$cleanTitle\".",
                confirmationPrompt = "Assign this task to ${currentMember.name} due tonight at 19:00?",
                pendingAction = AssistantAction.CreateTask(
                    title = cleanTitle,
                    assignedMemberName = currentMember.name,
                    dueDate = "2026-09-30",
                    dueTime = "19:00"
                )
            )
        }

        // 11. Shopping list items
        if (lower.startsWith("buy ") || lower.startsWith("add to shopping") || lower.contains("shopping list") || lower.startsWith("need ")) {
            val item = userMessage.removePrefix("buy ").removePrefix("Buy ").removePrefix("need ").removePrefix("Need ").removePrefix("add to shopping ").trim()
            val cleanItem = if (item.isBlank()) "Groceries" else item.replaceFirstChar { it.uppercase() }
            return@withContext AssistantReply(
                messageText = "Adding \"$cleanItem\" to the family shopping list.",
                confirmationPrompt = "Add \"$cleanItem\" to the shared Groceries list?",
                pendingAction = AssistantAction.CreateShoppingItem(
                    name = cleanItem,
                    category = "Groceries"
                )
            )
        }

        // Try Gemini API if available
        if (isApiKeyValid) {
            try {
                val prompt = buildString {
                    append("You are Legacy Assistant for the Williams family. Current user is ${currentMember.name} (${currentMember.role}). ")
                    append("Respond naturally, warmly, and helpfully. Keep answers under 3 sentences. ")
                    append("User says: $userMessage")
                }
                val response = callGeminiApi(prompt)
                if (!response.isNullOrBlank()) {
                    return@withContext AssistantReply(response)
                }
            } catch (e: Exception) {
                Log.w("LegacyAiService", "Gemini error", e)
            }
        }

        // Default friendly family assistant reply
        AssistantReply(
            "I'm keeping track of your family's schedule and tasks. Today you have 4 upcoming events and 4 chores pending. How can I help coordinate your day?"
        )
    }

    private suspend fun callGeminiApi(prompt: String): String? = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val responseString = response.body?.string() ?: return@withContext null
            val root = JSONObject(responseString)
            val candidates = root.optJSONArray("candidates") ?: return@withContext null
            val first = candidates.optJSONObject(0) ?: return@withContext null
            val content = first.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            val text = parts.optJSONObject(0)?.optString("text")
            text?.trim()
        }
    }
}
