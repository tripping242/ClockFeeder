package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_logos")
data class TokenLogo(
    @PrimaryKey val unit: String,
    val logoBase64: String
)

@Entity(tableName = "nft_logos")
data class NFTLogo(
    @PrimaryKey val policy: String,
    val url: String
)