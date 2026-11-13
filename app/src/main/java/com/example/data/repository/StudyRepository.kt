package com.example.data.repository

import androidx.compose.ui.graphics.Color
import com.example.data.local.BadgeId
import com.example.data.local.GameScoreEntity
import com.example.data.local.GameType
import com.example.data.local.PersistedPreferences
import com.example.data.local.PreferencesStore
import com.example.data.local.ReportBucket
import com.example.data.local.ReportPeriod
import com.example.data.local.StudyDao
import com.example.data.local.StudySessionEntity
import com.example.data.local.SubjectBreakdownItem
import com.example.data.local.SubjectEntity
import com.example.data.local.TimerMode
import com.example.data.local.TimerStatus
import com.example.i18n.AppLanguage
import com.example.ui.theme.ThemeMode
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.math.max

class StudyRepository(
    private val dao: StudyDao,
    private val prefs: PreferencesStore
) {
    val subjectsFlow: Flow<List<SubjectEntity>> = dao.observeAllSubjects()
    val sessionsFlow: Flow<List<StudySessionEntity>> = dao.observeAllSessions()
    val gameScoresFlow: Flow<List<GameScoreEntity>> = dao.observeAllGameScores()
    val preferencesFlow: Flow<PersistedPreferences> = prefs.preferencesFlow

    suspend fun ensureDefaultSubjectsSeeded() {
        if (dao.getSubjectCount() == 0) {
            dao.insertSubjects(
                listOf(
                    SubjectEntity(
                        nameEn = "Mathematics",
                        nameHi = "गणित",
                        colorHex = 0xFF2563EB,
                        iconKey = "math",
                        dailyGoalMinutes = 45
                    ),
                    SubjectEntity(
                        nameEn = "Science",
                        nameHi = "विज्ञान",
                        colorHex = 0xFF7C3AED,
                        iconKey = "science",
                        dailyGoalMinutes = 30
                    ),
                    SubjectEntity(
                        nameEn = "English",
                        nameHi = "अंग्रेज़ी",
                        colorHex = 0xFF06B6D4,
                        iconKey = "book",
                        dailyGoalMinutes = 30
                    ),
                    SubjectEntity(
                        nameEn = "Hindi",
                        nameHi = "हिन्दी",
                        colorHex = 0xFFF59E0B,
                        iconKey = "language",
                        dailyGoalMinutes = 25
                    ),
                    SubjectEntity(
                        nameEn = "Coding",
                        nameHi = "कोडिंग",
                        colorHex = 0xFF10B981,
                        iconKey = "code",
                        dailyGoalMinutes = 30
                    ),
                    SubjectEntity(
                        nameEn = "History",
                        nameHi = "इतिहास",
                        colorHex = 0xFFEC4899,
                        iconKey = "history",
                        dailyGoalMinutes = 20
                    )
                )
            )
        }
    }

    suspend fun addSubject(name: String, colorHex: Long, dailyGoalMinutes: Int): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return -1L
        return dao.insertSubject(
            SubjectEntity(
                nameEn = trimmed,
                nameHi = trimmed,
                colorHex = colorHex,
                iconKey = "custom",
                dailyGoalMinutes = dailyGoalMinutes.coerceIn(5, 480),
                isCustom = true
            )
        )
    }

    suspend fun deleteCustomSubject(subjectId: Long) {
        dao.deleteCustomSubject(subjectId)
    }

    suspend fun setLanguage(language: AppLanguage) = prefs.setLanguage(language)
    suspend fun setThemeMode(themeMode: ThemeMode) = prefs.setThemeMode(themeMode)
    suspend fun setDailyGoalMinutes(minutes: Int) = prefs.setDailyGoalMinutes(minutes)

    suspend fun saveTimerSnapshot(
        mode: TimerMode,
        status: TimerStatus,
        subjectId: Long,
        accumulatedMs: Long,
        lastResumedEpochMs: Long,
        startedAtEpochMs: Long,
        breakTargetSeconds: Int
    ) {
        prefs.saveTimerSnapshot(
            mode = mode,
            status = status,
            subjectId = subjectId,
            accumulatedMs = accumulatedMs,
            lastResumedEpochMs = lastResumedEpochMs,
            startedAtEpochMs = startedAtEpochMs,
            breakTargetSeconds = breakTargetSeconds
        )
    }

    suspend fun recordCompletedSession(
        subject: SubjectEntity?,
        durationSeconds: Long,
        isBreak: Boolean,
        startedAtEpochMs: Long,
        endedAtEpochMs: Long = System.currentTimeMillis()
    ): Int {
        if (durationSeconds < 5L) return 0

        val today = DateUtils.todayKey(endedAtEpochMs)
        val coins = if (isBreak) {
            2 // Small reward for taking a healthy break
        } else {
            val mins = (durationSeconds / 60L).toInt()
            max(5, mins * 2)
        }

        val session = StudySessionEntity(
            subjectId = if (isBreak) -1L else (subject?.id ?: 1L),
            subjectNameEn = if (isBreak) "Break" else (subject?.nameEn ?: "General"),
            subjectNameHi = if (isBreak) "ब्रेक" else (subject?.nameHi ?: "सामान्य"),
            subjectColorHex = if (isBreak) 0xFF06B6D4 else (subject?.colorHex ?: 0xFF2563EB),
            durationSeconds = durationSeconds,
            coinsEarned = coins,
            isBreak = isBreak,
            startedAtEpochMs = if (startedAtEpochMs > 0L) startedAtEpochMs else endedAtEpochMs - (durationSeconds * 1000L),
            endedAtEpochMs = endedAtEpochMs,
            dateKey = today
        )
        dao.insertSession(session)

        val currentPrefs = prefs.preferencesFlow.first()
        var bonusCoins = 0

        if (!isBreak) {
            // Recompute today's study total including this new session
            val allSessions = dao.observeAllSessions().first()
            val studySessions = allSessions.filter { !it.isBreak }
            val todayTotalSeconds = studySessions.filter { it.dateKey == today }.sumOf { it.durationSeconds }
            val goalSeconds = currentPrefs.dailyGoalMinutes * 60L

            val yesterdayKey = DateUtils.dateKeyDaysAgo(1, endedAtEpochMs)
            val newStreak = when (currentPrefs.lastStudyDateKey) {
                today -> max(1, currentPrefs.currentStreak)
                yesterdayKey -> currentPrefs.currentStreak + 1
                else -> 1
            }
            val newBestStreak = max(currentPrefs.bestStreak, newStreak)

            // Daily goal bonus if just crossed goal today
            val previousTodaySeconds = todayTotalSeconds - durationSeconds
            if (previousTodaySeconds < goalSeconds && todayTotalSeconds >= goalSeconds) {
                bonusCoins += 20
            }

            val totalCoinsAfter = currentPrefs.totalCoins + coins + bonusCoins
            val gameScores = dao.observeAllGameScores().first()
            val updatedBadges = evaluateBadges(
                existing = currentPrefs.unlockedBadges,
                studySessions = studySessions,
                gameScores = gameScores,
                todayTotalSeconds = todayTotalSeconds,
                goalSeconds = goalSeconds,
                streak = newStreak,
                totalCoins = totalCoinsAfter
            )
            prefs.addCoins(coins + bonusCoins)
            prefs.updateStreakAndBadges(
                currentStreak = newStreak,
                bestStreak = newBestStreak,
                lastStudyDateKey = today,
                unlockedBadges = updatedBadges
            )
        } else {
            prefs.addCoins(coins)
        }

        return coins + bonusCoins
    }

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteSession(sessionId)
    }

    suspend fun recordGameResult(gameType: GameType, score: Int, won: Boolean): Int {
        val coins = when (gameType) {
            GameType.SNAKE -> max(2, (score / 2).coerceAtMost(30))
            GameType.TICTACTOE -> if (won) 12 else 3
            GameType.BLOCKS -> max(3, (score / 25).coerceAtMost(35))
            GameType.MEMORY -> if (won) 15 else 2
        }
        dao.insertGameScore(
            GameScoreEntity(
                gameType = gameType.id,
                score = score,
                coinsEarned = coins,
                playedAtEpochMs = System.currentTimeMillis()
            )
        )
        prefs.addCoins(coins)

        val currentPrefs = prefs.preferencesFlow.first()
        val allSessions = dao.observeAllSessions().first().filter { !it.isBreak }
        val gameScores = dao.observeAllGameScores().first()
        val today = DateUtils.todayKey()
        val todayTotal = allSessions.filter { it.dateKey == today }.sumOf { it.durationSeconds }
        val updatedBadges = evaluateBadges(
            existing = currentPrefs.unlockedBadges,
            studySessions = allSessions,
            gameScores = gameScores,
            todayTotalSeconds = todayTotal,
            goalSeconds = currentPrefs.dailyGoalMinutes * 60L,
            streak = currentPrefs.currentStreak,
            totalCoins = currentPrefs.totalCoins + coins
        )
        if (updatedBadges != currentPrefs.unlockedBadges) {
            prefs.updateUnlockedBadges(updatedBadges)
        }
        return coins
    }

    private fun evaluateBadges(
        existing: Set<String>,
        studySessions: List<StudySessionEntity>,
        gameScores: List<GameScoreEntity>,
        todayTotalSeconds: Long,
        goalSeconds: Long,
        streak: Int,
        totalCoins: Int
    ): Set<String> {
        val next = existing.toMutableSet()
        if (studySessions.isNotEmpty()) next.add(BadgeId.FIRST_STEP.id)
        if (studySessions.sumOf { it.durationSeconds } >= 3600L) next.add(BadgeId.DEEP_FOCUS.id)
        if (todayTotalSeconds >= goalSeconds) next.add(BadgeId.GOAL_CRUSHER.id)
        if (streak >= 3) next.add(BadgeId.STREAK_3.id)
        if (streak >= 7) next.add(BadgeId.STREAK_7.id)
        if (studySessions.map { it.subjectId }.distinct().size >= 3) next.add(BadgeId.EXPLORER.id)
        if (totalCoins >= 100) next.add(BadgeId.COIN_100.id)
        if (gameScores.map { it.gameType }.distinct().size >= 4) next.add(BadgeId.GAMER.id)
        return next
    }

    companion object {
        fun buildReportBuckets(
            sessions: List<StudySessionEntity>,
            period: ReportPeriod,
            nowMs: Long = System.currentTimeMillis()
        ): List<ReportBucket> {
            val studyOnly = sessions.filter { !it.isBreak }
            return when (period) {
                ReportPeriod.DAILY -> {
                    // Last 7 days (day by day)
                    (6 downTo 0).map { daysAgo ->
                        val key = DateUtils.dateKeyDaysAgo(daysAgo, nowMs)
                        val secs = studyOnly.filter { it.dateKey == key }.sumOf { it.durationSeconds }
                        ReportBucket(
                            label = DateUtils.shortLabelForDateKey(key),
                            totalSeconds = secs,
                            minutes = secs / 60f
                        )
                    }
                }
                ReportPeriod.WEEKLY -> {
                    // Last 4 weeks (7-day blocks)
                    (3 downTo 0).map { weeksAgo ->
                        val startDaysAgo = (weeksAgo + 1) * 7 - 1
                        val endDaysAgo = weeksAgo * 7
                        val keys = (endDaysAgo..startDaysAgo).map { DateUtils.dateKeyDaysAgo(it, nowMs) }.toSet()
                        val secs = studyOnly.filter { it.dateKey in keys }.sumOf { it.durationSeconds }
                        val label = if (weeksAgo == 0) "This Wk" else "W-${weeksAgo}"
                        ReportBucket(
                            label = label,
                            totalSeconds = secs,
                            minutes = secs / 60f
                        )
                    }
                }
                ReportPeriod.MONTHLY -> {
                    // Last 6 months
                    val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
                    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    (5 downTo 0).map { monthsAgo ->
                        val target = (cal.clone() as Calendar).apply { add(Calendar.MONTH, -monthsAgo) }
                        val targetYear = target.get(Calendar.YEAR)
                        val targetMonth = target.get(Calendar.MONTH) + 1 // 1..12
                        val prefix = String.format("%04d-%02d", targetYear, targetMonth)
                        val secs = studyOnly.filter { it.dateKey.startsWith(prefix) }.sumOf { it.durationSeconds }
                        ReportBucket(
                            label = monthNames[targetMonth - 1],
                            totalSeconds = secs,
                            minutes = secs / 60f
                        )
                    }
                }
            }
        }

        fun buildSubjectBreakdown(
            sessions: List<StudySessionEntity>,
            period: ReportPeriod,
            nowMs: Long = System.currentTimeMillis()
        ): List<SubjectBreakdownItem> {
            val today = DateUtils.todayKey(nowMs)
            val validKeys: Set<String>? = when (period) {
                ReportPeriod.DAILY -> setOf(today)
                ReportPeriod.WEEKLY -> (0..6).map { DateUtils.dateKeyDaysAgo(it, nowMs) }.toSet()
                ReportPeriod.MONTHLY -> (0..29).map { DateUtils.dateKeyDaysAgo(it, nowMs) }.toSet()
            }
            val filtered = sessions.filter { !it.isBreak && (validKeys == null || it.dateKey in validKeys) }
            val total = filtered.sumOf { it.durationSeconds }.coerceAtLeast(1L)
            return filtered
                .groupBy { it.subjectId }
                .map { (subjectId, list) ->
                    val first = list.first()
                    val secs = list.sumOf { it.durationSeconds }
                    SubjectBreakdownItem(
                        subjectId = subjectId,
                        nameEn = first.subjectNameEn,
                        nameHi = first.subjectNameHi,
                        color = Color(first.subjectColorHex),
                        totalSeconds = secs,
                        percentage = secs.toFloat() / total.toFloat()
                    )
                }
                .sortedByDescending { it.totalSeconds }
        }
    }
}
