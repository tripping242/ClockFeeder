package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "nft_stats")
data class NFTStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val policy: String,
    val listings: Int,
    val owners: Int,
    val price: Double,
    val sales: Double,
    val supply: Int,
    val topOffer: Double,
    val volume: Double,
    val timestamp: Long = System.currentTimeMillis(),
)