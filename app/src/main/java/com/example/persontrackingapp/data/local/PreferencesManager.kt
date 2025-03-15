package com.example.persontrackingapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "location_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val trackingEnabledKey = booleanPreferencesKey("tracking_enabled")
    
    val isTrackingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[trackingEnabledKey] ?: false
        }
    
    suspend fun setTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[trackingEnabledKey] = enabled
        }
    }
} 