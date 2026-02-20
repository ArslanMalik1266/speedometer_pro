package com.example.speedometerpro.presentation.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speedometerpro.data.repository.SpeedHistoryRepositoryImpl
import com.example.speedometerpro.domain.model.SpeedHistory
import com.example.speedometerpro.domain.usecase.GetAutoSaveUseCase
import com.example.speedometerpro.domain.usecase.InsertSpeedHistoryUseCase
import com.example.speedometerpro.domain.usecase.ObserveSpeedHistoryUseCase
import com.example.speedometerpro.util.HistoryListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SpeedHistoryViewModel(
    private val repository: SpeedHistoryRepositoryImpl,
    private val getAutoSaveUseCase: GetAutoSaveUseCase
) : ViewModel() {

    private val getAllSpeedsUseCase = ObserveSpeedHistoryUseCase(repository)
    private val insertSpeedUseCase = InsertSpeedHistoryUseCase(repository)

    private val _historyItems = MutableLiveData<List<HistoryListItem>>()
    val historyItems: LiveData<List<HistoryListItem>> get() = _historyItems

    private val _closeScreen = MutableSharedFlow<Unit>()
    val closeScreen = _closeScreen.asSharedFlow()

    init {
        loadHistory()
    }

    /**
     * Saves a trip only if Auto Save is enabled.
     */
    fun saveTrip(trip: SpeedHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            val autoSave = getAutoSaveUseCase().first()
            if (autoSave) {
                repository.insertHistory(trip) // Insert the pre-built object
                loadHistory()
            }
        }
    }

    /**
     * Inserts a new trip with Auto Save check.
     */
    fun insertSpeedHistory(
        distance: Double,
        avgSpeed: Double,
        maxSpeed: Double,
        trackingTimeMillis: Long,
        currentSpeed: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val autoSaveEnabled = getAutoSaveUseCase().first() // check current Auto Save
            if (!autoSaveEnabled) return@launch  // skip insert if Auto Save OFF

            val newHistory = SpeedHistory(
                id = 0,  // Room will auto-generate
                distance = distance,
                avg_speed = avgSpeed,
                max_speed = maxSpeed,
                timeStamp = System.currentTimeMillis(),
                durationMillis = trackingTimeMillis,
                current_speed = currentSpeed
            )

            insertSpeedUseCase(newHistory)
            loadHistory() // refresh LiveData
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _closeScreen.emit(Unit)
        }
    }

    /**
     * Loads all trips from DB and groups by date for UI.
     */
    private fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            getAllSpeedsUseCase().collectLatest { list ->
                val uiItems = mapToHistoryItems(list)
                _historyItems.postValue(uiItems)
            }
        }
    }

    private fun mapToHistoryItems(history: List<SpeedHistory>): List<HistoryListItem> {
        val result = mutableListOf<HistoryListItem>()
        val grouped = history.groupBy { timestamp ->
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp.timeStamp))
        }

        grouped.forEach { (date, items) ->
            result.add(HistoryListItem.DateHeader(date))
            items.forEach { historyItem ->
                result.add(HistoryListItem.SpeedItem(historyItem))
            }
        }

        return result
    }
}
