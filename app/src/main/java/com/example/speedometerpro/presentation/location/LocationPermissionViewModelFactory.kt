package com.example.speedometerpro.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.speedometerpro.domain.usecase.CheckLocationPermissionUseCase

class LocationPermissionViewModelFactory(
    private val checkLocationPermissionUseCase: CheckLocationPermissionUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationPermissionViewModel::class.java)) {
            return LocationPermissionViewModel(checkLocationPermissionUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}