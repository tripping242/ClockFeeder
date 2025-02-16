package com.codingblocks.clock.base.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckBoxRowWithText(
    text: String,
    onCheckedChanged: (Boolean) -> Unit,
    checkedState: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Checkbox(
            enabled = enabled,
            checked = checkedState,
            onCheckedChange = onCheckedChanged,
            modifier = Modifier
                .padding(end = 8.dp),
        )
        Text(
            text = text,
        )
    }
}