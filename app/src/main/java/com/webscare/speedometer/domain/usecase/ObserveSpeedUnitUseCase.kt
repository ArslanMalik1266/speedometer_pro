package com.webscare.speedometer.domain.usecase

import com.webscare.speedometer.domain.model.SpeedUnit
import com.webscare.speedometer.domain.repository.SpeedUnitRepository
import kotlinx.coroutines.flow.Flow

class ObserveSpeedUnitUseCase(
    private val repository: SpeedUnitRepository
) {
    operator fun invoke(): Flow<SpeedUnit> {
        return repository.observeSpeedUnit()
    }
}