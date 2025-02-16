package com.codingblocks.clock.ui.watchlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingblocks.clock.R
import com.codingblocks.clock.base.ui.card.ExpandableCard
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.AppTheme
import com.codingblocks.clock.base.ui.theme.md_theme_light_error
import com.codingblocks.clock.base.ui.utils.formatMax8decimals
import com.codingblocks.clock.base.ui.utils.formatToNoDecimals
import com.codingblocks.clock.base.ui.utils.prettyPrintDataClass
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import com.codingblocks.clock.core.local.data.formattedHHMM
import com.codingblocks.clock.ui.watchlists.WatchListViewModel.PositionItem
import com.codingblocks.clock.ui.watchlists.WatchListViewModel.ShowType
import org.koin.androidx.compose.getViewModel

@Composable
fun WatchlistsScreen(
    viewModel: WatchListViewModel = getViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState: LazyListState = rememberLazyListState()

    AppScaffold(
        title = stringResource(id = R.string.screen_watchlists),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            if (state.currentFirstWatchListWithPositions == null) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.dispatch(WatchListViewModel.Action.GetPositions) },
                ) {
                    Text(text = "GET DUMMY WATCH LIST LOADED")
                }
            } else {
                // todo lazylist laer, not it maps to first possible
                state.currentFirstWatchListWithPositions?.let { currentWatchListWithPositions ->
                    ExpandableCard(topContent = {
                        val config = currentWatchListWithPositions.watchListConfig
                        val sizeFT = currentWatchListWithPositions.positionsFT.size
                        val sizeNFT = currentWatchListWithPositions.positionsNFT.size
                        val sizeLP = currentWatchListWithPositions.positionsLP.size
                        val sizeFTLP = currentWatchListWithPositions.positionsFTIncludingLP.size

                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                        }

                    }, expandedContent = {
                        val positionItems = when (state.showType) {
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
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    modifier = Modifier,
                                    onClick = {
                                        viewModel.dispatch(
                                            WatchListViewModel.Action.SelectListToShow(
                                                WatchListViewModel.ShowType.FT
                                            )
                                        )
                                    },
                                ) {
                                    Text(text = " FT")
                                }
                                Button(
                                    modifier = Modifier,
                                    onClick = {
                                        viewModel.dispatch(
                                            WatchListViewModel.Action.SelectListToShow(
                                                WatchListViewModel.ShowType.NFT
                                            )
                                        )
                                    },
                                ) {
                                    Text(text = "NTF")
                                }
                                Button(
                                    modifier = Modifier,
                                    onClick = {
                                        viewModel.dispatch(
                                            WatchListViewModel.Action.SelectListToShow(
                                                WatchListViewModel.ShowType.LP
                                            )
                                        )
                                    },
                                ) {
                                    Text(text = " LP")
                                }
                                Button(
                                    modifier = Modifier,
                                    onClick = {
                                        viewModel.dispatch(
                                            WatchListViewModel.Action.SelectListToShow(
                                                WatchListViewModel.ShowType.FT_LP
                                            )
                                        )
                                    },
                                ) {
                                    Text(text = " FT & LP")
                                }
                            }

                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.padding(vertical = 4.dp),
                            ) {
                                items(positionItems) { positionItem ->
                                    when (positionItem) {
                                        is PositionItem.FT -> PositionFTItem(item = positionItem.positionFT)
                                        is PositionItem.NFT -> PositionNFTItem(item = positionItem.positionNFT)
                                        is PositionItem.LP -> PositionLPItem(item = positionItem.positionLP)
                                    }
                                }
                            }
                        }
                    })
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = md_theme_light_error,
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.dispatch(WatchListViewModel.Action.GetClockStatus) },
                ) {
                    Text(text = "GET CLOCK STATUS")
                }
                state.status?.let {
                    Text(prettyPrintDataClass(it))
                }
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