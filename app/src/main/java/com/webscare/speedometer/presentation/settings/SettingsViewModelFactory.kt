package com.webscare.speedometer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.webscare.speedometer.domain.usecase.DeleteSpeedHistoryUseCase
import com.webscare.speedometer.domain.usecase.GetAutoSaveUseCase
import com.webscare.speedometer.domain.usecase.GetSpeedAlertEnabledUseCase
import com.webscare.speedometer.domain.usecase.GetSpeedLimitUseCase
import com.webscare.speedometer.domain.usecase.ObserveSpeedUnitUseCase
import com.webscare.speedometer.domain.usecase.SetAutoSaveUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedAlertEnabledUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedLimitUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedUnitUseCase

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
