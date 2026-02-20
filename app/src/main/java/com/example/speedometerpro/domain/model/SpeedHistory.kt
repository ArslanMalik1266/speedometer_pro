package com.example.speedometerpro.domain.model

data class SpeedHistory(
    val id: Long,
    val distance: Double,
    val avg_speed: Double,
    val max_speed: Double,
    val timeStamp: Long,
    val durationMillis: Long,
    val current_speed: Double
)