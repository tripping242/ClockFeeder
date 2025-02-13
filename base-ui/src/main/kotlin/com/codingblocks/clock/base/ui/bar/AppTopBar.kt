package com.codingblocks.clock.base.ui.bar

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.codingblocks.clock.base.ui.theme.AppTheme

@SuppressLint("ComposeModifierMissing")
@Composable
fun AppTopBar(
    title: String,
    usesTonalElevation: Boolean = true,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    color: Color = AppTheme.colors.surface,
) = TopAppBar(
    title = { Text(text = title) },
    navigationIcon = navigationIcon,
    actions = actions,
)
