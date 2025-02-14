package com.codingblocks.clock.core.model.taptools

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PositionsResponse (
    val adaBalance: Double,
    val adaValue: Double,
    val liquidValue: Double,
    val numFTs: Int,
    val numNFTs: Int,
    val positionsFt: List<PositionsFt>,
    val positionsLp: List<PositionsLp>,
    val positionsNft: List<PositionsNft>
)

@Serializable
data class PositionsFt (
    @SerialName("24h")
    val change24H: Double,

    @SerialName("30d")
    val change30D: Double,

    @SerialName("7d")
    val change7D: Double,

    val adaValue: Double,
    val balance: Double,
    val fingerprint: String,
    val liquidBalance: Double,
    val liquidValue: Double,
    val price: Double?,
    val ticker: String,
    val unit: String
)

@Serializable
data class PositionsLp (
    val adaValue: Double,
    val amountLP: Double,
    val exchange: String,
    val ticker: String,
    val tokenA: String,
    val tokenAAmount: Double,
    val tokenAName: String,
    val tokenB: String,
    val tokenBAmount: Double,
    val tokenBName: String,
    val unit: String
)

@Serializable
data class PositionsNft (
    @SerialName("24h")
    val change24H: Double,

    @SerialName("30d")
    val change30D: Double,

    @SerialName("7d")
    val change7D: Double,

    val adaValue: Double,
    val balance: Double,
    val floorPrice: Double,
    val liquidValue: Double,
    val listings: Double,
    val name: String,
    val policy: String
)

