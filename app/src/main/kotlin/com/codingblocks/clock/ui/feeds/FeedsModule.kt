package com.codingblocks.clock.ui.feeds

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val feedsModule = module {
    viewModel { FeedsViewModel(dataRepo = get()) }
}