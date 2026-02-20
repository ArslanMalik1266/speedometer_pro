package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.domain.model.SpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.roundToInt

fun formatSpeedUseCase(speedKmh: Double, unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.KMH -> "${speedKmh.roundToInt()}"
        SpeedUnit.MPH -> "${(speedKmh * 0.621371).roundToInt()}"
    }
}
