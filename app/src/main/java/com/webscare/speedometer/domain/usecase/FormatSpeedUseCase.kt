package com.webscare.speedometer.domain.usecase

import com.webscare.speedometer.domain.model.SpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.roundToInt

fun formatSpeedUseCase(speedKmh: Double, unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.KMH -> "${speedKmh.roundToInt()}"
        SpeedUnit.MPH -> "${(speedKmh * 0.621371).roundToInt()}"
    }
}
