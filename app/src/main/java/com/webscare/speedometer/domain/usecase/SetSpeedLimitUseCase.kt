package com.webscare.speedometer.domain.usecase

import com.webscare.speedometer.data.datastore.SettingsDataStore

class GetSpeedLimitUseCase(private val repository: SettingsDataStore) {
    operator fun invoke() = repository.getSpeedLimit()
}

class SetSpeedLimitUseCase(private val repository: SettingsDataStore) {
    suspend operator fun invoke(limit: Int) = repository.saveSpeedLimit(limit)
}