package com.focussense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussense.app.data.repository.StudySessionRepository
import com.focussense.app.domain.model.DayStudyStat
import com.focussense.app.domain.model.StatisticsCalculator
import com.focussense.app.domain.model.StatisticsSummary
import com.focussense.app.domain.model.toDomainList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val summary: StatisticsSummary = StatisticsSummary(),
    val completionRate: Float = 0f,
    val totalSessionsCount: Int = 0,
    val weeklyBreakdown: List<DayStudyStat> = emptyList(),
    val isEmpty: Boolean = true,
    val isLoading: Boolean = true
)

class StatisticsViewModel(
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            studySessionRepository.getAllSessions().collect { entities ->
                val sessions = entities.toDomainList()
                val summary = StatisticsCalculator.calculateSummary(sessions)
                val rate = StatisticsCalculator.calculateCompletionRate(sessions)

                _uiState.update { current ->
                    current.copy(
                        summary = summary,
                        completionRate = rate,
                        totalSessionsCount = sessions.size,
                        weeklyBreakdown = summary.dailyBreakdown,
                        isEmpty = sessions.isEmpty(),
                        isLoading = false
                    )
                }
            }
        }
    }
}
