package com.webscare.speedometer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.webscare.speedometer.data.repository.SpeedHistoryRepositoryImpl
import com.webscare.speedometer.domain.usecase.GetAutoSaveUseCase

class SpeedHistoryViewModelFactory(
    private val repository: SpeedHistoryRepositoryImpl,
    private val getAutoSaveUseCase: GetAutoSaveUseCase

) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpeedHistoryViewModel::class.java)) {
            return SpeedHistoryViewModel(repository, getAutoSaveUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}