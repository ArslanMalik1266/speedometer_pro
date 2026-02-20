package com.example.speedometerpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_history")
data class SpeedHistoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val distanceMeters: Double,

    val averageSpeed: Double,
    val currentSpeed: Double,

    val maxSpeed: Double,

    val timestamp: Long,
    val durationMillis: Long
)