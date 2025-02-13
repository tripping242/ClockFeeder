package com.codingblocks.clock.navigation.destinations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.codingblocks.clock.ui.feeds.FeedsScreen

internal const val ROUTE_FEEDS: String = "feeds"

internal fun NavGraphBuilder.feedsScreen() {
    composable(ROUTE_FEEDS) {
        FeedsScreen()
    }
}

internal fun NavController.navigateToFeeds() = this.navigate(ROUTE_FEEDS)
