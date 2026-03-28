package com.webscare.speedometer.domain.repository

import com.webscare.speedometer.domain.model.SpeedUnit
import kotlinx.coroutines.flow.Flow

interface SpeedUnitRepository {

    fun observeSpeedUnit(): Flow<SpeedUnit>

    suspend fun setSpeedUnit(unit: SpeedUnit)
}