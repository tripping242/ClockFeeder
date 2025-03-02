package com.codingblocks.clock.base.ui.button

import android.annotation.SuppressLint
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.codingblocks.clock.base.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SingleChoiceSegmentedButton(
    options: ImmutableList<String>,
    selected: Int?,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(selected ?: -1) }

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = AppTheme.colors.background,
                    activeContentColor = AppTheme.colors.onBackground,
                    inactiveContainerColor = AppTheme.colors.surface,
                    inactiveContentColor = AppTheme.colors.onSurface
                ),
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                onClick = {
                    selectedIndex = index
                    onSelected(index)
                },
                selected = index == selectedIndex,
                label = { Text(label) },
            )
        }
    }
}
@Composable
fun MultiChoiceTwoSegmentedButton(
    options: ImmutableList<String>,
    @SuppressLint("ComposeUnstableCollections") selectedOptions: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {


    MultiChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            val isChecked = selectedOptions.contains(index)
            SegmentedButton(
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = AppTheme.colors.background,
                    activeContentColor = AppTheme.colors.onBackground,
                    inactiveContainerColor = AppTheme.colors.surface,
                    inactiveContentColor = AppTheme.colors.onSurface
                ),
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                checked = isChecked,
                onCheckedChange = {
                    val newSelection = selectedOptions.toMutableSet()
                    if (isChecked) {
                        newSelection.remove(index)  // Deselect
                    } else {
                        newSelection.add(index)  // Select
                    }
                    onSelectionChange(newSelection)  // Update state
                },
                icon = { SegmentedButtonDefaults.Icon(isChecked) },
                label = { Text(label) },
            )
        }
    }
}