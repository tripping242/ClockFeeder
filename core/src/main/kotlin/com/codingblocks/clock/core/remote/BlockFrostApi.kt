package com.codingblocks.clock.core.remote

import com.codingblocks.clock.core.model.blockfrost.AssetAddress
import com.codingblocks.clock.core.model.blockfrost.AssetsData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val apiKey = "mainnetYpTwo16JgFrAzAqsDRt7I4PqRxGTR1bs"

interface BlockFrostApi {
    @GET("assets/{assetHexed}/addresses")
    suspend fun getAdaHandleAddresses(
        @Path("assetHexed") assetHexed: String,
        @Query("count") count: Int
    ): Response<List<AssetAddress>>

    @GET("addresses/{address}")
    suspend fun getStakeAddress(
        @Path("address") address: String
    ): Response<AssetsData>

}