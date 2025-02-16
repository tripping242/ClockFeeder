package com.codingblocks.clock.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingblocks.clock.R
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.AppTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel = getViewModel(),
    onListElementClicked: (id: Int) -> Unit,
) {
    val viewModelState by viewModel.state.collectAsStateWithLifecycle()

    OverviewView(
        title = stringResource(id = R.string.app_name),
        onListElementClicked = onListElementClicked,
    )
}

@Composable
private fun OverviewView(title: String, onListElementClicked: (id: Int) -> Unit) {
    AppScaffold { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Text(
                text = "ClockFeeder",
                style = AppTheme.typography.h4
            )
        }
        /*LazyColumn(
            modifier = Modifier.padding(contentPadding),
        ) {
            items((0..500).toList()) {
                Text(
                    text = it.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onListElementClicked(it) }
                        .padding(AppTheme.dimens.dimen16),
                )
            }
        }*/
    }
}

@Preview
@Composable
private fun OverviewPreview() {
    AppTheme {
        OverviewView(
            title = stringResource(id = R.string.app_name),
            onListElementClicked = {},
        )
    }
}
