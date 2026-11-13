package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nameEn: String,
    val nameHi: String,
    val colorHex: Long,
    val iconKey: String,
    val dailyGoalMinutes: Int = 30,
    val isCustom: Boolean = false
)
