package com.example.speedometerpro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedHistoryDao {

    @Query("SELECT * FROM speed_history ORDER BY timestamp DESC")
    fun observeHistory(): Flow<List<SpeedHistoryEntity>>

    @Query("SELECT * FROM speed_history WHERE id = :id")
    suspend fun getById(id: Long): SpeedHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SpeedHistoryEntity)

    @Query("DELETE FROM speed_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM speed_history")
    suspend fun clearAll()
}
