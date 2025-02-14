package com.codingblocks.clock.ui.watchlists

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val watchListModule = module {
    viewModel { WatchListViewModel(dataRepo = get()) }
}