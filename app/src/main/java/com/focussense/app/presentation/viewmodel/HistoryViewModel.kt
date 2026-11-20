package com.focussense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussense.app.data.repository.StudySessionRepository
import com.focussense.app.domain.model.FocusSession
import com.focussense.app.domain.model.SessionStatus
import com.focussense.app.domain.model.toDomainList
import com.focussense.app.domain.model.toEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val sessions: List<FocusSession> = emptyList(),
    val filteredSessions: List<FocusSession> = emptyList(),
    val selectedFilter: SessionStatus? = null,
    val sessionToEditNotes: FocusSession? = null,
    val editedNotesText: String = "",
    val sessionToDelete: FocusSession? = null,
    val showClearAllDialog: Boolean = false,
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            studySessionRepository.getAllSessions().collect { entities ->
                val domainList = entities.toDomainList()
                _uiState.update { current ->
                    val filtered = applyFilter(domainList, current.selectedFilter)
                    current.copy(
                        sessions = domainList,
                        filteredSessions = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setFilter(status: SessionStatus?) {
        _uiState.update { current ->
            val filtered = applyFilter(current.sessions, status)
            current.copy(
                selectedFilter = status,
                filteredSessions = filtered
            )
        }
    }

    private fun applyFilter(sessions: List<FocusSession>, filter: SessionStatus?): List<FocusSession> {
        return if (filter == null) {
            sessions
        } else {
            sessions.filter { it.status == filter }
        }
    }

    fun openEditNotesDialog(session: FocusSession) {
        _uiState.update { current ->
            current.copy(
                sessionToEditNotes = session,
                editedNotesText = session.notes ?: ""
            )
        }
    }

    fun onNotesTextChanged(text: String) {
        _uiState.update { it.copy(editedNotesText = text) }
    }

    fun saveEditedNotes() {
        val session = _uiState.value.sessionToEditNotes ?: return
        val updatedNotes = _uiState.value.editedNotesText.trim().ifEmpty { null }

        viewModelScope.launch {
            studySessionRepository.updateNotes(session.id, updatedNotes)
            _uiState.update { current ->
                current.copy(
                    sessionToEditNotes = null,
                    editedNotesText = ""
                )
            }
        }
    }

    fun dismissEditNotesDialog() {
        _uiState.update { current ->
            current.copy(
                sessionToEditNotes = null,
                editedNotesText = ""
            )
        }
    }

    fun openDeleteConfirmation(session: FocusSession) {
        _uiState.update { it.copy(sessionToDelete = session) }
    }

    fun confirmDeleteSession() {
        val session = _uiState.value.sessionToDelete ?: return
        viewModelScope.launch {
            studySessionRepository.deleteSessionById(session.id)
            _uiState.update { it.copy(sessionToDelete = null) }
        }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(sessionToDelete = null) }
    }

    fun openClearAllConfirmation() {
        _uiState.update { it.copy(showClearAllDialog = true) }
    }

    fun confirmClearAll() {
        viewModelScope.launch {
            studySessionRepository.clearAllSessions()
            _uiState.update { it.copy(showClearAllDialog = false) }
        }
    }

    fun dismissClearAllConfirmation() {
        _uiState.update { it.copy(showClearAllDialog = false) }
    }
}
