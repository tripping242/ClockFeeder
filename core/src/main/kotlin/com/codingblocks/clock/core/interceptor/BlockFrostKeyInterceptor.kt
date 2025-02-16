package com.codingblocks.clock.core.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class BlockFrostKeyInterceptor(
    private val key: String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val keyResult = key ?: return chain.proceed(chain.request())
        Timber.d("key: $keyResult")
        return chain.proceed(
            chain.request()
                .newBuilder()
                .header("project_id", keyResult)
                .header("Content-Type", "application/json")
                .build()
        )
    }
}
