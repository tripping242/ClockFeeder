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
import com.codingblocks.clock.core.local.data.CustomFTAlert
import com.codingblocks.clock.core.local.data.CustomNFTAlert
import com.codingblocks.clock.core.local.data.FeedFT
import com.codingblocks.clock.core.local.data.FeedFTWithAlerts
import com.codingblocks.clock.core.local.data.FeedNFT
import com.codingblocks.clock.core.local.data.FeedNFTWithAlerts
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
import timber.log.Timber
import java.time.ZonedDateTime

interface DataRepo {
    val watchlistsWithPositions: List<WatchlistWithPositions>
    val feedFTWithAlerts: List<FeedFTWithAlerts>
    val feedsNFTWithAlerts: List<FeedNFTWithAlerts>

    // Blockclock
    suspend fun getClockStatus() : Result<StatusResponse>

    // wallet with address
    suspend fun resolveAdaHandle(handle: String): Result<String>
    suspend fun getStakeAddress(address: String) : Result<String>
    suspend fun loadPositionsForAddress(address: String) : Result<PositionsResponse>
    suspend fun updateOrInsertPositions(watchList: Int, positionResponse: PositionsResponse)
    suspend fun deleteWatchlist(watchList: Int)

    // watchlist with contents
    suspend fun addWatchlist(name: String, includeLPinFT: Boolean, includeNFT: Boolean, showLPTab: Boolean, walletAddress: String?) : Int
    suspend fun findWatchlistWithAddressOrName(address: String?, name: String): WatchListConfig?
    suspend fun updateWatchlistSettings(watchListConfig: WatchListConfig)

    // positions
    suspend fun getFTPositionBy(unit: String, watchList: Int): PositionFTLocal?
    suspend fun updatePosition(position: PositionFTLocal)
    suspend fun getNFTPositionBy(policy: String, watchList: Int): PositionNFTLocal?
    suspend fun updatePosition(position: PositionNFTLocal)
    suspend fun getLPPositionByTicker(ticker: String, watchList: Int): PositionLPLocal?
    suspend fun updatePosition(position: PositionLPLocal)
    suspend fun getLPPositionByUnit(unit: String, watchList: Int) : PositionLPLocal?

    // feed to set alerts
    suspend fun getAllFeedFT(): List<FeedFT>
    suspend fun addFeedFT(feedFT: FeedFT): Boolean
    suspend fun updateFeedFT(feedFT: FeedFT)
    suspend fun deleteFeedFT(feedFT: FeedFT)
    suspend fun deleteFeedFTByUnit(unit: String)
    suspend fun replacePositionInFeedFT(oldUnit: String, newUnit: String)
    suspend fun getAllFeedNFT(): List<FeedNFT>
    suspend fun addFeedNFT(feedNFT: FeedNFT): Boolean
    suspend fun updateFeedNFT(feedNFT: FeedNFT)
    suspend fun deleteFeedNFT(feedNFT: FeedNFT)
    suspend fun deleteFeedNFTByPolicy(policy: String)
    suspend fun replacePositionInFeedNFT(oldPolicy: String, newPolicy: String)

    // feed with alerts
    suspend fun addFeedFTWithAlerts(feedFT: FeedFT, alerts: List<CustomFTAlert>)
    suspend fun getFeedFTWithAlerts(positionUnit: String): FeedFTWithAlerts?
    suspend fun addFeedNFTWithAlerts(feedNFT: FeedNFT, alerts: List<CustomNFTAlert>)
    suspend fun getFeedNFTWithAlerts(positionPolicy: String): FeedNFTWithAlerts?

        // alerts
    suspend fun deleteAlert(alert: CustomFTAlert)
    suspend fun deleteAlert(alert: CustomNFTAlert)

    suspend fun addAlertForUnit(alert: CustomFTAlert)
    suspend fun addAlertForPolicy(alert: CustomNFTAlert)
    suspend fun deleteAlertsForFeedWithUnit(feedFT: FeedFT)
    suspend fun deleteAlertsForFeedWithPolicy(policy: String)

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
    private val feedFTDao = database.getFeedFTDao()
    private val feedNFTDao = database.getFeedNFTDao()
    private val customFTAlertDao = database.getFTAlertsDao()
    private val customNFTAlertDao = database.getNFTAlertsDao()

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
    override val feedFTWithAlerts: List<FeedFTWithAlerts>
        get() = database.getFeedFTDao().getFeedsFTWithAlerts()
    override val feedsNFTWithAlerts: List<FeedNFTWithAlerts>
        get() = database.getFeedNFTDao().getFeedsNFTWithAlerts()

    override suspend fun getClockStatus(): Result<StatusResponse> = clockManager.getStatus()
    override suspend fun resolveAdaHandle(handle: String): Result<String> =
        blockFrostManager.resolveAdaHandle(handle)

    override suspend fun getStakeAddress(address: String): Result<String> =
        blockFrostManager.getStakeAddress(address)

    override suspend fun loadPositionsForAddress(address: String): Result<PositionsResponse> =
        tapToolsManager.getPositionsForAddress(address)

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

    override suspend fun deleteWatchlist(watchList: Int) {
        watchlistsDao.deleteWatchlistById(watchList)
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

    override suspend fun findWatchlistWithAddressOrName(
        address: String?, name: String
    ): WatchListConfig? {
        return watchlistsDao.getAllWatchlists()
            .find {
                it.name == name || it.walletAddress == address

        }
    }

    override suspend fun updateWatchlistSettings(watchListConfig: WatchListConfig, ) {
        watchlistsDao.updateWatchListSettingsDb(watchListConfig)
    }

    override suspend fun getFTPositionBy(unit: String, watchList: Int): PositionFTLocal? {
        return positionsDao.getFTPositionByUnit(unit, watchList)
    }

    override suspend fun updatePosition(position: PositionFTLocal) {
        Timber.tag("wims").i("update Position ${position.unit} ${position.showInFeed}\"")
        positionsDao.insertOrUpdateFT(position)
    }

    override suspend fun updatePosition(position: PositionNFTLocal) {
        positionsDao.insertOrUpdateNFT(position)
    }

    override suspend fun updatePosition(position: PositionLPLocal) {
        positionsDao.insertOrUpdateLP(position)
    }

    override suspend fun getNFTPositionBy(policy: String, watchList: Int): PositionNFTLocal? {
        return positionsDao.getNFTPositionByPolicy(policy, watchList)
    }

    override suspend fun getLPPositionByTicker(ticker: String, watchList: Int): PositionLPLocal? {
        return positionsDao.getLPPositionByTicker(ticker, watchList)
    }

    override suspend fun getLPPositionByUnit(unit: String, watchList: Int): PositionLPLocal? {
        // so we jsut take the position with highest adaValue here, in case we are sorting?
        return positionsDao.getLPPositionsByUnit(unit, watchList).sortedByDescending { it.adaValue  }.firstOrNull()
    }

    override suspend fun getAllFeedFT(): List<FeedFT> {
        return feedFTDao.getAllFeedFT()
    }

    override suspend fun addFeedFT(feedFT: FeedFT): Boolean {
        Timber.tag("wims").i("add feed")
        val result = feedFTDao.insert(feedFT)
        Timber.tag("wims").i("add feed result = $result")
        return result != -1L // Returns true if insertion was successful, false if a conflict occurred
    }

    override suspend fun updateFeedFT(feedFT: FeedFT) {
        feedFTDao.update(feedFT)
    }

    override suspend fun deleteFeedFT(feedFT: FeedFT) {
        feedFTDao.delete(feedFT)
    }

    override suspend fun deleteFeedFTByUnit(unit: String) {
        feedFTDao.deleteByPositionUnit(unit)
    }

    // todo this wont work if haveing either positionUnit, positionTokenA and positionTokenB
    override suspend fun replacePositionInFeedFT(oldUnit: String, newUnit: String) {
        feedFTDao.replaceReferencedPositionFTLocal(oldUnit, newUnit)
    }

    override suspend fun getAllFeedNFT(): List<FeedNFT> {
        return feedNFTDao.getAllFeedNFT()
    }

    override suspend fun addFeedNFT(feedNFT: FeedNFT): Boolean {
        val result = feedNFTDao.insert(feedNFT)
        Timber.tag("wims").i("add feed result = $result")

        return result != -1L // Returns true if insertion was successful, false if a conflict occurred
    }

    override suspend fun updateFeedNFT(feedNFT: FeedNFT) {
        feedNFTDao.update(feedNFT)
    }

    override suspend fun deleteFeedNFT(feedNFT: FeedNFT) {
        feedNFTDao.delete(feedNFT)
    }

    override suspend fun deleteFeedNFTByPolicy(policy: String) {
        feedNFTDao.deleteByPositionPolicy(policy)
    }

    override suspend fun replacePositionInFeedNFT(oldPolicy: String, newPolicy: String) {
        feedNFTDao.replaceReferencedPositionNFTLocal(oldPolicy, newPolicy)
    }

    override suspend fun deleteAlert(alert: CustomFTAlert) {
        customFTAlertDao.delete(alert)
    }

    override suspend fun deleteAlert(alert: CustomNFTAlert) {
        customNFTAlertDao.delete(alert)
    }

    override suspend fun addAlertForUnit(alert: CustomFTAlert) {
        customFTAlertDao.insert(alert)
    }

    override suspend fun addAlertForPolicy(alert: CustomNFTAlert) {
        customNFTAlertDao.insert(alert)
    }

    override suspend fun deleteAlertsForFeedWithUnit(feedFT: FeedFT) {
    // todo check
    //customFTAlertDao.deleteAlertsForFeed(it) }
    }

    override suspend fun deleteAlertsForFeedWithPolicy(policy: String) {
        customNFTAlertDao.deleteAlertsForFeed(policy)
    }

    override suspend fun addFeedFTWithAlerts(feedFT: FeedFT, alerts: List<CustomFTAlert>) {
        feedFTDao.insert(feedFT)
        alerts.forEach { customFTAlertDao.insert(it) }
    }

    override suspend fun getFeedFTWithAlerts(positionUnit: String): FeedFTWithAlerts? {
        val feedFT = feedFTDao.getFeedByUnit(positionUnit)
        return if (feedFT != null) {
            val alerts = customFTAlertDao.getAlertsForFeed(positionUnit)
            FeedFTWithAlerts(feedFT, alerts)
        } else {
            null
        }
    }

    override suspend fun addFeedNFTWithAlerts(feedNFT: FeedNFT, alerts: List<CustomNFTAlert>) {
        feedNFTDao.insert(feedNFT)
        alerts.forEach { customNFTAlertDao.insert(it) }
    }

    override suspend fun getFeedNFTWithAlerts(positionPolicy: String): FeedNFTWithAlerts? {
        val feedNFT = feedNFTDao.getFeedByPositionPolicy(positionPolicy)
        return if (feedNFT != null) {
            val alerts = customNFTAlertDao.getAlertsForFeed(positionPolicy)
            FeedNFTWithAlerts(feedNFT, alerts)
        } else {
            null
        }
    }
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