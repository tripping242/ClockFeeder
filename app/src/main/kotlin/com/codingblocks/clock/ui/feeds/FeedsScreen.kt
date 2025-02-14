package com.codingblocks.clock.ui.feeds

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingblocks.clock.R
import com.codingblocks.clock.base.ui.scaffold.AppScaffold
import com.codingblocks.clock.base.ui.theme.md_theme_light_error
import com.codingblocks.clock.base.ui.utils.prettyPrintDataClass
import org.koin.androidx.compose.getViewModel

@Composable
fun FeedsScreen(
    viewModel: FeedsViewModel = getViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    AppScaffold(
        title = stringResource(id = R.string.screen_feeds),
        scrollState = scrollState,
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp).fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            Text("Hello watchlists:")
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.dispatch(FeedsViewModel.Action.GetPositions) },
            ) {
                Text(text = "GET POSITION")
            }
            state.positions?.let {
                Text(prettyPrintDataClass(it))
            }
            state.error?.let {
                Text(
                    text = it,
                    color = md_theme_light_error,
                )
            }
        }
    }
}
