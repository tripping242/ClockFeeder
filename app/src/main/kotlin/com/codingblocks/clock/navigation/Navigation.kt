package com.codingblocks.clock.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.codingblocks.clock.R
import com.codingblocks.clock.navigation.destinations.ROUTE_FEEDS
import com.codingblocks.clock.navigation.destinations.ROUTE_SETTINGS
import com.codingblocks.clock.navigation.destinations.ROUTE_WATCHLISTS

sealed class Screen(@StringRes val titleRes: Int, val route: String) {
    object Watchlists : Screen(R.string.screen_watchlists, ROUTE_WATCHLISTS)
    object Feeds : Screen(R.string.screen_feeds, ROUTE_FEEDS)
    object Settings : Screen(R.string.screen_settings, ROUTE_SETTINGS)
}

enum class BottomNav(
    val screen: Screen,
    val icon: ImageVector,
) {
    WatchLists(Screen.Watchlists, Icons.Outlined.List),
    Feeds(Screen.Feeds, Icons.Outlined.AddAlert),
    Settings(Screen.Settings, Icons.Outlined.Settings),
}
