package com.codingblocks.clock.core.model.taptools

import kotlinx.serialization.Serializable

@Serializable
data class NFTStatsResponse (
    val listings: Int,
    val owners: Int,
    val price: Double,
    val sales: Double,
    val supply: Int,
    val topOffer: Double,
    val volume: Double,
)

@Serializable
data class TokenRequest(
    val policyHexStrings: List<String>
)
