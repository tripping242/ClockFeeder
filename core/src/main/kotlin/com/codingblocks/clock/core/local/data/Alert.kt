package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val feedPositionUnit: String, // References FeedFT.positionUnit
    // Add other fields for the alert here
    var alertName: String,
    var threshold: Double,
    var isEnabled: Boolean
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
    val feedPositionPolicy: String, // References FeedNFT.positionPolicy
    // Add other fields for the alert here
    var alertName: String,
    var threshold: Double,
    var isEnabled: Boolean
)