package com.example.speedometerpro.domain.repository

import com.example.speedometerpro.domain.model.SpeedUnit
import kotlinx.coroutines.flow.Flow

interface SpeedUnitRepository {

    fun observeSpeedUnit(): Flow<SpeedUnit>

    suspend fun setSpeedUnit(unit: SpeedUnit)
}