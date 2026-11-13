package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "study_time_prefs")

enum class TimerMode { STUDY, BREAK }
enum class TimerStatus { IDLE, RUNNING, PAUSED }

data class PersistedPreferences(
    val language: AppLanguage = AppLanguage.EN,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val dailyGoalMinutes: Int = 120,
    val totalCoins: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastStudyDateKey: String = "",
    val unlockedBadges: Set<String> = emptySet(),
    val timerMode: TimerMode = TimerMode.STUDY,
    val timerStatus: TimerStatus = TimerStatus.IDLE,
    val timerSubjectId: Long = 1L,
    val timerAccumulatedMs: Long = 0L,
    val timerLastResumedEpochMs: Long = 0L,
    val timerStartedAtEpochMs: Long = 0L,
    val breakTargetSeconds: Int = 600
)

class PreferencesStore(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val TOTAL_COINS = intPreferencesKey("total_coins")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val BEST_STREAK = intPreferencesKey("best_streak")
        val LAST_STUDY_DATE_KEY = stringPreferencesKey("last_study_date_key")
        val UNLOCKED_BADGES = stringPreferencesKey("unlocked_badges")

        val TIMER_MODE = stringPreferencesKey("timer_mode")
        val TIMER_STATUS = stringPreferencesKey("timer_status")
        val TIMER_SUBJECT_ID = longPreferencesKey("timer_subject_id")
        val TIMER_ACCUMULATED_MS = longPreferencesKey("timer_accumulated_ms")
        val TIMER_LAST_RESUMED_MS = longPreferencesKey("timer_last_resumed_ms")
        val TIMER_STARTED_AT_MS = longPreferencesKey("timer_started_at_ms")
        val BREAK_TARGET_SECONDS = intPreferencesKey("break_target_seconds")
    }

    val preferencesFlow: Flow<PersistedPreferences> = context.dataStore.data.map { prefs ->
        val lang = prefs[Keys.LANGUAGE]?.let { code ->
            AppLanguage.entries.firstOrNull { it.code == code }
        } ?: AppLanguage.EN

        val theme = prefs[Keys.THEME_MODE]?.let { name ->
            runCatching { ThemeMode.valueOf(name) }.getOrNull()
        } ?: ThemeMode.LIGHT

        val timerMode = prefs[Keys.TIMER_MODE]?.let { name ->
            runCatching { TimerMode.valueOf(name) }.getOrNull()
        } ?: TimerMode.STUDY

        val timerStatus = prefs[Keys.TIMER_STATUS]?.let { name ->
            runCatching { TimerStatus.valueOf(name) }.getOrNull()
        } ?: TimerStatus.IDLE

        val badges = prefs[Keys.UNLOCKED_BADGES]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet() ?: emptySet()

        PersistedPreferences(
            language = lang,
            themeMode = theme,
            dailyGoalMinutes = (prefs[Keys.DAILY_GOAL_MINUTES] ?: 120).coerceIn(15, 720),
            totalCoins = (prefs[Keys.TOTAL_COINS] ?: 0).coerceAtLeast(0),
            currentStreak = (prefs[Keys.CURRENT_STREAK] ?: 0).coerceAtLeast(0),
            bestStreak = (prefs[Keys.BEST_STREAK] ?: 0).coerceAtLeast(0),
            lastStudyDateKey = prefs[Keys.LAST_STUDY_DATE_KEY] ?: "",
            unlockedBadges = badges,
            timerMode = timerMode,
            timerStatus = timerStatus,
            timerSubjectId = prefs[Keys.TIMER_SUBJECT_ID] ?: 1L,
            timerAccumulatedMs = (prefs[Keys.TIMER_ACCUMULATED_MS] ?: 0L).coerceAtLeast(0L),
            timerLastResumedEpochMs = (prefs[Keys.TIMER_LAST_RESUMED_MS] ?: 0L).coerceAtLeast(0L),
            timerStartedAtEpochMs = (prefs[Keys.TIMER_STARTED_AT_MS] ?: 0L).coerceAtLeast(0L),
            breakTargetSeconds = (prefs[Keys.BREAK_TARGET_SECONDS] ?: 600).coerceIn(60, 3600)
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_MINUTES] = minutes.coerceIn(15, 720) }
    }

    suspend fun addCoins(delta: Int) {
        if (delta <= 0) return
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.TOTAL_COINS] ?: 0
            prefs[Keys.TOTAL_COINS] = current + delta
        }
    }

    suspend fun updateStreakAndBadges(
        currentStreak: Int,
        bestStreak: Int,
        lastStudyDateKey: String,
        unlockedBadges: Set<String>
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENT_STREAK] = currentStreak
            prefs[Keys.BEST_STREAK] = bestStreak
            prefs[Keys.LAST_STUDY_DATE_KEY] = lastStudyDateKey
            prefs[Keys.UNLOCKED_BADGES] = unlockedBadges.joinToString(",")
        }
    }

    suspend fun updateUnlockedBadges(unlockedBadges: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNLOCKED_BADGES] = unlockedBadges.joinToString(",")
        }
    }

    suspend fun saveTimerSnapshot(
        mode: TimerMode,
        status: TimerStatus,
        subjectId: Long,
        accumulatedMs: Long,
        lastResumedEpochMs: Long,
        startedAtEpochMs: Long,
        breakTargetSeconds: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TIMER_MODE] = mode.name
            prefs[Keys.TIMER_STATUS] = status.name
            prefs[Keys.TIMER_SUBJECT_ID] = subjectId
            prefs[Keys.TIMER_ACCUMULATED_MS] = accumulatedMs
            prefs[Keys.TIMER_LAST_RESUMED_MS] = lastResumedEpochMs
            prefs[Keys.TIMER_STARTED_AT_MS] = startedAtEpochMs
            prefs[Keys.BREAK_TARGET_SECONDS] = breakTargetSeconds
        }
    }
}
