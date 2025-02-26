package com.codingblocks.clock.core.model.taptools

import kotlinx.serialization.Serializable

@Serializable
data class AssetResponse (
    val image: String,
    val name: String,
    val price: Double,
    val rank: Int,
)
