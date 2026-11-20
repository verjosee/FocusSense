package com.focussense.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.focusDataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_settings")

data class FocusSettings(
    val defaultDurationMinutes: Int = 25,
    val movementAlertEnabled: Boolean = true,
    val themeMode: String = "SYSTEM" // "SYSTEM", "DARK", "LIGHT"
)

class FocusSettingsDataStore(private val context: Context) {

    companion object {
        val KEY_DEFAULT_DURATION_MINUTES = intPreferencesKey("default_duration_minutes")
        val KEY_MOVEMENT_ALERT_ENABLED = booleanPreferencesKey("movement_alert_enabled")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

        const val DEFAULT_DURATION_MINUTES = 25
        const val DEFAULT_MOVEMENT_ALERT_ENABLED = true
        const val DEFAULT_THEME_MODE = "SYSTEM"
    }

    val settingsFlow: Flow<FocusSettings> = context.focusDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            FocusSettings(
                defaultDurationMinutes = preferences[KEY_DEFAULT_DURATION_MINUTES] ?: DEFAULT_DURATION_MINUTES,
                movementAlertEnabled = preferences[KEY_MOVEMENT_ALERT_ENABLED] ?: DEFAULT_MOVEMENT_ALERT_ENABLED,
                themeMode = preferences[KEY_THEME_MODE] ?: DEFAULT_THEME_MODE
            )
        }

    val defaultDurationMinutesFlow: Flow<Int> = context.focusDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_DEFAULT_DURATION_MINUTES] ?: DEFAULT_DURATION_MINUTES
        }

    val movementAlertEnabledFlow: Flow<Boolean> = context.focusDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_MOVEMENT_ALERT_ENABLED] ?: DEFAULT_MOVEMENT_ALERT_ENABLED
        }

    val themeModeFlow: Flow<String> = context.focusDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_THEME_MODE] ?: DEFAULT_THEME_MODE
        }

    suspend fun setDefaultDurationMinutes(minutes: Int) {
        context.focusDataStore.edit { preferences ->
            preferences[KEY_DEFAULT_DURATION_MINUTES] = minutes
        }
    }

    suspend fun setMovementAlertEnabled(enabled: Boolean) {
        context.focusDataStore.edit { preferences ->
            preferences[KEY_MOVEMENT_ALERT_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.focusDataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun updateSettings(durationMinutes: Int, movementAlert: Boolean, theme: String) {
        context.focusDataStore.edit { preferences ->
            preferences[KEY_DEFAULT_DURATION_MINUTES] = durationMinutes
            preferences[KEY_MOVEMENT_ALERT_ENABLED] = movementAlert
            preferences[KEY_THEME_MODE] = theme
        }
    }

    suspend fun clearSettings() {
        context.focusDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
