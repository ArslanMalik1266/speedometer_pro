package com.example.speedometerpro.data.repository

import com.example.speedometerpro.data.local.SpeedHistoryDao
import com.example.speedometerpro.data.mapper.toDomain
import com.example.speedometerpro.data.mapper.toEntity
import com.example.speedometerpro.domain.model.SpeedHistory
import com.example.speedometerpro.domain.repository.SpeedHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SpeedHistoryRepositoryImpl (
    private val dao: SpeedHistoryDao
) : SpeedHistoryRepository {

    override fun observeHistory(): Flow<List<SpeedHistory>> {
        return dao.observeHistory()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getHistoryById(id: Long): SpeedHistory? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun insertHistory(history: SpeedHistory) {
        dao.insert(history.toEntity())
    }

    override suspend fun deleteHistory(id: Long) {
        dao.delete(id)
    }

    override suspend fun deleteAllHistory() {
        dao.clearAll()
    }
}
