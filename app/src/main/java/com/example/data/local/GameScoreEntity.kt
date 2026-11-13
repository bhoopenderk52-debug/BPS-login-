package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val gameType: String,
    val score: Int,
    val coinsEarned: Int,
    val playedAtEpochMs: Long
)
