package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

enum class FeedType {
    FeedFT, FeedNFT, AlertFT, AlertNFT, Custom
}

enum class ColorMode {
    BlinkOnce, AlternateBlink, Blink5Fast, Blink3Slow, StartToEnd, EndToStart
}

@Entity(
    tableName = "feedTheClock",
    indices = [Index(value = ["name"])]
)
data class FeedToClockItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unit: String,
    val name: String,
    val tickerOver: String? = null,
    val tickerUnder: String? = null,
    val price: Double? = null,
    val feedType: FeedType,
    val customOver: String? = null,
    val customUnder: String? = null,
    val colorStart: String? = null,
    val colorEnd: String? = null,
    val colorMode: ColorMode? = null,
    val deleteWhenDone: Boolean = false,
    val orderIndex: Int,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
)