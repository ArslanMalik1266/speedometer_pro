package com.webscare.speedometer.util

import com.webscare.speedometer.domain.model.SpeedHistory

sealed class HistoryListItem {
    data class DateHeader(val date: String) : HistoryListItem()
    data class SpeedItem(val data: SpeedHistory) : HistoryListItem()
}