package com.codingblocks.clock.ui.feeds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlarm
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.codingblocks.clock.base.ui.card.ExpandableCard
import com.codingblocks.clock.base.ui.icon.AppIcon
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.utils.formatMax8decimals
import com.codingblocks.clock.base.ui.utils.formatToNoDecimals
import com.codingblocks.clock.core.local.data.FeedFTWithAlerts
import com.codingblocks.clock.core.local.data.FeedNFTWithAlerts
import com.codingblocks.clock.core.local.data.formattedHHMM
import com.codingblocks.clock.ui.feeds.FeedsViewModel.ShowType
import com.codingblocks.clock.ui.watchlists.ExpandableItem
import org.koin.androidx.compose.getViewModel

@Composable
fun FeedsScreen(
    viewModel: FeedsViewModel = getViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val parentLazyListState: LazyListState = rememberLazyListState()
    val childLazyListState = rememberLazyListState()
    var expandedItemIndex by remember { mutableStateOf(-1) }
    var showType by remember { mutableStateOf(FeedsViewModel.ShowType.FT) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(expandedItemIndex) {
        if (expandedItemIndex != -1) parentLazyListState.animateScrollToItem(expandedItemIndex) // Smooth scroll to the item
    }
    LaunchedEffect(viewModel) {
        viewModel.dispatch(FeedsViewModel.Action.Initialize)
    }

    AppScaffold(
        title = stringResource(id = R.string.screen_feeds),
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier,
                    onClick = {
                        showType =
                            ShowType.FT
                    },
                ) {
                    Text(text = " FT")
                }
                Button(
                    modifier = Modifier,
                    onClick = {
                        showType = ShowType.NFT
                    },

                    ) {
                    Text(text = "NTF")
                }
            }

            if (showType == ShowType.FT) {
                LazyColumn(
                    state = parentLazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    items(state.feedFTWithAlerts)
                    { item ->
                        FeedFTItem(
                            item = item,
                            onDeleteFeedClicked = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.DeleteFeedFTItem(
                                        item
                                    )
                                )
                            },
                            onFeedClockPriceChanged = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.FeedClockPriceFTChanged(
                                        item
                                    )
                                )
                            },
                            onFeedClockVolumeChanged = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.FeedClockVolumeFTChanged(
                                        item
                                    )
                                )
                            },
                            state = childLazyListState,
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = parentLazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    items(state.feedNFTWithAlerts)
                    { item ->
                        FeedNFTItem(
                            item = item,
                            onDeleteFeedClicked = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.DeleteFeedNFTItem(
                                        item
                                    )
                                )
                            },
                            onFeedClockPriceChanged = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.FeedClockPriceNFTChanged(
                                        item
                                    )
                                )
                            },
                            onFeedClockVolumeChanged = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.FeedClockVolumeNFTChanged(
                                        item
                                    )
                                )
                            },
                            state = childLazyListState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeedNFTItem(
    item: FeedNFTWithAlerts,
    onDeleteFeedClicked: () -> Unit,
    onFeedClockPriceChanged: () -> Unit,
    onFeedClockVolumeChanged: () -> Unit,
    state: LazyListState,
) {
    Text(text = item.feedNFT.name)
}

@Composable
fun FeedFTItem(
    item: FeedFTWithAlerts,
    onDeleteFeedClicked: () -> Unit,
    onFeedClockPriceChanged: () -> Unit,
    onFeedClockVolumeChanged: () -> Unit,
    state: LazyListState,
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExpandableCard(
        isExpanded = isExpanded,
        onClick = { isExpanded = !isExpanded },
        topContent = {
            Column()
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(4.dp)
                        .wrapContentHeight()
                        .fillMaxWidth(),
                ) {
                    Text(
                        // todo we need to also pack in the name silly
                        // and maybe icon link
                        text = item.feedFT.name,
                        modifier = Modifier
                            .width(100.dp)
                            .padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = item.feedFT.lastUpdatedAt.formattedHHMM(),
                        modifier = Modifier
                            .width(80.dp)
                            .padding(end = 8.dp)
                    )
                    IconButton(
                        onClick = onDeleteFeedClicked,
                        modifier = Modifier,
                    ) {
                        AppIcon(icon = Icons.Outlined.Delete)
                    }
                }

                CheckBoxRowWithText(
                    modifier = Modifier
                        .padding(4.dp)
                        .wrapContentHeight(),
                    text = "Show price feed on BlockClock",
                    onCheckedChanged = { onFeedClockPriceChanged.invoke() },
                    checkedState = item.feedFT.feedClockPrice,
                )

                CheckBoxRowWithText(
                    modifier = Modifier
                        .padding(4.dp)
                        .wrapContentHeight(),
                    enabled = item.feedFT.feedClockPrice,
                    text = "also show volume indicators on BlockClock",
                    onCheckedChanged = { onFeedClockVolumeChanged.invoke() },
                    checkedState = item.feedFT.feedClockVolume,
                )
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = { /*TODO*/ }
                    ) {
                        AppIcon(icon = Icons.Outlined.AddAlarm)
                    }
                    IconButton(
                        enabled = item.alerts.isNotEmpty(),
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier,
                    ) {
                        AppIcon(icon = if (isExpanded) Icons.Outlined.ArrowDropUp else Icons.Outlined.ArrowDropDown)
                    }
                }
            }
        },
        expandedContent = {
            val alerts = item.alerts
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                items(alerts)
                { alert ->
                    Text(text = "some asert info here")
                }
            }
        },
    )

}
