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
import android.os.Build
import androidx.annotation.RequiresApi
import com.codingblocks.clock.core.local.AppDatabase
import com.codingblocks.clock.core.local.data.PositionFT
import com.codingblocks.clock.core.local.data.PositionLP
import com.codingblocks.clock.core.local.data.PositionNFT
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
import java.time.format.DateTimeFormatter
import java.util.Locale

interface DataRepo {
    suspend fun getClockStatus() : Result<StatusResponse>
    suspend fun getPositionsForAddress(address: String) : Result<PositionsResponse>
    suspend fun getFTPositionsForWatchlist() : List<PositionFT>
    suspend fun getNFTPositionsForWatchlist() : List<PositionNFT>
    suspend fun getLPPositionsForWatchlist() : List<PositionLP>
    suspend fun updateOrInsertPositions(positionResponse: PositionsResponse)
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

    override suspend fun getClockStatus(): Result<StatusResponse> =
        clockManager.getStatus()

    override suspend fun getPositionsForAddress(address: String): Result<PositionsResponse> =
        tapToolsManager.getPositionsForAddress(address)

    override suspend fun getFTPositionsForWatchlist(): List<PositionFT> {
        return positionsDao.getAllFTPositions()
    }

    override suspend fun getNFTPositionsForWatchlist(): List<PositionNFT> {
        return positionsDao.getAllNFTPositions()
    }

    override suspend fun getLPPositionsForWatchlist(): List<PositionLP> {
        return positionsDao.getAllLPPositions()
    }

    override suspend fun updateOrInsertPositions(positionResponse: PositionsResponse) {
        Timber.tag("wims").i("start with FT ${positionResponse.positionsFt.size}")
        positionsDao.insertOrUpdateFTList(positionResponse.positionsFt.map { it.toPositionFT() })
        Timber.tag("wims").i("done with FT, now start NFT ${positionResponse.positionsNft.size}")
        positionsDao.insertOrUpdateNFTList(positionResponse.positionsNft.map { it.toPositionNFT() })
        Timber.tag("wims").i("done with NFT,now start NFT ${positionResponse.positionsLp.size}")
        positionsDao.insertOrUpdateLPList(positionResponse.positionsLp.map { it.toPositionsLP() })
        Timber.tag("wims").i("done with LP")
    }
}

fun PositionsFt.toPositionFT(): PositionFT {
    return PositionFT(
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

fun PositionsNft.toPositionNFT(): PositionNFT {
    return PositionNFT(
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

fun PositionsLp.toPositionsLP(): PositionLP {
    return PositionLP(
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
