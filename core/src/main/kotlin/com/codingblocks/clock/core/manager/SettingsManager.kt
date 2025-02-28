package com.codingblocks.clock.core.manager

import android.content.Context
import com.codingblocks.clock.core.database.SettingsDatabase

interface SettingsManager {
    val settings: SettingsDatabase
}

class SettingsManagerImpl(
    private val context: Context
) : SettingsManager {
    override val settings = SettingsDatabase(context)
}