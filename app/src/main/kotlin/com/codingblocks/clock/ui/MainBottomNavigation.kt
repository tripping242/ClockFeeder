package com.codingblocks.clock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.codingblocks.clock.base.ui.theme.AppTheme
import com.codingblocks.clock.navigation.BottomNav

@Composable
fun MainBottomNavigation(
    navController: NavController,
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Column(modifier = Modifier.fillMaxWidth()) {
        BottomNavigation(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = AppTheme.colors.background,
            contentColor = AppTheme.colors.onBackground,
        ) {
            BottomNav.entries.forEach { item ->
                val title = stringResource(id = item.screen.titleRes)
                BottomNavigationItem(
                    selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                    onClick = {
                        navController.navigate(item.screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }

                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = title,
                        )
                    },
                    label = {
                        Text(
                            title,
                        )
                    },
                )
            }
        }
        Box(
            modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars).fillMaxWidth()
                .background(AppTheme.colors.background),
        )
    }
}
