package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.domain.repository.SpeedHistoryRepository

class ObserveSpeedHistoryUseCase (
    private val repository: SpeedHistoryRepository
) {
    operator fun invoke() = repository.observeHistory()
}