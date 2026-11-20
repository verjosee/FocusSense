package com.focussense.app.data.repository

import com.focussense.app.data.local.preferences.FocusSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<FocusSettings>
    val defaultDurationMinutesFlow: Flow<Int>
    val movementAlertEnabledFlow: Flow<Boolean>
    val themeModeFlow: Flow<String>

    suspend fun setDefaultDurationMinutes(minutes: Int)
    suspend fun setMovementAlertEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun updateSettings(durationMinutes: Int, movementAlert: Boolean, theme: String)
    suspend fun clearSettings()
}
