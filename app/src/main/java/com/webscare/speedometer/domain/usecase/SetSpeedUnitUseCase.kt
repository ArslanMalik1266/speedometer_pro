package com.webscare.speedometer.domain.usecase

import com.webscare.speedometer.domain.model.SpeedUnit
import com.webscare.speedometer.domain.repository.SpeedUnitRepository

class SetSpeedUnitUseCase(
    private val repository: SpeedUnitRepository
) {
    suspend operator fun invoke(unit: SpeedUnit) {
        repository.setSpeedUnit(unit)
    }
}