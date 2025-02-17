package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(tableName = "watchListConfig")
data class WatchListConfig(
    @PrimaryKey(autoGenerate = true)
    val watchlistNumber: Int = 0,
    val name: String,
    val includeLPinFT: Boolean,
    val includeNFT: Boolean,
    val showLPTab: Boolean,
    val walletAddress: String? = null,
    val minFTAmount: Int = 0,
    val minNFTAmount: Int = 0,
    val createdAt: ZonedDateTime,
)