package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.domain.repository.SpeedHistoryRepository

class DeleteSpeedHistoryUseCase(
    private val repository: SpeedHistoryRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllHistory()
    }
}
