package com.example.speedometerpro.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.speedometerpro.data.repository.SpeedHistoryRepositoryImpl
import com.example.speedometerpro.domain.usecase.GetAutoSaveUseCase

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