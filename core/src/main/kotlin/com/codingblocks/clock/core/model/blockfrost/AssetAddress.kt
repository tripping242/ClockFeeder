package com.codingblocks.clock.core.model.blockfrost

import kotlinx.serialization.Serializable

@Serializable
data class AssetAddress(
    val address: String,
    val quantity: Long
)