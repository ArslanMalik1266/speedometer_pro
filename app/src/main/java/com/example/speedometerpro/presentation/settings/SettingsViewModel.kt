package com.example.speedometerpro.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speedometerpro.domain.model.SpeedUnit
import com.example.speedometerpro.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val clearHistoryUseCase: DeleteSpeedHistoryUseCase,
    private val getAutoSaveUseCase: GetAutoSaveUseCase,
    private val setAutoSaveUseCase: SetAutoSaveUseCase,
    private val observeSpeedUnitUseCase: ObserveSpeedUnitUseCase,
    private val setSpeedUnitUseCase: SetSpeedUnitUseCase,
    private val getSpeedAlertUseCase: GetSpeedAlertEnabledUseCase,
    private val setSpeedAlertUseCase: SetSpeedAlertEnabledUseCase,
    private val getSpeedLimitUseCase: GetSpeedLimitUseCase,
    private val setSpeedLimitUseCase: SetSpeedLimitUseCase
) : ViewModel() {


    private val _isLimitLayoutVisible = MutableStateFlow(false)
    val isLimitLayoutVisible: StateFlow<Boolean> = _isLimitLayoutVisible

    private val _historyCleared = MutableLiveData<Boolean>()
    val historyCleared: LiveData<Boolean> = _historyCleared

    private var lastAlertTime = 0L

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack

    private val _showAlertTrigger = MutableSharedFlow<String>()
    val showAlertTrigger: SharedFlow<String> = _showAlertTrigger

    // -----------------------------
    // Reactive Sources (Single Source of Truth)
    // -----------------------------

    val autoSave: StateFlow<Boolean> =
        getAutoSaveUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val speedAlertEnabled: StateFlow<Boolean> =
        getSpeedAlertUseCase()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val currentUnit: StateFlow<SpeedUnit> =
        observeSpeedUnitUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpeedUnit.KMH)

    // Always stored internally in KMH
    val rawSpeedLimitKmh: StateFlow<Int> =
        getSpeedLimitUseCase()
            .stateIn(viewModelScope, SharingStarted.Eagerly, 120)
    // UI display value (auto converts)
    val speedLimitDisplay: StateFlow<Int> =
        combine(rawSpeedLimitKmh, currentUnit) { rawValue, unit ->
            if (unit == SpeedUnit.MPH)
                (rawValue * 0.621371).toInt()
            else
                rawValue
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 120)

    // -----------------------------
    // UI Actions
    // -----------------------------


    fun toggleLimitLayout() {
        _isLimitLayoutVisible.value = !_isLimitLayoutVisible.value
    }

    fun updateSpeedLimit(uiValue: Int) {
        val safeValue = uiValue.coerceAtLeast(0)
        val unit = currentUnit.value

        val kmhValue = if (unit == SpeedUnit.MPH) {
            (safeValue / 0.621371).toInt()
        } else {
            safeValue
        }

        viewModelScope.launch {
            setSpeedLimitUseCase(kmhValue)
        }
    }

    fun setSpeedAlert(enabled: Boolean) {
        viewModelScope.launch {
            setSpeedAlertUseCase(enabled)
        }
    }

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            setAutoSaveUseCase(enabled)
        }
    }

    fun onUnitSelected(unit: SpeedUnit) {
        viewModelScope.launch {
            setSpeedUnitUseCase(unit)
        }
    }

    // -----------------------------
    // Speed Check Logic
    // -----------------------------

    // Inside SettingsViewModel.kt

    // Inside SettingsViewModel.kt

    fun checkSpeed(currentSpeedKmh: Float) { // Note: HomeFragment is passing speedKmh now
        android.util.Log.d("SpeedFlow", "CHECK CALLED: Speed KMH = $currentSpeedKmh")

        // 1. Safety check
        if (!speedAlertEnabled.value) return

        val limitKmh = rawSpeedLimitKmh.value
        android.util.Log.d("SpeedFlow", "Current: $currentSpeedKmh | Limit: $limitKmh")

        // 2. THE MISSING PART: The comparison and emission
        if (currentSpeedKmh > limitKmh) {
            val currentTime = System.currentTimeMillis()

            // 3. Cooldown check (5 seconds)
            if (currentTime - lastAlertTime <= 5000) return

            lastAlertTime = currentTime
            viewModelScope.launch {
                val displayVal = speedLimitDisplay.value
                val unitStr = if (currentUnit.value == SpeedUnit.KMH) "km/h" else "mph"

                // 4. This sends the message to the Fragment
                _showAlertTrigger.emit("Slow down! You exceeded the $displayVal $unitStr limit.")
            }
        }
    }




    fun clearAllHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
            _historyCleared.value = true
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _navigateBack.emit(Unit)
        }
    }
}
