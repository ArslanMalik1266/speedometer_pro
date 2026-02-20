package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.domain.model.SpeedHistory
import com.example.speedometerpro.domain.repository.SpeedHistoryRepository

class InsertSpeedHistoryUseCase (
    private val repository: SpeedHistoryRepository
) {
    suspend operator fun invoke(history: SpeedHistory) {
        repository.insertHistory(history)
    }
}