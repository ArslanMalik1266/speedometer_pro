package com.webscare.speedometer.domain.usecase

import com.webscare.speedometer.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow


class SetAutoSaveUseCase(private val dataStore: SettingsDataStore) {
    suspend operator fun invoke(enabled: Boolean) = dataStore.setAutoSave(enabled)
}

class GetAutoSaveUseCase(private val dataStore: SettingsDataStore) {
    operator fun invoke(): Flow<Boolean> = dataStore.autoSaveFlow
}