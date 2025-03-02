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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.codingblocks.clock.core.local.AppDatabase
import com.codingblocks.clock.core.local.data.ColorMode
import com.codingblocks.clock.core.local.data.CustomFTAlert
import com.codingblocks.clock.core.local.data.CustomNFTAlert
import com.codingblocks.clock.core.local.data.FTPriceEntity
import com.codingblocks.clock.core.local.data.FeedFT
import com.codingblocks.clock.core.local.data.FeedFTWithAlerts
import com.codingblocks.clock.core.local.data.FeedNFT
import com.codingblocks.clock.core.local.data.FeedNFTWithAlerts
import com.codingblocks.clock.core.local.data.FeedToClockItem
import com.codingblocks.clock.core.local.data.FeedType
import com.codingblocks.clock.core.local.data.NFTLogo
import com.codingblocks.clock.core.local.data.NFTStatsEntity
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import com.codingblocks.clock.core.local.data.TokenLogo
import com.codingblocks.clock.core.local.data.WatchListConfig
import com.codingblocks.clock.core.local.data.WatchlistWithPositions
import com.codingblocks.clock.core.manager.BlockFrostManager
import com.codingblocks.clock.core.manager.BlockFrostManagerImpl
import com.codingblocks.clock.core.manager.ClockManager
import com.codingblocks.clock.core.manager.ClockManagerImpl
import com.codingblocks.clock.core.manager.LogoManager
import com.codingblocks.clock.core.manager.LogoManagerImpl
import com.codingblocks.clock.core.manager.SettingsManager
import com.codingblocks.clock.core.manager.SettingsManagerImpl
import com.codingblocks.clock.core.manager.TapToolsManager
import com.codingblocks.clock.core.manager.TapToolsManagerImpl
import com.codingblocks.clock.core.model.AppBuildInfo
import com.codingblocks.clock.core.model.blockfrost.BlockFrostConfig
import com.codingblocks.clock.core.model.tokenlogo.LogoConfig
import com.codingblocks.clock.core.model.clock.StatusResponse
import com.codingblocks.clock.core.model.taptools.PositionsFt
import com.codingblocks.clock.core.model.taptools.PositionsLp
import com.codingblocks.clock.core.model.taptools.PositionsNft
import com.codingblocks.clock.core.model.taptools.PositionsResponse
import com.codingblocks.clock.core.model.taptools.TapToolsConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import timber.log.Timber
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

interface DataRepo {
    val watchlistsWithPositions: List<WatchlistWithPositions>
    val feedFTWithAlerts: List<FeedFTWithAlerts>
    val feedsNFTWithAlerts: List<FeedNFTWithAlerts>

    val autoFeed: Boolean
    fun setAutoFeed(bool: Boolean)

    val autoReloadPositions: Boolean
    fun setAutoReloadPositions(bool: Boolean)

    val smallTrendPercent: Double
    fun setSmallTrendPercent(percent: Double)

    val highTrendPercent: Double
    fun setHighTrendPercent(percent: Double)

    val cyclingDelayMiliseconds: Long
    val logoCache:LruCache<String, Bitmap>

    // worker with price retrieval and storage
    fun schedulePeriodicFetching()
    fun cancelPeriodicFetching()
    fun scheduleNFTAlertWorker()
    fun scheduleFTAlertWorker()

    // BlockClock
    suspend fun pauseBlockClock()
    suspend fun resumeBlockClock()

    suspend fun getClockStatus(): Result<StatusResponse>
    suspend fun sendFTPriceFeed(
        encodedPrice: String,
        pair: String,
        colorMode: ColorMode?
    ): Result<Any>

    suspend fun sendNFTPriceFeed(name: String, price: Int, colorMode: ColorMode?)
    suspend fun sendFTPriceAlert(
        name: String,
        reachedPrice: String,
        crossedPrice: String,
        colorMode: ColorMode?
    )

    suspend fun sendNFTPriceAlert(
        name: String,
        reachedPrice: String,
        crossedPrice: String,
        colorMode: ColorMode?
    )

    // wallet with address
    suspend fun resolveAdaHandle(handle: String): Result<String>
    suspend fun getStakeAddress(address: String): Result<String>
    suspend fun loadPositionsForAddress(address: String): Result<PositionsResponse>
    suspend fun updateOrInsertPositions(watchList: Int, positionResponse: PositionsResponse)
    suspend fun deleteWatchlist(watchList: Int)

    // init
    suspend fun loadPositionsForAllWatchlists()

    // watchlist with contents
    suspend fun addWatchlist(
        name: String,
        includeLPinFT: Boolean,
        includeNFT: Boolean,
        showLPTab: Boolean,
        walletAddress: String?
    ): Int



    suspend fun findWatchlistWithAddressOrName(address: String?, name: String): WatchListConfig?
    suspend fun updateWatchlistSettings(watchListConfig: WatchListConfig)

    // positions
    suspend fun getFTPositionBy(unit: String, watchList: Int): PositionFTLocal?
    suspend fun updatePosition(position: PositionFTLocal)
    suspend fun updateAllFTAndLPPositionsShowFeed(unit: String, showFeed: Boolean)
    suspend fun getNFTPositionBy(policy: String, watchList: Int): PositionNFTLocal?
    suspend fun updatePosition(position: PositionNFTLocal)
    suspend fun updateAllNFTPositionsShowFeed(policy: String, showFeed: Boolean)
    suspend fun getLPPositionByTicker(ticker: String, watchList: Int): PositionLPLocal?
    suspend fun updatePosition(position: PositionLPLocal)
    suspend fun getLPPositionByUnit(unit: String, watchList: Int): PositionLPLocal?

    // feed to set alerts
    suspend fun getAllFeedFT(): List<FeedFT>
    suspend fun addFeedFT(feedFT: FeedFT): Boolean
    suspend fun updateFeedFT(feedFT: FeedFT)
    suspend fun deleteFeedFT(feedFT: FeedFT)
    suspend fun deleteFeedFTByUnit(unit: String)

    suspend fun getAllFeedNFT(): List<FeedNFT>
    suspend fun addFeedNFT(feedNFT: FeedNFT): Boolean
    suspend fun updateFeedNFT(feedNFT: FeedNFT)
    suspend fun deleteFeedNFT(feedNFT: FeedNFT)
    suspend fun deleteFeedNFTByPolicy(policy: String)

    // feed with alerts
    suspend fun addFeedFTWithAlerts(feedFT: FeedFT, alerts: List<CustomFTAlert>)
    suspend fun getFeedFTWithAlerts(positionUnit: String): FeedFTWithAlerts?
    suspend fun addFeedNFTWithAlerts(feedNFT: FeedNFT, alerts: List<CustomNFTAlert>)
    suspend fun getFeedNFTWithAlerts(positionPolicy: String): FeedNFTWithAlerts?

    // alerts
    suspend fun deleteAlert(alert: CustomFTAlert)
    suspend fun deleteAlert(alert: CustomNFTAlert)
    suspend fun setLastTriggered(alert: CustomFTAlert)
    suspend fun setLastTriggered(alert: CustomNFTAlert)

    suspend fun addAlertForUnit(alert: CustomFTAlert)
    suspend fun addAlertForPolicy(alert: CustomNFTAlert)
    suspend fun deleteAlertsForFeedWithUnit(feedFT: FeedFT)
    suspend fun deleteAlertsForFeedWithPolicy(policy: String)

    suspend fun getAllEnabledNFTAlerts(): List<CustomNFTAlert>
    suspend fun getAllEnabledFTAlerts(): List<CustomFTAlert>

    suspend fun checkFTAlertsAfterPriceUpdates()

    // Logos
    suspend fun getAndStoreRemoteLogo(unit: String)
    suspend fun getLocalLogo(unit: String): Bitmap?
    suspend fun checkOrGetRemoteLogo(position: PositionFTLocal)
    suspend fun checkOrGetRemoteLogo(position: PositionNFTLocal)
    suspend fun checkOrGetRemoteLogo(position: PositionLPLocal)


    // prices
    suspend fun getAndStorePricesForTokens()
    suspend fun getAndStorePricesForPolicies()
    suspend fun getLatest2StatsForPolicy(policy: String): List<NFTStatsEntity>
    suspend fun getLatest2PricesForUnit(unit: String): List<FTPriceEntity>

    // feedTheClockDao stores the list of feedItems that we cycle through
    // need to get updated when feed item deleted, or when alert triggers...
    suspend fun loadAndUpdateFeedFTToClockItems()
    suspend fun loadAndUpdateFeedNFTToClockItems()
    suspend fun loadAndUpdateNextFeedToClockItem()
    suspend fun addAlertFTToFeedToClockItems(alert: CustomFTAlert, reachedPrice: Double)
    suspend fun addAlertNFTToFeedToClockItems(alert: CustomNFTAlert, reachedPrice: Double)
    suspend fun pushFTAlert(alert: CustomFTAlert, reachedPrice: Double)
    suspend fun pushNFTAlert(alert: CustomNFTAlert, reachedPrice: Double)

    fun getAllFeedToClockItems(): List<FeedToClockItem>
    fun deleteFeedToClockItem(item: FeedToClockItem)
    fun deleteFeedToClockItemsForUnit(unit: String)
    fun insertFeedToClockItem(item: FeedToClockItem)
    fun moveFeedToClockItemToTheEnd(item: FeedToClockItem)
}

class CoreDataRepo(
    private val context: Context,
    private val database: AppDatabase,
    private val okHttpClient: OkHttpClient,
    private val appBuildInfo: AppBuildInfo,
    private val workManager: WorkManager,
    private val notificationHelper: NotificationHelper,
) : DataRepo {
    private val tapToolsManager: TapToolsManager = provideTapToolsManager()
    private val clockManager: ClockManager = provideClockManager()
    private val blockFrostManager: BlockFrostManager = provideBlockFrostManager()
    private val settingsManager: SettingsManager = provideSettingsManager()
    private val tokenLogoManager: LogoManager = provideTokenLogoManager()
    private val positionsDao = database.getPositionsDao()
    private val watchlistsDao = database.getWatchListsDao()
    private val feedFTDao = database.getFeedFTDao()
    private val feedNFTDao = database.getFeedNFTDao()
    private val customFTAlertDao = database.getFTAlertsDao()
    private val customNFTAlertDao = database.getNFTAlertsDao()
    private val nftStatsDao = database.getNFTStatsDao()
    private val ftPriceDao = database.getFTPriceDao()
    private val feedTheclockDao = database.getFeedTheClockDao()
    private val tokenLogoDao = database.getTokenLogoDao()
    private val nftLogoDao = database.getNFTLogoDao()

    var fetchDelay: Long = 1 // todo move to settings

    override fun schedulePeriodicFetching() {
        Timber.tag("wims").i("schedulePeriodicFetching")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateWorkRequest = OneTimeWorkRequestBuilder<FetchPricesWorker>().build()

        workManager.enqueue(immediateWorkRequest)

        // Create a periodic work request
        val fetchWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<FetchPricesWorker>(
                fetchDelay, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "FetchPricesWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            fetchWorkRequest
        )
    }

    override fun cancelPeriodicFetching() {
        workManager.cancelUniqueWork("FetchPricesWork")
    }

    override fun scheduleNFTAlertWorker() {
        Timber.tag("wims").i("scheduleNFTAlertWorker")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateWorkRequest = OneTimeWorkRequestBuilder<NFTAlertWorker>().build()

        workManager.enqueue(immediateWorkRequest)

        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<NFTAlertWorker>(
                fetchDelay, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "NFTAlertWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun scheduleFTAlertWorker() {
        Timber.tag("wims").i("scheduleFTAlertWorker")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateWorkRequest = OneTimeWorkRequestBuilder<FTAlertWorker>().build()

        workManager.enqueue(immediateWorkRequest)

        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<FTAlertWorker>(
                fetchDelay, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "FTAlertWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override suspend fun pauseBlockClock() {
        clockManager.pauseBlockClock()
    }

    override suspend fun resumeBlockClock() {
        clockManager.resumeBlockClock()
    }

    // use this from settings
    fun updateFetchDelay(newDelay: Long) {
        fetchDelay = newDelay
        cancelPeriodicFetching() // Cancel the existing work
        schedulePeriodicFetching() // Reschedule with the new delay
    }

    override val logoCache = LruCache<String, Bitmap>(200)
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

    private fun provideSettingsManager(): SettingsManager {
        return SettingsManagerImpl(context)
    }

    private fun provideTokenLogoManager(): LogoManager {
        Timber.tag("wims").i("provideTokenLogoManager   ! with ${appBuildInfo.tokenCardanoBaseUrl}")
        return LogoManagerImpl.Builder(
            context, LogoConfig(appBuildInfo.tokenCardanoBaseUrl)
        ).setOkHttpClient(okHttpClient).build()
    }

    override val watchlistsWithPositions: List<WatchlistWithPositions>
        get() = database.getWatchListsDao().getWatchlistsWithPositions()
    override val feedFTWithAlerts: List<FeedFTWithAlerts>
        get() = database.getFeedFTDao().getFeedsFTWithAlerts()
    override val feedsNFTWithAlerts: List<FeedNFTWithAlerts>
        get() = database.getFeedNFTDao().getFeedsNFTWithAlerts()
    override val autoFeed: Boolean
        get() = settingsManager.settings.autoFeed
    override val cyclingDelayMiliseconds: Long
        get() = settingsManager.settings.feedClockCycleSeconds * 1000.toLong()


    override fun setAutoFeed(bool: Boolean) {
        settingsManager.settings.autoFeed = bool
    }

    override val autoReloadPositions: Boolean
        get() = settingsManager.settings.autoReloadPositions

    override fun setAutoReloadPositions(bool: Boolean) {
        settingsManager.settings.autoReloadPositions = bool
    }

    override val smallTrendPercent: Double
        get() = settingsManager.settings.smallTrendPercent

    override fun setSmallTrendPercent(percent: Double) {
        settingsManager.settings.smallTrendPercent = percent
    }

    override val highTrendPercent: Double
        get() = settingsManager.settings.highTrendPercent

    override fun setHighTrendPercent(percent: Double) {
        settingsManager.settings.highTrendPercent = percent
    }

    override suspend fun getClockStatus(): Result<StatusResponse> = clockManager.getStatus()
    override suspend fun sendFTPriceFeed(
        encodedPrice: String,
        pair: String,
        colorMode: ColorMode?
    ): Result<Any> {
        val sendSuccess = clockManager.sendFTPriceFeed(encodedPrice, pair)
            .onSuccess { if (colorMode != null) clockManager.color(colorMode) }
        return sendSuccess
    }

    override suspend fun sendNFTPriceFeed(name: String, price: Int, colorMode: ColorMode?) {
        val formattedPrice = formatNFTPrice(price)
        val nameChunks = splitAndFormatName(name)
        for ((position, over, under) in nameChunks) {
            clockManager.setOverUnderText(position, over, under)
        }
        clockManager.setOverUnderText(6, formattedPrice, "ADA")
        if (colorMode != null) clockManager.color(colorMode)
    }

    override suspend fun sendFTPriceAlert(
        name: String,
        reachedPrice: String,
        crossedPrice: String,
        colorMode: ColorMode?
    ) {
        val priceChunks = splitAndFormatPrices(crossedPrice, reachedPrice)
        clockManager.setOverUnderText(0, name.take(5), "ADA")
        clockManager.setOverUnderText(1, "", "")
        clockManager.setOverUnderText(2, "CRO", "")
        clockManager.setOverUnderText(3, "SSED", "NOW")
        for ((position, over, under) in priceChunks) {
            clockManager.setOverUnderText(position, over, under)
        }
        clockManager.setOverUnderText(6, "ADA", "ADA")
    }

    override suspend fun sendNFTPriceAlert(
        name: String,
        reachedPrice: String,
        crossedPrice: String,
        colorMode: ColorMode?
    ) {
        val nameChunks = splitAndFormatNameAlertFirst3Positions(name)
        for ((position, over, under) in nameChunks) {
            clockManager.setOverUnderText(position, over, under)
        }
        clockManager.setOverUnderText(3, "CRO", "")
        clockManager.setOverUnderText(4, "SSED", "NOW")
        clockManager.setOverUnderText(5, crossedPrice, reachedPrice)
        clockManager.setOverUnderText(6, "ADA", "ADA")

    }

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
            // get image from image table
            val logo = tokenLogoDao.getTokenLogo(it.unit)?.logoBase64
            it.toPositionFT(
                watchList,
                logo
            )
        })
        positionsDao.insertOrUpdateNFTList(positionResponse.positionsNft.map {
            val logo = nftLogoDao.getNFTLogo(it.policy)?.url
            it.toPositionNFT(
                watchList,
                logo
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

    override suspend fun loadPositionsForAllWatchlists() {
        withContext(Dispatchers.IO) {
            watchlistsDao.getAllWatchlists().forEach { watchlist ->
                watchlist.walletAddress?.let { tapToolsManager.getPositionsForAddress(it) }
            }
        }
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

    override suspend fun updateWatchlistSettings(watchListConfig: WatchListConfig) {
        watchlistsDao.updateWatchListSettingsDb(watchListConfig)
    }

    override suspend fun getFTPositionBy(unit: String, watchList: Int): PositionFTLocal? {
        return positionsDao.getFTPositionsByUnit(unit, watchList)
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

    override suspend fun updateAllFTAndLPPositionsShowFeed(unit: String, showFeed: Boolean) {
        positionsDao.getFTPositionsByUnit(unit).forEach { pos ->
            positionsDao.insertOrUpdateFT(pos.copy(showInFeed = showFeed))
        }
        positionsDao.getLPPositionsByUnit(unit).forEach { pos ->
            positionsDao.insertOrUpdateLP(
                pos.copy(
                    showInFeedA = if (pos.tokenA == unit) showFeed else pos.showInFeedA,
                    showInFeedB = if (pos.tokenB == unit) showFeed else pos.showInFeedB
                )
            )
        }
    }

    override suspend fun getNFTPositionBy(policy: String, watchList: Int): PositionNFTLocal? {
        return positionsDao.getNFTPositionByPolicy(policy, watchList)
    }

    override suspend fun updateAllNFTPositionsShowFeed(policy: String, showFeed: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun getLPPositionByTicker(ticker: String, watchList: Int): PositionLPLocal? {
        return positionsDao.getLPPositionByTicker(ticker, watchList)
    }

    override suspend fun getLPPositionByUnit(unit: String, watchList: Int): PositionLPLocal? {
        // so we jsut take the position with highest adaValue here, in case we are sorting?
        return positionsDao.getLPPositionsByUnit(unit, watchList).sortedByDescending { it.adaValue }
            .firstOrNull()
    }

    override suspend fun getAllFeedFT(): List<FeedFT> {
        return feedFTDao.getAllFeedFT()
    }

    override suspend fun addFeedFT(feedFT: FeedFT): Boolean {
        val result = feedFTDao.insert(feedFT)
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

    override suspend fun getAllFeedNFT(): List<FeedNFT> {
        return feedNFTDao.getAllFeedNFT()
    }

    override suspend fun addFeedNFT(feedNFT: FeedNFT): Boolean {
        val result = feedNFTDao.insert(feedNFT)
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

    override suspend fun deleteAlert(alert: CustomFTAlert) {
        customFTAlertDao.delete(alert)
    }

    override suspend fun deleteAlert(alert: CustomNFTAlert) {
        customNFTAlertDao.delete(alert)
    }

    override suspend fun setLastTriggered(alert: CustomFTAlert) {
        customFTAlertDao.update(alert.copy(lastTriggeredTimeStamp = System.currentTimeMillis()))
    }

    override suspend fun setLastTriggered(alert: CustomNFTAlert) {
        customNFTAlertDao.update(alert.copy(lastTriggeredTimeStamp = System.currentTimeMillis()))
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

    override suspend fun getAllEnabledNFTAlerts() =
        customNFTAlertDao.getAllEnabledAlerts()

    override suspend fun getAllEnabledFTAlerts(): List<CustomFTAlert> =
        customFTAlertDao.getAllEnabledAlerts()

    override suspend fun checkFTAlertsAfterPriceUpdates() {
        withContext(Dispatchers.IO) {
            try {
                // Fetch all enabled alerts
                val alerts = getAllEnabledFTAlerts()

                // Process each alert
                alerts.forEach { alert ->
                    Timber.tag("wims").i("check for alert: ${alert.ticker} ${alert.threshold} and priceOrVolume ${alert.priceOrVolume}")

                    // Fetch the latest two stats entries for the policy
                    val stats = getLatest2PricesForUnit(alert.feedPositionUnit)
                    Timber.tag("wims").i("prices.size == ${stats.size}")
                    if (stats.size == 2) {

                        val previousStat = stats[1]
                        val currentStat = stats[0]

                        Timber.tag("wims").i("previous price ${previousStat.price} treshold ${alert.threshold} current ${currentStat.price}")
                        // Check if the alert condition is met
                        val conditionMet = if (alert.priceOrVolume) {
                            checkIfNotAlreadyTriggeredByThesePrices(alert.lastTriggeredTimeStamp, previousStat.timestamp)
                                    && checkPriceCrossing(alert.crossingOver, alert.threshold, previousStat.price, currentStat.price)
                        } else {
                            // no volume yet on FTAlert
                            false
                        }

                        if (conditionMet) {
                            triggerAlerts(alert, currentStat)
                            if (alert.onlyOnce) {
                                deleteAlert(alert)
                            } else {
                                // update alert with lastAlerted Timestamp
                                setLastTriggered(alert)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.d("could not check alerts with token prices")
            }
        }
    }

    override suspend fun getAndStoreRemoteLogo(unit: String) {
        // todo load from website
        // val bitmap = get from remote base64,
        // then decodeBase64ToBitmap, store in TokenLogo + put in memórycache
        //memoryCache.put(policy,bitmap)
    }

    override suspend fun getLocalLogo(unit: String): Bitmap? {
        var resultBitmap = logoCache.get(unit)
        if (resultBitmap == null) {
            resultBitmap =
                tokenLogoDao
                    .getTokenLogo(unit)
                    ?.logoBase64?.let { it ->
                        decodeBase64ToBitmap(it)
                    }
            logoCache.put(unit, resultBitmap)
        }
        return resultBitmap
    }

    private suspend fun checkOrGetRemoteLogoByUnit(unit: String): String?  {
        // if no logo info is available, get it remote, store it on TokenLogoDao and memory
        var base64String: String? = null
        if (tokenLogoDao.getTokenLogo(unit) == null) {
                tokenLogoManager.getLogoForUnit(unit)
                    .onSuccess {
                        base64String = it
                        if (it.isNotEmpty()) {
                            tokenLogoDao.insertLogo(
                                TokenLogo(
                                    unit = unit,
                                    logoBase64 = it
                                )
                            )
                            logoCache.put(unit, decodeBase64ToBitmap(it))
                        }
                    }
                    .onFailure {  }
        } else { base64String = tokenLogoDao.getTokenLogo(unit)!!.logoBase64 }
        return base64String
    }

    override suspend fun checkOrGetRemoteLogo(position: PositionFTLocal) {
        checkOrGetRemoteLogoByUnit(position.unit)?.let {
            positionsDao.insertOrUpdateFT(
                position.copy(logo = it)
            )
        }
    }

    override suspend fun checkOrGetRemoteLogo(position: PositionNFTLocal) {
        if (nftLogoDao.getNFTLogo(position.policy) == null) {
            if (position.logo != null) {
                nftLogoDao.insertLogo(
                    NFTLogo(
                        policy = position.policy,
                        url = position.logo!!
                    )
                )
            } else {
                tapToolsManager.getLogoForPolicy(position.policy)
                    .onSuccess {
                        nftLogoDao.insertLogo(
                            NFTLogo(
                                policy = position.policy,
                                url = it.logo
                            )
                        )
                        positionsDao.updateNFT(position.copy(logo = it.logo))
                    }
            }
        } else if (position.logo == null) positionsDao.updateNFT(
            position.copy(
                logo = nftLogoDao.getNFTLogo(
                    position.policy
                )!!.url
            )
        )
    }

    override suspend fun checkOrGetRemoteLogo(position: PositionLPLocal) {
        checkOrGetRemoteLogoByUnit(position.tokenA)
        if (position.tokenBName.isNotEmpty()) checkOrGetRemoteLogoByUnit(position.tokenB)
    }

    override suspend fun getAndStorePricesForTokens() {
        withContext(Dispatchers.IO) {
            Timber.tag("wims").i("retrieving token price info")
            try {
                val unitList = feedFTDao.getAllFTUnitsFromFeed()
                tapToolsManager.getPricesForTokens(unitList)
                    .onSuccess { tokenPrices ->
                        val priceList = tokenPrices.map { (unit, price) ->
                            FTPriceEntity(
                                unit = unit,
                                price = price,
                            )
                        }
                        ftPriceDao.insertAll(priceList)
                    }
                    .onFailure {
                        Timber.d("could not retreive price info $it")
                    }

            } catch (e: Exception) {
                Timber.d("Error fetching prices for tokens ${e.message}")
            }
        }
    }

    override suspend fun getAndStorePricesForPolicies() {
        withContext(Dispatchers.IO) {
            Timber.tag("wims").i("retrieving NFT price info")
            val policies = feedNFTDao.getAllNFTPoliciesFromFeed()
            policies.forEach { policy ->
                try {
                    tapToolsManager.getStatsForPolicy(policy)
                        .onSuccess { result ->
                            // Convert the response to an entity and store it in the database
                            Timber.tag("wims").i("adding NFTStatsEntity for $policy")
                            val nftStatsEntity = NFTStatsEntity(
                                policy = policy,
                                listings = result.listings,
                                owners = result.owners,
                                price = result.price,
                                sales = result.sales,
                                supply = result.supply,
                                volume = result.volume
                            )
                            nftStatsDao.insert(nftStatsEntity)
                        }
                        .onFailure {
                            Timber.d("Error fetching stats for $policy: ${it.message}")
                        }
                } catch (e: Exception) {
                    // Handle errors (e.g., log or retry)
                    Timber.d("Error fetching stats for policy $policy: ${e.message}")
                }
            }
        }
    }

    override suspend fun getLatest2StatsForPolicy(policy: String): List<NFTStatsEntity> =
        nftStatsDao.getLatestStatsForPolicy(policy, 2)

    override suspend fun getLatest2PricesForUnit(unit: String): List<FTPriceEntity> =
        ftPriceDao.getLatestPricesForUnit(unit, 2)

    override suspend fun loadAndUpdateFeedFTToClockItems() {
        val validTime =
            System.currentTimeMillis() - (settingsManager.settings.priceTooOldSeconds * 1000)
        val previousValidTime =
            System.currentTimeMillis() - (3 * settingsManager.settings.priceTooOldSeconds * 1000)
        feedFTDao.getAllFeedFTClockEnabled().forEach { item ->
            val latestPrice =
                ftPriceDao.getLatestValidPricesForUnit(item.positionUnit, validTime, 1)
                    ?.getOrNull(0)?.price
            if (latestPrice != null) {
                var percentageChange: Double? = null
                if (item.feedClockVolume) {
                    val previousPrice =
                        try {
                            ftPriceDao.getLatestValidPricesForUnit(
                                item.positionUnit,
                                previousValidTime,
                                10
                            )?.lastOrNull()?.price
                        } catch (e: Exception) {
                            null
                        }
                    percentageChange = if (previousPrice != null && previousPrice != 0.0) {
                        ((latestPrice - previousPrice) / previousPrice) * 100
                    } else {
                        null
                    }
                }

                val colorMode = when {
                    percentageChange == null -> null
                    // todo et these limits manually in settings?
                    percentageChange > 5.0 -> ColorMode.BlinkUpMuch
                    percentageChange > 0.1 -> ColorMode.BlinkUp
                    percentageChange < -5.0 -> ColorMode.BlinkDownMuch
                    percentageChange < -0.1 -> ColorMode.BlinkDown
                    else -> ColorMode.BlinkOnce
                }
                val existingFeedTheClockItem = feedTheclockDao.getByUnit(item.positionUnit)
                if (existingFeedTheClockItem != null) {
                    feedTheclockDao.update(
                        existingFeedTheClockItem.copy(
                            price = latestPrice,
                            colorMode = colorMode
                        )
                    )
                } else {
                    // add new feedToClockItem
                    feedTheclockDao.insertAtEnd(
                        FeedToClockItem(
                            unit = item.positionUnit,
                            name = item.name,
                            price = latestPrice,
                            feedType = FeedType.FeedFT,
                            colorMode = colorMode,
                            orderIndex = 0,
                        )
                    )
                }
            }
        }
    }

    override suspend fun loadAndUpdateNextFeedToClockItem() {
        val validTime =
            System.currentTimeMillis() - (settingsManager.settings.priceTooOldSeconds * 1000)
        val previousValidTime =
            System.currentTimeMillis() - (3 * settingsManager.settings.priceTooOldSeconds * 1000)
        val nextFeedToclock = getAllFeedToClockItems().firstOrNull()
        nextFeedToclock?.let {
            if (nextFeedToclock.feedType == FeedType.FeedNFT) {
                feedNFTDao.getAllFeedNFTClockEnabled().find {
                    it.positionPolicy == nextFeedToclock.unit
                }?.let { item ->
                    Timber.tag("wims").i("FeedNFTToClock item $item}")
                    val latestPrice =
                        nftStatsDao.getLatestValidPricesForPolicy(item.positionPolicy, validTime, 1)
                            ?.getOrNull(0)?.price

                    if (latestPrice != null) {
                        var percentageChange: Double? = null
                        if (item.feedClockVolume) {
                            val previousPrice =
                                try {
                                    nftStatsDao.getLatestValidPricesForPolicy(
                                        item.positionPolicy,
                                        previousValidTime,
                                        10
                                    )?.lastOrNull()?.price
                                } catch (e: Exception) {
                                    null
                                }
                            Timber.tag("wims").i("          has previousPrice: $previousPrice")

                            percentageChange = if (previousPrice != null && previousPrice != 0.0) {
                                ((latestPrice - previousPrice) / previousPrice) * 100
                            } else {
                                null
                            }
                        }

                        val colorMode = when {
                            percentageChange == null -> null
                            percentageChange > highTrendPercent -> ColorMode.BlinkUpMuch
                            percentageChange > smallTrendPercent -> ColorMode.BlinkUp
                            percentageChange < -highTrendPercent -> ColorMode.BlinkDownMuch
                            percentageChange < -smallTrendPercent -> ColorMode.BlinkDown
                            else -> ColorMode.BlinkOnce
                        }
                        feedTheclockDao.update(
                            nextFeedToclock.copy(
                                price = latestPrice,
                                colorMode = colorMode
                            )
                        )
                    }
                }
            } else if (nextFeedToclock.feedType == FeedType.FeedFT) {
                feedFTDao.getAllFeedFTClockEnabled().find {
                    it.positionUnit == nextFeedToclock.unit
                }?.let { item ->
                    Timber.tag("wims").i("FeedFTToClock item $item}")
                    val latestPrice =
                        ftPriceDao.getLatestValidPricesForUnit(item.positionUnit, validTime, 1)
                            ?.getOrNull(0)?.price

                    if (latestPrice != null) {
                        var percentageChange: Double? = null
                        if (item.feedClockVolume) {
                            val previousPrice =
                                try {
                                    ftPriceDao.getLatestValidPricesForUnit(
                                        item.positionUnit,
                                        previousValidTime,
                                        10
                                    )?.lastOrNull()?.price
                                } catch (e: Exception) {
                                    null
                                }
                            Timber.tag("wims").i("          has previousPrice: $previousPrice")

                            percentageChange = if (previousPrice != null && previousPrice != 0.0) {
                                ((latestPrice - previousPrice) / previousPrice) * 100
                            } else {
                                null
                            }
                        }

                        val colorMode = when {
                            percentageChange == null -> null
                            percentageChange!! > highTrendPercent -> ColorMode.BlinkUpMuch
                            percentageChange!! > smallTrendPercent -> ColorMode.BlinkUp
                            percentageChange!! < -highTrendPercent -> ColorMode.BlinkDownMuch
                            percentageChange!! < -smallTrendPercent -> ColorMode.BlinkDown
                            else -> ColorMode.BlinkOnce
                        }
                        feedTheclockDao.update(
                            nextFeedToclock.copy(
                                price = latestPrice,
                                colorMode = colorMode
                            )
                        )
                    }
                }
            } else {
                // not found, probably an alert coming
            }
        }
    }

    // todo load this in worker??
    override suspend fun loadAndUpdateFeedNFTToClockItems() {
        val validTime =
            System.currentTimeMillis() - (settingsManager.settings.priceTooOldSeconds * 1000)
        val previousValidTime =
            System.currentTimeMillis() - (3 * settingsManager.settings.priceTooOldSeconds * 1000)
        feedNFTDao.getAllFeedNFTClockEnabled().forEach { item ->
            Timber.tag("wims").i("FeedNFTToClock item $item}")
            val latestPrice =
                nftStatsDao.getLatestValidPricesForPolicy(item.positionPolicy, validTime, 1)
                    ?.getOrNull(0)?.price

            if (latestPrice != null) {
                var percentageChange: Double? = null
                if (item.feedClockVolume) {
                    val previousPrice =
                        try {
                            nftStatsDao.getLatestValidPricesForPolicy(
                                item.positionPolicy,
                                previousValidTime,
                                10
                            )?.lastOrNull()?.price
                        } catch (e: Exception) {
                            null
                        }
                    Timber.tag("wims").i("          has previousPrice: $previousPrice")

                    percentageChange = if (previousPrice != null && previousPrice != 0.0) {
                        ((latestPrice - previousPrice) / previousPrice) * 100
                    } else {
                        null
                    }
                }

                val colorMode = when {
                    percentageChange == null -> null
                    // todo et these limits manually in settings?
                    percentageChange > 5.0 -> ColorMode.BlinkUpMuch
                    percentageChange > 0.1 -> ColorMode.BlinkUp
                    percentageChange < -5.0 -> ColorMode.BlinkDownMuch
                    percentageChange < -0.1 -> ColorMode.BlinkDown
                    else -> ColorMode.BlinkOnce
                }

                val existingFeedTheClockItem = feedTheclockDao.getByUnit(item.positionPolicy)
                if (existingFeedTheClockItem != null) {
                    feedTheclockDao.update(
                        existingFeedTheClockItem.copy(
                            price = latestPrice,
                            colorMode = colorMode
                        )
                    )
                } else {
                    // add new feedToClockItem
                    feedTheclockDao.insertAtEnd(
                        FeedToClockItem(
                            unit = item.positionPolicy,
                            name = item.name,
                            price = latestPrice,
                            feedType = FeedType.FeedNFT,
                            colorMode = colorMode,
                            orderIndex = 0,
                        )
                    )
                }
            }
        }
    }

    override suspend fun addAlertFTToFeedToClockItems(alert: CustomFTAlert, reachedPrice: Double) {
        Timber.tag("wims").i("ALERT being added to front of the Clock feed $alert")
        val feedToClockItem = FeedToClockItem(
            unit = alert.threshold.toString(),
            name = alert.ticker,
            price = reachedPrice,
            feedType = FeedType.AlertFT,
            deleteWhenDone = true,
            colorMode = ColorMode.BlinkAlert,
            orderIndex = 0,
        )
        feedTheclockDao.insertAtFront(feedToClockItem)
    }

    override suspend fun addAlertNFTToFeedToClockItems(
        alert: CustomNFTAlert,
        reachedPrice: Double
    ) {
        Timber.tag("wims").i("ALERT being added to front of the Clock feed $alert")
        val feedToClockItem = FeedToClockItem(
            unit = alert.threshold.toString(),
            name = alert.ticker,
            price = reachedPrice,
            feedType = FeedType.AlertNFT,
            deleteWhenDone = true,
            colorMode = ColorMode.BlinkAlert,
            orderIndex = 0,
        )
        feedTheclockDao.insertAtFront(feedToClockItem)
    }

    override suspend fun pushFTAlert(alert: CustomFTAlert, reachedPrice: Double) {
        notificationHelper.sendNotification(
            title = "Token price alert!",
            message = "The price of ${alert.ticker} has crossed ${reachedPrice} ₳",
        )
    }

    override suspend fun pushNFTAlert(alert: CustomNFTAlert, reachedPrice: Double) {
        notificationHelper.sendNotification(
            title = "NFT Price alert!",
            message = "The floor price of ${alert.ticker} has crossed ${reachedPrice} ₳",
        )
    }

    override fun getAllFeedToClockItems(): List<FeedToClockItem> =
        feedTheclockDao.getAll()

    override fun deleteFeedToClockItem(item: FeedToClockItem) =
        feedTheclockDao.delete(item)

    override fun deleteFeedToClockItemsForUnit(unit: String) {
        feedTheclockDao.deleteByUnit(unit)
    }

    override fun insertFeedToClockItem(item: FeedToClockItem) =
        feedTheclockDao.insert(item)

    override fun moveFeedToClockItemToTheEnd(item: FeedToClockItem) {
        feedTheclockDao.delete(item)
        feedTheclockDao.insertAtEnd(item)
    }

    override suspend fun addFeedFTWithAlerts(feedFT: FeedFT, alerts: List<CustomFTAlert>) {
        feedFTDao.insert(feedFT)
        alerts.forEach { customFTAlertDao.insert(it) }
    }

    override suspend fun getFeedFTWithAlerts(positionUnit: String): FeedFTWithAlerts? {
        val feedFT = feedFTDao.getFeedFTByPositionUnit(positionUnit)
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
    private suspend fun triggerAlerts(alert: CustomFTAlert, currentStat: FTPriceEntity) {
        if (alert.pushAlert) {
            Timber.tag("wims").i("push alert now for ${alert.ticker}")
            pushFTAlert(alert, currentStat.price)
        }
        if (alert.clockAlert) {
            addAlertFTToFeedToClockItems(alert, currentStat.price)
        }
        if (alert.mail) {
            // Send email alert (implement this in dataRepo if needed)
        }
    }
}

fun PositionsFt.toPositionFT(watchList: Int, logo: String?): PositionFTLocal {
    return PositionFTLocal(
        logo = logo,
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

fun PositionsNft.toPositionNFT(watchList: Int, logo: String?): PositionNFTLocal {
    return PositionNFTLocal(
        logo = logo,
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
        showInFeedA = false,
        showInFeedB = false,
        watchList = watchList,
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now(),
    )
}

fun PositionLPLocal.tokenAToPositionFT(watchList: Int): PositionFTLocal {
    val lp = this
    return PositionFTLocal(
        ticker = lp.tokenAName,
        fingerprint = "",
        adaValue = lp.adaValue / 2,
        price = -1.0, // Handle null price
        unit = lp.tokenA,
        balance = lp.tokenAAmount,
        change30D = 0.0,
        showInFeed = lp.showInFeedA || lp.showInFeedB,  // Default value, adjust if needed
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
        adaValue = lp.adaValue / 2,
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

fun decodeBase64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}

private fun checkIfNotAlreadyTriggeredByThesePrices(lastTriggeredTimeStamp: Long?, previousPriceTimeStamp: Long): Boolean =
    if (lastTriggeredTimeStamp == null) true else lastTriggeredTimeStamp < previousPriceTimeStamp


private fun checkPriceCrossing(
    crossingOver: Boolean,
    threshold: Double,
    previousPrice: Double,
    currentPrice: Double
): Boolean {
    return if (crossingOver) {
        previousPrice < threshold && currentPrice >= threshold
    } else {
        previousPrice > threshold && currentPrice <= threshold
    }
}