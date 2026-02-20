package com.example.speedometerpro.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speedometerpro.domain.usecase.CheckLocationPermissionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationPermissionViewModel(
    private val checkLocationPermissionUseCase: CheckLocationPermissionUseCase
) : ViewModel() {

    private val _navigateToSettings = MutableSharedFlow<Unit>()
    val navigateToSettings = _navigateToSettings.asSharedFlow()

    private var rejectionCount = 0

    fun handlePermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        viewModelScope.launch {
            if (!isGranted) {
                rejectionCount++
                if (!shouldShowRationale && rejectionCount > 2) {
                    _navigateToSettings.emit(Unit)
                }
            }
        }
    }
}