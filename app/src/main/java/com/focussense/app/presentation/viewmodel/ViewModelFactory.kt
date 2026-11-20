package com.focussense.app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.focussense.app.data.local.FocusSenseDatabase
import com.focussense.app.data.local.preferences.FocusSettingsDataStore
import com.focussense.app.data.repository.SettingsRepository
import com.focussense.app.data.repository.SettingsRepositoryImpl
import com.focussense.app.data.repository.StudySessionRepository
import com.focussense.app.data.repository.StudySessionRepositoryImpl
import com.focussense.app.domain.timer.DefaultFocusTimerEngine
import com.focussense.app.domain.timer.FocusTimerEngine
import com.focussense.app.sensors.DefaultSensorRepository
import com.focussense.app.sensors.SensorRepository

class ViewModelFactory(
    private val studySessionRepository: StudySessionRepository,
    private val settingsRepository: SettingsRepository,
    private val sensorRepository: SensorRepository,
    private val timerEngine: FocusTimerEngine
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    studySessionRepository = studySessionRepository,
                    sensorRepository = sensorRepository,
                    timerEngine = timerEngine,
                    settingsRepository = settingsRepository
                ) as T
            }
            modelClass.isAssignableFrom(FocusTimerViewModel::class.java) -> {
                FocusTimerViewModel(
                    timerEngine = timerEngine,
                    studySessionRepository = studySessionRepository,
                    settingsRepository = settingsRepository
                ) as T
            }
            modelClass.isAssignableFrom(SensorsViewModel::class.java) -> {
                SensorsViewModel(
                    sensorRepository = sensorRepository
                ) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(
                    studySessionRepository = studySessionRepository
                ) as T
            }
            modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                StatisticsViewModel(
                    studySessionRepository = studySessionRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    settingsRepository = settingsRepository,
                    studySessionRepository = studySessionRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val appContext = context.applicationContext
                    val database = FocusSenseDatabase.getInstance(appContext)
                    val sessionRepo = StudySessionRepositoryImpl(database.studySessionDao())
                    val dataStore = FocusSettingsDataStore(appContext)
                    val settingsRepo = SettingsRepositoryImpl(dataStore)
                    val sensorRepo = DefaultSensorRepository(appContext)
                    val timerEngine = DefaultFocusTimerEngine(sensorRepository = sensorRepo)

                    ViewModelFactory(
                        studySessionRepository = sessionRepo,
                        settingsRepository = settingsRepo,
                        sensorRepository = sensorRepo,
                        timerEngine = timerEngine
                    ).also { INSTANCE = it }
                }
            }
        }

        fun resetInstance() {
            INSTANCE = null
        }
    }
}
