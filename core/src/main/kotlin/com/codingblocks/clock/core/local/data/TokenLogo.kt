package com.codingblocks.clock.core.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logos")
data class TokenLogo(
    @PrimaryKey val policyId: String,
    val logoBase64: String // Alternative: Use `ByteArray` for Blob storage
)