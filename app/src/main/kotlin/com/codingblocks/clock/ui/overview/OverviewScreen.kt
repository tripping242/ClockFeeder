package com.codingblocks.clock.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingblocks.clock.R
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.AppTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel = getViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.dispatch(OverviewViewModel.Action.Initialize)
    }

    OverviewView(
        title = stringResource(id = R.string.app_name),
        isLoading = state.isLoading,
    )
}

@Composable
private fun OverviewView(title: String, isLoading: Boolean) {
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
            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(32.dp)
                            .size(56.dp)
                    )
                }
            }
        }
    }
}
