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

package com.codingblocks.clock.core.remote

import com.codingblocks.clock.core.model.taptools.AssetResponse
import com.codingblocks.clock.core.model.taptools.InfoResponse
import com.codingblocks.clock.core.model.taptools.NFTStatsResponse
import com.codingblocks.clock.core.model.taptools.PositionsResponse
import com.codingblocks.clock.core.model.taptools.TokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TapToolsApi {
    @GET("wallet/portfolio/positions")
    suspend fun getPositionsForAddress(
        @Query("address") address: String
    ): Response<PositionsResponse>

    @GET("nft/collection/assets")
    suspend fun getAssetsForPolicy(
        @Query("policy") policy: String
    ): Response<AssetResponse>

    // use below to add logo ifps links n a policy, unit to logo table
    @GET("nft/collection/info")
    suspend fun getInfoForPolicy(
        @Query("policy") policy: String
    ): Response<InfoResponse>

    // use to get prices and volume info every x minutes 5 = 288 per day...
    @GET("nft/collection/stats")
    suspend fun getStatsForPolicy(
        @Query("policy") policy: String
    ): Response<NFTStatsResponse>

    @POST("token/prices")
    suspend fun getTokenPrices(
        @Body tokenRequest: List<String>): Response<Map<String, Double>>



}