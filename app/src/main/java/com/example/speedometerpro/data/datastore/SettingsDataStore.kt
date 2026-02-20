package com.example.speedometerpro.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.speedometerpro.domain.model.SpeedUnit
import com.example.speedometerpro.domain.repository.SpeedUnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Property delegate for DataStore
private val Context.dataStore by preferencesDataStore(name = "settings_prefs")

class SettingsDataStore(context: Context) : SpeedUnitRepository {

    private val dataStore = context.dataStore

    companion object {
        private val AUTO_SAVE_KEY = booleanPreferencesKey("auto_save_trips")
        private val SPEED_UNIT_KEY = stringPreferencesKey("speed_unit")
        private val SPEED_ALERT_KEY = booleanPreferencesKey("speed_alert_enabled")
        private val SPEED_LIMIT_KEY = intPreferencesKey("speed_limit")
    }

    /** Auto Save Trips */
    val autoSaveFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[AUTO_SAVE_KEY] ?: true }

    suspend fun setAutoSave(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[AUTO_SAVE_KEY] = enabled
        }
    }

    /** Speed Unit Handling (Implementation of SpeedUnitRepository) */
    override fun observeSpeedUnit(): Flow<SpeedUnit> {
        return dataStore.data.map { prefs ->
            when (prefs[SPEED_UNIT_KEY]) {
                SpeedUnit.MPH.name -> SpeedUnit.MPH
                else -> SpeedUnit.KMH
            }
        }
    }

    override suspend fun setSpeedUnit(unit: SpeedUnit) {
        dataStore.edit { prefs ->
            prefs[SPEED_UNIT_KEY] = unit.name
        }
    }

    /** Speed Alert Toggle */
    fun isSpeedAlertEnabled(): Flow<Boolean> = dataStore.data
        .map { prefs ->
            prefs[SPEED_ALERT_KEY] ?: true
        }

    suspend fun setSpeedAlertEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[SPEED_ALERT_KEY] = enabled
        }
    }

    /** Speed Limit Value Persistence */
    fun getSpeedLimit(): Flow<Int> = dataStore.data
        .map { prefs ->
            prefs[SPEED_LIMIT_KEY] ?: 120
        }

    suspend fun saveSpeedLimit(limit: Int) {
        dataStore.edit { prefs ->
            prefs[SPEED_LIMIT_KEY] = limit
        }
    }
}