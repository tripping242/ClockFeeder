package com.codingblocks.clock.core.manager

import android.content.Context
import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.codingblocks.clock.core.database.ClockDatabase
import com.codingblocks.clock.core.model.clock.OverUnderResponse
import com.codingblocks.clock.core.model.clock.StatusResponse
import com.codingblocks.clock.core.remote.ClockApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

interface ClockManager {
    fun setCredentials(password: String?, ipAddress: String?)
    suspend fun pauseBlockClock(): Result<Any>
    suspend fun resumeBlockClock(): Result<Any>
    suspend fun getStatus(): Result<StatusResponse>
    suspend fun sendFTPriceFeed(encodedPrice: String, pair: String): Result<Any>
    suspend fun setOverUnderText(position: Int, over: String, under: String): Result<OverUnderResponse>
    /*suspend fun sendNFTPriceAlert()
    suspend fun sendFTPriceFeed()
    suspend fun sendNFTPriceFeed()*/
}

class ClockManagerImpl private constructor(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
) : ClockManager {
    class Builder(
        private val context: Context,
    ) {
        private var okHttpClient: OkHttpClient = OkHttpClient().newBuilder().build()
        fun setOkHttpClient(okHttpClient: OkHttpClient): Builder {
            this.okHttpClient = okHttpClient
            return this
        }

        fun build(): ClockManager = ClockManagerImpl(
            context, okHttpClient
        )
    }

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    private val database by lazy { ClockDatabase(context) }

    // todo settings to set the blockClock ip and password when set, call setCredentials
    private var api: ClockApi? = provideApi("tripping", "192.168.0.100")

    override fun setCredentials(password: String?, ipAddress: String?) {
        database.password = password
        database.clockIpAddress = ipAddress
        api = provideApi(password, ipAddress)
    }

    override suspend fun pauseBlockClock(): Result<Any> = safeCall(api) {
        pauseClock()
    }

    override suspend fun resumeBlockClock(): Result<Any> = safeCall(api) {
        resumeClock()
    }

    override suspend fun getStatus(): Result<StatusResponse> = safeCall(api) {
        status()
    }

    override suspend fun sendFTPriceFeed(encodedPrice: String, pair: String): Result<Any> = safeCall(api) {
        setTextTlBrPairSym(
            text = encodedPrice,
            tl = null,
            br = null,
            pair = pair,
            omitLine = 1,
        )
    }

    override suspend fun setOverUnderText(position: Int, over: String, under: String): Result<OverUnderResponse> = safeCall(api) {
        setOverUnderText(position, over, under)
    }

    private fun provideApi(password: String?, ipAddress: String?): ClockApi? {
        if (password == null || ipAddress == null) {
            return null
        }
        val authenticator = DigestAuthenticator(Credentials("x", password))

        val authCache: Map<String, CachingAuthenticator> = ConcurrentHashMap()

        return Retrofit.Builder()
            .baseUrl("http://${ipAddress}/api/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(
                okHttpClient.newBuilder()
                    .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
                    .addInterceptor(AuthenticationCacheInterceptor(authCache))
                    .build()
            )
            .build().create(ClockApi::class.java)
    }

    private suspend fun <T> safeCall(
        api: ClockApi?,
        action: suspend ClockApi.() -> Response<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            if (api != null) {
                val result = action(api)
                val body = result.body()
                val errorBody = result.errorBody()

                if (body != null) {
                    Result.success(body)
                } else if (errorBody != null) {
                    if (result.code() == 429) { // Handling 429 specifically
                        val errorJson = errorBody.string()
                        val waitTime = extractWaitTime(errorJson)
                        if (waitTime != null) {
                            Result.failure(ClockApiError.TooManyRequests(waitTime))
                        } else { Result.failure(ClockApiError.ErrorBody(errorBody)) }
                    } else {
                        Result.failure(ClockApiError.ErrorBody(errorBody))
                    }
                } else {
                    Result.failure(ClockApiError.BodyAndErrorNull)
                }
            } else {
                Result.failure(ClockApiError.NoTokenFound)
            }
        } catch (e: HttpException) {
            Result.failure(ClockApiError.HttpException(e.code(), e.message()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            Result.failure(ClockApiError.ClockNotOnline)
        } catch (e: ConnectException) {
            Result.failure(ClockApiError.ClockNotOnline)
        } catch (e: Exception) {
            Result.failure(ClockApiError.UnkownError(e))
        }

    }
        .onFailure { Timber.e(it) }
}

sealed class ClockApiError(msg: String? = null, throwable: Throwable? = null) : Throwable(message = msg, cause = throwable) {
    data object NoTokenFound : ClockApiError()
    data object NoUserId : ClockApiError()
    data object BodyAndErrorNull : ClockApiError()
    data class TooManyRequests(val waitTime: Double) : ClockApiError()
    data object ClockNotOnline : ClockApiError(msg = "Your BlockClock seems to be unreachable")
    data class ErrorBody(val errorBody: ResponseBody) : ClockApiError(msg = errorBody.string())
    data class HttpException(val code: Int, override val message: String) : ClockApiError(msg = "CODE: ${code}\nMESSAGE: $message")
    data class UnkownError(val throwable: Throwable) : ClockApiError(throwable = throwable)
}

private fun extractWaitTime(errorJson: String): Double? {
    return try {
        val jsonObject = JSONObject(errorJson)
        val errorMessage = jsonObject.getString("error")

        errorMessage.substringAfter("Please wait ").substringBefore(" seconds").toDoubleOrNull()
    } catch (e: Exception) {
        Timber.e("Failed to parse wait time: ${e.message}")
        null
    }
}