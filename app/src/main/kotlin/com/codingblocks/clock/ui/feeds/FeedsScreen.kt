package com.codingblocks.clock.ui.feeds

import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlarm
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AlarmOff
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.WaterfallChart
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingblocks.clock.R
import com.codingblocks.clock.base.ui.CheckBoxRowWithText
import com.codingblocks.clock.base.ui.TextRowWithDoubleInputTextField
import com.codingblocks.clock.base.ui.TextRowWithIntegerInputTextField
import com.codingblocks.clock.base.ui.button.MultiChoiceTwoSegmentedButton
import com.codingblocks.clock.base.ui.button.SingleChoiceSegmentedButton
import com.codingblocks.clock.base.ui.card.ExpandableCard
import com.codingblocks.clock.base.ui.dialog.FullWidthDialog
import com.codingblocks.clock.base.ui.icon.AppIcon
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.AppTheme
import com.codingblocks.clock.base.ui.utils.formatMax8decimals
import com.codingblocks.clock.base.ui.utils.formatToNoDecimals
import com.codingblocks.clock.core.local.data.CustomFTAlert
import com.codingblocks.clock.core.local.data.CustomNFTAlert
import com.codingblocks.clock.core.local.data.FeedFTWithAlerts
import com.codingblocks.clock.core.local.data.FeedNFTWithAlerts
import com.codingblocks.clock.core.local.data.formattedHHMM
import com.codingblocks.clock.ui.feeds.FeedsViewModel.ShowType
import com.codingblocks.clock.ui.watchlists.LogoImage
import kotlinx.collections.immutable.toImmutableList
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
    val optionsShowTypeValue = remember {
        FeedsViewModel.ShowType.entries.map { it.name }
    }.toImmutableList()

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SingleChoiceSegmentedButton(
                    modifier = Modifier.fillMaxWidth(),
                    options = optionsShowTypeValue,
                    selected = optionsShowTypeValue.indexOf(showType.name),
                    onSelected = { index ->
                        showType = if (optionsShowTypeValue[index] == ShowType.FT.name) ShowType.FT else ShowType.NFT
                    }
                )
                /*Button(
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
                }*/
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
                                    FeedsViewModel.Action.FeedClockLightsFTChanged(
                                        item
                                    )
                                )
                            },
                            onAddAlertClicked = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.AddFTAlert(it)
                                )
                            },
                            onDeleteAlertClicked = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.DeleteFTAlert(it)
                                )
                            },
                            logoCache = state.logoCache,
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
                            onFeedClockLightsChanged = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.FeedClockLightsNFTChanged(
                                        item
                                    )
                                )
                            },
                            onAddAlertClicked = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.AddNFTAlert(it)
                                )
                            },
                            onDeleteAlertClicked = {
                                viewModel.dispatch(
                                    FeedsViewModel.Action.DeleteNFTAlert(it)
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
    onFeedClockLightsChanged: () -> Unit,
    onAddAlertClicked: (CustomNFTAlert) -> Unit,
    onDeleteAlertClicked: (CustomNFTAlert) -> Unit,
    state: LazyListState,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val optionsAlertValue = remember {
        FeedsViewModel.AlertValue.entries.map { it.label }
    }.toImmutableList()
    val optionsAlertTrigger = remember {
        FeedsViewModel.AlertTrigger.entries.map { it.label }
    }.toImmutableList()
    val optionsAlertType = remember {
        FeedsViewModel.AlertType.entries.map { it.label }
    }.toImmutableList()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("This will also delete all related alerts and your clock feed for this NFT!") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteFeedClicked.invoke()
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddDialog) {
        var selectedTypeSet: Set<Int> by remember { mutableStateOf(emptySet()) }
        var selectedTriggerIndex by remember { mutableStateOf(-1) }
        var selectedValueIndex  by remember { mutableStateOf(-1) }
        var amount: Double? by remember { mutableStateOf(null) }
        var volume: Int? by remember { mutableStateOf(null) }

        FullWidthDialog(
            onDismissRequest = { showAddDialog = !showAddDialog },
            dialogContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp),

                    ) {
                    Text(
                        text = "Add new alert for ${item.feedNFT.name}:",
                        style = AppTheme.typography.h4,
                    )
                    SingleChoiceSegmentedButton(
                        modifier = Modifier.fillMaxWidth(),
                        options = optionsAlertValue,
                        selected = null,
                        onSelected = { index ->
                            selectedValueIndex = index
                        }
                    )
                    SingleChoiceSegmentedButton(
                        modifier = Modifier.fillMaxWidth(),
                        options = optionsAlertTrigger,
                        selected = null,
                        onSelected = { index ->
                            selectedTriggerIndex = index
                        }
                    )
                    MultiChoiceTwoSegmentedButton(
                        modifier = Modifier.fillMaxWidth(),
                        options = optionsAlertType,
                        selectedOptions = selectedTypeSet,
                        onSelectionChange = { newSelection ->
                            selectedTypeSet = newSelection
                        }
                    )
                    if (selectedValueIndex == 0) {
                        TextRowWithDoubleInputTextField(
                            text = "Trigger when price reaches:",
                            amount = amount,
                            onAmountChanged = { newAmount ->
                                amount = newAmount
                            },
                            hint = "₳",
                        )
                    }
                    if (selectedValueIndex == 1) {
                        TextRowWithIntegerInputTextField(
                            text = "Trigger when volume reaches:",
                            amount = volume ?: 0,
                            onAmountChanged = { newAmount ->
                                volume = newAmount
                            },
                            hint = "₳",
                        )
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedTriggerIndex != -1 && selectedValueIndex != -1 && selectedTypeSet.isNotEmpty() && amount != null,
                        onClick = {
                            with (item.feedNFT) {
                                val alert: CustomNFTAlert = CustomNFTAlert(
                                    feedPositionPolicy = positionPolicy,
                                    ticker = name,
                                    threshold = if (selectedValueIndex == 0) amount!! else volume!!.toDouble(),
                                    isEnabled = true,
                                    onlyOnce = selectedTriggerIndex == 0,
                                    priceOrVolume = selectedValueIndex == 0,
                                    pushAlert = selectedTypeSet.contains(0),
                                    clockAlert = selectedTypeSet.contains(1),
                                )
                                onAddAlertClicked(alert)
                                showAddDialog = false
                            }
                        },
                    ) {
                        Text(text = "ADD NFT ALERT")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showAddDialog = false
                        }
                    ) {
                        Text(text = "CANCEL")
                    }
                }
            }
        )
    }

    ExpandableCard(
        isExpanded = isExpanded,
        onClick = {
            if (item.alerts.isNotEmpty()) { isExpanded = !isExpanded }
        },
        topContent = {
            Column()
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .wrapContentHeight()
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = item.feedNFT.name,
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier,
                    ) {
                        AppIcon(icon = Icons.Outlined.Delete)
                    }
                }

                CheckBoxRowWithText(
                    modifier = Modifier
                        .padding(4.dp)
                        .wrapContentHeight(),
                    text = "Show floor price feed on BlockClock",
                    onCheckedChanged = { onFeedClockPriceChanged.invoke() },
                    checkedState = item.feedNFT.feedClockPrice,
                )

                CheckBoxRowWithText(
                    modifier = Modifier
                        .padding(4.dp)
                        .wrapContentHeight(),
                    enabled = item.feedNFT.feedClockPrice,
                    text = "Indicate trend with lights",
                    onCheckedChanged = { onFeedClockLightsChanged.invoke() },
                    checkedState = item.feedNFT.feedClockVolume,
                )
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = {
                            showAddDialog = true },
                        modifier = Modifier
                            .padding(start = 4.dp)
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
            val alerts = item.alerts.sortedByDescending { it.threshold }
            LazyColumn(
                state = state,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
            ) {
                items(alerts)
                { alert ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppTheme.shapes.small,
                        elevation = 16.dp,
                        color = MaterialTheme.colors.primarySurface
                    ) {
                        NFTAlertItem(
                            item = alert,
                            onDeleteClicked = { onDeleteAlertClicked(alert) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun FeedFTItem(
    item: FeedFTWithAlerts,
    onDeleteFeedClicked: () -> Unit,
    onFeedClockPriceChanged: () -> Unit,
    onFeedClockVolumeChanged: () -> Unit,
    onAddAlertClicked: (CustomFTAlert) -> Unit,
    onDeleteAlertClicked: (CustomFTAlert) -> Unit,
    logoCache: LruCache<String, Bitmap>,
    state: LazyListState,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val optionsAlertValue = remember {
        FeedsViewModel.AlertValue.entries.map { it.label }
    }.toImmutableList()
    val optionsAlertTrigger = remember {
        FeedsViewModel.AlertTrigger.entries.map { it.label }
    }.toImmutableList()
    val optionsAlertType = remember {
        FeedsViewModel.AlertType.entries.map { it.label }
    }.toImmutableList()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("This will also delete all related alerts and your clock feed for this Token!") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteFeedClicked.invoke()
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showAddDialog) {
        var selectedTypeSet: Set<Int> by remember { mutableStateOf(emptySet()) }
        var selectedTriggerIndex by remember { mutableStateOf(-1) }
        var selectedValueIndex  by remember { mutableStateOf(0) }
        var amount: Double? by remember { mutableStateOf(null) }
        var volume: Int? by remember { mutableStateOf(null) }

        FullWidthDialog(
            onDismissRequest = { showAddDialog = !showAddDialog },
            dialogContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp),

                    ) {
                    Text(
                        text = "Add new price alert for ${item.feedFT.name}:",
                        style = AppTheme.typography.h4,
                    )
                    SingleChoiceSegmentedButton(
                        modifier = Modifier.fillMaxWidth(),
                        options = optionsAlertTrigger,
                        selected = null,
                        onSelected = { index ->
                            selectedTriggerIndex = index
                        }
                    )
                    MultiChoiceTwoSegmentedButton(
                        modifier = Modifier.fillMaxWidth(),
                        options = optionsAlertType,
                        selectedOptions = selectedTypeSet,
                        onSelectionChange = { newSelection ->
                            selectedTypeSet = newSelection
                        }
                    )

                    TextRowWithDoubleInputTextField(
                        text = "Trigger when price reaches:",
                        amount = amount,
                        onAmountChanged = { newAmount ->
                            amount = newAmount
                        },
                        hint = "₳",
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedTriggerIndex != -1 && selectedValueIndex != -1 && selectedTypeSet.isNotEmpty() && amount != null,
                        onClick = {
                            with (item.feedFT) {
                                val alert: CustomFTAlert = CustomFTAlert(
                                    feedPositionUnit = positionUnit,
                                    ticker = name,
                                    threshold = if (selectedValueIndex == 0) amount!! else volume!!.toDouble(),
                                    isEnabled = true,
                                    onlyOnce = selectedTriggerIndex == 0,
                                    priceOrVolume = selectedValueIndex == 0,
                                    pushAlert = selectedTypeSet.contains(0),
                                    clockAlert = selectedTypeSet.contains(1),
                                )
                                onAddAlertClicked(alert)
                                showAddDialog = false
                            }
                        },
                    ) {
                        Text(text = "ADD FT ALERT")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                             showAddDialog = false
                        }
                    ) {
                        Text(text = "CANCEL")
                    }
                }
            }
        )
    }
    ExpandableCard(
        isExpanded = isExpanded,
        onClick = {
            if (item.alerts.isNotEmpty()) { isExpanded = !isExpanded }
        },
        topContent = {
            Column()
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentHeight()
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = item.feedFT.name,
                        modifier = Modifier
                            .width(100.dp)
                            .padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = { showDeleteDialog = true },
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
                    text = "Indicate trend with lights",
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
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .padding(4.dp)
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
            val alerts = item.alerts.sortedByDescending { it.threshold }
            LazyColumn(
                state = state,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
            ) {
                items(alerts)
                { alert ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppTheme.shapes.small,
                        elevation = 16.dp,
                        color = MaterialTheme.colors.primarySurface
                    ) {
                        FTAlertItem(
                            item = alert,
                            onDeleteClicked = { onDeleteAlertClicked(alert) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun NFTAlertItem(
    item: CustomNFTAlert,
    onDeleteClicked: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
    ) {

        if (item.priceOrVolume) {
            AppIcon(icon = Icons.Outlined.WaterfallChart)
            Text(
                text = item.threshold.formatMax8decimals()  + " ₳",
                modifier = Modifier
                    .width(100.dp)
                    .padding(start = 8.dp, end = 16.dp)
            )
        } else {
            AppIcon(icon = Icons.Outlined.BarChart)
            Text(
                text = item.threshold.formatToNoDecimals() + " ₳",
                modifier = Modifier
                    .width(100.dp)
                    .padding(start = 8.dp, end = 16.dp)
            )
        }
        AppIcon(icon = if (item.onlyOnce) Icons.Outlined.LooksOne else Icons.Outlined.Repeat)
        AppIcon(icon = if (item.pushAlert) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff)
        AppIcon(icon = if (item.clockAlert) Icons.Outlined.Alarm else Icons.Outlined.AlarmOff)

        IconButton(
            onClick = onDeleteClicked,
            modifier = Modifier
                .padding(start = 32.dp),
        ) {
            AppIcon(icon = Icons.Outlined.Delete)
        }
    }
}

@Composable
fun FTAlertItem(
    item: CustomFTAlert,
    onDeleteClicked: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
    ) {
        if (item.priceOrVolume) {
            AppIcon(icon = Icons.Outlined.WaterfallChart)
            Text(
                text = item.threshold.formatMax8decimals() + " ₳",
                modifier = Modifier
                    .width(100.dp)
                    .padding(start = 8.dp, end = 8.dp)
            )
        } else {
            AppIcon(icon = Icons.Outlined.BarChart)
            Text(
                text = item.threshold.formatToNoDecimals()  + " ₳",
                modifier = Modifier
                    .width(100.dp)
                    .padding(start = 8.dp, end = 8.dp)
            )
        }
        AppIcon(icon = if (item.onlyOnce) Icons.Outlined.LooksOne else Icons.Outlined.Repeat)
        AppIcon(icon = if (item.pushAlert) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff)
        AppIcon(icon = if (item.clockAlert) Icons.Outlined.Alarm else Icons.Outlined.AlarmOff)

        IconButton(
            onClick = onDeleteClicked,
            modifier = Modifier
                .padding(start = 32.dp),
        ) {
            AppIcon(icon = Icons.Outlined.Delete)
        }
    }

}

