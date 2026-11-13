package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TimerMode
import com.example.data.local.TimerStatus
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.ui.components.SessionHistoryRow
import com.example.ui.theme.CoinGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VividPurple
import com.example.ui.viewmodel.MainUiState
import com.example.util.DateUtils

@Composable
fun TimerScreen(
    state: MainUiState,
    onSwitchMode: (TimerMode) -> Unit,
    onSelectSubject: (Long) -> Unit,
    onOpenAddSubject: () -> Unit,
    onSetBreakMinutes: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopAndSave: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isBreak = state.timerMode == TimerMode.BREAK
    val isIdle = state.timerStatus == TimerStatus.IDLE
    val activeSubject = state.selectedSubject
    val accentColor = if (isBreak) CyanAccent else Color(activeSubject?.colorHex ?: 0xFF2563EB)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Study / Break Segmented Switch
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = !isBreak,
                    onClick = { onSwitchMode(TimerMode.STUDY) },
                    enabled = isIdle,
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("timer_mode_study")
                ) {
                    Text(strings.studyMode, fontWeight = FontWeight.SemiBold)
                }
                SegmentedButton(
                    selected = isBreak,
                    onClick = { onSwitchMode(TimerMode.BREAK) },
                    enabled = isIdle,
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("timer_mode_break")
                ) {
                    Text(strings.breakMode, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Subject selector (Study mode) OR Break Presets (Break mode)
        item {
            if (!isBreak) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.subjectsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(enabled = isIdle, onClick = onOpenAddSubject)
                                .testTag("timer_add_subject_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = strings.addSubject,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = strings.addSubject,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.subjects, key = { it.id }) { subject ->
                            val selected = subject.id == state.selectedSubjectId
                            val subColor = Color(subject.colorHex)
                            val label = if (state.language == AppLanguage.HI) subject.nameHi else subject.nameEn
                            FilterChip(
                                selected = selected,
                                enabled = isIdle,
                                onClick = { onSelectSubject(subject.id) },
                                label = { Text(label, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (selected) PureWhite else subColor)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = subColor,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("timer_subject_${subject.id}")
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.breakDurationLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 25).forEach { mins ->
                            val selected = state.breakTargetSeconds == mins * 60
                            FilterChip(
                                selected = selected,
                                enabled = isIdle,
                                onClick = { onSetBreakMinutes(mins) },
                                label = { Text("$mins ${strings.minutesShort}") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("break_preset_${mins}m")
                            )
                        }
                    }
                }
            }
        }

        // Main Circular Clock Card
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 26.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    val subjectTitle = if (isBreak) {
                        strings.breakMode
                    } else if (activeSubject != null) {
                        if (state.language == AppLanguage.HI) activeSubject.nameHi else activeSubject.nameEn
                    } else {
                        strings.studyMode
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = subjectTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    // Circular Ring
                    val ringProgress = if (isBreak) {
                        1f - state.breakProgress
                    } else {
                        val elapsedSec = (state.elapsedMillis / 1000L)
                        ((elapsedSec % 3600L) / 3600f).coerceIn(0.02f, 1f)
                    }
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant

                    Box(
                        modifier = Modifier.size(230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(230.dp)) {
                            val stroke = 16.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            val sweep = if (isIdle && !isBreak) 0f else (ringProgress * 360f)
                            if (sweep > 0f) {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(RoyalBlue, VividPurple, accentColor, RoyalBlue)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val displayTime = if (isBreak) {
                                DateUtils.formatDurationMs(state.remainingBreakSeconds)
                            } else {
                                DateUtils.formatDurationHms(state.elapsedMillis / 1000L)
                            }
                            Text(
                                text = displayTime,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isBreak) 42.sp else 36.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("timer_clock_display")
                            )
                            Spacer(Modifier.height(4.dp))
                            val statusLabel = when (state.timerStatus) {
                                TimerStatus.IDLE -> if (isBreak) "${state.breakTargetSeconds / 60} ${strings.minutesShort}" else strings.earnCoinsHint
                                TimerStatus.RUNNING -> strings.activeTimerRunning
                                TimerStatus.PAUSED -> strings.activeTimerPaused
                            }
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = when (state.timerStatus) {
                                    TimerStatus.RUNNING -> EmeraldSuccess
                                    TimerStatus.PAUSED -> CoinGold
                                    TimerStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // Large Accessible Control Buttons
                    when (state.timerStatus) {
                        TimerStatus.IDLE -> {
                            Button(
                                onClick = onStart,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .testTag("timer_start_button"),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = PureWhite
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = strings.start,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        TimerStatus.RUNNING -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = onPause,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .testTag("timer_pause_button"),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.pause, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onStopAndSave,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .testTag("timer_stop_button"),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoseAlert,
                                        contentColor = PureWhite
                                    )
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.stopAndSave, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        TimerStatus.PAUSED -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onResume,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .testTag("timer_resume_button"),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldSuccess,
                                        contentColor = PureWhite
                                    )
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.resume, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onStopAndSave,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .testTag("timer_stop_button"),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoseAlert,
                                        contentColor = PureWhite
                                    )
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.stopAndSave, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Session History
        item {
            Text(
                text = strings.recentHistory,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.sessions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.noSessionsTip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(state.sessions, key = { it.id }) { session ->
                SessionHistoryRow(
                    session = session,
                    language = state.language,
                    onDelete = { onDeleteSession(session.id) }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
