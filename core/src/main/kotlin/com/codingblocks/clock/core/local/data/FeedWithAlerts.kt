package com.codingblocks.clock.core.local.data

import androidx.room.Embedded
import androidx.room.Relation

data class FeedFTWithAlerts(
    @Embedded val feedFT: FeedFT,
    @Relation(
        parentColumn = "positionUnit",
        entityColumn = "feedPositionUnit"
    )
    val alerts: List<CustomFTAlert>
)

data class FeedNFTWithAlerts(
    @Embedded val feedNFT: FeedNFT,
    @Relation(
        parentColumn = "positionPolicy",
        entityColumn = "feedPositionPolicy"
    )
    val alerts: List<CustomNFTAlert>
)