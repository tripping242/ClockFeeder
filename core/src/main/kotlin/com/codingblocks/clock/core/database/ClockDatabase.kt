package com.codingblocks.clock.core.database

import android.content.Context

internal class ClockDatabase(
    private val context: Context,
) {
    companion object {
        private const val PREFS_NAME = "clock_settings"
        private const val PASSWORD = "password"
        private const val IP_ADDRESS = "ip_address"
    }

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var password: String?
        get() = prefs.getString(PASSWORD, null)
        set(value) {
            prefs.edit()
                .putString(PASSWORD, value)
                .apply()
        }

    var clockIpAddress: String?
        get() = prefs.getString(IP_ADDRESS, null)
        set(value) {
            prefs.edit()
                .putString(IP_ADDRESS, value)
                .apply()
        }

    fun invalidate() {
        password = null
    }
}