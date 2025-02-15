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
import com.codingblocks.clock.core.manager.ClockManager
import com.codingblocks.clock.core.manager.ClockManagerImpl
import com.codingblocks.clock.core.manager.TapToolsManager
import com.codingblocks.clock.core.manager.TapToolsManagerImpl
import com.codingblocks.clock.core.model.AppBuildInfo
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
    val positionsFT: List<PositionFTLocal>
    val positionsNFT: List<PositionNFTLocal>
    val positionsLP: List<PositionLPLocal>
    val positionsFTIncludingLP: List<PositionFTLocal>
    suspend fun getClockStatus() : Result<StatusResponse>
    suspend fun getPositionsForAddress(address: String) : Result<PositionsResponse>
    suspend fun getFTPositionsForWatchlist() : List<PositionFTLocal>
    suspend fun getNFTPositionsForWatchlist() : List<PositionNFTLocal>
    suspend fun getLPPositionsForWatchlist() : List<PositionLPLocal>
    suspend fun updateOrInsertPositions(positionResponse: PositionsResponse)
    suspend fun getFTPositionsForWatchlistIncludingLP() : List<PositionFTLocal>
}

class CoreDataRepo(
    private val context: Context,
    private val database: AppDatabase,
    private val okHttpClient: OkHttpClient,
    private val appBuildInfo: AppBuildInfo,
) : DataRepo {
    private val tapToolsManager: TapToolsManager = provideTapToolsManager()
    private val clockManager: ClockManager = provideClockManager()
    private val positionsDao = database.getPositionsDao()

    private fun provideTapToolsManager() : TapToolsManager {
        return TapToolsManagerImpl.Builder(
            context,
            TapToolsConfig(appBuildInfo.tapToolsBaseUrl)
        )
            .setOkHttpClient(okHttpClient)
            .build()
    }

    private fun provideClockManager() : ClockManager {
        return ClockManagerImpl.Builder(
            context,
        )
            .setOkHttpClient(
                okHttpClient.newBuilder()
                    .build()
            )
            .build()
    }

    override val positionsFT: List<PositionFTLocal>
        get() = database.getPositionsDao().getAllFTPositions()
    override val positionsNFT: List<PositionNFTLocal>
        get() = database.getPositionsDao().getAllNFTPositions()
    override val positionsLP: List<PositionLPLocal>
        get() = database.getPositionsDao().getAllLPPositions()
    override val positionsFTIncludingLP: List<PositionFTLocal>
        get() = getAllPositionsFTIncludingLP()

    override suspend fun getClockStatus(): Result<StatusResponse> =
        clockManager.getStatus()

    override suspend fun getPositionsForAddress(address: String): Result<PositionsResponse> =
        tapToolsManager.getPositionsForAddress(address)

    override suspend fun getFTPositionsForWatchlist(): List<PositionFTLocal> {
        return positionsDao.getAllFTPositions()
    }

    override suspend fun getNFTPositionsForWatchlist(): List<PositionNFTLocal> {
        return positionsDao.getAllNFTPositions()
    }

    override suspend fun getLPPositionsForWatchlist(): List<PositionLPLocal> {
        return positionsDao.getAllLPPositions()
    }

    override suspend fun updateOrInsertPositions(positionResponse: PositionsResponse) {
        positionsDao.insertOrUpdateFTList(positionResponse.positionsFt.map { it.toPositionFT() })
        positionsDao.insertOrUpdateNFTList(positionResponse.positionsNft.map { it.toPositionNFT() })
        positionsDao.insertOrUpdateLPList(positionResponse.positionsLp.map { it.toPositionsLP() })
    }

    override suspend fun getFTPositionsForWatchlistIncludingLP(): List<PositionFTLocal> =
        getAllPositionsFTIncludingLP()

    private fun getAllPositionsFTIncludingLP(): List<PositionFTLocal> {
        val cachedPositionsLP = positionsDao.getAllLPPositions()
        val cachedPositionsFT = positionsDao.getAllFTPositions()
        val ftUnits = cachedPositionsFT.map { it.unit }.toSet()

        // add adaValue and Balance for each PostionFT we already have
        val positionsFTIncludingLP = cachedPositionsFT.map { positionFT ->
            val (lpAdaValue, lpBalance) = calculateTotalAdaValueAndBalance(cachedPositionsLP, positionFT.unit)
            if (lpAdaValue != positionFT.adaValue) {
                positionFT.copy(
                    adaValue = lpAdaValue + positionFT.adaValue,
                    balance = lpBalance + positionFT.balance,
                )
            } else {
                positionFT
            }
        }
        // create FT positions for LP positions A and/or B that dont exist yet as a seperate FT position
        val positionsFtFromLP: MutableList<PositionFTLocal> = mutableListOf()
        cachedPositionsLP.mapNotNull { lp ->
            when {
                lp.tokenA !in ftUnits && lp.tokenB !in ftUnits -> {
                    positionsFtFromLP.add(lp.tokenAToPositionFT())
                    positionsFtFromLP.add(lp.tokenBToPositionFT())
                }
                lp.tokenA !in ftUnits -> positionsFtFromLP.add(lp.tokenAToPositionFT())
                lp.tokenB !in ftUnits -> positionsFtFromLP.add(lp.tokenBToPositionFT())
                else -> null
            }
        }

        // group multiple entries for same unt and merge into one
        val groupedPositions = positionsFtFromLP.groupBy { it.unit }
        val uniqueFTFromLP = groupedPositions.map { (unit, positionsForUnit) ->
            val mergedPosition = positionsForUnit.reduce { acc, position ->
                PositionFTLocal(
                    unit = unit,
                    fingerprint = position.fingerprint,
                    adaValue = acc.adaValue + position.adaValue,
                    price = acc.price,
                    ticker = position.unit,
                    balance = acc.balance + position.balance,
                    change30D = acc.change30D,
                    showInFeed = acc.showInFeed,
                    watchList = acc.watchList,
                    createdAt = acc.createdAt,
                    lastUpdated = ZonedDateTime.now()
                )
            }
            mergedPosition
        }

        return (positionsFTIncludingLP + uniqueFTFromLP).sortedByDescending { it.adaValue }
    }
}

fun calculateTotalAdaValueAndBalance(cachedPositionsLP: List<PositionLPLocal>, unit: String): Pair<Double, Double> {
    var totalAdaValue = 0.0
    var totalBalance = 0.0

    cachedPositionsLP.forEach { lp ->
        if (lp.tokenA == unit || lp.tokenB == unit) {
            totalAdaValue += lp.adaValue / 2
            totalBalance += when (unit) {
                lp.tokenA -> lp.tokenAAmount
                lp.tokenB -> lp.tokenBAmount
                else -> 0.0
            }
        }
    }

    return Pair(totalAdaValue, totalBalance)
}


fun PositionsFt.toPositionFT(): PositionFTLocal {
    return PositionFTLocal(
        ticker = this.ticker,
        fingerprint = this.fingerprint,
        adaValue = this.adaValue,
        price = this.price ?: 1.0, // Handle null price
        unit = this.unit,
        balance = this.balance,
        change30D = this.change30D,
        showInFeed = false,  // Default value, adjust if needed
        watchList = 0,  // Default value, adjust if needed
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now()
    )
}

fun PositionsNft.toPositionNFT(): PositionNFTLocal {
    return PositionNFTLocal(
        name = this.name,
        policy = this.policy,
        adaValue = this.adaValue,
        price = this.floorPrice ?: 1.0, // Handle null price
        balance = this.balance,
        change30D = this.change30D,
        showInFeed = false,  // Default value, adjust if needed
        watchList = 0,  // Default value, adjust if needed
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now()
    )
}

fun PositionsLp.toPositionsLP(): PositionLPLocal {
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
        watchList = 0,  // Default value, adjust if needed
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now(),
    )
}

fun PositionLPLocal.tokenAToPositionFT(): PositionFTLocal {
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
        watchList = 0,  // Default value, adjust if needed
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now()
    )
}
fun PositionLPLocal.tokenBToPositionFT(): PositionFTLocal {
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
        watchList = 0,  // Default value, adjust if needed
        createdAt = ZonedDateTime.now(),
        lastUpdated = ZonedDateTime.now()
    )
}