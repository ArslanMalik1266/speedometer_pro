package com.example.speedometerpro.domain.repository

import com.example.speedometerpro.domain.model.SpeedHistory
import kotlinx.coroutines.flow.Flow

interface  SpeedHistoryRepository {
    fun observeHistory(): Flow<List<SpeedHistory>>
    suspend fun insertHistory(history: SpeedHistory)
    suspend fun deleteHistory(id: Long)
    suspend fun getHistoryById(id: Long): SpeedHistory?
    suspend fun deleteAllHistory()
}


