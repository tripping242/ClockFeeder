package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(
    tableName = "feedFT",
    indices = [Index(value = ["positionUnit"])]
)
data class FeedFT(
    @PrimaryKey
    val positionUnit: String,
    val name: String,
    val createdAt: ZonedDateTime,
    var lastUpdatedAt: ZonedDateTime,
    var feedClockPrice: Boolean,
    var feedClockVolume: Boolean,
)

@Entity(
    tableName = "feedNFT",
    indices = [Index(value = ["positionPolicy"])]
)
data class FeedNFT(
    @PrimaryKey
    val positionPolicy: String,
    val name: String,
    val createdAt: ZonedDateTime,
    var lastUpdatedAt: ZonedDateTime,
    var feedClockPrice: Boolean,
    var feedClockVolume: Boolean,
)