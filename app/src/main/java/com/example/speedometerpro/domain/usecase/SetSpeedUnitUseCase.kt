package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.domain.model.SpeedUnit
import com.example.speedometerpro.domain.repository.SpeedUnitRepository

class SetSpeedUnitUseCase(
    private val repository: SpeedUnitRepository
) {
    suspend operator fun invoke(unit: SpeedUnit) {
        repository.setSpeedUnit(unit)
    }
}