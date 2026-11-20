package com.focussense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussense.app.data.repository.SettingsRepository
import com.focussense.app.data.repository.StudySessionRepository
import com.focussense.app.domain.model.FocusSession
import com.focussense.app.domain.model.SessionStatus
import com.focussense.app.domain.model.toEntity
import com.focussense.app.domain.timer.FocusTimerEngine
import com.focussense.app.domain.timer.TimerMode
import com.focussense.app.domain.timer.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusTimerUiState(
    val timerState: TimerState = TimerState(),
    val selectedDurationMinutes: Int = 25,
    val isMotionAlertEnabled: Boolean = true,
    val showSaveDialog: Boolean = false,
    val pendingSession: FocusSession? = null,
    val sessionNotes: String = "",
    val availableDurations: List<Int> = listOf(15, 25, 45, 60)
)

class FocusTimerViewModel(
    val timerEngine: FocusTimerEngine,
    private val studySessionRepository: StudySessionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusTimerUiState())
    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()

    private var hasHandledCompletion = false

    init {
        observeSettings()
        observeTimerState()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.defaultDurationMinutesFlow.collect { defaultMinutes ->
                if (_uiState.value.timerState.mode == TimerMode.IDLE) {
                    _uiState.update { it.copy(selectedDurationMinutes = defaultMinutes) }
                }
            }
        }

        viewModelScope.launch {
            settingsRepository.movementAlertEnabledFlow.collect { alertEnabled ->
                _uiState.update { it.copy(isMotionAlertEnabled = alertEnabled) }
            }
        }
    }

    private fun observeTimerState() {
        viewModelScope.launch {
            timerEngine.timerState.collect { state ->
                val shouldShowMotionAlert = state.isMotionAlertActive && _uiState.value.isMotionAlertEnabled
                val adjustedState = state.copy(isMotionAlertActive = shouldShowMotionAlert)

                _uiState.update { current ->
                    current.copy(timerState = adjustedState)
                }

                if (state.mode == TimerMode.COMPLETED && !hasHandledCompletion) {
                    hasHandledCompletion = true
                    val session = timerEngine.buildAndConsumeSession(SessionStatus.COMPLETED)
                    if (session != null) {
                        _uiState.update { current ->
                            current.copy(
                                showSaveDialog = true,
                                pendingSession = session,
                                sessionNotes = ""
                            )
                        }
                    }
                } else if (state.mode == TimerMode.RUNNING) {
                    hasHandledCompletion = false
                }
            }
        }
    }

    fun selectDuration(minutes: Int) {
        if (_uiState.value.timerState.mode == TimerMode.IDLE) {
            _uiState.update { it.copy(selectedDurationMinutes = minutes) }
            timerEngine.reset(minutes)
        }
    }

    fun startTimer() {
        hasHandledCompletion = false
        val duration = _uiState.value.selectedDurationMinutes
        timerEngine.start(duration)
    }

    fun pauseTimer() {
        timerEngine.pause()
    }

    fun resumeTimer() {
        timerEngine.resume()
    }

    fun stopTimer(saveInterrupted: Boolean = true) {
        val session = timerEngine.stop()
        if (session != null && saveInterrupted && session.durationSeconds >= 10) {
            _uiState.update { current ->
                current.copy(
                    showSaveDialog = true,
                    pendingSession = session,
                    sessionNotes = ""
                )
            }
        } else {
            resetTimer()
        }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(sessionNotes = notes) }
    }

    fun savePendingSession() {
        val pending = _uiState.value.pendingSession
        val notes = _uiState.value.sessionNotes.trim()

        if (pending != null) {
            val sessionToSave = pending.copy(notes = notes.ifEmpty { null })
            viewModelScope.launch {
                studySessionRepository.insertSession(sessionToSave.toEntity())
                _uiState.update { current ->
                    current.copy(
                        showSaveDialog = false,
                        pendingSession = null,
                        sessionNotes = ""
                    )
                }
                resetTimer()
            }
        } else {
            _uiState.update { it.copy(showSaveDialog = false) }
            resetTimer()
        }
    }

    fun discardPendingSession() {
        _uiState.update { current ->
            current.copy(
                showSaveDialog = false,
                pendingSession = null,
                sessionNotes = ""
            )
        }
        resetTimer()
    }

    fun resetTimer() {
        hasHandledCompletion = false
        val defaultMinutes = _uiState.value.selectedDurationMinutes
        timerEngine.reset(defaultMinutes)
    }

    fun dismissMotionAlert() {
        timerEngine.dismissMotionAlert()
    }
}
