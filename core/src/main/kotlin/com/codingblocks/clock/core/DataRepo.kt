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
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.codingblocks.clock.core.local.AppDatabase
import com.codingblocks.clock.core.local.data.CustomFTAlert
import com.codingblocks.clock.core.local.data.CustomNFTAlert
import com.codingblocks.clock.core.local.data.FTPriceEntity
import com.codingblocks.clock.core.local.data.FeedFT
import com.codingblocks.clock.core.local.data.FeedFTWithAlerts
import com.codingblocks.clock.core.local.data.FeedNFT
import com.codingblocks.clock.core.local.data.FeedNFTWithAlerts
import com.codingblocks.clock.core.local.data.FeedToClockItem
import com.codingblocks.clock.core.local.data.FeedType
import com.codingblocks.clock.core.local.data.NFTStatsEntity
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import com.codingblocks.clock.core.local.data.WatchListConfig
import com.codingblocks.clock.core.local.data.WatchlistWithPositions
import com.codingblocks.clock.core.manager.BlockFrostManager
import com.codingblocks.clock.core.manager.BlockFrostManagerImpl
import com.codingblocks.clock.core.manager.ClockManager
import com.codingblocks.clock.core.manager.ClockManagerImpl
import com.codingblocks.clock.core.manager.SettingsManager
import com.codingblocks.clock.core.manager.SettingsManagerImpl
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

    val cyclingDelayMiliseconds: Long
    // worker with price retrieval and storage
    fun schedulePeriodicFetching()
    fun cancelPeriodicFetching()
    fun scheduleNFTAlertWorker()
    fun scheduleFTAlertWorker()

    // BlockClock
    suspend fun getClockStatus(): Result<StatusResponse>
    suspend fun sendFTPriceAlert()
    suspend fun sendNFTPriceAlert()

    // wallet with address
    suspend fun resolveAdaHandle(handle: String): Result<String>
    suspend fun getStakeAddress(address: String): Result<String>
    suspend fun loadPositionsForAddress(address: String): Result<PositionsResponse>
    suspend fun updateOrInsertPositions(watchList: Int, positionResponse: PositionsResponse)
    suspend fun deleteWatchlist(watchList: Int)

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

    suspend fun addAlertForUnit(alert: CustomFTAlert)
    suspend fun addAlertForPolicy(alert: CustomNFTAlert)
    suspend fun deleteAlertsForFeedWithUnit(feedFT: FeedFT)
    suspend fun deleteAlertsForFeedWithPolicy(policy: String)

    suspend fun getAllEnabledNFTAlerts():  List<CustomNFTAlert>
    suspend fun getAllEnabledFTAlerts():  List<CustomFTAlert>

    // Logos
    suspend fun getAndStoreRemoteLogo(policy: String)
    suspend fun getLocalLogo(policy: String): Bitmap?

    // prices
    suspend fun getAndStorePricesForTokens()
    suspend fun getAndStorePricesForPolicies()
    suspend fun getLatest2StatsForPolicy(policy: String): List<NFTStatsEntity>
    suspend fun getLatest2PricesForUnit(unit: String): List<FTPriceEntity>

    // feedTheClockDao stores the list of feedItems that we cycle through
    // need to get updated when feed item deleted, or when alert triggers...
    suspend fun loadAndUpdateFeedFTToClockItems()
    suspend fun loadAndUpdateFeedNFTToClockItems()
    suspend fun addAlertFTToFeedToClockItems(alert: CustomFTAlert, reachedPrice: Double)
    suspend fun addAlertNFTToFeedToClockItems(alert: CustomNFTAlert, reachedPrice: Double)

    fun getAllFeedToClockItems() : List<FeedToClockItem>
    fun deleteFeedToClockItem(item : FeedToClockItem)
    fun insertFeedToClockItem(item : FeedToClockItem)
}

class CoreDataRepo(
    private val context: Context,
    private val database: AppDatabase,
    private val okHttpClient: OkHttpClient,
    private val appBuildInfo: AppBuildInfo,
    private val workManager: WorkManager,
) : DataRepo {
    private val tapToolsManager: TapToolsManager = provideTapToolsManager()
    private val clockManager: ClockManager = provideClockManager()
    private val blockFrostManager: BlockFrostManager = provideBlockFrostManager()
    private val settingsManager: SettingsManager = provideSettingsManager()

    private val positionsDao = database.getPositionsDao()
    private val watchlistsDao = database.getWatchListsDao()
    private val feedFTDao = database.getFeedFTDao()
    private val feedNFTDao = database.getFeedNFTDao()
    private val customFTAlertDao = database.getFTAlertsDao()
    private val customNFTAlertDao = database.getNFTAlertsDao()
    private val nftStatsDao = database.getNFTStatsDao()
    private val ftPriceDao = database.getFTPriceDao()
    private val feedTheclockDao = database.getFeedTheClockDao()

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


    // use this from settings
    fun updateFetchDelay(newDelay: Long) {
        fetchDelay = newDelay
        cancelPeriodicFetching() // Cancel the existing work
        schedulePeriodicFetching() // Reschedule with the new delay
    }


    private val memoryCache = LruCache<String, Bitmap>(200)
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

    override val watchlistsWithPositions: List<WatchlistWithPositions>
        get() = database.getWatchListsDao().getWatchlistsWithPositions()
    override val feedFTWithAlerts: List<FeedFTWithAlerts>
        get() = database.getFeedFTDao().getFeedsFTWithAlerts()
    override val feedsNFTWithAlerts: List<FeedNFTWithAlerts>
        get() = database.getFeedNFTDao().getFeedsNFTWithAlerts()
    override val cyclingDelayMiliseconds: Long
        get() = settingsManager.settings.feedClockCycleSeconds * 1000.toLong()

    override suspend fun getClockStatus(): Result<StatusResponse> = clockManager.getStatus()
    override suspend fun sendFTPriceAlert() {
        TODO("Not yet implemented")
    }

    override suspend fun sendNFTPriceAlert() {
        TODO("Not yet implemented")
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

    override suspend fun getAndStoreRemoteLogo(policy: String) {
        // todo load from website
        // val bitmap = get from remote base64,
        // then decodeBase64ToBitmap, store in TokenLogo + put in memórycache
        //memoryCache.put(policy,bitmap)
    }

    override suspend fun getLocalLogo(policy: String): Bitmap? {
        var resultBitmap = memoryCache.get(policy)
        if (resultBitmap == null) {
            resultBitmap =
                database
                    .getLogoDao()
                    .getLogo(policy)
                    ?.logoBase64?.let { it ->
                        decodeBase64ToBitmap(it)
                    }
            memoryCache.put(policy, resultBitmap)
        }
        return resultBitmap
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
                                topOffer = result.topOffer,
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
        val validTime = System.currentTimeMillis() - (settingsManager.settings.priceTooOldSeconds * 1000)
        feedFTDao.getAllFeedFTClockEnabled().forEach { item ->
            val price = ftPriceDao.getLatestValidPriceForUnit(item.positionUnit, validTime)?.price
            if (price != null) {
                // todo we can also map other values if updated, like colorcoding on, colorMode?
                // val colorStart =
                val existingFeedTheClockItem = feedTheclockDao.getByUnit(item.positionUnit)
                if (existingFeedTheClockItem != null) {
                    feedTheclockDao.update(existingFeedTheClockItem.copy(price = price))
                } else {
                    // add new feedToClockItem
                    feedTheclockDao.insertAtEnd(
                        FeedToClockItem(
                            unit = item.positionUnit,
                            name = item.name,
                            price = price,
                            feedType = FeedType.FeedFT,
                            orderIndex = 0,
                        )
                    )
                }
            } else {
                // trigger loadprices once and retry loadUpdate...
                // or let this be done before we call this loadAndUpdateFeedToClockItems
            }

        }
    }

    override suspend fun loadAndUpdateFeedNFTToClockItems() {
        val validTime = System.currentTimeMillis() - (settingsManager.settings.priceTooOldSeconds * 1000)
        feedNFTDao.getAllFeedNFTClockEnabled().forEach { item ->
            Timber.tag("wims").i("FeedNFTClockEnabled item ${item.name} with price: ${item.feedClockPrice}")
            val price = nftStatsDao.getLatestValidPriceForPolicy(item.positionPolicy, validTime)?.price
            Timber.tag("wims").i("    stored latest price = $price ")
            if (price != null) {
                // todo we can also map other values if updated, like colorcoding on, colorMode?
                // val colorStart =
                val existingFeedTheClockItem = feedTheclockDao.getByUnit(item.positionPolicy)
                if (existingFeedTheClockItem != null) {
                    feedTheclockDao.update(existingFeedTheClockItem.copy(price = price))
                } else {
                    // add new feedToClockItem
                    feedTheclockDao.insertAtEnd(
                        FeedToClockItem(
                            unit = item.positionPolicy,
                            name = item.name,
                            price = price,
                            feedType = FeedType.FeedNFT,
                            orderIndex = 0,
                        )
                    )
                }
            } else {
                // trigger loadprices once and retry loadUpdate...
                // or let this be done before we call this loadAndUpdateFeedToClockItems
            }

        }
    }

    override suspend fun addAlertFTToFeedToClockItems(alert: CustomFTAlert, reachedPrice: Double) {
        val feedToClockItem = FeedToClockItem(
            unit = "alert",
            name = alert.ticker,
            price = reachedPrice,
            feedType = FeedType.AlertFT,
            deleteWhenDone = true,
            orderIndex = 0,
        )
        feedTheclockDao.insertAtFront(feedToClockItem)
    }

    override suspend fun addAlertNFTToFeedToClockItems(
        alert: CustomNFTAlert,
        reachedPrice: Double
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllFeedToClockItems(): List<FeedToClockItem> =
        // todo optional ordering with tiemstamp?
        feedTheclockDao.getAll()

    override fun deleteFeedToClockItem(item: FeedToClockItem) =
        feedTheclockDao.delete(item)

    override fun insertFeedToClockItem(item: FeedToClockItem) =
        feedTheclockDao.insert(item)

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
