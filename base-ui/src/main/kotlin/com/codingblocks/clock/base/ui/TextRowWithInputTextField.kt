package com.codingblocks.clock.base.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Checkbox
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
//noinspection UsingMaterialAndMaterial3Libraries
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
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = text,
            maxLines = 4,
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(0.5f),
        )
        TextField(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(0.5f),
            enabled = enabled,
            value = if (amount != 0) amount.toString() else "",
            onValueChange = { value ->
                if (value.isEmpty() || value.matches(Regex("^\\d+\$"))) {
                    val intValue: Int? = try { value.toInt() } catch(e: Exception) { null }
                    isError = intValue == null || value.isNotEmpty() && intValue <= 0
                    if (!isError) onAmountChanged(intValue!!) else if (value.isEmpty()) onAmountChanged(0)
                } else {
                    isError = true
                }
            },
            singleLine = true,
            label = { hint?.let { Text(text = hint) } },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            isError = isError,
        )
    }
}
@Composable
fun TextRowWithDoubleInputTextField(
    text: String,
    amount: Double,
    hint: String?,
    onAmountChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var isError by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = text,
            maxLines = 4,
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(0.5f),
        )
        TextField(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(0.5f),
            enabled = enabled,
            value = if (amount != 0.0) amount.toString() else "",
            onValueChange = { value ->
                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    val doubleValue: Double? = try { value.toDouble() } catch (e: Exception) { null }
                    isError = doubleValue == null || (value.isNotEmpty() && doubleValue <= 0)

                    if (!isError) {
                        onAmountChanged(doubleValue!!)
                    } else if (value.isEmpty()) {
                        onAmountChanged(0.0) // Default to 0 when empty
                    }
                } else {
                    isError = true
                }
            },
            singleLine = true,
            label = { hint?.let { Text(text = hint) } },
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
