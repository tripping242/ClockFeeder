package com.codingblocks.clock.core.model.taptools

import kotlinx.serialization.Serializable

@Serializable
data class NFTStatsResponse (
    val listings: Int,
    val owners: Int,
    val price: Int,
    val sales: Int,
    val supply: Int,
    val topOffer: Int,
    val volume: Int,
)

@Serializable
data class TokenRequest(
    val policyHexStrings: List<String>
)
