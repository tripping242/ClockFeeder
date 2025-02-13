package com.codingblocks.clock.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.codingblocks.clock.base.ui.scaffold.AppScaffold

@Composable
fun DetailScreen(id: Int?) {
    AppScaffold { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            Text(text = id.toString())
        }
    }
}
