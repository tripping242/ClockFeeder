package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ft_prices")
data class FTPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unit: String,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis(),
)
