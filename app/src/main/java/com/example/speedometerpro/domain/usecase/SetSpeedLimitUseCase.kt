package com.example.speedometerpro.domain.usecase

import com.example.speedometerpro.data.datastore.SettingsDataStore

class GetSpeedLimitUseCase(private val repository: SettingsDataStore) {
    operator fun invoke() = repository.getSpeedLimit()
}

class SetSpeedLimitUseCase(private val repository: SettingsDataStore) {
    suspend operator fun invoke(limit: Int) = repository.saveSpeedLimit(limit)
}