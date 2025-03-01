package com.codingblocks.clock.core.manager

import android.content.Context
import com.codingblocks.clock.core.database.BlockFrostDatabase
import com.codingblocks.clock.core.interceptor.BlockFrostKeyInterceptor
import com.codingblocks.clock.core.model.blockfrost.AssetAddress
import com.codingblocks.clock.core.model.blockfrost.BlockFrostConfig
import com.codingblocks.clock.core.remote.BlockFrostApi
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
import java.math.BigInteger
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException


private const val adaHandlePolicyID = "f0ff48bbb7bbe9d59a40f1ce90e9e9d0ff5002ec48f232b49ca0fb9a"

interface BlockFrostManager {
    fun updateApiKey(key: String?)
    suspend fun resolveAdaHandle(handle: String): Result<String>
    suspend fun getAdaHandleAddresses(handle: String): Result<List<AssetAddress>>
    suspend fun getStakeAddress(address: String): Result<String>
}

class BlockFrostManagerImpl private constructor(
    private val context: Context,
    private val config: BlockFrostConfig,
    private val okHttpClient: OkHttpClient,
) : BlockFrostManager {
    class Builder(
        private val context: Context,
        private val config: BlockFrostConfig,
    ) {
        private var okHttpClient: OkHttpClient = OkHttpClient().newBuilder().build()
        fun setOkHttpClient(okHttpClient: OkHttpClient): Builder {
            this.okHttpClient = okHttpClient
            return this
        }

        fun build(): BlockFrostManager = BlockFrostManagerImpl(
            context, config, okHttpClient
        )
    }

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    private val database by lazy { BlockFrostDatabase(context) }

    private var api: BlockFrostApi? = provideApi("mainnetYpTwo16JgFrAzAqsDRt7I4PqRxGTR1bs")

    override fun updateApiKey(key: String?) {
        database.apiKey = key
        api = provideApi(key)
    }

    override suspend fun resolveAdaHandle(handle: String): Result<String> = safeCall(api) {
        // Call the API to get the addresses and return the Response
        getAdaHandleAddresses(adaHandlePolicyID + handle.toHex(), 1)
    }
        .mapCatching { response ->
            response
                .firstOrNull()
                ?.address
                ?: throw BlockFrostError.CouldNotBeResolved
        }
        .recoverCatching { throwable ->
            // Handle failures and propagate a custom error
            throw BlockFrostError.CouldNotBeResolved
        }

    override suspend fun getAdaHandleAddresses(handle: String): Result<List<AssetAddress>> =
        safeCall(api) {
            getAdaHandleAddresses(adaHandlePolicyID + handle.toHex(), 1)
        }

    override suspend fun getStakeAddress(address: String): Result<String> = safeCall(api) {
        getStakeAddress(address)
    }
        .mapCatching { response ->
            response.stakeAddress
        }
        .recoverCatching { throwable ->
            // Handle failures and propagate a custom error
            throw BlockFrostError.CouldNotResolveStakeAddress
        }


    private fun provideApi(key: String?): BlockFrostApi? {
        if (key == null) {
            return null
        }

        return Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(
                okHttpClient.newBuilder()
                    .addInterceptor(BlockFrostKeyInterceptor(key))
                    .build()
            )
            .build().create(BlockFrostApi::class.java)
    }

    private suspend fun <T> safeCall(
        api: BlockFrostApi?,
        action: suspend BlockFrostApi.() -> Response<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            if (api != null) {
                val result = action(api)
                val body = result.body()
                val errorBody = result.errorBody()

                if (body != null) {
                    Result.success(body)
                } else if (errorBody != null) {
                    Result.failure(BlockFrostError.ErrorBody(errorBody))
                } else {
                    Result.failure(BlockFrostError.BodyAndErrorNull)
                }
            } else {
                Result.failure(BlockFrostError.NoTokenFound)
            }
        } catch (e: HttpException) {
            Result.failure(BlockFrostError.HttpException(e.code(), e.message()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            Result.failure(BlockFrostError.BlockFrostNotOnline)
        } catch (e: ConnectException) {
            Result.failure(BlockFrostError.BlockFrostNotOnline)
        } catch (e: Exception) {
            Result.failure(BlockFrostError.UnkownError(e))
        }
    }
        .onFailure { Timber.e(it) }
}

sealed class BlockFrostError(msg: String? = null, throwable: Throwable? = null) :
    Throwable(message = msg, cause = throwable) {
    data object NoTokenFound : BlockFrostError()
    data object NoUserId : BlockFrostError()
    data object BodyAndErrorNull : BlockFrostError()
    data object BlockFrostNotOnline :
        BlockFrostError(msg = "The BlockFrost api seems to be unreachable")

    data object CouldNotBeResolved : BlockFrostError(msg = "The handle could not be resolved")
    data object CouldNotResolveStakeAddress :
        BlockFrostError(msg = "The address could not be resolved")

    data class ErrorBody(val errorBody: ResponseBody) : BlockFrostError(msg = errorBody.string())
    data class HttpException(val code: Int, override val message: String) :
        BlockFrostError(msg = "CODE: ${code}\nMESSAGE: $message")

    data class UnkownError(val throwable: Throwable) : BlockFrostError(throwable = throwable)
}

fun String.toHex(): String {
    return java.lang.String.format("%x", BigInteger(1, this.toByteArray()))
}
