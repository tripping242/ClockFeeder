package com.codingblocks.clock.ui.feeds

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun FeedsScreen() {
    val mainNavHostController = rememberNavController()
    Surface {
        Text("Hello feeds")
    }
}
