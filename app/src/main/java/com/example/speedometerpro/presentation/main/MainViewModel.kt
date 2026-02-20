package com.example.speedometerpro.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speedometerpro.util.DrawerDestination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _drawerDestination = MutableSharedFlow<DrawerDestination>()
    val drawerDestination = _drawerDestination.asSharedFlow()

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