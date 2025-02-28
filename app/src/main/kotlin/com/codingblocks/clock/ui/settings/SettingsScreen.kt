package com.codingblocks.clock.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.codingblocks.clock.core.local.data.FeedToClockItem
import org.koin.androidx.compose.getViewModel

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
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(SettingsViewModel.Action.StartClockFeedCycler) },
                ) {
                    Text(text = "START CYCLING FEED")
                }
                Button(
                    modifier = Modifier,
                    onClick = { viewModel.dispatch(SettingsViewModel.Action.PauseClockFeedCycler) },
                ) {
                    Text(text = "PAUSE CYCLING FEED")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier,
                    onClick = {
                        viewModel.dispatch(SettingsViewModel.Action.LoadAndUpdateFeedFTToClockItems)
                    },
                ) {
                    Text(text = "RELOAD FT")
                }
                Button(
                    modifier = Modifier,
                    onClick = {
                        viewModel.dispatch(SettingsViewModel.Action.LoadAndUpdateFeedNFTToClockItems)
                    },
                ) {
                    Text(text = "RELOAD NFT")
                }
            }
            state.error?.let {
                Text(
                    text = it,
                    color = md_theme_light_error,
                )
            }

            if (state.feedToClockItems.isNotEmpty()) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        items(state.feedToClockItems) { item ->
                            FeedToClock(item = item)
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
