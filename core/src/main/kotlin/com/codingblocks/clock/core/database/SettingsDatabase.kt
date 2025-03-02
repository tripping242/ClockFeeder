package com.codingblocks.clock.core.database

import android.content.Context

class SettingsDatabase(
    private val context: Context,
) {
    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val PRICE_TOO_OLD_SECONDS = "price_too_old"
        private const val FEED_CLOCK_CYCLE_SECONDS = "feed_clock_cycle"
        private const val AUTO_FEED = "auto_feed"
        private const val AUTO_LOAD = "auto_load"
        private const val SMALL_TREND = "small_trend"
        private const val HIGH_TREND = "high_trend"

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

    var autoFeed: Boolean
        get() = prefs.getBoolean(AUTO_FEED, false)
        set(value) {
            prefs.edit()
                .putBoolean(AUTO_FEED, value)
                .apply()
        }
    var autoReloadPositions: Boolean
        get() = prefs.getBoolean(AUTO_LOAD, false)
        set(value) {
            prefs.edit()
                .putBoolean(AUTO_LOAD, value)
                .apply()
        }

    var smallTrendPercent: Double
        get() = prefs.getString (SMALL_TREND, "0.1")?.toDouble() ?: 0.0
        set(value) {
            prefs.edit()
                .putString(SMALL_TREND, value.toString())
                .apply()
        }

    var highTrendPercent: Double
        get() = prefs.getString (HIGH_TREND, "5")?.toDouble() ?: 5.0
        set(value) {
            prefs.edit()
                .putString(HIGH_TREND, value.toString())
                .apply()
        }
}