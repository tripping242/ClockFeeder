package com.codingblocks.clock.navigation.destinations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.codingblocks.clock.ui.settings.SettingsScreen

internal const val ROUTE_SETTINGS: String = "settings"

internal fun NavGraphBuilder.settingsScreen() {
    composable(ROUTE_SETTINGS) {
        SettingsScreen()
    }
}

internal fun NavController.navigateToSettings() = this.navigate(ROUTE_SETTINGS)
