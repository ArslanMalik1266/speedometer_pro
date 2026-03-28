package com.webscare.speedometer.util

sealed class SpeedometerUiState {
    object Idle : SpeedometerUiState()
    object Running : SpeedometerUiState()
    object Paused : SpeedometerUiState()
}