package com.focussense.app.data.repository

import com.focussense.app.data.local.preferences.FocusSettings
import com.focussense.app.data.local.preferences.FocusSettingsDataStore
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val dataStore: FocusSettingsDataStore
) : SettingsRepository {

    override val settingsFlow: Flow<FocusSettings> = dataStore.settingsFlow

    override val defaultDurationMinutesFlow: Flow<Int> = dataStore.defaultDurationMinutesFlow

    override val movementAlertEnabledFlow: Flow<Boolean> = dataStore.movementAlertEnabledFlow

    override val themeModeFlow: Flow<String> = dataStore.themeModeFlow

    override suspend fun setDefaultDurationMinutes(minutes: Int) {
        dataStore.setDefaultDurationMinutes(minutes)
    }

    override suspend fun setMovementAlertEnabled(enabled: Boolean) {
        dataStore.setMovementAlertEnabled(enabled)
    }

    override suspend fun setThemeMode(mode: String) {
        dataStore.setThemeMode(mode)
    }

    override suspend fun updateSettings(durationMinutes: Int, movementAlert: Boolean, theme: String) {
        dataStore.updateSettings(durationMinutes, movementAlert, theme)
    }

    override suspend fun clearSettings() {
        dataStore.clearSettings()
    }
}
