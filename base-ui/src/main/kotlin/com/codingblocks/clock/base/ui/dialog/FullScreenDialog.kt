package com.codingblocks.clock.base.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FullScreenDialog(
    onDismissRequest: () -> Unit,
    dialogContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
        .background(Color.Gray)
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            // only allow dismissRequests, to be handled in calling function (are you sure)...
            // dont close dialog on back press, as we cant show "are you sure"
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            )
        ) {
            dialogContent.invoke(this)
        }
    }
}