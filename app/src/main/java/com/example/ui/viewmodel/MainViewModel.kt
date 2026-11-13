package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BadgeId
import com.example.data.local.GameScoreEntity
import com.example.data.local.GameType
import com.example.data.local.PersistedPreferences
import com.example.data.local.PreferencesStore
import com.example.data.local.ReportBucket
import com.example.data.local.ReportPeriod
import com.example.data.local.StudyDatabase
import com.example.data.local.StudySessionEntity
import com.example.data.local.SubjectBreakdownItem
import com.example.data.local.SubjectEntity
import com.example.data.local.TimerMode
import com.example.data.local.TimerStatus
import com.example.data.local.AchievementBadge
import com.example.data.repository.StudyRepository
import com.example.i18n.AppLanguage
import com.example.i18n.stringsFor
import com.example.ui.theme.ThemeMode
import com.example.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val language: AppLanguage = AppLanguage.EN,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val dailyGoalMinutes: Int = 120,
    val totalCoins: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubjectId: Long = 1L,
    val sessions: List<StudySessionEntity> = emptyList(),
    val todayStudySeconds: Long = 0L,
    val todaySessionCount: Int = 0,
    val timerMode: TimerMode = TimerMode.STUDY,
    val timerStatus: TimerStatus = TimerStatus.IDLE,
    val elapsedMillis: Long = 0L,
    val breakTargetSeconds: Int = 600,
    val selectedReportPeriod: ReportPeriod = ReportPeriod.DAILY,
    val reportBuckets: List<ReportBucket> = emptyList(),
    val subjectBreakdown: List<SubjectBreakdownItem> = emptyList(),
    val gameHighScores: Map<GameType, Int> = emptyMap(),
    val badges: List<AchievementBadge> = emptyList(),
    val weeklyActiveDays: Set<Int> = emptySet(),
    val feedbackBanner: String? = null
) {
    val selectedSubject: SubjectEntity?
        get() = subjects.firstOrNull { it.id == selectedSubjectId } ?: subjects.firstOrNull()

    val dailyGoalProgress: Float
        get() {
            val target = (dailyGoalMinutes * 60L).coerceAtLeast(60L)
            return (todayStudySeconds.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        }

    val remainingBreakSeconds: Long
        get() {
            val elapsedSec = elapsedMillis / 1000L
            return (breakTargetSeconds - elapsedSec).coerceAtLeast(0L)
        }

    val breakProgress: Float
        get() {
            val targetMs = (breakTargetSeconds * 1000L).coerceAtLeast(1000L)
            return (elapsedMillis.toFloat() / targetMs.toFloat()).coerceIn(0f, 1f)
        }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StudyDatabase.getInstance(application)
    private val preferencesStore = PreferencesStore(application)
    private val repository = StudyRepository(database.studyDao(), preferencesStore)

    private val tickerNowMs = MutableStateFlow(System.currentTimeMillis())
    private val selectedReportPeriod = MutableStateFlow(ReportPeriod.DAILY)
    private val feedbackBanner = MutableStateFlow<String?>(null)

    private var breakAutoCompleting = false

    val uiState: StateFlow<MainUiState> = combine(
        repository.preferencesFlow,
        repository.subjectsFlow,
        repository.sessionsFlow,
        repository.gameScoresFlow,
        combine(tickerNowMs, selectedReportPeriod, feedbackBanner) { now, period, banner ->
            Triple(now, period, banner)
        }
    ) { prefs, subjects, sessions, gameScores, extra ->
        val (nowMs, period, banner) = extra
        buildUiState(prefs, subjects, sessions, gameScores, nowMs, period, banner)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultSubjectsSeeded()
        }
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                tickerNowMs.value = now
                val current = uiState.value
                if (
                    current.timerStatus == TimerStatus.RUNNING &&
                    current.timerMode == TimerMode.BREAK &&
                    current.remainingBreakSeconds <= 0L &&
                    !breakAutoCompleting
                ) {
                    breakAutoCompleting = true
                    completeBreakTimer()
                    breakAutoCompleting = false
                }
                delay(250L)
            }
        }
    }

    private fun buildUiState(
        prefs: PersistedPreferences,
        subjects: List<SubjectEntity>,
        sessions: List<StudySessionEntity>,
        gameScores: List<GameScoreEntity>,
        nowMs: Long,
        period: ReportPeriod,
        banner: String?
    ): MainUiState {
        val elapsedMs = when (prefs.timerStatus) {
            TimerStatus.IDLE -> 0L
            TimerStatus.PAUSED -> prefs.timerAccumulatedMs
            TimerStatus.RUNNING -> {
                val delta = if (prefs.timerLastResumedEpochMs > 0L) {
                    (nowMs - prefs.timerLastResumedEpochMs).coerceAtLeast(0L)
                } else 0L
                prefs.timerAccumulatedMs + delta
            }
        }

        val todayKey = DateUtils.todayKey(nowMs)
        val studySessions = sessions.filter { !it.isBreak }
        val todayStudySessions = studySessions.filter { it.dateKey == todayKey }
        val todayStudySeconds = todayStudySessions.sumOf { it.durationSeconds }

        // Effective streak: keep active if studied today or yesterday
        val yesterdayKey = DateUtils.dateKeyDaysAgo(1, nowMs)
        val effectiveStreak = if (
            prefs.lastStudyDateKey == todayKey || prefs.lastStudyDateKey == yesterdayKey
        ) prefs.currentStreak else 0

        val startOfWeekDaysAgo = DateUtils.startOfCurrentWeekDaysAgo(nowMs)
        val activeWeekDays = mutableSetOf<Int>()
        for (offset in 0..startOfWeekDaysAgo) {
            val key = DateUtils.dateKeyDaysAgo(offset, nowMs)
            if (studySessions.any { it.dateKey == key }) {
                activeWeekDays.add(startOfWeekDaysAgo - offset)
            }
        }

        val highScores = GameType.entries.associateWith { type ->
            gameScores.filter { it.gameType == type.id }.maxOfOrNull { it.score } ?: 0
        }

        val totalStudySecsAllTime = studySessions.sumOf { it.durationSeconds }
        val distinctSubjectsStudied = studySessions.map { it.subjectId }.distinct().size
        val distinctGamesPlayed = gameScores.map { it.gameType }.distinct().size
        val goalSeconds = (prefs.dailyGoalMinutes * 60L).coerceAtLeast(60L)

        val badges = listOf(
            AchievementBadge(
                id = BadgeId.FIRST_STEP,
                unlocked = BadgeId.FIRST_STEP.id in prefs.unlockedBadges || studySessions.isNotEmpty(),
                progress = if (studySessions.isNotEmpty()) 1f else 0f
            ),
            AchievementBadge(
                id = BadgeId.DEEP_FOCUS,
                unlocked = BadgeId.DEEP_FOCUS.id in prefs.unlockedBadges || totalStudySecsAllTime >= 3600L,
                progress = (totalStudySecsAllTime.toFloat() / 3600f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = BadgeId.GOAL_CRUSHER,
                unlocked = BadgeId.GOAL_CRUSHER.id in prefs.unlockedBadges || todayStudySeconds >= goalSeconds,
                progress = (todayStudySeconds.toFloat() / goalSeconds.toFloat()).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = BadgeId.STREAK_3,
                unlocked = BadgeId.STREAK_3.id in prefs.unlockedBadges || prefs.bestStreak >= 3,
                progress = (effectiveStreak.toFloat() / 3f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = BadgeId.STREAK_7,
                unlocked = BadgeId.STREAK_7.id in prefs.unlockedBadges || prefs.bestStreak >= 7,
                progress = (effectiveStreak.toFloat() / 7f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = BadgeId.EXPLORER,
                unlocked = BadgeId.EXPLORER.id in prefs.unlockedBadges || distinctSubjectsStudied >= 3,
                progress = (distinctSubjectsStudied.toFloat() / 3f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = BadgeId.COIN_100,
                unlocked = BadgeId.COIN_100.id in prefs.unlockedBadges || prefs.totalCoins >= 100,
                progress = (prefs.totalCoins.toFloat() / 100f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = BadgeId.GAMER,
                unlocked = BadgeId.GAMER.id in prefs.unlockedBadges || distinctGamesPlayed >= 4,
                progress = (distinctGamesPlayed.toFloat() / 4f).coerceIn(0f, 1f)
            )
        )

        val validSubjectId = if (subjects.any { it.id == prefs.timerSubjectId }) {
            prefs.timerSubjectId
        } else {
            subjects.firstOrNull()?.id ?: 1L
        }

        return MainUiState(
            language = prefs.language,
            themeMode = prefs.themeMode,
            dailyGoalMinutes = prefs.dailyGoalMinutes,
            totalCoins = prefs.totalCoins,
            currentStreak = effectiveStreak,
            bestStreak = prefs.bestStreak,
            subjects = subjects,
            selectedSubjectId = validSubjectId,
            sessions = sessions,
            todayStudySeconds = todayStudySeconds,
            todaySessionCount = todayStudySessions.size,
            timerMode = prefs.timerMode,
            timerStatus = prefs.timerStatus,
            elapsedMillis = elapsedMs,
            breakTargetSeconds = prefs.breakTargetSeconds,
            selectedReportPeriod = period,
            reportBuckets = StudyRepository.buildReportBuckets(sessions, period, nowMs),
            subjectBreakdown = StudyRepository.buildSubjectBreakdown(sessions, period, nowMs),
            gameHighScores = highScores,
            badges = badges,
            weeklyActiveDays = activeWeekDays,
            feedbackBanner = banner
        )
    }

    fun clearFeedbackBanner() {
        feedbackBanner.value = null
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun toggleLanguage() {
        val next = if (uiState.value.language == AppLanguage.EN) AppLanguage.HI else AppLanguage.EN
        setLanguage(next)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setDailyGoalMinutes(minutes: Int) {
        viewModelScope.launch { repository.setDailyGoalMinutes(minutes) }
    }

    fun setReportPeriod(period: ReportPeriod) {
        selectedReportPeriod.value = period
    }

    fun addSubject(name: String, colorHex: Long, goalMinutes: Int) {
        viewModelScope.launch {
            val id = repository.addSubject(name, colorHex, goalMinutes)
            if (id > 0L) {
                val p = repository.preferencesFlow.first()
                if (p.timerStatus == TimerStatus.IDLE) {
                    repository.saveTimerSnapshot(
                        mode = p.timerMode,
                        status = p.timerStatus,
                        subjectId = id,
                        accumulatedMs = p.timerAccumulatedMs,
                        lastResumedEpochMs = p.timerLastResumedEpochMs,
                        startedAtEpochMs = p.timerStartedAtEpochMs,
                        breakTargetSeconds = p.breakTargetSeconds
                    )
                }
            }
        }
    }

    fun deleteCustomSubject(subjectId: Long) {
        viewModelScope.launch { repository.deleteCustomSubject(subjectId) }
    }

    fun selectSubject(subjectId: Long) {
        viewModelScope.launch {
            val p = repository.preferencesFlow.first()
            repository.saveTimerSnapshot(
                mode = p.timerMode,
                status = p.timerStatus,
                subjectId = subjectId,
                accumulatedMs = p.timerAccumulatedMs,
                lastResumedEpochMs = p.timerLastResumedEpochMs,
                startedAtEpochMs = p.timerStartedAtEpochMs,
                breakTargetSeconds = p.breakTargetSeconds
            )
        }
    }

    fun switchTimerMode(mode: TimerMode) {
        viewModelScope.launch {
            val p = repository.preferencesFlow.first()
            if (p.timerStatus == TimerStatus.IDLE && p.timerMode != mode) {
                repository.saveTimerSnapshot(
                    mode = mode,
                    status = TimerStatus.IDLE,
                    subjectId = p.timerSubjectId,
                    accumulatedMs = 0L,
                    lastResumedEpochMs = 0L,
                    startedAtEpochMs = 0L,
                    breakTargetSeconds = p.breakTargetSeconds
                )
            }
        }
    }

    fun setBreakDurationMinutes(minutes: Int) {
        viewModelScope.launch {
            val p = repository.preferencesFlow.first()
            if (p.timerStatus == TimerStatus.IDLE) {
                repository.saveTimerSnapshot(
                    mode = p.timerMode,
                    status = TimerStatus.IDLE,
                    subjectId = p.timerSubjectId,
                    accumulatedMs = 0L,
                    lastResumedEpochMs = 0L,
                    startedAtEpochMs = 0L,
                    breakTargetSeconds = (minutes * 60).coerceIn(60, 3600)
                )
            }
        }
    }

    fun startTimer() {
        viewModelScope.launch {
            val p = repository.preferencesFlow.first()
            val now = System.currentTimeMillis()
            tickerNowMs.value = now
            repository.saveTimerSnapshot(
                mode = p.timerMode,
                status = TimerStatus.RUNNING,
                subjectId = p.timerSubjectId,
                accumulatedMs = 0L,
                lastResumedEpochMs = now,
                startedAtEpochMs = now,
                breakTargetSeconds = p.breakTargetSeconds
            )
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            val p = repository.preferencesFlow.first()
            if (p.timerStatus != TimerStatus.RUNNING) return@launch
            val now = System.currentTimeMillis()
            tickerNowMs.value = now
            val delta = if (p.timerLastResumedEpochMs > 0L) (now - p.timerLastResumedEpochMs).coerceAtLeast(0L) else 0L
            repository.saveTimerSnapshot(
                mode = p.timerMode,
                status = TimerStatus.PAUSED,
                subjectId = p.timerSubjectId,
                accumulatedMs = p.timerAccumulatedMs + delta,
                lastResumedEpochMs = 0L,
                startedAtEpochMs = p.timerStartedAtEpochMs,
                breakTargetSeconds = p.breakTargetSeconds
            )
        }
    }

    fun resumeTimer() {
        viewModelScope.launch {
            val p = repository.preferencesFlow.first()
            if (p.timerStatus != TimerStatus.PAUSED) return@launch
            val now = System.currentTimeMillis()
            tickerNowMs.value = now
            repository.saveTimerSnapshot(
                mode = p.timerMode,
                status = TimerStatus.RUNNING,
                subjectId = p.timerSubjectId,
                accumulatedMs = p.timerAccumulatedMs,
                lastResumedEpochMs = now,
                startedAtEpochMs = p.timerStartedAtEpochMs,
                breakTargetSeconds = p.breakTargetSeconds
            )
        }
    }

    fun stopAndSaveTimer() {
        viewModelScope.launch {
            val p = repository.preferencesFlow.first()
            if (p.timerStatus == TimerStatus.IDLE) return@launch
            val now = System.currentTimeMillis()
            tickerNowMs.value = now
            val delta = if (p.timerStatus == TimerStatus.RUNNING && p.timerLastResumedEpochMs > 0L) {
                (now - p.timerLastResumedEpochMs).coerceAtLeast(0L)
            } else 0L
            val totalSeconds = (p.timerAccumulatedMs + delta) / 1000L

            val strings = stringsFor(p.language)
            val subjects = repository.subjectsFlow.first()
            val subject = subjects.firstOrNull { it.id == p.timerSubjectId } ?: subjects.firstOrNull()

            if (totalSeconds >= 5L) {
                val earned = repository.recordCompletedSession(
                    subject = subject,
                    durationSeconds = totalSeconds,
                    isBreak = p.timerMode == TimerMode.BREAK,
                    startedAtEpochMs = p.timerStartedAtEpochMs,
                    endedAtEpochMs = now
                )
                feedbackBanner.value = "${strings.sessionSavedMsg} (${String.format(strings.earnedCoinsFmt, earned)})"
            } else {
                feedbackBanner.value = strings.sessionTooShortMsg
            }

            repository.saveTimerSnapshot(
                mode = p.timerMode,
                status = TimerStatus.IDLE,
                subjectId = p.timerSubjectId,
                accumulatedMs = 0L,
                lastResumedEpochMs = 0L,
                startedAtEpochMs = 0L,
                breakTargetSeconds = p.breakTargetSeconds
            )
        }
    }

    private suspend fun completeBreakTimer() {
        val p = repository.preferencesFlow.first()
        val now = System.currentTimeMillis()
        val earned = repository.recordCompletedSession(
            subject = null,
            durationSeconds = p.breakTargetSeconds.toLong(),
            isBreak = true,
            startedAtEpochMs = p.timerStartedAtEpochMs,
            endedAtEpochMs = now
        )
        val strings = stringsFor(p.language)
        feedbackBanner.value = "${strings.breakFinishedMsg} (${String.format(strings.earnedCoinsFmt, earned)})"
        repository.saveTimerSnapshot(
            mode = TimerMode.STUDY,
            status = TimerStatus.IDLE,
            subjectId = p.timerSubjectId,
            accumulatedMs = 0L,
            lastResumedEpochMs = 0L,
            startedAtEpochMs = 0L,
            breakTargetSeconds = p.breakTargetSeconds
        )
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch { repository.deleteSession(sessionId) }
    }

    fun recordGameScore(gameType: GameType, score: Int, won: Boolean) {
        viewModelScope.launch {
            val earned = repository.recordGameResult(gameType, score, won)
            val strings = stringsFor(uiState.value.language)
            feedbackBanner.value = String.format(strings.earnedCoinsFmt, earned)
        }
    }
}
