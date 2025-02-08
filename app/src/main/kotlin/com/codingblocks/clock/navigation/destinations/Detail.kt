package com.codingblocks.clock.navigation.destinations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.codingblocks.clock.navigation.getIntArgOrNull
import com.codingblocks.clock.ui.detail.DetailScreen

internal const val ROUTE_DETAIL_WITH_ARGS: String = "detail/{id}"

internal fun NavGraphBuilder.detailScreen() {
    composable(ROUTE_DETAIL_WITH_ARGS) { navBackStackEntry ->
        DetailScreen(id = navBackStackEntry.getIntArgOrNull("id"))
    }
}

internal fun NavController.navigateToDetail(id: Int) =
    this.navigate(ROUTE_DETAIL_WITH_ARGS.replace("{id}", id.toString()))
