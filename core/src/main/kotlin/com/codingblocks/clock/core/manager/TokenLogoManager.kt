package com.codingblocks.clock.core.manager

import android.content.Context
import com.codingblocks.clock.core.model.tokenlogo.LogoConfig
import com.codingblocks.clock.core.remote.TokenLogoApi
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
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

interface LogoManager {
    suspend fun getLogoForUnit(unit: String): Result<String>
}

class LogoManagerImpl private constructor(
    private val context: Context,
    private val config: LogoConfig,
    private val okHttpClient: OkHttpClient,
) : LogoManager {
    class Builder(
        private val context: Context,
        private val config: LogoConfig,
    ) {
        private var okHttpClient: OkHttpClient = OkHttpClient().newBuilder().build()
        fun setOkHttpClient(okHttpClient: OkHttpClient): Builder {
            this.okHttpClient = okHttpClient
            return this
        }

        fun build(): LogoManager = LogoManagerImpl(
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

    private var api: TokenLogoApi? = provideApi()

    override suspend fun getLogoForUnit(unit: String): Result<String> = safeCall(api) {
        getTokenLogo(unit)
    }
        .mapCatching { response ->
            response.logo.value
        }
        .recoverCatching { throwable ->
            throw TokenLogoError.UnkownError(throwable)
        }


    private fun provideApi(): TokenLogoApi? {
        return Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(
                okHttpClient
            )
            .build().create(TokenLogoApi::class.java)
    }

    private suspend fun <T> safeCall(
        api: TokenLogoApi?,
        action: suspend TokenLogoApi.() -> Response<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            if (api != null) {
                val result = action(api)
                val body = result.body()
                val errorBody = result.errorBody()

                if (body != null) {
                    Result.success(body)
                } else if (errorBody != null) {
                    Result.failure(TokenLogoError.ErrorBody(errorBody))
                } else {
                    Result.failure(TokenLogoError.BodyAndErrorNull)
                }
            } else {
                Result.failure(TokenLogoError.BodyAndErrorNull)
            }
        } catch (e: HttpException) {
            Result.failure(TokenLogoError.HttpException(e.code(), e.message()))
        } catch (e: Exception) {
            Result.failure(TokenLogoError.UnkownError(e))
        }
    }
        .onFailure { Timber.e(it) }

}

sealed class TokenLogoError(msg: String? = null, throwable: Throwable? = null) :
    Throwable(message = msg, cause = throwable) {
    data object NoTokenFound : TokenLogoError()
    data object BodyAndErrorNull : TokenLogoError()
    data class UnkownError(val throwable: Throwable) : TokenLogoError(throwable = throwable)
    data class ErrorBody(val errorBody: ResponseBody) : TokenLogoError(msg = errorBody.string())
    data class HttpException(val code: Int, override val message: String) :
        TokenLogoError(msg = "CODE: ${code}\nMESSAGE: $message")
}