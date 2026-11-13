package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val keyFormatter: ThreadLocal<SimpleDateFormat> = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    private val shortDayFormatter: ThreadLocal<SimpleDateFormat> = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
            SimpleDateFormat("EEE", Locale.getDefault())
    }

    private val dayMonthFormatter: ThreadLocal<SimpleDateFormat> = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
            SimpleDateFormat("d MMM", Locale.getDefault())
    }

    private val timeFormatter: ThreadLocal<SimpleDateFormat> = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
            SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    fun todayKey(nowMs: Long = System.currentTimeMillis()): String =
        keyFormatter.get()!!.format(Date(nowMs))

    fun dateKeyDaysAgo(daysAgo: Int, nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = nowMs
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return keyFormatter.get()!!.format(cal.time)
    }

    fun shortLabelForDateKey(dateKey: String): String {
        val date = keyFormatter.get()!!.parse(dateKey) ?: return dateKey
        return shortDayFormatter.get()!!.format(date)
    }

    fun dayMonthLabelForDateKey(dateKey: String): String {
        val date = keyFormatter.get()!!.parse(dateKey) ?: return dateKey
        return dayMonthFormatter.get()!!.format(date)
    }

    fun formatClockTime(epochMs: Long): String =
        timeFormatter.get()!!.format(Date(epochMs))

    fun currentDayOfWeekIndexMondayFirst(nowMs: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = nowMs
        // Calendar.MONDAY = 2 ... Calendar.SATURDAY = 7, Calendar.SUNDAY = 1
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    fun startOfCurrentWeekDaysAgo(nowMs: Long = System.currentTimeMillis()): Int {
        return currentDayOfWeekIndexMondayFirst(nowMs)
    }

    fun currentHourOfDay(nowMs: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = nowMs
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    fun formatDurationHms(totalSeconds: Long): String {
        val safe = totalSeconds.coerceAtLeast(0L)
        val hours = safe / 3600
        val minutes = (safe % 3600) / 60
        val seconds = safe % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatDurationMs(totalSeconds: Long): String {
        val safe = totalSeconds.coerceAtLeast(0L)
        val minutes = safe / 60
        val seconds = safe % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun formatHoursMinutes(totalSeconds: Long, hoursShort: String, minutesShort: String, secondsShort: String): String {
        val safe = totalSeconds.coerceAtLeast(0L)
        val totalMinutes = safe / 60
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return when {
            hours > 0 -> "$hours$hoursShort $mins$minutesShort"
            totalMinutes > 0 -> "$totalMinutes$minutesShort"
            else -> "$safe$secondsShort"
        }
    }
}
