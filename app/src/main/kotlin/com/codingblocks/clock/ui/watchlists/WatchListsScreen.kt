package com.codingblocks.clock.ui.watchlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.codingblocks.clock.ui.watchlists.WatchListViewModel.PositionItem
import com.codingblocks.clock.ui.watchlists.WatchListViewModel.ShowType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.time.ZonedDateTime

@Composable
fun WatchlistsScreen(
    viewModel: WatchListViewModel = getViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val parentLazyListState: LazyListState = rememberLazyListState()
    val childLazyListState = rememberLazyListState()
    var expandedItemIndex by remember { mutableStateOf(-1) }
    var showAddWatchListDialog by remember { mutableStateOf(state.showAddWatchListDialog) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var enteredAddress by remember { mutableStateOf("") }

    AppScaffold(
        title = stringResource(id = R.string.screen_watchlists),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            LazyColumn(
                state = parentLazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                itemsIndexed(state.watchlistsWithPositions) { index, currentWatchListWithPositions ->
                    ExpandableItem(
                        isExpanded = expandedItemIndex == index,
                        onClick = {
                            if (expandedItemIndex != index) {
                                expandedItemIndex = -1
                                CoroutineScope(Dispatchers.Main).launch {
                                    parentLazyListState.animateScrollToItem(index)
                                    expandedItemIndex = index
                                }
                            } else {
                                // Toggle off if clicked again
                                expandedItemIndex = -1
                            }
                        },
                        onReloadPositionsClick = {
                            with(currentWatchListWithPositions.watchListConfig) { WatchListViewModel.Action.ReloadPositions(watchlistNumber, walletAddress) }},
                        onSettingsClick = { showSettingsDialog = true},
                        currentWatchListWithPositions = currentWatchListWithPositions,
                        showType = state.showType,
                        state = childLazyListState,
                        onSelectListToShow = { WatchListViewModel.Action.SelectListToShow(it) }
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
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // not sure if passing it in remember with state and than chaning locally is good...
                    showAddWatchListDialog = true
                }
            ) {
                Text(text = "ADD WATCH LIST")
            }
        }
    }
    if (showAddWatchListDialog) {
        FullScreenDialog(
            modifier = Modifier,
            onDismissRequest = { showAddWatchListDialog = false },
            dialogContent = {
                AddWatchListDialog(
                    onDismiss = { showAddWatchListDialog = false },
                    onAddClick = {
                        viewModel.dispatch(WatchListViewModel.Action.AddNewWatchlist(it))
                    },
                    address = enteredAddress,
                    onAddressChange = {
                        enteredAddress = it
                    },
                    onResolveClick = {
                        viewModel.dispatch(
                            WatchListViewModel.Action.OnResolveClick(
                                enteredAddress
                            )
                        )
                    },
                    resolvedAddress = state.resolvedAddress,
                    resolveError = state.resolveError,
                    addError = state.errorAddWatchlist,
                )
            },
        )
    }
    if (showSettingsDialog) {
        if (expandedItemIndex in 0..state.watchlistsWithPositions.size) {
            val watchListConfig = state.watchlistsWithPositions[expandedItemIndex].watchListConfig
            // todo
            SettingsDialog(
                watchListConfig = watchListConfig,
                onDismiss = { showSettingsDialog = false },
                onSaveClick = { viewModel.dispatch(WatchListViewModel.Action.SettingsChanged(it) )}
            )
        } else {
            Dialog(
                onDismissRequest = { showSettingsDialog = false }
            ) {
                Text(text = "Something went wrong, the settings could not be loaded")
            }
        }
    }

}

@Composable
fun ExpandableItem(
    isExpanded: Boolean,
    onClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onReloadPositionsClick: () -> Unit,
    currentWatchListWithPositions: WatchlistWithPositions,
    showType: ShowType,
    state: LazyListState,
    onSelectListToShow: (ShowType) -> Unit
) {
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
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = config.name,
                        style = AppTheme.typography.h6,
                    )
                    Column(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(text = "#FT = $sizeFT")
                        Text(text = "#NFT = $sizeNFT")
                        Text(text = "#LP = $sizeLP")
                        Text(text = "#FT&LP = $sizeFTLP")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .padding(end = 16.dp),
                    ) {
                        AppIcon(icon = Icons.Outlined.Settings)
                    }
                    IconButton(
                        onClick = onReloadPositionsClick,
                        modifier = Modifier
                            .padding(end = 16.dp),
                    ) {
                        AppIcon(icon = Icons.Outlined.Refresh)
                    }
                }
            }
        },
        expandedContent = {
            val positionItems = when (showType) {
                ShowType.FT -> currentWatchListWithPositions.positionsFT.map {
                    PositionItem.FT(
                        it
                    )
                }

                ShowType.FT_LP -> currentWatchListWithPositions.positionsFTIncludingLP.map {
                    PositionItem.FT(
                        it
                    )
                }

                ShowType.NFT -> currentWatchListWithPositions.positionsNFT.map {
                    PositionItem.NFT(
                        it
                    )
                }

                ShowType.LP -> currentWatchListWithPositions.positionsLP.map {
                    PositionItem.LP(
                        it
                    )
                }
            }
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
                        onClick = { onSelectListToShow(ShowType.FT) },
                    ) {
                        Text(text = " FT")
                    }
                    Button(
                        modifier = Modifier,
                        onClick = { onSelectListToShow(ShowType.NFT) },
                    ) {
                        Text(text = "NTF")
                    }
                    Button(
                        modifier = Modifier,
                        onClick = { onSelectListToShow(ShowType.LP) },
                    ) {
                        Text(text = " LP")
                    }
                    Button(
                        modifier = Modifier,
                        onClick = { onSelectListToShow(ShowType.FT_LP) },
                    ) {
                        Text(text = " FT & LP")
                    }
                }

                Box(modifier = Modifier.height(600.dp)) {
                    LazyColumn(
                        state = state,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        items(positionItems, key = { it.hashCode() }) { positionItem ->
                            when (positionItem) {
                                is PositionItem.FT -> PositionFTItem(item = positionItem.positionFT)
                                is PositionItem.NFT -> PositionNFTItem(item = positionItem.positionNFT)
                                is PositionItem.LP -> PositionLPItem(item = positionItem.positionLP)
                            }
                        }
                    }
                }
            }
        })
}

@Composable
fun AddWatchListDialog(
    address: String,
    addError: String?,
    resolvedAddress: String?,
    resolveError: String?,
    onAddressChange: (String) -> Unit,
    onResolveClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddClick: (WatchListConfig) -> Unit,
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
        Text(
            text = "Add new Watchlist from wallet:",
            style = AppTheme.typography.h4,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextField(
                value = address,
                onValueChange = onAddressChange,
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
                enabled = resolvedAddress != null,
                value = watchListName,
                onValueChange = { watchListName = it },
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
                onDismiss.invoke()
            },
        ) {
            Text(text = "ADD WATCH LIST")
        }
        addError?.let {
            // would be nice if it is a name error to error the name TextField...
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
                //.background(Color.White)
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
fun PositionNFTItem(item: PositionNFTLocal) {
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
    }
}

@Composable
fun PositionLPItem(item: PositionLPLocal) {
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
fun PositionFTItem(item: PositionFTLocal) {
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
    }
}