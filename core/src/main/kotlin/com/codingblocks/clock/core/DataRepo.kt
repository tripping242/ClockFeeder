/*
 * Copyright 2020 Tailored Media GmbH.
 * Created by Florian Schuster.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codingblocks.clock.core

import android.content.Context
import com.codingblocks.clock.core.local.AppDatabase
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import com.codingblocks.clock.core.local.data.WatchListConfig
import com.codingblocks.clock.core.local.data.WatchlistWithPositions
import com.codingblocks.clock.core.manager.BlockFrostManager
import com.codingblocks.clock.core.manager.BlockFrostManagerImpl
import com.codingblocks.clock.core.manager.ClockManager
import com.codingblocks.clock.core.manager.ClockManagerImpl
import com.codingblocks.clock.core.manager.TapToolsManager
import com.codingblocks.clock.core.manager.TapToolsManagerImpl
import com.codingblocks.clock.core.model.AppBuildInfo
import com.codingblocks.clock.core.model.blockfrost.BlockFrostConfig
import com.codingblocks.clock.core.model.clock.StatusResponse
import com.codingblocks.clock.core.model.taptools.PositionsFt
import com.codingblocks.clock.core.model.taptools.PositionsLp
import com.codingblocks.clock.core.model.taptools.PositionsNft
import com.codingblocks.clock.core.model.taptools.PositionsResponse
import com.codingblocks.clock.core.model.taptools.TapToolsConfig
import okhttp3.OkHttpClient
import java.time.ZonedDateTime

interface DataRepo {
    val watchlistsWithPositions: List<WatchlistWithPositions>

    suspend fun getClockStatus() : Result<StatusResponse>
    // wallet with address
    suspend fun resolveAdaHandle(handle: String): Result<String>
    suspend fun getStakeAddress(address: String) : Result<String>
    suspend fun loadPositionsForAddress(address: String) : Result<PositionsResponse>
    suspend fun updateOrInsertPositions(watchList: Int, positionResponse: PositionsResponse)

    // watchlist with contents
    suspend fun addWatchlist(name: String, includeLPinFT: Boolean, includeNFT: Boolean, showLPTab: Boolean, walletAddress: String?) : Int
    // todo to refresh, like reload watchlists we mmight still need a call refreshPositionsForWatchlist
    /*suspend fun getFTPositionsForWatchlist() : List<PositionFTLocal>
    suspend fun getNFTPositionsForWatchlist() : List<PositionNFTLocal>
    suspend fun getLPPositionsForWatchlist() : List<PositionLPLocal>
    suspend fun getFTPositionsForWatchlistIncludingLP() : List<PositionFTLocal>*/
}

class CoreDataRepo(
    private val context: Context,
    private val database: AppDatabase,
    private val okHttpClient: OkHttpClient,
    private val appBuildInfo: AppBuildInfo,
) : DataRepo {
    private val tapToolsManager: TapToolsManager = provideTapToolsManager()
    private val clockManager: ClockManager = provideClockManager()
    private val blockFrostManager: BlockFrostManager = provideBlockFrostManager()
    private val positionsDao = database.getPositionsDao()
    private val watchlistsDao = database.getWatchListsDao()

    private fun provideTapToolsManager(): TapToolsManager {
        return TapToolsManagerImpl.Builder(
            context, TapToolsConfig(appBuildInfo.tapToolsBaseUrl)
        ).setOkHttpClient(okHttpClient).build()
    }

    private fun provideClockManager(): ClockManager {
        return ClockManagerImpl.Builder(
            context,
        ).setOkHttpClient(
                okHttpClient.newBuilder().build()
            ).build()
    }

    private fun provideBlockFrostManager(): BlockFrostManager {
        return BlockFrostManagerImpl.Builder(
            context, BlockFrostConfig(appBuildInfo.blockFrostBaseUrl)
        ).setOkHttpClient(okHttpClient).build()
    }


    override val watchlistsWithPositions: List<WatchlistWithPositions>
        get() = database.getWatchListsDao().getWatchlistsWithPositions()

    /*override val positionsFT: Pair<WatchListConfig, List<PositionFTLocal>>
        get() = database.getPositionsDao().getAllFTPositions()
    override val positionsNFT: List<PositionNFTLocal>
        get() = database.getPositionsDao().getAllNFTPositions()
    override val positionsLP: List<PositionLPLocal>
        get() = database.getPositionsDao().getAllLPPositions()
    override val positionsFTIncludingLP: List<PositionFTLocal>
        get() = getAllPositionsFTIncludingLP()*/

    override suspend fun getClockStatus(): Result<StatusResponse> = clockManager.getStatus()
    override suspend fun resolveAdaHandle(handle: String): Result<String> =
        blockFrostManager.resolveAdaHandle(handle)

    override suspend fun getStakeAddress(address: String): Result<String> =
        blockFrostManager.getStakeAddress(address)

    override suspend fun loadPositionsForAddress(address: String): Result<PositionsResponse> =
        tapToolsManager.getPositionsForAddress(address)

    /*override suspend fun getFTPositionsForWatchlist(): List<PositionFTLocal> {
        return positionsDao.getAllFTPositions()
    }

    override suspend fun getNFTPositionsForWatchlist(): List<PositionNFTLocal> {
        return positionsDao.getAllNFTPositions()
    }

    override suspend fun getLPPositionsForWatchlist(): List<PositionLPLocal> {
        return positionsDao.getAllLPPositions()
    }
*/
    override suspend fun updateOrInsertPositions(
        watchList: Int,
        positionResponse: PositionsResponse
    ) {
        positionsDao.insertOrUpdateFTList(positionResponse.positionsFt.map {
            it.toPositionFT(
                watchList
            )
        })
        positionsDao.insertOrUpdateNFTList(positionResponse.positionsNft.map {
            it.toPositionNFT(
                watchList
            )
        })
        positionsDao.insertOrUpdateLPList(positionResponse.positionsLp.map {
            it.toPositionsLP(
                watchList
            )
        })
    }

    override suspend fun addWatchlist(
        name: String,
        includeLPinFT: Boolean,
        includeNFT: Boolean,
        showLPTab: Boolean,
        walletAddress: String?
    ): Int {
        return watchlistsDao.insertWatchlist(
            WatchListConfig(
                name = name,
                includeNFT = includeNFT,
                includeLPinFT = includeLPinFT,
                showLPTab = showLPTab,
                walletAddress = walletAddress,
                createdAt = ZonedDateTime.now(),
            )
        ).toInt()
    }

    /* override suspend fun getFTPositionsForWatchlistIncludingLP(): List<PositionFTLocal> =
         getAllPositionsFTIncludingLP()*/

}




fun PositionsFt.toPositionFT(watchList: Int): PositionFTLocal {
    return PositionFTLocal(
        ticker = this.ticker,
        fingerprint = this.fingerprint,
        adaValue = this.adaValue,
        price = this.price ?: 1.0, // Handle null price
        unit = this.unit,
        balance = this.balance,
        change30D = this.change30D,
        showInFeed = false,  // Default value, adjust if needed
        watchList = watchList,
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now()
    )
}

fun PositionsNft.toPositionNFT(watchList: Int): PositionNFTLocal {
    return PositionNFTLocal(
        name = this.name,
        policy = this.policy,
        adaValue = this.adaValue,
        price = this.floorPrice,
        balance = this.balance,
        change30D = this.change30D,
        showInFeed = false,  // Default value, adjust if needed
        watchList = watchList,
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now()
    )
}

fun PositionsLp.toPositionsLP(watchList: Int): PositionLPLocal {
    return PositionLPLocal(
        adaValue = this.adaValue,
        amountLP = this.amountLP,
        exchange = this.exchange,
        ticker = this.ticker,
        tokenA = this.tokenA,
        tokenAAmount = this.tokenAAmount,
        tokenAName = this.tokenAName,
        tokenB = this.tokenB,
        tokenBAmount = this.tokenBAmount,
        tokenBName = this.tokenBName,
        unit = this.unit,
        showInFeed = false,  // Default value, adjust if needed
        watchList = watchList,  // Default value, adjust if needed
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now(),
    )
}

fun PositionLPLocal.tokenAToPositionFT(watchList: Int): PositionFTLocal {
    val lp = this
    return PositionFTLocal(
        ticker = lp.tokenAName,
        fingerprint = "",
        adaValue = lp.adaValue/2,
        price = -1.0, // Handle null price
        unit = lp.tokenA,
        balance = lp.tokenAAmount,
        change30D = 0.0,
        showInFeed = false,  // Default value, adjust if needed
        watchList = watchList,
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now()
    )
}
fun PositionLPLocal.tokenBToPositionFT(watchList: Int): PositionFTLocal {
    val lp = this
    return PositionFTLocal(
        ticker = lp.tokenBName,
        fingerprint = "",
        adaValue = lp.adaValue/2,
        price = -1.0, // Handle null price
        unit = lp.tokenB,
        balance = lp.tokenBAmount,
        change30D = 0.0,
        showInFeed = false,  // Default value, adjust if needed
        watchList = watchList,  // Default value, adjust if needed
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now()
    )
}