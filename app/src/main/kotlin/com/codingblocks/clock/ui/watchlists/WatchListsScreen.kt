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
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
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
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(WatchListViewModel.Action.SelectListToShow(WatchListViewModel.ShowType.FT)) },
                ) {
                    Text(text = " FT")
                }
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(WatchListViewModel.Action.SelectListToShow(WatchListViewModel.ShowType.NFT)) },
                ) {
                    Text(text = "NTF")
                }
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(WatchListViewModel.Action.SelectListToShow(WatchListViewModel.ShowType.LP)) },
                ) {
                    Text(text = " LP")
                }
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(WatchListViewModel.Action.SelectListToShow(WatchListViewModel.ShowType.FT_LP)) },
                ) {
                    Text(text = " FT & LP")
                }
            }

            val positionItems = when (state.showType) {
                ShowType.FT -> state.positionsFT.map { PositionItem.FT(it) }
                ShowType.FT_LP -> state.positionsFTIncludingLP.map { PositionItem.FT(it) }
                ShowType.NFT -> state.positionsNFT.map { PositionItem.NFT(it) }
                ShowType.LP -> state.positionsLP.map { PositionItem.LP(it) }
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
            text = item.name,
            modifier = Modifier.width(120.dp), // Adjust width as needed
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.width(120.dp), // Adjust width as needed
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = item.balance.formatMax8decimals(), modifier = Modifier.weight(1f))
            Text(text = item.adaValue.formatToNoDecimals(), modifier = Modifier.weight(1f))
        }

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

        Text(text = item.tokenAAmount.formatToNoDecimals(), modifier = Modifier.width(100.dp).padding(end = 16.dp),)
        Text(text = (item.adaValue / 2).formatToNoDecimals(), modifier = Modifier.width(60.dp),)

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
        Text(text = item.ticker)

        Text(text = item.balance.formatMax8decimals())

        Text(text = item.adaValue.formatToNoDecimals())

        Text(text = item.lastUpdated.formattedHHMM())
    }
}