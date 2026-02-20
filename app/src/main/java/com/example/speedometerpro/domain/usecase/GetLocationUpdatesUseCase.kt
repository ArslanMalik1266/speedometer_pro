package com.example.speedometerpro.domain.usecase

import android.location.Location
import com.example.speedometerpro.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetLocationUpdatesUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<Location> {
        return repository.getLocationUpdates()
    }
}