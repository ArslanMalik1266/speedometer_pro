package com.webscare.speedometer.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.speedometer.util.DrawerDestination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ToolbarState { NORMAL, TRACKING }
class MainViewModel : ViewModel() {

    private val _drawerDestination = MutableSharedFlow<DrawerDestination>()
    val drawerDestination = _drawerDestination.asSharedFlow()

    private val _toolbarState = MutableStateFlow(ToolbarState.NORMAL)
    val toolbarState: StateFlow<ToolbarState> = _toolbarState.asStateFlow()

    fun onTrackingStarted() {
        _toolbarState.value = ToolbarState.TRACKING
    }

    fun onTrackingStopped() {
        _toolbarState.value = ToolbarState.NORMAL
    }

    fun onHistoryClicked() {
        viewModelScope.launch {
            _drawerDestination.emit(DrawerDestination.History)
        }
    }

    fun onSettingsClicked() {
        viewModelScope.launch {
            _drawerDestination.emit(DrawerDestination.Settings)
        }
    }
}