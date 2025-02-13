package com.codingblocks.clock.ui.watchlists

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun WatchlistsScreen() {
    val mainNavHostController = rememberNavController()
    Surface {
        Text("Hello watchlists")
    }
}
