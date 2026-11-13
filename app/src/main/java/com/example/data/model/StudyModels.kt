package com.example.data.local

import androidx.compose.ui.graphics.Color
import com.example.i18n.AppLanguage

enum class ReportPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class GameType(val id: String) {
    SNAKE("SNAKE"),
    TICTACTOE("TICTACTOE"),
    BLOCKS("BLOCKS"),
    MEMORY("MEMORY")
}

enum class BadgeId(val id: String) {
    FIRST_STEP("first_step"),
    DEEP_FOCUS("deep_focus"),
    GOAL_CRUSHER("goal_crusher"),
    STREAK_3("streak_3"),
    STREAK_7("streak_7"),
    EXPLORER("explorer"),
    COIN_100("coin_100"),
    GAMER("gamer")
}

data class SubjectBreakdownItem(
    val subjectId: Long,
    val nameEn: String,
    val nameHi: String,
    val color: Color,
    val totalSeconds: Long,
    val percentage: Float
) {
    fun localizedName(lang: AppLanguage): String =
        if (lang == AppLanguage.HI) nameHi else nameEn
}

data class ReportBucket(
    val label: String,
    val totalSeconds: Long,
    val minutes: Float
)

data class AchievementBadge(
    val id: BadgeId,
    val unlocked: Boolean,
    val progress: Float
)
