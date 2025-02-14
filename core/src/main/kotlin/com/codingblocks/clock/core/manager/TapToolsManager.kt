package com.codingblocks.clock.core.manager

import android.content.Context
import com.codingblocks.clock.core.database.TapToolsDatabase
import com.codingblocks.clock.core.interceptor.TapToolsKeyInterceptor
import com.codingblocks.clock.core.model.taptools.TapToolsConfig
import com.codingblocks.clock.core.remote.TapToolsApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface TapToolsManager {
    fun updateApiKey(key: String?)
}

class TapToolsManagerImpl private constructor(
    private val context: Context,
    private val config: TapToolsConfig,
    private val okHttpClient: OkHttpClient,
) : TapToolsManager {
    class Builder(
        private val context: Context,
        private val config: TapToolsConfig,
    ) {
        private var okHttpClient: OkHttpClient = OkHttpClient().newBuilder().build()
        fun setOkHttpClient(okHttpClient: OkHttpClient): Builder {
            this.okHttpClient = okHttpClient
            return this
        }

        fun build(): TapToolsManager = TapToolsManagerImpl(
            context, config, okHttpClient
        )
    }

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }

    private val database by lazy { TapToolsDatabase(context) }

    // todo settings to set the apiToken, when set, call updateApiKey
    private var api: TapToolsApi? = provideApi("eOSYRNQmimLJg0OXUGcq3Pf2KP0Bvc88")

    override fun updateApiKey(key: String?) {
        database.apiKey = key
        api = provideApi(key)
    }


    private fun provideApi(key: String?): TapToolsApi? {
        if (key == null) {
            return null
        }
        return Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(
                okHttpClient.newBuilder()
                    .addInterceptor(TapToolsKeyInterceptor(key))
                    .build()
            )
            .build().create(TapToolsApi::class.java)
    }
}