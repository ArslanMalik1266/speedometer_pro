package com.example.speedometerpro.presentation.customview

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speedometerpro.data.repository.LocationRepositoryImpl
import com.example.speedometerpro.domain.model.SpeedHistory
import com.example.speedometerpro.domain.model.SpeedUnit
import com.example.speedometerpro.domain.usecase.CalculateSessionUseCase
import com.example.speedometerpro.domain.usecase.GetLocationUpdatesUseCase
import com.example.speedometerpro.domain.usecase.formatSpeedUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TrackingState { IDLE, TRACKING, PAUSED }
enum class LocationPermissionState { GRANTED, DENIED }

class SpeedometerViewModel(
    private val calculateSessionUseCase: CalculateSessionUseCase,
    val speedUnitFlow: Flow<SpeedUnit>,
    private val getLocationUpdatesUseCase: GetLocationUpdatesUseCase,
    private val locationRepository: LocationRepositoryImpl
) : ViewModel() {

    private val _locationPermissionState = MutableStateFlow(LocationPermissionState.DENIED)
    val locationPermissionState: StateFlow<LocationPermissionState> = _locationPermissionState.asStateFlow()

    fun setLocationPermissionGranted(granted: Boolean) {
        _locationPermissionState.value =
            if (granted) LocationPermissionState.GRANTED else LocationPermissionState.DENIED
    }

    fun startLocationUpdatesIfPermitted() {
        if (_locationPermissionState.value == LocationPermissionState.GRANTED) {
            startLocationUpdates()
        }
    }
    /** Raw speed in km/h */
    private val _rawSpeed = MutableStateFlow(0.0)
    val currentSpeed: StateFlow<Float> = _rawSpeed.map { it.toFloat() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    private val _maxSpeed = MutableStateFlow(0f)
    val maxSpeed: StateFlow<Float> = _maxSpeed.asStateFlow()

    private val _distance = MutableStateFlow(0f)
    val distance: StateFlow<Float> = _distance.asStateFlow()

    private val _trackingTime = MutableStateFlow(0L)
    val trackingTime: StateFlow<Long> = _trackingTime

    private val _trackingState = MutableStateFlow(TrackingState.IDLE)
    val trackingState: StateFlow<TrackingState> = _trackingState

    private var lastLocation: Location? = null
    private var locationJob: Job? = null
    private var timerJob: Job? = null

    private val _locationState = MutableStateFlow<Location?>(null)
    val locationState: StateFlow<Location?> = _locationState.asStateFlow()

    /** Conversion helpers */
    private fun kmhToMph(speedKmh: Float) = speedKmh / 1.60934f
    private fun metersToMiles(distanceMeters: Float) = distanceMeters / 1609.34f
    private fun metersToKm(distanceMeters: Float) = distanceMeters / 1000f

    /** Speed formatted for UI */
    val formattedSpeed: StateFlow<String> = combine(_rawSpeed, speedUnitFlow) { speedKmh, unit ->
        val displayed = when (unit) {
            SpeedUnit.KMH -> speedKmh
            SpeedUnit.MPH -> kmhToMph(speedKmh.toFloat()).toDouble()
        }
        "%.0f".format(displayed)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "0")

    /** Max speed formatted for UI */
    val formattedMaxSpeed: StateFlow<String> = combine(_maxSpeed, speedUnitFlow) { speed, unit ->
        val displayed = when (unit) {
            SpeedUnit.KMH -> speed
            SpeedUnit.MPH -> kmhToMph(speed)
        }
        "%.0f".format(displayed)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "0")

    /** Avg speed in km/h */
    private val _avgSpeed = combine(_distance, _trackingTime) { dist, time ->
        if (time > 0) (dist / 1000f) / (time / 3600f) else 0f
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    /** Avg speed formatted for UI */
    val formattedAvgSpeed: StateFlow<String> = combine(_avgSpeed, speedUnitFlow) { speed, unit ->
        val displayed = when (unit) {
            SpeedUnit.KMH -> speed
            SpeedUnit.MPH -> kmhToMph(speed)
        }
        "%.0f".format(displayed)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "0")

    /** Distance in selected unit */
    val distanceInSelectedUnit: StateFlow<Float> = combine(_distance, speedUnitFlow) { dist, unit ->
        when (unit) {
            SpeedUnit.KMH -> metersToKm(dist)
            SpeedUnit.MPH -> metersToMiles(dist)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    /** Start tracking */
    fun startTracking() {
        if (_trackingState.value == TrackingState.TRACKING) return
        _trackingState.value = TrackingState.TRACKING
        startTimer()
        startLocationUpdates()
    }

    /** Pause tracking */
    fun pauseTracking() {
        if (_trackingState.value != TrackingState.TRACKING) return
        _trackingState.value = TrackingState.PAUSED
        timerJob?.cancel()
    }

    /** Resume tracking */
    fun resumeTracking() {
        if (_trackingState.value != TrackingState.PAUSED) return
        _trackingState.value = TrackingState.TRACKING
        startTimer()
    }

    /** Stop tracking */
    fun stopTracking() {
        _trackingState.value = TrackingState.IDLE
        timerJob?.cancel()
        stopLocationUpdates()
        _rawSpeed.value = 0.0
        resetMetrics()
    }

    /** Reset metrics */
    fun resetMetrics() {
        lastLocation = null
        _distance.value = 0f
        _maxSpeed.value = 0f
        _trackingTime.value = 0L
        _rawSpeed.value = 0.0
        _locationState.value = null
    }

    /** Timer coroutine */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_trackingState.value == TrackingState.TRACKING) {
                kotlinx.coroutines.delay(1000)
                _trackingTime.value += 1
            }
        }
    }

    /** Location updates */
    fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            getLocationUpdatesUseCase().collect { location ->
                if (_trackingState.value == TrackingState.TRACKING) {
                    _locationState.value = location
                    updateMetrics(location)
                }
            }
        }
    }

    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    /** Update metrics on new location */
    private fun updateMetrics(location: Location) {
        val speedKmh = maxOf(0f, location.speed * 3.6f)
        _rawSpeed.value = speedKmh.toDouble()
        if (speedKmh > _maxSpeed.value) _maxSpeed.value = speedKmh

        val currentLastLocation = lastLocation
        if (currentLastLocation != null) {
            val deltaMeters = currentLastLocation.distanceTo(location)
            if (deltaMeters >= 0.5f && deltaMeters < 100f) {
                _distance.value += deltaMeters
            }
        }
        lastLocation = location
    }

    /** Save current session */
    fun saveCurrentSession(): SpeedHistory {
        return calculateSessionUseCase.calculate(
            distanceMeters = _distance.value,
            maxSpeedKmh = _maxSpeed.value,
            trackingTimeSeconds = _trackingTime.value,
            currentSpeedKmh = _rawSpeed.value.toFloat()
        )
    }
}
