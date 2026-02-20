package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow


class SetAutoSaveUseCase(private val dataStore: SettingsDataStore) {
    suspend operator fun invoke(enabled: Boolean) = dataStore.setAutoSave(enabled)
}

class GetAutoSaveUseCase(private val dataStore: SettingsDataStore) {
    operator fun invoke(): Flow<Boolean> = dataStore.autoSaveFlow
}