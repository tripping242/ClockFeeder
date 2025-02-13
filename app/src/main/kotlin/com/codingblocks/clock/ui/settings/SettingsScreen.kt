package com.codingblocks.clock.ui.settings

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SettingsScreen() {
    val mainNavHostController = rememberNavController()
    Surface {
        Text("Hello settings")
    }
}
