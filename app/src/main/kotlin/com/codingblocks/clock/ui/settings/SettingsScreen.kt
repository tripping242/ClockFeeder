package com.codingblocks.clock.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingblocks.clock.R
import com.codingblocks.clock.base.ui.CheckBoxRowWithText
import com.codingblocks.clock.base.ui.TextRowWithDoubleInputTextField
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.md_theme_light_error
import com.codingblocks.clock.base.ui.utils.formatMax8decimals
import com.codingblocks.clock.core.local.data.FeedToClockItem
import org.koin.androidx.compose.getViewModel
import timber.log.Timber

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = getViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState: LazyListState = rememberLazyListState()
    val scrollState = rememberScrollState()

    var smallTrendPercent by remember { mutableStateOf(state.smallTrendPercent) }
    var highTrendPercent by remember { mutableStateOf(state.highTrendPercent) }

    var showAdvanced by remember { mutableStateOf(false) }

    AppScaffold(
        title = stringResource(id = R.string.screen_settings),
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 32.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        enabled = !state.isCycling,
                        modifier = Modifier
                            .weight(1f)
                            .height(IntrinsicSize.Min),
                        onClick = { viewModel.dispatch(SettingsViewModel.Action.StartClockFeedCycler) },
                    ) {
                        Text(
                            text = "START FEED"
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        enabled = state.isCycling,
                        modifier = Modifier
                            .weight(1f)
                            .height(IntrinsicSize.Min),
                        onClick = { viewModel.dispatch(SettingsViewModel.Action.PauseClockFeedCycler) },
                    ) {
                        Text(text = "PAUSE FEED")
                    }
                }

                CheckBoxRowWithText(
                    text = "Auto-Start Feed on App Start",
                    onCheckedChanged = {
                        viewModel.dispatch(SettingsViewModel.Action.AutoFeedChanged(it))
                    },
                    checkedState = state.autoFeed
                )
                CheckBoxRowWithText(
                    text = "Auto-Reload all positions on App Start",
                    onCheckedChanged = {
                        viewModel.dispatch(SettingsViewModel.Action.AutoReloadPositionsChanged(it))
                    },
                    checkedState = state.autoReloadPositions
                )
                TextRowWithDoubleInputTextField(
                    text = "Percentage of change needed for default light animation on feed to BlockClock:",
                    amount = smallTrendPercent,
                    onAmountChanged = { newAmount ->
                        newAmount?.let  {
                            smallTrendPercent = newAmount
                        }
                    },
                    hint = "%",
                )

                TextRowWithDoubleInputTextField(
                    text = "Percentage of change needed for double light animation on feed to BlockClock:",
                    amount = highTrendPercent,
                    onAmountChanged = { newAmount ->
                        newAmount?.let  {
                            highTrendPercent = newAmount
                        }
                    },
                    hint = "%",
                )

                if (smallTrendPercent != state.smallTrendPercent || highTrendPercent != state.highTrendPercent) {
                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        onClick = {
                            viewModel.dispatch(SettingsViewModel.Action.SmallTrendChanged(smallTrendPercent))
                            viewModel.dispatch(SettingsViewModel.Action.HighTrendChanged(highTrendPercent))
                        },
                    ) {
                        Text(text = "SAVE TREND PRECENTAGE CHANGES")
                    }
                }
            }

            Text(text = "Advanced Settings:" )
            CheckBoxRowWithText(
                text = "show advanced settings",
                onCheckedChanged = {
                    showAdvanced = !showAdvanced
                },
                checkedState = showAdvanced
            )

            if (showAdvanced) {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, bottom = 32.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                ) {
                    Button(
                        modifier = Modifier,
                        onClick = {
                            viewModel.dispatch(SettingsViewModel.Action.LoadAndUpdateFeedFTToClockItems)
                        },
                    ) {
                        Text(text = "SHOW CURRENT FEED TO CLOCK POSITIONS")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        state.error?.let {
                            Text(
                                text = it,
                                color = md_theme_light_error,
                            )
                        }

                        if (state.feedToClockItems.isNotEmpty()) {
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .heightIn(max = 600.dp),
                            ) {
                                items(state.feedToClockItems) { item ->
                                    FeedToClock(item = item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedToClock(item: FeedToClockItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = item.name,
            modifier = Modifier.width(120.dp), // Adjust width as needed
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.width(120.dp), // Adjust width as needed
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            item.price?.let { Text(text = it.formatMax8decimals(), modifier = Modifier.weight(1f)) }
            Text(text = item.feedType.name, modifier = Modifier.weight(1f))
        }

        Text(text = item.orderIndex.toString())
    }
}
