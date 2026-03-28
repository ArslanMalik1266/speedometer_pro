package com.webscare.speedometer.domain.usecase

import android.location.Location
import com.webscare.speedometer.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetLocationUpdatesUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<Location> {
        return repository.getLocationUpdates()
    }
}