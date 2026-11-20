package com.focussense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.focussense.app.presentation.navigation.AppNavigation
import com.focussense.app.presentation.viewmodel.FocusTimerViewModel
import com.focussense.app.presentation.viewmodel.HistoryViewModel
import com.focussense.app.presentation.viewmodel.HomeViewModel
import com.focussense.app.presentation.viewmodel.SensorsViewModel
import com.focussense.app.presentation.viewmodel.SettingsViewModel
import com.focussense.app.presentation.viewmodel.StatisticsViewModel
import com.focussense.app.presentation.viewmodel.ViewModelFactory
import com.focussense.app.ui.theme.FocusSenseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = ViewModelFactory.getInstance(applicationContext)

        val homeViewModel: HomeViewModel by viewModels { factory }
        val focusTimerViewModel: FocusTimerViewModel by viewModels { factory }
        val sensorsViewModel: SensorsViewModel by viewModels { factory }
        val historyViewModel: HistoryViewModel by viewModels { factory }
        val statisticsViewModel: StatisticsViewModel by viewModels { factory }
        val settingsViewModel: SettingsViewModel by viewModels { factory }

        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val isDarkTheme = when (settingsUiState.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            FocusSenseTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    homeViewModel = homeViewModel,
                    focusTimerViewModel = focusTimerViewModel,
                    sensorsViewModel = sensorsViewModel,
                    historyViewModel = historyViewModel,
                    statisticsViewModel = statisticsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
