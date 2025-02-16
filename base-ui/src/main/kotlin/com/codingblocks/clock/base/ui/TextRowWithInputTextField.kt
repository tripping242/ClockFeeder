package com.codingblocks.clock.base.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TextRowWithIntegerInputTextField(
    text: String,
    amount: Int,
    hint: String?,
    onAmountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var isError by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(end = 8.dp),
        )
        TextField(
            enabled = enabled,
            value = if (amount != 0) amount.toString() else "",
            onValueChange = { value ->
                if (value.isEmpty() || value.matches(Regex("^\\d+\$"))) {
                    onAmountChanged(value.toInt())
                    isError = value.isNotEmpty() && value.toInt() <= 0
                } else {
                    isError = true
                }
            },
            singleLine = true,
            label = { hint?.let { Text(text = hint)}},
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            isError = isError,
        )
    }
}
@Composable
fun TextRowWithStringInputTextField(
    text: String,
    input: String,
    onInputChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(end = 8.dp),
        )
        TextField(
            value = input,
            onValueChange = onInputChanged,
        )
    }
}
