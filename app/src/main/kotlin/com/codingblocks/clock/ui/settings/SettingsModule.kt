package com.codingblocks.clock.ui.settings

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val settingsModule = module {
    viewModel { SettingsViewModel(dataRepo = get()) }
}