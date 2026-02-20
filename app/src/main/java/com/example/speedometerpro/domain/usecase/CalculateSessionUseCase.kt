package com.example.speedometerpro.domain.usecase
import com.example.speedometerpro.domain.model.SpeedHistory


class CalculateSessionUseCase {

    fun calculate(
        distanceMeters: Float,
        maxSpeedKmh: Float,
        trackingTimeSeconds: Long,
        currentSpeedKmh: Float = 0f,
        id: Long = 0L
    ): SpeedHistory {
        // 1. Convert everything to Double immediately for precision
        val distanceKm = distanceMeters.toDouble() / 1000.0

        // 2. Explicitly convert time to hours as a Double
        // This prevents any potential Long/Int math issues
        val timeHours = trackingTimeSeconds.toDouble() / 3600.0

        val avgSpeed = if (timeHours > 0) {
            distanceKm / timeHours
        } else {
            0.0
        }

        return SpeedHistory(
            id = id,
            distance = distanceKm,
            avg_speed = avgSpeed,
            max_speed = maxSpeedKmh.toDouble(),
            timeStamp = System.currentTimeMillis(),
            durationMillis = trackingTimeSeconds * 1000L,
            current_speed =  currentSpeedKmh.toDouble()
        )
    }
}

