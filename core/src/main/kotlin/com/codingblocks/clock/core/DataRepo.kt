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
import com.codingblocks.clock.core.local.Database
import com.codingblocks.clock.core.manager.ClockManager
import com.codingblocks.clock.core.manager.ClockManagerImpl
import com.codingblocks.clock.core.manager.TapToolsManager
import com.codingblocks.clock.core.manager.TapToolsManagerImpl
import com.codingblocks.clock.core.model.AppBuildInfo
import com.codingblocks.clock.core.model.clock.StatusResponse
import com.codingblocks.clock.core.model.taptools.PositionsResponse
import com.codingblocks.clock.core.model.taptools.TapToolsConfig
import okhttp3.OkHttpClient

interface DataRepo {
    suspend fun getClockStatus() : Result<StatusResponse>
    suspend fun getPositionsForAddress(address: String) : Result<PositionsResponse>
}

class CoreDataRepo(
    private val context: Context,
    private val database: Database,
    private val okHttpClient: OkHttpClient,
    private val appBuildInfo: AppBuildInfo,
) : DataRepo {
    private val tapToolsManager: TapToolsManager = provideTapToolsManager()
    private val clockManager: ClockManager = provideClockManager()

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

}
