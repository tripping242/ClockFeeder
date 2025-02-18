package com.codingblocks.clock.base.ui.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableCard(
    isExpanded: Boolean,
    onClick: () -> Unit,
    topContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
            ) { topContent.invoke() }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 200))
            ) {
                expandedContent.invoke()
            }
        }
        /*SubcomposeLayout { constraints ->
            val mainContent = subcompose("mainContent") { topContent() }
            val mainPlaceables = mainContent.map { it.measure(constraints) }

            val expandContent = if (isExpanded) {
                subcompose("expandedContent") { expandedContent() }
            } else {
                emptyList()
            }
            val expandedPlaceables = expandContent.map { it.measure(constraints) }

            layout(constraints.maxWidth, mainPlaceables.sumOf { it.height } + expandedPlaceables.sumOf { it.height }) {
                var yPosition = 0
                mainPlaceables.forEach {
                    it.place(0, yPosition)
                    yPosition += it.height
                }
                expandedPlaceables.forEach {
                    it.place(0, yPosition)
                    yPosition += it.height
                }
            }
        }*/
    }
}
