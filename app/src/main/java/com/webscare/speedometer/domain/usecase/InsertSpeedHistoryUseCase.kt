package com.webscare.speedometer.domain.usecase

import com.webscare.speedometer.domain.model.SpeedHistory
import com.webscare.speedometer.domain.repository.SpeedHistoryRepository

class InsertSpeedHistoryUseCase (
    private val repository: SpeedHistoryRepository
) {
    suspend operator fun invoke(history: SpeedHistory) {
        repository.insertHistory(history)
    }
}