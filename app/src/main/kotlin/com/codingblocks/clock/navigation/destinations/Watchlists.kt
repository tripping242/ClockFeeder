package com.codingblocks.clock.navigation.destinations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.codingblocks.clock.ui.watchlists.WatchlistsScreen

internal const val ROUTE_WATCHLISTS: String = "watchlists"

internal fun NavGraphBuilder.watchListsScreen() {
    composable(ROUTE_WATCHLISTS) {
        WatchlistsScreen()
    }
}

internal fun NavController.navigateToWatchLists() = this.navigate(ROUTE_WATCHLISTS)
