package com.webscare.speedometer.domain.usecase

import com.webscare.speedometer.data.datastore.SettingsDataStore

class GetSpeedAlertEnabledUseCase(private val repository: SettingsDataStore) {
    operator fun invoke() = repository.isSpeedAlertEnabled()
}

class SetSpeedAlertEnabledUseCase(private val repository: SettingsDataStore) {
    suspend operator fun invoke(enabled: Boolean) = repository.setSpeedAlertEnabled(enabled)
}