package com.example.speedometerpro.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.speedometerpro.domain.model.SpeedUnit
import com.example.speedometerpro.domain.repository.SpeedUnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SpeedUnitRepositoryImpl(
    private val context: Context
) : SpeedUnitRepository {

    private val KEY_UNIT = stringPreferencesKey("speed_unit")

    override fun observeSpeedUnit(): Flow<SpeedUnit> {
        return context.dataStore.data.map { preferences ->
            val value = preferences[KEY_UNIT] ?: SpeedUnit.KMH.name
            SpeedUnit.valueOf(value)
        }
    }

    override suspend fun setSpeedUnit(unit: SpeedUnit) {
        context.dataStore.edit { preferences ->
            preferences[KEY_UNIT] = unit.name
        }
    }
}