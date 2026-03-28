package com.webscare.speedometer.data.repository

import com.webscare.speedometer.data.local.SpeedHistoryDao
import com.webscare.speedometer.data.mapper.toDomain
import com.webscare.speedometer.data.mapper.toEntity
import com.webscare.speedometer.domain.model.SpeedHistory
import com.webscare.speedometer.domain.repository.SpeedHistoryRepository
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
