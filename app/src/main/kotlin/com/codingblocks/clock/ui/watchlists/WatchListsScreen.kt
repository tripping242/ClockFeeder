package com.codingblocks.clock.ui.watchlists

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingblocks.clock.R
import com.codingblocks.clock.base.ui.CheckBoxRowWithText
import com.codingblocks.clock.base.ui.TextRowWithIntegerInputTextField
import com.codingblocks.clock.base.ui.card.ExpandableCard
import com.codingblocks.clock.base.ui.dialog.FullScreenDialog
import com.codingblocks.clock.base.ui.icon.AppIcon
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.AppTheme
import com.codingblocks.clock.base.ui.theme.md_theme_light_error
import com.codingblocks.clock.base.ui.theme.md_theme_light_secondary
import com.codingblocks.clock.base.ui.utils.formatMax8decimals
import com.codingblocks.clock.base.ui.utils.formatToNoDecimals
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import com.codingblocks.clock.core.local.data.WatchListConfig
import com.codingblocks.clock.core.local.data.WatchlistWithPositions
import com.codingblocks.clock.core.local.data.formattedHHMM
import com.codingblocks.clock.core.local.data.getAllPositionsFTIncludingLP
import com.codingblocks.clock.ui.watchlists.WatchListViewModel.PositionItem
import com.codingblocks.clock.ui.watchlists.WatchListViewModel.ShowType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import timber.log.Timber
import java.time.ZonedDateTime

@Composable
fun WatchlistsScreen(
    viewModel: WatchListViewModel = getViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val parentLazyListState: LazyListState = rememberLazyListState()
    val childLazyListState = rememberLazyListState()
    var expandedItemIndex by remember { mutableStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(expandedItemIndex) {
        if (expandedItemIndex != -1) parentLazyListState.animateScrollToItem(expandedItemIndex) // Smooth scroll to the item
    }

    AppScaffold(
        title = stringResource(id = R.string.screen_watchlists),
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
        ) {
            LazyColumn(
                state = parentLazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                itemsIndexed(
                    items = state.watchlistsWithPositions,
                    key = { index, item -> "${item.watchListConfig.watchlistNumber}-${item.watchListConfig.includeLPinFT}-${item.watchListConfig.includeLPinFT}" }
                ) { index, item ->
                    ExpandableItem(
                        isReloading = state.reloadingWatchListNumber == item.watchListConfig.watchlistNumber,
                        isExpanded = expandedItemIndex == index,
                        onClick = {
                            if (state.error != null) {
                                viewModel.dispatch(WatchListViewModel.Action.ResetError)
                            }
                            if (expandedItemIndex != index) {
                                expandedItemIndex = index
                            } else {
                                // Toggle off if clicked again
                                expandedItemIndex = -1
                            }
                        },
                        onSaveClick = { config ->
                            // also reload showFTwithLp to update expanded part
                            viewModel.dispatch(WatchListViewModel.Action.SettingsChanged(config)) },
                        onFTAlertClicked = { unit, watchList ->
                            viewModel.dispatch(WatchListViewModel.Action.FTALertChanged(unit, watchList)) },
                        onNFTAlertClicked = { policy, watchList ->
                            viewModel.dispatch(WatchListViewModel.Action.NFTALertChanged(policy, watchList)) },
                        onLPAlertClicked = { ticker, watchList ->
                            viewModel.dispatch(WatchListViewModel.Action.LPALertChanged(ticker, watchList)) },
                        onReloadPositionsClick = { config ->
                            if (state.error != null) {
                                viewModel.dispatch(WatchListViewModel.Action.ResetError)
                            }
                            viewModel.dispatch(WatchListViewModel.Action.ReloadPositions(config.watchlistNumber, config.walletAddress)) },
                        onConfirmDeleteClick = {
                            viewModel.dispatch(WatchListViewModel.Action.DeleteWatchList(it))
                        },
                        currentWatchListWithPositions = item,
                        state = childLazyListState,
                    )
                }
            }

            state.error?.let {
                Text(
                    text = it,
                    color = md_theme_light_error,
                )
            }
            // todo move to FAB
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(vertical = 32.dp)
                        .size(56.dp)
                )
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.dispatch(
                            WatchListViewModel.Action.ShowAddWatchListDialogChanged(
                                true
                            )
                        )
                    }
                ) {
                    Text(text = "ADD NEW WATCH LIST")
                }
            }
        }
    }
    if (state.showAddWatchListDialog) {
        FullScreenDialog(
            modifier = Modifier,
            onDismissRequest = { viewModel.dispatch(WatchListViewModel.Action.ShowAddWatchListDialogChanged(false)) },
            dialogContent = {
                AddWatchListDialog(
                    onDismiss = { viewModel.dispatch(WatchListViewModel.Action.ShowAddWatchListDialogChanged(false)) },
                    onAddClick = {
                        viewModel.dispatch(WatchListViewModel.Action.AddNewWatchlist(it))
                    },
                    address = state.enteredAddress,
                    onAddressChange = {
                        viewModel.dispatch(WatchListViewModel.Action.EnteredAddressChanged(it))
                    },
                    onResolveClick = {
                        viewModel.dispatch(
                            WatchListViewModel.Action.OnResolveClick(
                                state.enteredAddress
                            )
                        )
                    },
                    resolvedAddress = state.resolvedAddress,
                    resolveError = state.resolveError,
                    duplicateAddress = state.errorDuplicateAddress,
                    duplicateName = state.errorDuplicateName,
                    addError = state.errorAddWatchlist,
                    onResetAddError = { viewModel.dispatch(WatchListViewModel.Action.ResetAddError)}
                )
            },
        )
    }
}

@Composable
fun ExpandableItem(
    isReloading: Boolean,
    isExpanded: Boolean,
    onFTAlertClicked: (String, Int) -> Unit,
    onNFTAlertClicked: (String, Int) -> Unit,
    onLPAlertClicked: (String, Int) -> Unit,
    onClick: () -> Unit,
    onSaveClick: (WatchListConfig) -> Unit,
    onConfirmDeleteClick: (Int) -> Unit,
    onReloadPositionsClick: (WatchListConfig) -> Unit,
    currentWatchListWithPositions: WatchlistWithPositions,
    state: LazyListState,
) {

    var showType by remember { mutableStateOf(ShowType.FT) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        val watchListConfig = currentWatchListWithPositions.watchListConfig
        SettingsDialog(
            watchListConfig = watchListConfig,
            onDismiss = { showSettingsDialog = false },
            onSaveClick = { onSaveClick(it) }
        )
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("All positions and alerts associated with this watchlist will also be removed!") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmationDialog = false
                    onConfirmDeleteClick(currentWatchListWithPositions.watchListConfig.watchlistNumber)
                }) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    ExpandableCard(
        isExpanded = isExpanded,
        onClick = onClick,
        topContent = {
            val config = currentWatchListWithPositions.watchListConfig
            val sizeFT = currentWatchListWithPositions.positionsFT.size
            val sizeNFT = currentWatchListWithPositions.positionsNFT.size
            val sizeLP = currentWatchListWithPositions.positionsLP.size
            val sizeFTLP = currentWatchListWithPositions.positionsFTIncludingLP.size

            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(1.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(8.dp)
                            .wrapContentHeight()
                            .weight(0.6f)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 16.dp),
                            text = config.name,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            style = AppTheme.typography.h5,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { showSettingsDialog = true },
                                modifier = Modifier.padding(end = 8.dp),
                            ) {
                                AppIcon(icon = Icons.Outlined.Settings)
                            }
                            IconButton(
                                onClick = { onReloadPositionsClick(config) },
                                modifier = Modifier.padding(end = 16.dp),
                            ) {
                                if (isReloading) CircularProgressIndicator(modifier = Modifier.size(8.dp))
                                else AppIcon(icon = Icons.Outlined.Refresh)
                            }
                            IconButton(
                                onClick = {
                                    showDeleteConfirmationDialog = true
                                },
                                modifier = Modifier,
                            ) {
                                AppIcon(icon = Icons.Outlined.Delete)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(4.dp)
                            .weight(0.4f)
                    ) {
                        Text(text = "#FT = $sizeFT")
                        Text(text = "#NFT = $sizeNFT")
                        Text(text = "#LP = $sizeLP")
                    }
                }
            }
        },
        expandedContent = {
            var positionItems = getPositionItems(showType, currentWatchListWithPositions)

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier,
                        onClick = {
                            showType = if (currentWatchListWithPositions.watchListConfig.includeLPinFT) ShowType.FT_LP else ShowType.FT
                        },
                    ) {
                        Text(text = " FT")
                    }
                    if (currentWatchListWithPositions.watchListConfig.includeNFT) {
                        Button(
                            modifier = Modifier,
                            onClick = {
                                showType = ShowType.NFT
                            },

                        ) {
                            Text(text = "NTF")
                        }
                    }
                    if (!currentWatchListWithPositions.watchListConfig.includeLPinFT) {
                        Button(
                            modifier = Modifier,
                            onClick = {
                                showType = ShowType.LP
                            },
                        ) {
                            Text(text = " LP")
                        }
                    }
                }

                //Box(modifier = Modifier) {
                    LazyColumn(
                        state = state,
                        modifier = Modifier
                            .heightIn(max = 600.dp)
                            .padding(vertical = 4.dp),
                    ) {
                        val items = getPositionItems(showType, currentWatchListWithPositions)
                        items(items, key = { it.hashCode() }) { positionItem ->
                            when (positionItem) {
                                is PositionItem.FT -> PositionFTItem(
                                    item = positionItem.positionFT,
                                    onAlertClicked = { onFTAlertClicked(positionItem.positionFT.unit, currentWatchListWithPositions.watchListConfig.watchlistNumber) }
                                )
                                is PositionItem.NFT -> PositionNFTItem(
                                    item = positionItem.positionNFT,
                                    onAlertClicked = { onNFTAlertClicked(positionItem.positionNFT.policy, currentWatchListWithPositions.watchListConfig.watchlistNumber) }
                                )
                                is PositionItem.LP -> PositionLPItem(item = positionItem.positionLP)
                            }
                        }
                    }
                //}
            }
        })
}

fun getPositionItems(showType: ShowType, currentWatchListWithPositions: WatchlistWithPositions): List<PositionItem> {
    val aggregatedShowType = if (showType == ShowType.FT && currentWatchListWithPositions.watchListConfig.includeLPinFT) ShowType.FT_LP else showType
    return when (aggregatedShowType) {
        ShowType.FT -> {
            currentWatchListWithPositions.positionsFT
                .map { PositionItem.FT(it) }
                .filter { it.positionFT.adaValue >= currentWatchListWithPositions.watchListConfig.minFTAmount }
        }

        ShowType.FT_LP -> currentWatchListWithPositions.positionsFTIncludingLP.map {
            PositionItem.FT(
                it
            )
        }.filter { it.positionFT.adaValue >= currentWatchListWithPositions.watchListConfig.minFTAmount }

        ShowType.NFT -> currentWatchListWithPositions.positionsNFT.map {
            PositionItem.NFT(
                it
            )
        }.filter { it.positionNFT.adaValue >= currentWatchListWithPositions.watchListConfig.minNFTAmount }

        ShowType.LP -> currentWatchListWithPositions.positionsLP.map {
            PositionItem.LP(
                it
            )
        }
    }
}

@Composable
fun AddWatchListDialog(
    address: String,
    duplicateAddress: Boolean,
    duplicateName: Boolean,
    addError: String?,
    resolvedAddress: String?,
    resolveError: String?,
    onAddressChange: (String) -> Unit,
    onResolveClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddClick: (WatchListConfig) -> Unit,
    onResetAddError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var watchListName by remember { mutableStateOf("") }
    var checkedStateIncludeLPInFT: Boolean by remember { mutableStateOf(true) }
    var checkedStateIncludeNFT: Boolean by remember { mutableStateOf(true) }
    var minFTPostionAmount: Int by remember { mutableIntStateOf(0) }
    var minNFTPostionAmount: Int by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),

    ) {
        Timber.tag("wims").i("duplicateAddress $duplicateAddress, duplicateName $duplicateName, addError: $addError  ")

        Text(
            text = "Add new Watchlist from wallet:",
            style = AppTheme.typography.h4,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextField(
                isError = duplicateAddress,
                value = address,
                onValueChange = {
                    onAddressChange(it)
                    if (duplicateAddress) onResetAddError.invoke()
                },
                modifier = Modifier
                    .padding(end = 16.dp)
                    .weight(0.3f)
            )
            Button(
                enabled = address.isNotEmpty() && address.length > 2,
                onClick = onResolveClick,
                modifier = Modifier
                    .weight(0.2f),
                ) {
                Text(text = "CHECK ADDRESS ")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            resolvedAddress?.let {
                Text(
                    text = "valid! StakeAddress=$it",
                    style = TextStyle(fontSize = 8.sp),
                    color = md_theme_light_secondary,
                )
            }
            // todo we could map stakeaddress already in use error on resolving!
            resolveError?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.body2,
                    color = md_theme_light_error,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (resolvedAddress != null) 1.0f else 0.2f)
        ) {
            TextField(
                isError = duplicateName,
                enabled = resolvedAddress != null,
                value = watchListName,
                onValueChange = {
                    watchListName = it
                    if (duplicateName) onResetAddError.invoke()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            )
            CheckBoxRowWithText(
                enabled = resolvedAddress != null,
                text = "Include LP in Token positions?",
                onCheckedChanged = { checkedStateIncludeLPInFT = it },
                checkedState = checkedStateIncludeLPInFT,
            )
            CheckBoxRowWithText(
                enabled = resolvedAddress != null,
                text = "Include NFTs in this watchlist?",
                onCheckedChanged = { checkedStateIncludeNFT = it },
                checkedState = checkedStateIncludeNFT,
            )
            TextRowWithIntegerInputTextField(
                enabled = resolvedAddress != null,
                text = "Exclude token positions with a value lower than:",
                amount = minFTPostionAmount,
                onAmountChanged = { newAmount ->
                    minFTPostionAmount = newAmount
                },
                hint = "₳",
            )
            if (checkedStateIncludeNFT) {
                TextRowWithIntegerInputTextField(
                    enabled = resolvedAddress != null,
                    text = "Exclude NFT positions with a value lower than:",
                    amount = minNFTPostionAmount,
                    onAmountChanged = { newAmount ->
                        minNFTPostionAmount = newAmount
                    },
                    hint = "₳",
                )
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = resolvedAddress!=null && watchListName.isNotEmpty(),
            onClick = {
                val config: WatchListConfig = WatchListConfig(
                    name = watchListName,
                    includeNFT = checkedStateIncludeNFT,
                    includeLPinFT = checkedStateIncludeLPInFT,
                    // todo, do we need this. We either include lp positions in ft aggregated or leave it out.
                    // but if left out, we will not get ft positions for unique lp positions
                    showLPTab = false,
                    walletAddress = resolvedAddress,
                    createdAt = ZonedDateTime.now(),
                )
                onAddClick(config)
            },
        ) {
            Text(text = "ADD WATCH LIST")
        }
        addError?.let {
            Text(
                text = it,
                style = AppTheme.typography.body2,
                color = md_theme_light_error,
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onDismiss.invoke()
            }
        ) {
            Text(text = "CANCEL")
        }
    }
}
@Composable
fun SettingsDialog(
    watchListConfig: WatchListConfig,
    onDismiss: () -> Unit,
    onSaveClick: (WatchListConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    var checkedStateIncludeLPInFT: Boolean by remember { mutableStateOf(watchListConfig.includeLPinFT) }
    var checkedStateIncludeNFT: Boolean by remember { mutableStateOf(watchListConfig.includeNFT) }
    var minFTPostionAmount: Int by remember { mutableIntStateOf(watchListConfig.minFTAmount) }
    var minNFTPostionAmount: Int by remember { mutableIntStateOf(watchListConfig.minNFTAmount) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = modifier
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Watchlist name:",
                    style = AppTheme.typography.h6,
                )
                Text(
                    text = watchListConfig.name,
                    style = AppTheme.typography.h6,
                    maxLines = 2,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                watchListConfig.walletAddress?.let {
                    Text(
                        text = it,
                        style = TextStyle(fontSize = 8.sp),
                        color = md_theme_light_secondary,
                    )
                }
            }
            CheckBoxRowWithText(
                text = "Include LP in Token positions?",
                onCheckedChanged = { checkedStateIncludeLPInFT = it },
                checkedState = checkedStateIncludeLPInFT,
            )
            CheckBoxRowWithText(
                text = "Include NFTs in this watchlist?",
                onCheckedChanged = { checkedStateIncludeNFT = it },
                checkedState = checkedStateIncludeNFT,
            )
            TextRowWithIntegerInputTextField(
                text = "Exclude token positions with a value lower than:",
                amount = minFTPostionAmount,
                onAmountChanged = { newAmount ->
                    minFTPostionAmount = newAmount
                },
                hint = "₳",
            )
            if (checkedStateIncludeNFT) {
                TextRowWithIntegerInputTextField(
                    text = "Exclude NFT positions with a value lower than:",
                    amount = minNFTPostionAmount,
                    onAmountChanged = { newAmount ->
                        minNFTPostionAmount = newAmount
                    },
                    hint = "₳",
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = with (watchListConfig) {
                        (includeNFT != checkedStateIncludeNFT ||
                        includeLPinFT != checkedStateIncludeLPInFT ||
                        minNFTAmount != minNFTPostionAmount ||
                        minFTAmount != minFTPostionAmount)
                },
                onClick = {
                    val config: WatchListConfig = watchListConfig.copy(
                        includeNFT = checkedStateIncludeNFT,
                        includeLPinFT = checkedStateIncludeLPInFT,
                        minNFTAmount = minNFTPostionAmount,
                        minFTAmount = minFTPostionAmount,
                        // showLPTab = showLPTab,
                    )
                    onSaveClick(config)
                    onDismiss.invoke()
                },
            ) {
                Text(text = "SAVE SETTINGS")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                onDismiss.invoke()
            }) {
                Text(text = "CANCEL")
            }
        }
    }
}

@Composable
fun PositionNFTItem(
    item: PositionNFTLocal,
    onAlertClicked: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = item.name, modifier = Modifier.width(160.dp), // Adjust width as needed
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.balance.formatToNoDecimals(),
            modifier = Modifier
                .width(40.dp)
                .padding(end = 16.dp)
        )
        Text(
            text = item.adaValue.formatToNoDecimals(),
            modifier = Modifier
                .width(60.dp)
                .padding(end = 16.dp)
        )

        Text(text = item.lastUpdated.formattedHHMM())

        IconButton(
            onClick = {
                //showInFeed = !showInFeed
                onAlertClicked.invoke()
            },
            modifier = Modifier,
        ) {
            AppIcon(icon = if (item.showInFeed) Icons.Outlined.NotificationsActive else Icons.Outlined.AddAlert)
        }
    }
}

@Composable
fun PositionLPItem(
    item: PositionLPLocal,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = item.ticker.removeSuffix(" LP"),
            modifier = Modifier
                .width(100.dp)
                .padding(end = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.tokenAAmount.formatToNoDecimals(),
            modifier = Modifier
                .width(100.dp)
                .padding(end = 16.dp),
        )
        Text(text = (item.adaValue / 2).formatToNoDecimals(), modifier = Modifier.width(60.dp))

        Text(text = item.lastUpdated.formattedHHMM())
    }
}

@Composable
fun PositionFTItem(
    item: PositionFTLocal,
    onAlertClicked: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = item.ticker,
            modifier = Modifier
                .width(100.dp)
                .padding(end = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.balance.formatMax8decimals(),
            modifier = Modifier
                .width(100.dp)
                .padding(end = 16.dp)
        )

        Text(
            text = item.adaValue.formatToNoDecimals(),
            modifier = Modifier
                .width(60.dp)
                .padding(end = 16.dp)
        )

        Text(text = item.lastUpdated.formattedHHMM())

        IconButton(
            onClick = onAlertClicked,
            modifier = Modifier,
        ) {
            AppIcon(icon = if (item.showInFeed) Icons.Outlined.NotificationsActive else Icons.Outlined.AddAlert)
        }
    }
}