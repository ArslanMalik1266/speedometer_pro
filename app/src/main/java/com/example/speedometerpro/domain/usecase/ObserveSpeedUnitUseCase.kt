package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.domain.model.SpeedUnit
import com.example.speedometerpro.domain.repository.SpeedUnitRepository
import kotlinx.coroutines.flow.Flow

class ObserveSpeedUnitUseCase(
    private val repository: SpeedUnitRepository
) {
    operator fun invoke(): Flow<SpeedUnit> {
        return repository.observeSpeedUnit()
    }
}