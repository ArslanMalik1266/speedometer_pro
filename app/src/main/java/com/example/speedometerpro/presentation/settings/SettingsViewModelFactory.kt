package com.example.speedometerpro.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.speedometerpro.domain.usecase.DeleteSpeedHistoryUseCase
import com.example.speedometerpro.domain.usecase.GetAutoSaveUseCase
import com.example.speedometerpro.domain.usecase.GetSpeedAlertEnabledUseCase
import com.example.speedometerpro.domain.usecase.GetSpeedLimitUseCase
import com.example.speedometerpro.domain.usecase.ObserveSpeedUnitUseCase
import com.example.speedometerpro.domain.usecase.SetAutoSaveUseCase
import com.example.speedometerpro.domain.usecase.SetSpeedAlertEnabledUseCase
import com.example.speedometerpro.domain.usecase.SetSpeedLimitUseCase
import com.example.speedometerpro.domain.usecase.SetSpeedUnitUseCase

// In your SettingsViewModelFactory.kt
class SettingsViewModelFactory(
    private val clearHistoryUseCase: DeleteSpeedHistoryUseCase,
    private val getAutoSaveUseCase: GetAutoSaveUseCase,
    private val setAutoSaveUseCase: SetAutoSaveUseCase,
    private val observeSpeedUnitUseCase: ObserveSpeedUnitUseCase,
    private val setSpeedUnitUseCase: SetSpeedUnitUseCase,
    private val getSpeedAlertUseCase: GetSpeedAlertEnabledUseCase,
    private val setSpeedAlertUseCase: SetSpeedAlertEnabledUseCase,
    private val getSpeedLimitUseCase: GetSpeedLimitUseCase,
    private val setSpeedLimitUseCase: SetSpeedLimitUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            clearHistoryUseCase, getAutoSaveUseCase, setAutoSaveUseCase,
            observeSpeedUnitUseCase, setSpeedUnitUseCase, getSpeedAlertUseCase,
            setSpeedAlertUseCase, getSpeedLimitUseCase, setSpeedLimitUseCase
        ) as T
    }
}
