package com.codingblocks.clock.base.ui.button

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.codingblocks.clock.base.ui.icon.AppIcon
import com.codingblocks.clock.base.ui.theme.AppTheme

@Composable
fun AppIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = AppTheme.colors.onBackground,
) = IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
    AppIcon(icon = icon, tint = tint)
}
