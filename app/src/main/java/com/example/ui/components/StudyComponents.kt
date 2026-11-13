package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ReportBucket
import com.example.data.local.StudySessionEntity
import com.example.data.local.SubjectBreakdownItem
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.ui.theme.CoinGold
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SubjectColors
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.VividPurple
import com.example.util.DateUtils
import kotlin.math.roundToInt

@Composable
fun StudyTopBar(
    title: String,
    totalCoins: Int,
    language: AppLanguage,
    themeMode: ThemeMode,
    onToggleLanguage: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = CoinGold.copy(alpha = 0.16f),
                modifier = Modifier.testTag("top_coins_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = LocalStrings.current.coinsLabel,
                        tint = CoinGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$totalCoins",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onToggleLanguage)
                    .testTag("language_toggle_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = LocalStrings.current.languageLabel,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.EN) "हिं" else "EN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = LocalStrings.current.settingsTitle,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SessionHistoryRow(
    session: StudySessionEntity,
    language: AppLanguage,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val subjectColor = Color(session.subjectColorHex)
    val label = if (session.isBreak) {
        strings.breakSessionLabel
    } else if (language == AppLanguage.HI) {
        session.subjectNameHi
    } else {
        session.subjectNameEn
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(subjectColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                if (session.isBreak) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = subjectColor,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(subjectColor)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${DateUtils.dayMonthLabelForDateKey(session.dateKey)} • ${DateUtils.formatClockTime(session.endedAtEpochMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateUtils.formatHoursMinutes(
                        session.durationSeconds,
                        strings.hoursShort,
                        strings.minutesShort,
                        strings.secondsShort
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "+${session.coinsEarned} 🪙",
                    style = MaterialTheme.typography.labelMedium,
                    color = CoinGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_session_${session.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = strings.delete,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StudyBarChart(
    buckets: List<ReportBucket>,
    goalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var selectedIndex by remember(buckets) { mutableIntStateOf(-1) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val maxVal = remember(buckets, goalMinutes) {
        val dataMax = buckets.maxOfOrNull { it.minutes } ?: 0f
        maxOf(dataMax, goalMinutes.toFloat(), 30f) * 1.15f
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedIndex in buckets.indices) {
            val item = buckets[selectedIndex]
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "${item.label}: ${
                        DateUtils.formatHoursMinutes(
                            item.totalSeconds,
                            strings.hoursShort,
                            strings.minutesShort,
                            strings.secondsShort
                        )
                    }",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(buckets) {
                    detectTapGestures { offset ->
                        if (buckets.isNotEmpty()) {
                            val slotWidth = size.width / buckets.size.toFloat()
                            val idx = (offset.x / slotWidth).toInt().coerceIn(0, buckets.lastIndex)
                            selectedIndex = if (selectedIndex == idx) -1 else idx
                        }
                    }
                }
        ) {
            val count = buckets.size.coerceAtLeast(1)
            val slotWidth = size.width / count.toFloat()
            val barWidth = (slotWidth * 0.52f).coerceAtMost(44.dp.toPx())
            val chartBottom = size.height - 6.dp.toPx()
            val chartHeight = chartBottom - 8.dp.toPx()

            // Horizontal guide lines
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                val y = chartBottom - chartHeight * frac
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Daily goal dashed line
            val goalRatio = (goalMinutes.toFloat() / maxVal).coerceIn(0f, 1f)
            val goalY = chartBottom - chartHeight * goalRatio
            drawLine(
                color = secondaryColor.copy(alpha = 0.7f),
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 1.8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            buckets.forEachIndexed { idx, bucket ->
                val ratio = (bucket.minutes / maxVal).coerceIn(0f, 1f)
                val barHeight = (chartHeight * ratio).coerceAtLeast(if (bucket.totalSeconds > 0) 6.dp.toPx() else 2.dp.toPx())
                val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                val top = chartBottom - barHeight

                val isSelected = idx == selectedIndex
                val brush = if (bucket.totalSeconds > 0) {
                    Brush.verticalGradient(
                        colors = if (isSelected) listOf(secondaryColor, primaryColor)
                        else listOf(primaryColor, secondaryColor.copy(alpha = 0.85f))
                    )
                } else {
                    Brush.verticalGradient(listOf(gridColor, gridColor))
                }

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            buckets.forEachIndexed { idx, bucket ->
                Text(
                    text = bucket.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Medium,
                    color = if (idx == selectedIndex) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SubjectDonutChart(
    breakdown: List<SubjectBreakdownItem>,
    totalSeconds: Long,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier.size(164.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(164.dp)) {
            val strokeWidth = 22.dp.toPx()
            if (breakdown.isEmpty() || totalSeconds <= 0L) {
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                var startAngle = -90f
                val gap = if (breakdown.size > 1) 4f else 0f
                breakdown.forEach { item ->
                    val sweep = (item.percentage * 360f - gap).coerceAtLeast(3f)
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += item.percentage * 360f
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = DateUtils.formatHoursMinutes(
                    totalSeconds,
                    strings.hoursShort,
                    strings.minutesShort,
                    strings.secondsShort
                ),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = strings.totalFocused,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, Int) -> Unit
) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(SubjectColors.first()) }
    var goalMinutes by remember { mutableFloatStateOf(30f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addSubject, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.subjectNameHint) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input")
                )

                Text(
                    text = "${strings.subjectGoalMinutes}: ${goalMinutes.roundToInt()} ${strings.minutesShort}",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = goalMinutes,
                    onValueChange = { goalMinutes = it },
                    valueRange = 10f..180f,
                    steps = 16
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubjectColors.forEach { hex ->
                        val selected = hex == selectedColor
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedColor, goalMinutes.roundToInt())
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_subject_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditGoalDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val strings = LocalStrings.current
    var goalMinutes by remember { mutableFloatStateOf(currentMinutes.toFloat()) }
    val presets = listOf(60, 90, 120, 180, 240)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.dailyGoalTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${goalMinutes.roundToInt()} ${strings.minutesShort} (${
                        DateUtils.formatHoursMinutes(
                            goalMinutes.roundToInt() * 60L,
                            strings.hoursShort,
                            strings.minutesShort,
                            strings.secondsShort
                        )
                    })",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = goalMinutes,
                    onValueChange = { goalMinutes = it },
                    valueRange = 15f..360f,
                    steps = 22
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { min ->
                        FilterChip(
                            selected = goalMinutes.roundToInt() == min,
                            onClick = { goalMinutes = min.toFloat() },
                            label = { Text("${min}${strings.minutesShort}") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(goalMinutes.roundToInt())
                    onDismiss()
                },
                modifier = Modifier.testTag("save_goal_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun SettingsDialog(
    currentLanguage: AppLanguage,
    currentThemeMode: ThemeMode,
    currentGoalMinutes: Int,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (ThemeMode) -> Unit,
    onOpenGoalEditor: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.settingsTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.languageLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = lang == currentLanguage,
                                onClick = { onSelectLanguage(lang) },
                                label = { Text(lang.label) },
                                modifier = Modifier.testTag("settings_lang_${lang.code}")
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.themeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ThemeMode.LIGHT to strings.themeLight,
                            ThemeMode.DARK to strings.themeDark,
                            ThemeMode.SYSTEM to strings.themeSystem
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = mode == currentThemeMode,
                                onClick = { onSelectTheme(mode) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("settings_theme_${mode.name.lowercase()}")
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onDismiss()
                            onOpenGoalEditor()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.dailyGoalLabel, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${currentGoalMinutes}${strings.minutesShort}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(strings.save)
            }
        }
    )
}
