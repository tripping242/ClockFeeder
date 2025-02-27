package com.codingblocks.clock.core.manager

import android.content.Context
import com.codingblocks.clock.core.database.TapToolsDatabase
import com.codingblocks.clock.core.interceptor.TapToolsKeyInterceptor
import com.codingblocks.clock.core.model.taptools.NFTStatsResponse
import com.codingblocks.clock.core.model.taptools.PositionsResponse
import com.codingblocks.clock.core.model.taptools.TapToolsConfig
import com.codingblocks.clock.core.remote.TapToolsApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

interface TapToolsManager {
    fun updateApiKey(key: String?)
    suspend fun getPositionsForAddress(address: String) : Result<PositionsResponse>
    suspend fun getPricesForTokens(list: List<String>) : Result<Map<String, Double>>
    suspend fun getStatsForPolicy(policy: String) : Result<NFTStatsResponse>
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
            explicitNulls = false
            allowStructuredMapKeys = true
        }
    }

    private val database by lazy { TapToolsDatabase(context) }

    // todo settings to set the apiToken, when set, call updateApiKey
    private var api: TapToolsApi? = provideApi("eOSYRNQmimLJg0OXUGcq3Pf2KP0Bvc88")

    override fun updateApiKey(key: String?) {
        database.apiKey = key
        api = provideApi(key)
    }

    override suspend fun getPositionsForAddress(address: String): Result<PositionsResponse> = safeCall(api) {
        getPositionsForAddress(address)
    }

    override suspend fun getPricesForTokens(list: List<String>): Result<Map<String, Double>> = safeCall(api) {
        getTokenPrices(list)
    }

    override suspend fun getStatsForPolicy(policy: String): Result<NFTStatsResponse> = safeCall(api) {
        getStatsForPolicy(policy)
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
    private suspend fun <T> safeCall(
        api: TapToolsApi?,
        action: suspend TapToolsApi.() -> Response<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            if (api != null) {
                val result = action(api)
                val body = result.body()
                val errorBody = result.errorBody()

                if (body != null) {
                    Result.success(body)
                } else if (errorBody != null) {
                    Result.failure(TapToolsApiError.ErrorBody(errorBody))
                } else {
                    Result.failure(TapToolsApiError.BodyAndErrorNull)
                }
            } else {
                Result.failure(TapToolsApiError.NoTokenFound)
            }
        } catch (e: HttpException) {
            Result.failure(TapToolsApiError.HttpException(e.code(), e.message()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(TapToolsApiError.UnkownError(e))
        }
    }
        .onFailure { Timber.e(it) }
}

sealed class TapToolsApiError(msg: String? = null, throwable: Throwable? = null) : Throwable(message = msg, cause = throwable) {
    data object NoTokenFound : TapToolsApiError()
    data object NoUserId : TapToolsApiError()
    data object BodyAndErrorNull : TapToolsApiError()
    data class ErrorBody(val errorBody: ResponseBody) : TapToolsApiError(msg = errorBody.string())
    data class HttpException(val code: Int, override val message: String) : TapToolsApiError(msg = "CODE: ${code}\nMESSAGE: $message")
    data class UnkownError(val throwable: Throwable) : TapToolsApiError(throwable = throwable)
}