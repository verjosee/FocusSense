package com.focussense.app.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.focussense.app.presentation.components.FocusIcons

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", FocusIcons.Home)
    data object Focus : Screen("focus", "Focus", FocusIcons.Timer)
    data object Sensors : Screen("sensors", "Sensors", FocusIcons.Sensors)
    data object History : Screen("history", "History", FocusIcons.History)
    data object Statistics : Screen("statistics", "Stats", FocusIcons.Statistics)
    data object Settings : Screen("settings", "Settings", FocusIcons.Settings)

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Home, Focus, Sensors, History, Statistics)
    }
}
