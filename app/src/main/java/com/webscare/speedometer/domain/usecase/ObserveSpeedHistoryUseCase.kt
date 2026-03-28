package com.webscare.speedometer.domain.usecase

import com.webscare.speedometer.domain.repository.SpeedHistoryRepository

class ObserveSpeedHistoryUseCase (
    private val repository: SpeedHistoryRepository
) {
    operator fun invoke() = repository.observeHistory()
}