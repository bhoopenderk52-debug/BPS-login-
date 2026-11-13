package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ReportPeriod
import com.example.i18n.LocalStrings
import com.example.ui.components.SessionHistoryRow
import com.example.ui.components.StudyBarChart
import com.example.ui.components.SubjectDonutChart
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.MainUiState
import com.example.util.DateUtils

@Composable
fun ReportsScreen(
    state: MainUiState,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onOpenGoalEditor: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val totalPeriodSeconds = state.subjectBreakdown.sumOf { it.totalSeconds }
    val bucketDaysCount = when (state.selectedReportPeriod) {
        ReportPeriod.DAILY -> 1
        ReportPeriod.WEEKLY -> 7
        ReportPeriod.MONTHLY -> 30
    }
    val avgPerDaySeconds = totalPeriodSeconds / bucketDaysCount

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period Selector
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val periods = listOf(
                    ReportPeriod.DAILY to strings.periodDaily,
                    ReportPeriod.WEEKLY to strings.periodWeekly,
                    ReportPeriod.MONTHLY to strings.periodMonthly
                )
                periods.forEachIndexed { idx, (period, label) ->
                    SegmentedButton(
                        selected = state.selectedReportPeriod == period,
                        onClick = { onSelectPeriod(period) },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = periods.size),
                        modifier = Modifier.testTag("report_period_${period.name.lowercase()}")
                    ) {
                        Text(label, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Summary Row + Goal Edit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = strings.totalFocused,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateUtils.formatHoursMinutes(
                                totalPeriodSeconds,
                                strings.hoursShort,
                                strings.minutesShort,
                                strings.secondsShort
                            ),
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (state.selectedReportPeriod == ReportPeriod.DAILY) strings.dailyGoalLabel else strings.avgPerDay,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val rightValue = if (state.selectedReportPeriod == ReportPeriod.DAILY) {
                            DateUtils.formatHoursMinutes(
                                state.dailyGoalMinutes * 60L,
                                strings.hoursShort,
                                strings.minutesShort,
                                strings.secondsShort
                            )
                        } else {
                            DateUtils.formatHoursMinutes(
                                avgPerDaySeconds,
                                strings.hoursShort,
                                strings.minutesShort,
                                strings.secondsShort
                            )
                        }
                        Text(
                            text = rightValue,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Progress Bar Chart Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.studyChartTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenGoalEditor,
                            modifier = Modifier.testTag("edit_goal_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(strings.editDailyGoal, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    StudyBarChart(
                        buckets = state.reportBuckets,
                        goalMinutes = when (state.selectedReportPeriod) {
                            ReportPeriod.DAILY -> state.dailyGoalMinutes
                            ReportPeriod.WEEKLY -> state.dailyGoalMinutes * 7
                            ReportPeriod.MONTHLY -> state.dailyGoalMinutes * 30
                        }
                    )
                }
            }
        }

        // Subject-wise Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.subjectBreakdownTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (state.subjectBreakdown.isEmpty()) {
                        Text(
                            text = strings.noReportData,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            SubjectDonutChart(
                                breakdown = state.subjectBreakdown,
                                totalSeconds = totalPeriodSeconds
                            )
                        }

                        state.subjectBreakdown.forEach { item ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(item.color)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = item.localizedName(state.language),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "${
                                            DateUtils.formatHoursMinutes(
                                                item.totalSeconds,
                                                strings.hoursShort,
                                                strings.minutesShort,
                                                strings.secondsShort
                                            )
                                        } (${(item.percentage * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontFamily = JetBrainsMonoFontFamily
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { item.percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = item.color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Complete Session History
        item {
            Text(
                text = strings.recentHistory,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.sessions.isEmpty()) {
            item {
                Text(
                    text = strings.noSessionsYet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
