package com.codingblocks.clock.core.model.blockfrost

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class AssetsData(
    @SerialName("stake_address")
    val stakeAddress: String
)

