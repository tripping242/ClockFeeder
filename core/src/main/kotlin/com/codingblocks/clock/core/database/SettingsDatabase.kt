package com.codingblocks.clock.core.database

import android.content.Context

class SettingsDatabase(
    private val context: Context,
) {
    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val PRICE_TOO_OLD_SECONDS = "price_too_old"
        private const val FEED_CLOCK_CYCLE_SECONDS = "feed_clock_cycle"
    }

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var feedClockCycleSeconds: Int
        get() = prefs.getInt(FEED_CLOCK_CYCLE_SECONDS, 1 * 60)
        set(value) {
            prefs.edit()
                .putInt(FEED_CLOCK_CYCLE_SECONDS, value)
                .apply()
        }

    var priceTooOldSeconds: Int
        get() = prefs.getInt(PRICE_TOO_OLD_SECONDS, 15 * 60)
        set(value) {
            prefs.edit()
                .putInt(PRICE_TOO_OLD_SECONDS, value)
                .apply()
        }

}