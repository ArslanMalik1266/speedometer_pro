package com.webscare.speedometer.data.mapper

import com.webscare.speedometer.data.local.SpeedHistoryEntity
import com.webscare.speedometer.domain.model.SpeedHistory

fun SpeedHistoryEntity.toDomain(): SpeedHistory {
    return SpeedHistory(
        id = id,
        distance = distanceMeters,
        avg_speed = averageSpeed,
        max_speed = maxSpeed,
        timeStamp = timestamp,
        durationMillis = durationMillis,
        current_speed = currentSpeed
    )
}

fun SpeedHistory.toEntity(): SpeedHistoryEntity {
    return SpeedHistoryEntity(
        id = id,
        distanceMeters = distance,
        averageSpeed = avg_speed,
        maxSpeed = max_speed,
        timestamp = timeStamp,
        durationMillis = durationMillis,
        currentSpeed = current_speed

    )
}
