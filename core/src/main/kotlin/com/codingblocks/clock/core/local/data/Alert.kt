package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(
    tableName = "customFTAlert",
    foreignKeys = [
        ForeignKey(
            entity = FeedFT::class,
            parentColumns = ["positionUnit"],
            childColumns = ["feedPositionUnit"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["feedPositionUnit"])]
)
data class CustomFTAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val feedPositionUnit: String,
    var ticker: String,
    var threshold: Double,
    var isEnabled: Boolean,
    var onlyOnce: Boolean,
    var crossingOver: Boolean = false,
    var priceOrVolume: Boolean,
    var pushAlert: Boolean,
    var clockAlert: Boolean,
    var mail: Boolean = false,
    var lastTriggeredTimeStamp: Long? = null,
    var createdAt: ZonedDateTime = ZonedDateTime.now()
)

@Entity(
    tableName = "customNFTAlert",
    foreignKeys = [
        ForeignKey(
            entity = FeedNFT::class,
            parentColumns = ["positionPolicy"],
            childColumns = ["feedPositionPolicy"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["feedPositionPolicy"])]
)
data class CustomNFTAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val feedPositionPolicy: String,
    var ticker: String,
    var threshold: Double,
    var isEnabled: Boolean,
    var onlyOnce: Boolean,
    var crossingOver: Boolean = false,
    var priceOrVolume: Boolean,
    var pushAlert: Boolean,
    var clockAlert: Boolean,
    var mail: Boolean = false,
    var lastTriggeredTimeStamp: Long? = null,
    var createdAt: ZonedDateTime = ZonedDateTime.now()
)