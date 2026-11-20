package com.focussense.app.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.focussense.app.presentation.components.FocusIcons

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", FocusIcons.Home)
    object Focus : Screen("focus", "Focus", FocusIcons.Timer)
    object Sensors : Screen("sensors", "Sensors", FocusIcons.Sensors)
    object History : Screen("history", "History", FocusIcons.History)
    object Statistics : Screen("statistics", "Stats", FocusIcons.Statistics)
    object Settings : Screen("settings", "Settings", FocusIcons.Settings)

    companion object {
        val bottomNavItems = listOf(Home, Focus, Sensors, History, Statistics)
    }
}
