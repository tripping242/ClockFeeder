package com.codingblocks.clock.ui.settings

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
import com.codingblocks.clock.core.local.data.PositionFT
import com.codingblocks.clock.core.local.data.PositionLP
import com.codingblocks.clock.core.local.data.PositionNFT
import com.codingblocks.clock.core.local.data.formattedHHMM
import okhttp3.internal.toImmutableList
import org.koin.androidx.compose.getViewModel
import timber.log.Timber

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = getViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState: LazyListState = rememberLazyListState()

    AppScaffold(
        title = stringResource(id = R.string.screen_feeds),
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
                    onClick = { viewModel.dispatch(SettingsViewModel.Action.GetPositionsFT) },
                ) {
                    Text(text = " FT")
                }
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(SettingsViewModel.Action.GetPositionsNFT) },
                ) {
                    Text(text = "NFT")
                }
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(SettingsViewModel.Action.GetPositionsLP) },
                ) {
                    Text(text = " LP")
                }
            }
            state.error?.let {
                Text(
                    text = it,
                    color = md_theme_light_error,
                )
            }
            val positionsFT = state.positionsFT.toImmutableList()
            val positionsNFT = state.positionsNFT.toImmutableList()
            val positionsLP = state.positionsLP.toImmutableList()

            when (state.showType) {
                SettingsViewModel.ShowType.FT -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        items(positionsFT) { positionFT ->
                            PositionFTItem(item = positionFT)
                        }
                    }
                }
                SettingsViewModel.ShowType.NFT -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        items(positionsNFT) { positionNFT ->
                            PositionNFTItem(item = positionNFT)
                        }
                    }
                }
                SettingsViewModel.ShowType.LP -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        items(positionsLP) { positionLP ->
                            PositionLPItem(item = positionLP)
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun PositionNFTItem(item: PositionNFT) {
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
fun PositionLPItem(item: PositionLP) {
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
fun PositionFTItem(item: PositionFT) {
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
