package com.focussense.app.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focussense.app.presentation.components.FocusBottomBar
import com.focussense.app.presentation.components.FocusSensorsTopAppBar
import com.focussense.app.presentation.screens.focus.FocusScreen
import com.focussense.app.presentation.screens.history.HistoryScreen
import com.focussense.app.presentation.screens.home.HomeScreen
import com.focussense.app.presentation.screens.sensors.SensorsScreen
import com.focussense.app.presentation.screens.settings.SettingsScreen
import com.focussense.app.presentation.screens.statistics.StatisticsScreen
import com.focussense.app.presentation.viewmodel.FocusTimerViewModel
import com.focussense.app.presentation.viewmodel.HistoryViewModel
import com.focussense.app.presentation.viewmodel.HomeViewModel
import com.focussense.app.presentation.viewmodel.SensorsViewModel
import com.focussense.app.presentation.viewmodel.SettingsViewModel
import com.focussense.app.presentation.viewmodel.StatisticsViewModel

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    focusTimerViewModel: FocusTimerViewModel,
    sensorsViewModel: SensorsViewModel,
    historyViewModel: HistoryViewModel,
    statisticsViewModel: StatisticsViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val isTopLevelDestination = Screen.bottomNavItems.any { it.route == currentRoute }

    val currentTitle = when (currentRoute) {
        Screen.Home.route -> "FocusSense"
        Screen.Focus.route -> "Focus Timer"
        Screen.Sensors.route -> "Sensors"
        Screen.History.route -> "History"
        Screen.Statistics.route -> "Statistics"
        Screen.Settings.route -> "Settings"
        else -> "FocusSense"
    }

    Scaffold(
        topBar = {
            FocusSensorsTopAppBar(
                title = currentTitle,
                canNavigateBack = !isTopLevelDestination,
                onNavigateBack = { navController.popBackStack() },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                    }
                },
                showSettingsButton = isTopLevelDestination
            )
        },
        bottomBar = {
            if (isTopLevelDestination) {
                FocusBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToFocus = {
                        navController.navigate(Screen.Focus.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSensors = {
                        navController.navigate(Screen.Sensors.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Focus.route) {
                FocusScreen(viewModel = focusTimerViewModel)
            }

            composable(Screen.Sensors.route) {
                SensorsScreen(viewModel = sensorsViewModel)
            }

            composable(Screen.History.route) {
                HistoryScreen(viewModel = historyViewModel)
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = statisticsViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
