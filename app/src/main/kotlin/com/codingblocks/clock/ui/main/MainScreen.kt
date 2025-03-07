package com.codingblocks.clock.ui.main

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(initNavigation: @Composable (navHostController: NavHostController) -> Unit) {
    val mainNavHostController = rememberNavController()
    Surface {
        initNavigation(mainNavHostController)
    }
}
