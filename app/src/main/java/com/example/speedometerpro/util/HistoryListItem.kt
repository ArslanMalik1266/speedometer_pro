package com.example.speedometerpro.util

import com.example.speedometerpro.domain.model.SpeedHistory

sealed class HistoryListItem {
    data class DateHeader(val date: String) : HistoryListItem()
    data class SpeedItem(val data: SpeedHistory) : HistoryListItem()
}