package com.codingblocks.clock.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingblocks.clock.R
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.md_theme_light_error
import com.codingblocks.clock.base.ui.utils.formatMax8decimals
import com.codingblocks.clock.base.ui.utils.formatToNoDecimals
import com.codingblocks.clock.core.local.data.PositionFT
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(SettingsViewModel.Action.GetPositionsFT) },
                ) {
                    Text(text = "UPDATE FT")
                }
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(SettingsViewModel.Action.GetPositionsNFT) },
                ) {
                    Text(text = "UPDATE NFT")
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

            if (state.showFT) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    items(positionsFT) { positionFT ->
                        PositionFTItem(item = positionFT)
                    }
                }
            } else {
                Timber.tag("wims").i("show Nft, size ${positionsNFT.size}")
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    items(positionsNFT) { positionNFT ->
                        PositionNFTItem(item = positionNFT)
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
        Text(text = item.name)

        Text(text = item.balance.formatMax8decimals())

        Text(text = item.adaValue.formatToNoDecimals())

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
