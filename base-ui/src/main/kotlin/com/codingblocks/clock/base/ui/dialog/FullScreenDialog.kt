package com.codingblocks.clock.base.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.codingblocks.clock.base.ui.theme.AppTheme

@Composable
fun FullScreenDialog(
    onDismissRequest: () -> Unit,
    dialogContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppTheme.shapes.large,
                color = AppTheme.colors.surface,
                elevation = 24.dp
            ) {
                dialogContent.invoke(this)
            }
        }
    }
}

@Composable
fun FullWidthDialog(
    onDismissRequest: () -> Unit,
    dialogContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppTheme.shapes.large,
                color = AppTheme.colors.surface,
                elevation = 24.dp
            ) {
                dialogContent.invoke(this)
            }
        }
    }
}