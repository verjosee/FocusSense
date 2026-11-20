package com.focussense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussense.app.data.repository.SettingsRepository
import com.focussense.app.data.repository.StudySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val defaultDurationMinutes: Int = 25,
    val movementAlertEnabled: Boolean = true,
    val showResetConfirmation: Boolean = false,
    val availableDurations: List<Int> = listOf(15, 25, 45, 60),
    val isDataCleared: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        themeMode = settings.themeMode,
                        defaultDurationMinutes = settings.defaultDurationMinutes,
                        movementAlertEnabled = settings.movementAlertEnabled
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setDefaultDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultDurationMinutes(minutes)
        }
    }

    fun setMovementAlertEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMovementAlertEnabled(enabled)
        }
    }

    fun openResetConfirmation() {
        _uiState.update { it.copy(showResetConfirmation = true) }
    }

    fun dismissResetConfirmation() {
        _uiState.update { it.copy(showResetConfirmation = false) }
    }

    fun confirmClearAllData() {
        viewModelScope.launch {
            studySessionRepository.clearAllSessions()
            settingsRepository.clearSettings()
            _uiState.update {
                it.copy(
                    showResetConfirmation = false,
                    isDataCleared = true
                )
            }
        }
    }
}
