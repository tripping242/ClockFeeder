/*
 * Copyright 2020 Tailored Media GmbH.
 * Created by Florian Schuster.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codingblocks.clock.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
//import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.AppTheme
import com.codingblocks.clock.core.NotificationActions
import com.codingblocks.clock.navigation.AppNavHost
import timber.log.Timber

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val view = LocalView.current
            val darkTheme = isSystemInDarkTheme()

            MainView()

            SideEffect {
                val window = this.window

                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }

                val windowsInsetsController = WindowCompat.getInsetsController(window, view)

                windowsInsetsController.isAppearanceLightStatusBars = !darkTheme
                windowsInsetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }

        if (intent?.action == NotificationActions.ACTION_OPEN_FEEDS) {
            // navigateToFeeds()
            Timber.tag("wims").i("navigate to feed?")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainView() {
    val scaffoldState = rememberScaffoldState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentDestinationRoute = currentDestination?.route
    val isBottomNavigationVisible = true
    // BottomNav.entries.any { it.screen.route == currentDestinationRoute }

    AppTheme {
        AppScaffold(
            scaffoldState = scaffoldState,
            bottomBar = {
                if (isBottomNavigationVisible) {
                    MainBottomNavigation(
                        navController = navController,
                    )
                }
            },
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    //.consumedWindowInsets(contentPadding)
                    .padding(contentPadding)
                    .imePadding(),
            ) {
                navController.AppNavHost()
            }
        }
    }
}

@Preview(name = "MainView")
@Composable
private fun MainViewPreview() {
    MainView()
}

@Preview(name = "MainView Night", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MainViewPreviewNight() {
    MainView()
}
