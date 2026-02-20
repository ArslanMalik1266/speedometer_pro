package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.data.datastore.SettingsDataStore

class GetSpeedAlertEnabledUseCase(private val repository: SettingsDataStore) {
    operator fun invoke() = repository.isSpeedAlertEnabled()
}

class SetSpeedAlertEnabledUseCase(private val repository: SettingsDataStore) {
    suspend operator fun invoke(enabled: Boolean) = repository.setSpeedAlertEnabled(enabled)
}