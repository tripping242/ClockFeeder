package com.codingblocks.clock.core.database

import android.content.Context

internal class TapToolsDatabase(
    private val context: Context,
) {
    companion object {
        private const val PREFS_NAME = "taptools_settings"
        private const val KEY_TOKEN = "api_key"
    }

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var apiKey: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit()
                .putString(KEY_TOKEN, value)
                .apply()
        }

    fun invalidate() {
        apiKey = null
    }
}