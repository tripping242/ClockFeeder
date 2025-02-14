package com.codingblocks.clock.core.local.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Entity(tableName = "positionFT")
data class PositionFT(
    var ticker: String,
    @PrimaryKey
    var fingerprint: String,
    var adaValue: Double,
    var price: Double,
    val unit: String,
    val balance: Double,
    val change30D: Double,
    var showInFeed: Boolean,
    var watchList: Int,
    var createdAt: ZonedDateTime,
    var lastUpdated: ZonedDateTime,
)

@Entity(tableName = "positionNFT")
data class PositionNFT(
    var name: String,
    @PrimaryKey
    var policy: String,
    var adaValue: Double,
    val balance: Double,
    val change30D: Double,
    var price: Double,
    var showInFeed: Boolean,
    var watchList: Int,
    var createdAt: ZonedDateTime,
    var lastUpdated: ZonedDateTime,
)

fun ZonedDateTime.formatteddMyHHMM(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MMM.yyyy, HH:mm", Locale.getDefault())
    return this.format(formatter)
}
fun ZonedDateTime.formattedHHMM(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    return this.format(formatter)
}