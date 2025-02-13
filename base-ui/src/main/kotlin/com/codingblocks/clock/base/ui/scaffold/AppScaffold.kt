package com.codingblocks.clock.base.ui.scaffold

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codingblocks.clock.base.ui.button.AppIconButton
import com.codingblocks.clock.base.ui.theme.AppTheme

@Composable
fun AppScaffold(
    title: String,
    scrollState: LazyListState,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector = Icons.Default.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {},
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    hideElevation: Boolean = false,
    content: @Composable PaddingValues.() -> Unit,
) = AppScaffold(
    modifier = modifier,
    scrollState = scrollState,
    title = title,
    navigationIcon = {
        AppIconButton(
            icon = navigationIcon,
            onClick = onNavigationClick,
        )
    },
    actions = actions,
    scaffoldState = scaffoldState,
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
    isFloatingActionButtonDocked = isFloatingActionButtonDocked,
    hideElevation = hideElevation,
    content = content,
)

@Composable
fun AppScaffold(
    title: String,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    hideElevation: Boolean = false,
    content: @Composable PaddingValues.() -> Unit,
) {
    val isScrolled by remember(hideElevation) {
        derivedStateOf {
            (scrollState.firstVisibleItemIndex != 0 || scrollState.firstVisibleItemScrollOffset != 0) && !hideElevation
        }
    }

    AppScaffold(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        toolbarElevationVisible = isScrolled,
        actions = actions,
        scaffoldState = scaffoldState,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        isFloatingActionButtonDocked = isFloatingActionButtonDocked,
        content = content,
    )
}

@Composable
fun AppScaffold(
    title: String,
    scrollState: ScrollState,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector = Icons.Default.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {},
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    hideElevation: Boolean = false,
    content: @Composable PaddingValues.() -> Unit,
) = AppScaffold(
    modifier = modifier,
    scrollState = scrollState,
    title = title,
    navigationIcon = {
        AppIconButton(
            icon = navigationIcon,
            onClick = onNavigationClick,
        )
    },
    actions = actions,
    scaffoldState = scaffoldState,
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
    isFloatingActionButtonDocked = isFloatingActionButtonDocked,
    hideElevation = hideElevation,
    content = content,
)

@Composable
fun AppScaffold(
    title: String,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    hideElevation: Boolean = false,
    content: @Composable PaddingValues.() -> Unit,
) {
    val isScrolled by remember {
        derivedStateOf {
            scrollState.value != 0 && !hideElevation
        }
    }

    AppScaffold(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        toolbarElevationVisible = isScrolled,
        actions = actions,
        scaffoldState = scaffoldState,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        isFloatingActionButtonDocked = isFloatingActionButtonDocked,
        content = content,
    )
}

@SuppressLint("ComposeParameterOrder")
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    onNavigationClick: () -> Unit,
    navigationIcon: ImageVector = Icons.Default.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {},
    toolbarElevationVisible: Boolean = false,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    content: @Composable PaddingValues.() -> Unit,
) = AppScaffold(
    modifier = modifier,
    title = title,
    navigationIcon = {
        AppIconButton(
            icon = navigationIcon,
            onClick = onNavigationClick,
        )
    },
    actions = actions,
    toolbarElevationVisible = toolbarElevationVisible,
    scaffoldState = scaffoldState,
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
    isFloatingActionButtonDocked = isFloatingActionButtonDocked,
    content = content,
)

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    toolbarElevationVisible: Boolean = false,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    content: @Composable PaddingValues.() -> Unit,
) {
    val topAppBarTextStyle = if (navigationIcon == null) AppTheme.typography.h5 else AppTheme.typography.h6

    AppScaffold(
        modifier = modifier,
        topAppBar = {
            if (title != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = topAppBarTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    backgroundColor = AppTheme.colors.background,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    elevation = if (toolbarElevationVisible) AppBarDefaults.TopAppBarElevation else 0.dp,
                )
            }
        },
        scaffoldState = scaffoldState,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        isFloatingActionButtonDocked = isFloatingActionButtonDocked,
        content = content,
    )
}

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topAppBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topAppBar,
        bottomBar = bottomBar,
        scaffoldState = scaffoldState,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        isFloatingActionButtonDocked = isFloatingActionButtonDocked,
        content = { contentPadding -> content(contentPadding) },
    )
}

@Preview
@Composable
private fun AppScaffoldPreview() {
    AppTheme {
        AppScaffold(
            title = "Test",
            content = {},
        )
    }
}

@Preview
@Composable
private fun AppScaffoldToolbarElevationPreview() {
    AppTheme {
        AppScaffold(
            title = "Test",
            toolbarElevationVisible = true,
            content = {},
        )
    }
}

@Preview
@Composable
private fun AppScaffoldBackNavigationPreview() {
    AppTheme {
        AppScaffold(
            title = "Test",
            navigationIcon = Icons.Default.ArrowBack,
            onNavigationClick = {},
            content = {},
        )
    }
}
